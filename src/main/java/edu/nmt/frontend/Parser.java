package edu.nmt.frontend;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	private Node parseTree;
	private Action action;
	
	public Parser(Grammar g, List<Token> tok) {
		grammar = g;
		tokens = tok;
		action = new Action("program");
		try {
			grammar.loadGrammar();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Goto.init(g);
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/conditions.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
		p.grammar.loadGrammar();
		//Goto.init(p.grammar);
		System.out.println(p.parse());
	}
	
	public boolean parse() {
		Iterator<Token> tokenIt = tokens.iterator();
		Token token = null;
		Token lookahead = tokenIt.next();
		
		while (true) {
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
				action.next(token, lookahead);
				break;
			case REDUCE:
				System.out.println(action.reduce());
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