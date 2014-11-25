package com.mycsense.carbondb;

import com.hp.hpl.jena.rdf.model.*;
import java.io.*;
import java.io.FileOutputStream;
import com.hp.hpl.jena.util.FileManager;
import com.mycsense.carbondb.architecture.RepoFactory;
import com.mycsense.carbondb.architecture.UnitToolsWebService;
import com.mycsense.carbondb.domain.Category;
import com.mycsense.carbondb.domain.Group;
import com.mycsense.carbondb.domain.Unit;

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
        UnitToolsWebService unitTools = new UnitToolsWebService();
        Unit.setUnitTools(unitTools);
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

    public static void run(String inputFileName) {
        UnitToolsWebService unitTools = new UnitToolsWebService();
        Unit.setUnitTools(unitTools);
        Model model = ModelFactory.createDefaultModel( );

        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                "File: " + inputFileName + " not found");
        }

        model.read( in, null );
        Reasoner reasoner = new Reasoner(model);
        reasoner.run();

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
    generator group?
    add property isReferenced for the link between elements and groups: NO (useless?)
    **/
}
