package edu.nmt.frontend;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import edu.nmt.util.Debugger;

/**
 * Converts a token stream into a parse tree
 * @author Terence
 * 
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
	
	/**
	 * prints the parse tree to console
	 */
	public void printParseTree() {
		System.out.println(Node.printTree(this.parseTree, " ", false));
	}

	/**
	 * iterate through stream of tokens and parse them
	 * at each iteration, perform an action based on action type
	 * @return true if successful parse, false else
	 */
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
	
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/base.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens(), false);
		p.grammar.loadGrammar();
		System.out.println("Output of parse(): " + p.parse() + "\n");
		p.printParseTree();
	}
}