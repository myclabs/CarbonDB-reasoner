package com.mycsense.carbondb;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import com.hp.hpl.jena.reasoner.Reasoner;

public class App 
{
    public static void main(String[] args) throws IOException {
        //runCalculation();
        //runConversion();
        if (args.length > 1){
	        runAll(args[0], args[1]);
        }
        else {
        	System.out.println("Usage: App inputFile.rdf outputFile.rdf");
        }
    }

    public static void runAll(String inputFileName, String outputFileName)
    {
        Reasoner reasoner = PelletReasonerFactory.theInstance().create();
        
        Model emptyModel = ModelFactory.createDefaultModel( );
        
        InfModel model = ModelFactory.createInfModel( reasoner, emptyModel );

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                        "File: " + inputFileName + " not found");
        }

        model.read( in, null );

        Conversion conv = new Conversion(model);
        conv.run();
        Calculation calc = new Calculation(model);
        calc.run();

        try {
            FileOutputStream out = new FileOutputStream( outputFileName );
            model.write(out);
        }
        catch (IOException e) {
            System.out.println("error while writing the onto file");
        }
    }
        /**
        read, write RDF files: OK
        query model: OK
        run calculation: OK
        convert macro-relations: OK
        use Pellet to infer the ontology with java: OK (downgraded jena to 2.10.0 ... try with pellet/master?)
        in conversion: foreach over all the macroRelations: OK
        check if SWRL rules are executed with Pellet: TODO
        cut the onto in two parts (vocabulary and data): TODO
        calculate uncertainties: TODO
        add property isReferenced for the link between elements and family: TODO
        use a class to store the ontology classes and property: TODO
        units?
        flow type?
        generator family?
        rename ontology concept for family -> group
        use OWL API instead of Jena: ???

        improve architecture:
            Maven: OK
            unit tests
            create multiple classes: OK
            use IDEA
            logger
            use stardog?

        add errors checking (throw exceptions)
        Protege plugin?

        Limits: no units conversions
                no uncertainty (is montecarlo necessary?)
                no flow type (economic, conversion...)
                rules and jena with pellet?
                macroRelations involving single elements
        **/
}
