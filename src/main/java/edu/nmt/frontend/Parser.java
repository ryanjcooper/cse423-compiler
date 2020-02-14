package edu.nmt.frontend;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import edu.nmt.util.Debugger;

/**
 * Converts a token stream into a parse tree
 * @author Terence
 */
public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	private Node parseTree;
	private Action action;
	private Debugger debugger;
	
	public Parser(Grammar g, List<Token> tok) {
		this.grammar = g;
		this.tokens = tok;
		this.action = new Action();
		this.debugger = new Debugger(false);
		
		try {
			grammar.loadGrammar();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Goto.init(g);
	}
	
	public Parser(Grammar g, List<Token> tok, boolean debug) {
		this.grammar = g;
		this.tokens = tok;
		this.action = new Action();
		this.debugger = new Debugger(debug);
		
		try {
			grammar.loadGrammar();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Goto.init(g);
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/base.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens(), false);
		p.grammar.loadGrammar();
		//Goto.init(p.grammar);
		System.out.println(p.parse());
		//System.out.println(p.parseTree.getChildren().get(0).getChildren());
		p.printParseTree();
	}
	
	public void printParseTree() {
		System.out.println(Node.printTree(this.parseTree, " ", false));
	}

	public boolean parse() {
		Iterator<Token> tokenIt = tokens.iterator();
		Token token = null;
		
		while (true) {
			switch (this.action.getType()) {
			case ACCEPT:
				this.parseTree = action.getRoot();
				return true;
			case REJECT:
				return false;
			case SHIFT:		
				try {
					token = tokenIt.next();
				} catch (NoSuchElementException nse) {
					token = null;
				}
			case REPEAT:
				debugger.printPhase(ActionType.SHIFT);
				
				try {
					action.setType(this.action.shift(token));
				} catch (NullPointerException npe) {
					debugger.printStackTrace(npe);
					return false;
				}
				
				break;
			case REDUCE:
				debugger.printPhase(ActionType.REDUCE);
				
				action.setType(this.action.reduce());
				break;
			}
		}
	}
	
	public static String getSpacedArray(String[] arr) {
		String out = "";
		
		for (String s : arr) {
			out += s + " ";
		}
		
		return out.trim();
	}
}