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

package com.mycsense.carbondb;

import java.util.ArrayList;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;

import com.mycsense.carbondb.architecture.RepoFactory;
import com.mycsense.carbondb.domain.*;
import com.mycsense.carbondb.domain.Process;
import com.mycsense.carbondb.domain.relation.TranslationDerivative;

import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.jena.PelletInfGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 *     This class contains the workflow to create all the inferred knowledge of the CarbonDB ontology.
 * </p>
 * <p>
 *     The workflow is as follows:
 *     <ul>
 *         <li>
 *             Load the ontology
 *         </li>
 *         <li>
 *             Use Pellet inference engine
 *         </li>
 *         <li>
 *             Convert the source relations into derived relations
 *         </li>
 *         <li>
 *             Calculate the cumulative elementary flows and the impacts
 *         </li>
 *         <li>
 *             Check the ontology for inconsistencies
 *         </li>
 *     </ul>
 * </p>
 * <p>
 *     During this workflow, all the inferred data are stored in the ontology residing in the memory.
 * </p>
 */
public class Reasoner {

    protected Model model;
    protected InfModel infModel;

    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;
    protected CarbonOntology ontology;

    private final Logger log = LoggerFactory.getLogger(Reasoner.class);

    /**
     * @param model the RDF model containing the CarbonDB ontology.
     */
    public Reasoner (Model model) {
        this.model = model;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    /**
     * Launch the workflow described above.
     */
    public void run () {
        log.info("Begin reasonning");
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        ((PelletInfGraph) infModel.getGraph()).classify();
        ((PelletInfGraph) infModel.getGraph()).realize();

        RepoFactory.clear();
        RepoFactory.setModel(infModel);

        log.info("Loading the object model");
        loadOntology();
        log.info("Translating source relations");
        // We process the ontology only using the object model
        for (SourceRelation sourceRelation: ontology.getSourceRelations().values()) {
            ArrayList<TranslationDerivative> derivatives = new ArrayList<>();
            try {
                derivatives = sourceRelation.translate();
            } catch (IncompatibleDimSetException | IncompatibleUnitsException e) {
                log.warn(e.getMessage());
            }
            for (TranslationDerivative derivative: derivatives) {
                try {
                    ontology.addDerivedRelation(derivative.transformToDerivedRelation());
                } catch (NoElementFoundException e) {
                    //log.warn(e.getMessage()); // we do not want to log this, as it is a normal behavior
                } catch (AlreadyExistsException e) {
                    log.warn(e.getMessage());
                }
            }
        }

        Calculation calculation = new Calculation();
        calculation.run();

        log.info("Creating calculated flows");
        try {
            calculation.createCalculatedElementaryFlows();
        } catch (AlreadyExistsException e) {
            log.warn(e.getMessage());
        }
        log.info("Creating impacts");
        try {
            calculation.createImpacts();
        } catch (AlreadyExistsException e) {
            log.warn(e.getMessage());
        }

        log.info("Checking the ontology");
        checkOntology();

        log.info("Reasoning finished");
    }

    /**
     * Check the ontology for the following inconsistencies:
     * <ul>
     *     <li>groups referencing no single element</li>
     *     <li>group not used in any source relation</li>
     *     <li>groups not belonging to any category</li>
     *     <li>single elements not referenced in any group</li>
     *     <li>single processes not having any cumulative elementary flow</li>
     *     <li>source relations not generating any derived relation</li>
     *     <li>unused dimensions</li>
     * </ul>
     */
    protected void checkOntology() {
        ontology = CarbonOntology.getInstance();
        HashSet<String> unusedDimensions = new HashSet<>(ontology.getDimensions().keySet());
        for (Group group: ontology.getCoefficientGroups().values()) {
            if (group.getElements().size() == 0) {
                log.warn("The coefficient group " + group.getId() + " does not contain any coefficient");
            }
            if (!group.hasCategory()) {
                log.warn("The coefficient group " + group.getId() + " has no category");
            }
            if (group.getSourceRelations().size() == 0) {
                log.warn("The coefficient group " + group.getId() + " has no source relation");
            }
            for (Dimension dimension: group.getDimSet().dimensions) {
                if (unusedDimensions.contains(dimension.getId())) {
                    unusedDimensions.remove(dimension.getId());
                }
            }
        }
        for (Group group: ontology.getProcessGroups().values()) {
            if (group.getElements().size() == 0) {
                log.warn("The process group " + group.getId() + " does not contain any coefficient");
            }
            if (!group.hasCategory()) {
                log.warn("The process group " + group.getId() + " has no category");
            }
            for (Dimension dimension: group.getDimSet().dimensions) {
                if (unusedDimensions.contains(dimension.getId())) {
                    unusedDimensions.remove(dimension.getId());
                }
            }
        }
        for (Coefficient coefficient: ontology.getCoefficients()) {
            if (coefficient.getGroups().size() == 0) {
                log.warn("The coefficient " + coefficient.getId() + " is not referenced in any group");
            }
        }
        for (Process process: ontology.getProcesses()) {
            if (process.getGroups().size() == 0) {
                log.warn("The process " + process.getId() + " is not referenced in any group");
            }
            if (process.getCalculatedFlows().size() == 0) {
                log.warn("The process " + process.getId() + " has no calculated elementary flow");
            }
        }
        for (SourceRelation sourceRelation: ontology.getSourceRelations().values()) {
            if (sourceRelation.getDerivedRelations().size() == 0) {
                log.warn("The source relation " + sourceRelation.getId() + " did not generate any derived relation");
            }
        }
        for (String dimensionId: unusedDimensions) {
            log.warn("The dimension " + dimensionId + " is not used in any group");
        }
    }

    /**
     * Load the ontology starting from the lowest elements in the dependency tree to the highest ones.
     * The ontology is loaded into the CarbonOntology singleton.
     */
    protected void loadOntology() {
        ontology = CarbonOntology.getInstance();
        ontology.setElementaryFlowTypesTree(RepoFactory.getTypeRepo().getElementaryFlowTypesTree());
        ontology.setImpactTypesTree(RepoFactory.getTypeRepo().getImpactTypesTree());
        ontology.setReferences(RepoFactory.getReferenceRepo().getReferences());
        ontology.setRelationTypes(RepoFactory.getRelationRepo().getRelationTypes());
        for (Process process: RepoFactory.getSingleElementRepo().getProcesses()) {
            try {
                ontology.addProcess(process);
            }
            catch (AlreadyExistsException e) {
                log.warn(e.getMessage());
            }
        }
        for (Coefficient coefficient: RepoFactory.getSingleElementRepo().getCoefficients()) {
            try {
                ontology.addCoefficient(coefficient);
            }
            catch (AlreadyExistsException e) {
                log.warn(e.getMessage());
            }
        }
        ontology.setDimensions(RepoFactory.getDimensionRepo().getDimensions());
        ontology.setProcessGroups(RepoFactory.getGroupRepo().getProcessGroups());
        ontology.setCoefficientGroups(RepoFactory.getGroupRepo().getCoefficientGroups());
        ontology.setSourceRelations(RepoFactory.getRelationRepo().getSourceRelations());
        ontology.setCategoryTree(RepoFactory.getCategoryRepo().getCategoriesTree());
    }

    /**
     * @return the inferred RDF model.
     */
    public InfModel getInfModel() {
        return infModel;
    }

    /**
     * @return the raw RDF model.
     */
    public Model getModel() {
        return model;
    }
}