package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;

public class RepoFactory {
    protected static CategoryRepo categoryRepo;
    protected static GroupRepo groupRepo;
    protected static SingleElementsRepo singleElementsRepo;
    protected static RelationRepo relationRepo;
    protected static Model model;
    protected static UnitsRepo unitsRepo;

    public static void setModel(Model pModel) {
        model = pModel;
    }

    public static void setUnitsRepo(UnitsRepo pUnitsRepo) {
        unitsRepo = pUnitsRepo;
    }

    public static void checkProperlyInitialized() throws RuntimeException {
        if (null != model && null != unitsRepo) {
            throw new RuntimeException("The RepoFactory could not be used without setting the Model and UnitsRepo first");
        }
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
            groupRepo = new GroupRepo(model);
        return groupRepo;
    }

    public static SingleElementsRepo getSingleElementsRepo() {
        checkProperlyInitialized();
        if (null == singleElementsRepo)
            singleElementsRepo = new SingleElementsRepo(model, unitsRepo);
        return singleElementsRepo;
    }

    public static RelationRepo getRelationRepo() {
        checkProperlyInitialized();
        if (null == relationRepo)
            relationRepo = new RelationRepo(model, unitsRepo);
        return relationRepo;
    }
}
