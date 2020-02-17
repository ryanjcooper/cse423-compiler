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

import edu.nmt.frontend.ASTParser;
import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.Parser;
import edu.nmt.frontend.Scanner;
import edu.nmt.RuntimeSettings;

public class Main {
	
	private static String sourceFilename;
	private static Boolean storeTokens;
	private static Boolean printParseTree;
	private static String writeParseFile;
	private static Boolean printAST;
	private static Boolean printST;
	
	private static void parseArgs(String[] args) {
		
        String helpStatement = "java -jar compiler.jar main.c [-t] [-o] outputname [-pp] [-wp] outputfile";

        Options options = new Options();
        
        Option help = new Option("help", "print this message");
        options.addOption(help);

        Option tok = new Option("t", "tokens", false, "dump tokens to file");
        tok.setRequired(false);
        options.addOption(tok);

        Option output = new Option("o", "output", true, "file to compile to");
        output.setRequired(false);
        options.addOption(output);
        
        Option ppt = new Option("pp", "print-parsetree", false, "print the parse tree");
        ppt.setRequired(false);
        options.addOption(ppt);
        
        Option wpt = new Option("wp", "write-parsetree", true, "write parse tree to file (not yet supported!)");
        wpt.setRequired(false);
        options.addOption(wpt);
        
        Option apt = new Option("ap", "print-ast", false, "print the abstract syntax tree (limited support)");
        ppt.setRequired(false);
        options.addOption(apt);
        
        Option stp = new Option("stp", "print-symboltable", false, "print all scoped symbol tables");
        ppt.setRequired(false);
        options.addOption(stp);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            List<String> arglist = cmd.getArgList();
            
            if (cmd.hasOption("help")) {
            	formatter.printHelp(helpStatement, options);
            	
            	System.exit(1);
            }

            storeTokens = cmd.hasOption("t");
            printParseTree = cmd.hasOption("pp");
            printAST = cmd.hasOption("ap");
            printST = cmd.hasOption("stp");
            
            if (cmd.hasOption("wp")) {
            	writeParseFile = cmd.getOptionValue("wp");
            } else {
            	writeParseFile = null;
            }

            if (arglist.size() == 1) {
            	sourceFilename = arglist.get(0);
            } else if (arglist.size() == 0) {
                formatter.printHelp(helpStatement, options);

                System.exit(0);
            } else { 
            	throw new ParseException("Too many files given: " + arglist.toString());
            }            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(helpStatement, options);

            System.exit(1);
        }
	}
    
	
    public static void main(String[] args) throws Exception {
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
    	Parser p = new Parser(grammar, s);
    	//p.parse();
    	if (printParseTree) {
    		p.printParseTree();
    	}
    	
    	if (writeParseFile != null) {
    		//p.writeParseTree(writeParseFile);
    	}
    	
    	// Start AST Parser
		ASTParser a = new ASTParser(p);
		
		if (a.parse()) {
	    	if (printAST) {
	    		a.printAST();
	    	}
	    	
	    	if (printST) {
	    		a.printSymbolTable();
	    	}
		}    	
    }
}
