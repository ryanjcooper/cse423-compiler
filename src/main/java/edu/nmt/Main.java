package edu.nmt;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Parser;
import edu.nmt.frontend.Scanner;
import edu.nmt.RuntimeSettings;

public class Main {
	
	private static String sourceFilename;
	private static Boolean storeTokens;
	
	private static void parseArgs(String[] args) {

        Options options = new Options();

        Option tok = new Option("t", "tokens", false, "dump tokens to file");
        tok.setRequired(false);
        options.addOption(tok);

        Option output = new Option("o", "output", true, "file to compile to");
        output.setRequired(false);
        options.addOption(output);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            List<String> arglist = cmd.getArgList();

            storeTokens = cmd.hasOption("t");

            if (arglist.size() == 1) {
            	sourceFilename = arglist.get(0);
            } else if (arglist.size() == 0) {
                formatter.printHelp("java -jar compiler.jar main.c [-t] [-o] outputname", options);

                System.exit(0);
            } else { 
            	throw new ParseException("Too many files given: " + arglist.toString());
            }            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar compiler.jar main.c [-t] [-o] outputname", options);

            System.exit(1);
        }
	}
    
	
    public static void main(String[] args) throws IOException {
    	parseArgs(args);
    	
    	// Start scanner
    	Scanner s = new Scanner(sourceFilename);
    	s.scan();
    	if (storeTokens) {
    		s.offloadToFile();
    	}
    	
    	// Initialize grammar
    	Grammar grammar = new Grammar(RuntimeSettings.grammarFile);
    	grammar.loadGrammar();
    	
    	// Start parser
    	Parser p = new Parser(grammar, s.getTokens());
    	p.parse();
    	
    	
    }
	
	
}
