/*
 * Copyright 2014, by Benjamin Bertin and Contributors.
 *
 * This file is part of CarbonDB-reasoner project <http://www.carbondb.org>
 *
 * CarbonDB-reasoner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CarbonDB-reasoner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CarbonDB-reasoner.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributor(s): -
 *
 */

package com.mycsense.carbondb.architecture;

import com.hp.hpl.jena.rdf.model.Model;

public class RepoFactory {
    protected static CategoryRepo categoryRepo;
    protected static GroupRepo groupRepo;
    protected static SingleElementRepo singleElementRepo;
    protected static RelationRepo relationRepo;
    protected static ReferenceRepo referenceRepo;
    protected static UnitsRepo unitsRepo;
    protected static TypeRepo typeRepo;
    protected static KeywordRepo keywordRepo;
    protected static Model model;

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
        keywordRepo = null;
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

    public static SingleElementRepo getSingleElementRepo() {
        checkProperlyInitialized();
        if (null == singleElementRepo)
            singleElementRepo = new SingleElementRepo(model, unitsRepo);
        return singleElementRepo;
    }

    public static RelationRepo getRelationRepo() {
        checkProperlyInitialized();
        if (null == relationRepo)
            relationRepo = new RelationRepo(model);
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

    public static KeywordRepo getKeywordRepo() {
        checkProperlyInitialized();
        if (null == keywordRepo)
            keywordRepo = new KeywordRepo(model);
        return keywordRepo;
    }
}
