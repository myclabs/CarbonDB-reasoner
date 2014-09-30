package com.mycsense.carbondb;

import com.hp.hpl.jena.rdf.model.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;
import com.mycsense.carbondb.architecture.RepoFactory;
import com.mycsense.carbondb.architecture.UnitsRepo;
import com.mycsense.carbondb.architecture.UnitsRepoWebService;
import com.mycsense.carbondb.domain.Category;
import com.mycsense.carbondb.domain.Group;

public class App
{
    public static void main(String[] args) throws IOException {
        if (args.length > 1){
	        runAll(args[0], args[1]);
        }
        else if (args.length == 1){
            run(args[0]);
        }
        else {
        	System.out.println("Usage: App inputFile.rdf outputFile.rdf");
        }
    }

    public static void runAll(String inputFileName, String outputFileName)
    {
        UnitsRepo unitRepo = new UnitsRepoWebService();
        Model model = ModelFactory.createDefaultModel( );

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                "File: " + inputFileName + " not found");
        }

        model.read( in, null );

        Reasoner reasoner = new Reasoner(model, unitRepo);
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

    public static void run(String inputFileName) {
        UnitsRepo unitsRepo = new UnitsRepoWebService();
        Model model = ModelFactory.createDefaultModel( );

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                "File: " + inputFileName + " not found");
        }

        model.read( in, null );
        Reasoner reasoner = new Reasoner(model, unitsRepo);
        reasoner.run();
        System.out.println(reasoner.report.errors);
        System.out.println(reasoner.report.warnings);

        Category cat = RepoFactory.getCategoryRepo().getCategoriesTree();
        for (Object child: cat.getChildren()) {
            if (child instanceof Category) {
                printCategory((Category) child, "");
            }
            else if (child instanceof Group) {
                System.out.println( ((Group)child).getLabel() );
            }
        }
    }

    public static void printCategory(Category cat, String prefix) {
        System.out.println(prefix + cat.getLabel());
        for (Object child: cat.getChildren()) {
            if (child instanceof Category) {
                printCategory((Category) child, prefix + "  ");
            }
            else if (child instanceof Group) {
                System.out.println(prefix + "  " + ((Group)child).getLabel() );
            }
        }
    }

        /**
        read, write RDF files: OK
        query model: OK
        run calculation: OK
        convert source-relations: OK
        use Pellet to infer the ontology with java: OK (downgraded jena to 2.10.0 ... try with pellet/master?)
        in conversion: foreach over all the sourceRelations: OK
        rename ontology concept for group -> group: OK
        check if SWRL rules are executed with Pellet: OK
        cut the onto in two parts (vocabulary and data): TODO
        calculate uncertainties: TODO
        use a class to store the ontology classes and property: OK
        units: OK
        flow type?
        generator group?
        use OWL API instead of Jena: Nope
        add property isReferenced for the link between elements and groups: NO (useless?)

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
                sourceRelations involving single elements
        **/
}
