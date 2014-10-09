package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.mycsense.carbondb.domain.dimension.Orientation;
import com.mycsense.carbondb.domain.Dimension;
import com.mycsense.carbondb.domain.DimensionSet;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Keyword;
import com.mycsense.carbondb.domain.group.Type;

import java.util.ArrayList;

public class GroupRepo extends AbstractRepo {

    public GroupRepo(Model model) {
        super(model);
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
        Group group = new Group(getGroupDimSet(groupResource), getGroupCommonKeywords(groupResource));
        group.setLabel(getLabelOrURI(groupResource));
        group.setURI(groupResource.getURI());
        group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
        group.setUnit(getUnit(groupResource));
        group.setUnitURI(getUnitURI(groupResource));
        group.setType(groupResource.hasProperty(RDF.type, Datatype.ProcessGroup) ? Type.PROCESS : Type.COEFFICIENT);
        if (groupResource.hasProperty(RDFS.comment) && groupResource.getProperty(RDFS.comment) != null) {
            group.setComment(groupResource.getProperty(RDFS.comment).getString());
        }
        return group;
    }

    public Group getSimpleGroup(Resource groupResource)
    {
        Group group = new Group();
        group.setLabel(getLabelOrURI(groupResource));
        group.setURI(groupResource.getURI());
        group.setId(groupResource.getURI().replace(Datatype.getURI(), ""));
        group.setUnit(getUnit(groupResource));
        group.setUnitURI(getUnitURI(groupResource));
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
        return dim;
    }
}
