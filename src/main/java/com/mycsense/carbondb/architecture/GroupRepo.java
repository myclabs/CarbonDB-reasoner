package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.dimension.Orientation;
import com.mycsense.carbondb.domain.group.Type;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupRepo extends AbstractRepo {
    private final UnitsRepo unitsRepo;
    protected HashMap<String, Group> groupCache;

    public GroupRepo(Model model, UnitsRepo unitsRepo) {
        super(model);
        this.unitsRepo = unitsRepo;
        groupCache = new HashMap<>();
    }

    public ArrayList<Group> getProcessGroups()
    {
        return getGroups(Datatype.ProcessGroup);
    }

    public ArrayList<Group> getCoefficientGroups()
    {
        return getGroups(Datatype.CoefficientGroup);
    }

    public ArrayList<Group> getGroups() {
        return getGroups(Datatype.Group);
    }

    protected ArrayList<Group> getGroups(Resource groupType)
    {
        ArrayList<Group> groups = new ArrayList<>();

        ResIterator i = model.listSubjectsWithProperty(RDF.type, groupType);
        while (i.hasNext()) {
            Resource groupResource = i.next();
            groups.add(getGroup(groupResource));
        }

        return groups;
    }

    public Group getGroup(String groupId)
    {
        return getGroup(model.getResource(Datatype.getURI() + groupId));
    }

    public Group getGroup(Resource groupResource)
    {
        if (!groupCache.containsKey(groupResource.getURI())) {
            Group group = new Group(getGroupDimSet(groupResource), getGroupCommonKeywords(groupResource));
            group.setLabel(getLabelOrURI(groupResource));
            group.setURI(groupResource.getURI());
            group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
            String unitRef = getUnit(groupResource);
            group.setUnit(new Unit(
                    getUnitURI(groupResource),
                    unitsRepo.getUnitSymbol(unitRef),
                    unitRef
            ));
            group.setType(groupResource.hasProperty(RDF.type, Datatype.ProcessGroup) ? Type.PROCESS : Type.COEFFICIENT);
            if (groupResource.hasProperty(RDFS.comment) && groupResource.getProperty(RDFS.comment) != null) {
                group.setComment(groupResource.getProperty(RDFS.comment).getString());
            }
            group.setReferences(RepoFactory.getReferenceRepo().getReferences(groupResource));
            groupCache.put(groupResource.getURI(), group);
        }
        return groupCache.get(groupResource.getURI());
    }

    public Group getSimpleGroup(Resource groupResource)
    {
        Group group = new Group();
        group.setLabel(getLabelOrURI(groupResource));
        group.setURI(groupResource.getURI());
        group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
        String unitRef = getUnit(groupResource);
        group.setUnit(new Unit(
                getUnitURI(groupResource),
                unitsRepo.getUnitSymbol(unitRef),
                unitRef
        ));
        group.setType(groupResource.hasProperty(RDF.type, Datatype.ProcessGroup) ? Type.PROCESS : Type.COEFFICIENT);
        if (groupResource.hasProperty(RDFS.comment) && groupResource.getProperty(RDFS.comment) != null) {
            group.setComment(groupResource.getProperty(RDFS.comment).getString());
        }
        return group;
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
                Keyword keyword = new Keyword(s.getObject().toString());
                keyword.setLabel(getLabelOrURI(s.getObject().asResource()));
                dim.add(keyword);
            }
        }
        return dim;
    }

    protected Dimension getDimensionKeywords(Resource dimensionResource)
    {
        ArrayList<Keyword> keywords = new ArrayList<>();
        Selector selector = new SimpleSelector(dimensionResource, Datatype.containsKeyword, (RDFNode) null);
        StmtIterator iter = model.listStatements( selector );

        Dimension dim = new Dimension();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                Statement s = iter.nextStatement();
                Keyword keyword = new Keyword(s.getObject().toString());
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
