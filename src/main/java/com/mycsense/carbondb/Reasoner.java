package com.mycsense.carbondb; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.Matrix;
import org.la4j.inversion.MatrixInverter;
import org.la4j.LinearAlgebra;

import org.mindswap.pellet.jena.PelletReasonerFactory;

public class Reasoner {

    protected Model model;
    protected InfModel infModel;
    protected Reader reader;
    protected Writer writer;
    protected com.hp.hpl.jena.reasoner.Reasoner jenaReasoner;
    protected Matrix a, b, c, d, uncertaintyMatrix, u;
    protected ArrayList<Resource> elementaryFlowNatures, processes;
    protected Double threshold = new Double(0.1);

    Reasoner (Model model) {
        this.model = model;
        jenaReasoner = PelletReasonerFactory.theInstance().create();
    }

    public void run () {
        /*
        load the ontology -> ontologyloader
        convert macro relations -> macrorelations convert
        calculate ecological flows -> calculate ecological flows
        */
        infModel = ModelFactory.createInfModel( jenaReasoner, model );
        reader = new Reader(infModel);
        writer = new Writer(infModel);
        for (MacroRelation macroRelation: reader.getMacroRelations()) {
            createMicroRelations(macroRelation.translate());
        }

        processes = reader.getSingleProcesses();
        elementaryFlowNatures = reader.getElementaryFlowNatures();

        //createMatrix();
        //inverseMatrix();
        createEcologicalMatrix();
        iterativeCalculation();
        //calculateCumulatedEcologicalFlows();
        createCumulatedEcologicalFlows();
        System.out.println(u);
        /*
        Calcul des incertitudes :
            on sait les calculer pour des additions et des multiplications
            on calcul : A + A.A + A.A.A + ... jusqu'à ce que le delta des incertitudes soit inférieur à un seuil donné
            on obtient la matrice des incertitudes des coefficients
            ensuite on fait A . B = matrice des incertitudes des émissions
        */
    }

    protected void iterativeCalculation()
    {
        Matrix m = getMatrix();
        Matrix result = m.copy();

        Matrix rPrev = m.copy();
        Matrix r = rPrev.multiply(rPrev);

        Matrix uPrev = uncertaintyMatrix.copy();
        System.out.println(uPrev);
        u = uPrev.power(2);

        int maxIter = 0;
        while (differenceLowerThanThreshold(rPrev, r, threshold) && maxIter < 1000) {
            System.out.println("A.A");
            rPrev = r.copy();
            r = r.multiply(m);
            result = result.add(r);
            maxIter++;
            // uncertainty calculation
            // uncertainty product = sqrt(pow(uncertainty^n-1) + pow(uncertainty^n))
            //                                uPrev                  u
            uPrev = u.copy();
            u = sqrtMatrix(u.power(2).add(uPrev.power(2)));
            // uncertainty sum = sqrt(pow(A^n-1 * uncertainty^n-1, 2) + pow(A^n * uncertainty^n, 2)) / abs(A^n + A^n-1)
            //                            rPrev   uPrev                     r     u                           result

            u = sqrtMatrix(uPrev.multiply(rPrev).power(2).add(u.multiply(result).power(2)));
        }
        for (int i = 0; i < result.rows(); i++)
            result.set(i, i, 1.0);
        d = result.multiply(b);
    }

    protected Matrix sqrtMatrix(Matrix m)
    {
        Matrix r = m.copy();
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                r.set(i, j, Math.sqrt(m.get(i,j)));
            }
        }
        return r;
    }

    protected boolean differenceLowerThanThreshold(Matrix a, Matrix b, Double threshold)
    {
        Matrix c = a.subtract(b);
        for (int i = 0; i < c.rows(); i++) {
            for (int j = 0; j < c.columns(); j++) {
                if (c.get(i,j) >= threshold) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void calculateCumulatedEcologicalFlows()
    {
        // We will use Gauss-Jordan method for inverting
        MatrixInverter inverter = a.withInverter(LinearAlgebra.GAUSS_JORDAN);
        // The 'b' matrix will be dense
        c = inverter.inverse(LinearAlgebra.DENSE_FACTORY);

        d = c.multiply(b);
    }

    protected void createMatrix() {
        a = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            a.set(i, i, 1.0);
            ArrayList<Resource> relations = reader.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestination).getResource();
                a.set(processes.indexOf(downStreamProcess), i, - reader.getCoefficientValueForRelation(relation));
            }
        }
    }

    protected void sumUncertainty() {

    }

    protected Matrix getMatrix() {
        Matrix m = new CCSMatrix(processes.size(), processes.size());
        uncertaintyMatrix = new CCSMatrix(processes.size(), processes.size());
        for (int i = 0; i < processes.size(); i++) {
            //m.set(i, i, 1.0);
            ArrayList<Resource> relations = reader.getRelationsForProcess(processes.get(i));
            for (Resource relation : relations) {
                RDFNode downStreamProcess = relation.getProperty(Datatype.hasDestination).getResource();
                m.set(processes.indexOf(downStreamProcess), i, reader.getCoefficientValueForRelation(relation));
                uncertaintyMatrix.set(processes.indexOf(downStreamProcess), i, reader.getCoefficientUncertaintyForRelation(relation));
            }
        }
        return m;
    }

    protected void createEcologicalMatrix() {
        b = new CCSMatrix(processes.size(), elementaryFlowNatures.size());

        for (int i = 0; i < processes.size(); i++) {
            HashMap<Resource, Double> emissions = reader.getEmissionsForProcess(processes.get(i));
            for (Entry<Resource, Double> e : emissions.entrySet()) {
                Resource nature = e.getKey();
                Double value = e.getValue();
                b.set(i, elementaryFlowNatures.indexOf(e.getKey()), e.getValue());
            }
        }
    }

    protected void createCumulatedEcologicalFlows()
    {
        for (int i = 0; i < processes.size(); i++) {
            for (int j = 0; j < elementaryFlowNatures.size(); j++) {
                writer.addCumulatedEcologicalFlow(processes.get(i), elementaryFlowNatures.get(j), (float) d.get(i, j));
            }
        }
    }

    protected void createMicroRelations(ArrayList<MicroRelation> microRelations)
    {
        for (MicroRelation microRelation: microRelations) {
            Resource sourceProcess = reader.getElementForDimension(microRelation.source, Datatype.SingleProcess);
            Resource coeff = reader.getElementForDimension(microRelation.coeff, Datatype.SingleCoefficient);
            Resource destinationProcess = reader.getElementForDimension(microRelation.destination, Datatype.SingleProcess);
            if (sourceProcess != null && coeff != null && destinationProcess != null) {
                writer.addMicroRelation(sourceProcess, coeff, destinationProcess);
            }
        }
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public Model getModel() {
        return model;
    }
}