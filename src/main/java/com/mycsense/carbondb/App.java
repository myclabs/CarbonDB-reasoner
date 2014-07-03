package com.mycsense.carbondb;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;

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
        Model model = ModelFactory.createDefaultModel( );

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                "File: " + inputFileName + " not found");
        }

        model.read( in, null );

        Reasoner reasoner = new Reasoner(model);
        reasoner.run();

        try {
            FileOutputStream out = new FileOutputStream( outputFileName );
            reasoner.getInfModel().write(out);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(
                "Cannot write to file " + outputFileName);
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
