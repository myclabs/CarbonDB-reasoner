package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mycsense.carbondb.NoUnitException;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.dimension.Orientation;
import com.mycsense.carbondb.domain.group.Type;

import java.util.HashMap;

public class GroupRepo extends AbstractRepo {
    protected HashMap<String, Group> groupCache;

    public GroupRepo(Model model) {
        super(model);
        groupCache = new HashMap<>();
    }

    public HashMap<String, Group> getProcessGroups()
    {
        return getGroups(Datatype.ProcessGroup);
    }

    public HashMap<String, Group> getCoefficientGroups()
    {
        return getGroups(Datatype.CoefficientGroup);
    }

    public HashMap<String, Group> getGroups() {
        return getGroups(Datatype.Group);
    }

    protected HashMap<String, Group> getGroups(Resource groupType)
    {
        HashMap<String, Group> groups = new HashMap<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, groupType);
        while (i.hasNext()) {
            Resource resource = i.next();
            try {
                groups.put(getId(resource), getGroup(resource));
            } catch (NoUnitException e) {
                log.warn(e.getMessage());
            }
        }

        return groups;
    }

    public Group getGroup(Resource groupResource) throws NoUnitException {
        if (!groupCache.containsKey(groupResource.getURI())) {
            Group group = new Group(getGroupDimSet(groupResource), getGroupCommonKeywords(groupResource));
            group.setLabel(getLabelOrURI(groupResource));
            group.setId(getId(groupResource));
            group.setUnit(RepoFactory.getUnitsRepo().getUnit(groupResource));
            group.setType(groupResource.hasProperty(RDF.type, Datatype.ProcessGroup) ? Type.PROCESS : Type.COEFFICIENT);
            if (groupResource.hasProperty(RDFS.comment) && groupResource.getProperty(RDFS.comment) != null) {
                group.setComment(groupResource.getProperty(RDFS.comment).getString());
            }
            group.setReferences(RepoFactory.getReferenceRepo().getReferencesForResource(groupResource));
            groupCache.put(groupResource.getURI(), group);
        }
        return groupCache.get(groupResource.getURI());
    }

    protected DimensionSet getGroupDimSet(Resource groupResource)
    {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasDimension, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        DimensionSet dimSet = new DimensionSet();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Resource dimensionResource = s.getObject().asResource();
                Dimension dim = getDimensionKeywords(dimensionResource);
                if (groupResource.hasProperty(Datatype.hasHorizontalDimension, dimensionResource))
                    dim.setOrientation(Orientation.HORIZONTAL);
                else if (groupResource.hasProperty(Datatype.hasVerticalDimension, dimensionResource))
                    dim.setOrientation(Orientation.VERTICAL);
                dimSet.add(dim);
            }
        }
        return dimSet;
    }

    protected Dimension getGroupCommonKeywords(Resource groupResource)
    {
        Selector selector = new SimpleSelector(groupResource, Datatype.hasCommonTag, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Keyword keyword = new Keyword(getId(s.getObject().asResource()));
                keyword.setLabel(getLabelOrURI(s.getObject().asResource()));
                dim.add(keyword);
            }
        }
        return dim;
    }

    protected Dimension getDimensionKeywords(Resource dimensionResource)
    {
        Selector selector = new SimpleSelector(dimensionResource, Datatype.containsKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Keyword keyword = new Keyword(getId(s.getObject().asResource()));
                keyword.setLabel(getLabelOrURI(s.getObject().asResource()));
                dim.add(keyword);
            }
        }
        setKeywordsPositionForDimension(dimensionResource, dim);
        return dim;
    }

    protected void setKeywordsPositionForDimension(Resource dimensionResource, Dimension dimension) {
        NodeIterator i = model.listObjectsOfProperty(dimensionResource, Datatype.hasPositionForSomeKeyword);
        while (i.hasNext()) {
            Resource positionResource = i.next().asResource();
            if (positionResource.hasProperty(Datatype.position)
                && positionResource.getProperty(Datatype.position) != null
                && positionResource.hasProperty(Datatype.providesPositionToKeyword)
                && positionResource.getProperty(Datatype.providesPositionToKeyword) != null
            ) {
                dimension.addKeywordPosition(
                        positionResource.getProperty(Datatype.position).getInt(),
                        positionResource.getPropertyResourceValue(Datatype.providesPositionToKeyword).getURI()
                );
            }
        }
    }
}
