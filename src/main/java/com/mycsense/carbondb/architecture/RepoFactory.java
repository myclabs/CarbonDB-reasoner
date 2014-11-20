package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;
import com.mycsense.carbondb.ReasonnerReport;

public class RepoFactory {
    protected static CategoryRepo categoryRepo;
    protected static GroupRepo groupRepo;
    protected static SingleElementRepo singleElementRepo;
    protected static RelationRepo relationRepo;
    protected static ReferenceRepo referenceRepo;
    protected static UnitsRepo unitsRepo;
    protected static TypeRepo typeRepo;
    protected static Model model;
    protected static ReasonnerReport reasonnerReport;

    public static void setModel(Model pModel) {
        model = pModel;
    }

    public static void checkProperlyInitialized() throws RuntimeException {
        if (null == model) {
            throw new RuntimeException("The RepoFactory cannot be used without setting the Model first");
        }
    }

    public static void clear() {
        categoryRepo = null;
        groupRepo = null;
        singleElementRepo = null;
        relationRepo = null;
        referenceRepo = null;
        unitsRepo = null;
        typeRepo = null;
    }

    public static CategoryRepo getCategoryRepo() {
        checkProperlyInitialized();
        if (null == categoryRepo)
            categoryRepo = new CategoryRepo(model);
        return categoryRepo;
    }

    public static GroupRepo getGroupRepo() {
        checkProperlyInitialized();
        if (null == groupRepo)
            groupRepo = new GroupRepo(model, unitsRepo);
        return groupRepo;
    }

    public static SingleElementRepo getSingleElementRepo() {
        checkProperlyInitialized();
        if (null == singleElementRepo)
            singleElementRepo = new SingleElementRepo(model, unitsRepo);
        return singleElementRepo;
    }

    public static RelationRepo getRelationRepo() {
        checkProperlyInitialized();
        if (null == relationRepo)
            relationRepo = new RelationRepo(model, unitsRepo);
        return relationRepo;
    }

    public static ReferenceRepo getReferenceRepo() {
        checkProperlyInitialized();
        if (null == referenceRepo)
            referenceRepo = new ReferenceRepo(model);
        return referenceRepo;
    }

    public static UnitsRepo getUnitsRepo() {
        checkProperlyInitialized();
        if (null == unitsRepo)
            unitsRepo = new UnitsRepo(model);
        return unitsRepo;
    }

    public static TypeRepo getTypeRepo() {
        checkProperlyInitialized();
        if (null == typeRepo)
            typeRepo = new TypeRepo(model);
        return typeRepo;
    }

    public static ReasonnerReport getReasonnerReport() {
        return reasonnerReport;
    }

    public static void setReasonnerReport(ReasonnerReport reasonnerReport) {
        RepoFactory.reasonnerReport = reasonnerReport;
    }
}
