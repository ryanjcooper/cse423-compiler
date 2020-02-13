package edu.nmt.frontend;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	private Node parseTree;
	private Action action;
	private Stack<Goto> stack;
	
	public Parser(Grammar g, List<Token> tok) {
		grammar = g;
		tokens = tok;
		action = new Action("program");
		this.stack = new Stack();
		try {
			grammar.loadGrammar();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Goto.init(g);
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/switch.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
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
		Token lookahead = tokenIt.next();
		
		while (true) {
			
			//if (token != null  && token.getTokenLabel().equals("return"))
				//return false;
			
			switch (this.action.getType()) {
			case ACCEPT:
				return true;
			case REJECT:
				return false;
			case SHIFT:		
				token = lookahead;
				
				try {
					lookahead = tokenIt.next();
				} catch (NoSuchElementException nse) {
					lookahead = null;
				}
				
			case REPEAT:
				System.out.println("\n----------------------------------------------");
				System.out.println("SHIFT PHASE");
				System.out.println("----------------------------------------------\n");	
				
				try {
					this.action.shift(token, lookahead);
				} catch (NullPointerException npe) {
					npe.printStackTrace();
					return false;
				}
				
				break;
			case REPLACE:
				System.out.println("----------------------------------------------");
				System.out.println("REPLACE PHASE");
				System.out.println("----------------------------------------------\n");
				this.parseTree = this.action.reduce();
				break;
			case REDUCE:
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