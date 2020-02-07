package edu.nmt.frontend;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	private Node parseTree;
	private ArrayList<Node> stack;	// stores each token as a node as they are read in
	private LockedStack lockedStack; 
	
	public Parser(Grammar g, List<Token> tok) {
		grammar = g;
		tokens = tok;
		stack = new ArrayList<Node>();
		lockedStack = new LockedStack();
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/divide.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
		p.grammar.loadGrammar();
		//System.out.println(p.canBeReduced("type identifier l_par"));
		System.out.println(p.parse());
		//System.out.println(p.getCommonAncestor(p.grammar.getFirstSets().get("return"), "l_brace"));
	}
	
	public static String getSpacedArray(String[] strarr) {
		String output = "";
		
		for (String s : strarr) {
			output += s + " ";
		}
		
		return output.trim();		
	}

	/*
	 * determines whether a symbol can be reduced to a non-terminal
	 * @param state is the stack state at a specific iteration
	 * @param lookahead is the next symbol to be added to the stack
	 * @return state reduced to a non-terminal
	 */
	private boolean canReduce(String nt, String state, Node lookahead, String lookbehind) {
		HashSet<String> lbFirstSets = (lookbehind != null) ? this.grammar.getFirstSets().get(lookbehind) : null;	
		HashSet<String> ntFollowSet = this.grammar.getFollowSets().get(nt);
		
		/* nt or nt ancestor must be in lookbehind first set */
		if (lbFirstSets == null || getCommonAncestor(lbFirstSets, nt) != null) {
			/* lookahead must be in nt's follow set */
			if (lookahead == null || ntFollowSet.contains(lookahead.toString())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/* short for "can ever be reduced" 
	 * checks if this state exists on the rhs of any rules
	 * @param state is the current state to check for reduction
	 * @return true if the state can eventually be reduced, false else
	 */
	public boolean canBeReduced(String state) {
		for (Rule rule : this.grammar.getRules()) {
			if (getSpacedArray(rule.getRightSide()).contains(state)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isRHS(String state) {
		for (Rule rule : this.grammar.getRules()) {
			if (getSpacedArray(rule.getRightSide()).equals(state)) {
				return true;
			}
		}
		
		return false;		
	}
	
	public String reduceTo(String src, String dest) {
		String tmp = src;
		
		while (!tmp.equals(dest)) {
			String next = this.grammar.getReducedEquil(tmp);
			System.out.println("state \"" + tmp + "\" --> \"" + next + "\"\n");
			replace(next, tmp, stack);
			tmp = next;
		}
		
		return dest;
	}
	
	/*
	 * reduces a state to a non-terminal based on lookahead
	 * @param state is the stack state at a specific iteration
	 * @param lookahead is the next symbol to be added to the stack
	 * @param stack is the stack of tokens
	 * @return state reduced to a non-terminal
	 */
	public String reduce(String state, Node lookahead, String lookbehind) {
		/* loop through nts, checking to see if to change the state or not */
		String nt;
		boolean lsFlag = false;
		
		if (lookbehind == null) {
			lookbehind = lockedStack.peekString();
			lsFlag = true;
		}
		
		//System.out.println("lookbehind: " + lookbehind);
		//System.out.println("lookahead: " + lookahead);
		//System.out.println("state: " + state);
		
		if (lookbehind != null) {
			nt = getCommonAncestor(this.grammar.computeFirsts(lookbehind), state);
			
			if (nt == null) {
				String tmp;
				nt = this.grammar.getReducedEquil(state);
				tmp = getCommonAncestor(this.grammar.computeFirsts(lookbehind), nt + " " + lookahead);
				
				if (tmp == null)
					nt = null;
			}
			
			if (nt == null || nt.equals(state)) {
				//System.out.println("Rejected state \"" + state + "\" because there were not common ancestors");
				return state;
			}
			
			System.out.println("state \"" + state + "\" can become \"" + nt + "\"\n");
			if (lookahead == null 
					|| lookahead.toString().equals("semi") 
					|| this.grammar.getFollowSets().get(nt).isEmpty()
					|| hasNonTerminal(this.grammar.getFollowSets().get(nt))
					|| this.grammar.getFollowSets().get(nt).contains(lookahead.toString())) {
				
				nt = reduceTo(state, nt);
				
				String withLock = lockedStack.peekString() + " " + nt;
				
				if (isRHS(withLock)) {
					System.out.println(withLock);
					stack = lockedStack.morph(stack);
					nt = reduceTo(withLock, this.grammar.getReducedEquil(withLock));
					System.out.println(stack);
				}
				
				return nt;
			} else {
				System.out.println("Rejected due to follow sets");
			}
		} else if (lookbehind == null) {
			if (this.grammar.getAncestors(state).contains("program")) {
				return "program";
			}
		}
		
		return state;
	}
	
	/*
	 * checks if set contains non-terminal
	 * @param hs is the set to be evaluated
	 * @return true if set contains nt, else false
	 */	
	public boolean hasNonTerminal(HashSet<String> hs) {
		for (String s : hs) {
			if (this.grammar.getVariables().contains(s))
				return true;
		}
		
		return false;
	}
	
	/*
	 * checks if set contains sym or ancestors of sym
	 * ancestor = reduced equivalent of sym
	 * @param hs is the set to be evaluated
	 * @param sym is the symbol to check hs for
	 * @return common ancestor
	 */	
	public String getCommonAncestor(HashSet<String> hs, String sym) {
		HashSet<String> ancestors = this.grammar.getAncestors(sym);

		for (String s : hs) {
			if (ancestors.contains(s)) {
				return s;
			}
		}
		
		return null;
	}
	
	/*
	 * replace the last n elements on the stack with nt
	 * @param nt is the newest symbol to be added to the stack
	 * @param n are the symbols to be replaced by nt
	 * @param stack is the stack to be manipulated
	 * @return altered stack
	 */
	public ArrayList<Node> replace(String nt, String n, ArrayList<Node> stack) {
		/* check if nt and n are identical else continue */
		if (nt.equals(n)) {
			return stack;
		}
		
		/* every successful call to replace adds a new node to the tree */
		Node parent = new Node(new Token(null, nt, null, null));	// new non-terminal node
		
		/* pop n nodes off stack and add them to parent node */
		for (int i = 0; i < n.split(" ").length; i++) {
			parent.addChild(stack.remove(stack.size()-1));
		}
		
		/* push nt node onto the stack */
		stack.add(parent);
		
		return stack;
	}
	
	public Node getParseTree() {
		return this.parseTree;
	}
	
	private ArrayList<Node> tokensToNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		for (Token token : tokens) {
			nodes.add(new Node(token));
		}
		
		return nodes;
	}
	
	/*
	 * parses through a list of tokens, printing interesting messages at each shift
	 */
	public boolean parse() {
		ArrayList<Node> nodes = tokensToNodes();
		Iterator<Node> tokenIt = nodes.iterator();			// used to iterate through list of tokens
		Node lookahead = tokenIt.next();					// looks ahead to next token to be read
		boolean repeat = true;
		
		while (repeat) {
			Node token = lookahead;							// current token to be added to stack
			String lookbehind = null;							// looks behind at the previous read token
			String state = "";								// represents the stack at each inverse iteration
			
			try {
				lookahead = tokenIt.next();
			} catch (Exception e) {
				lookahead = null;
			}
			
			if (!stack.isEmpty()) {
				//state = stack.get(0).toString();
				//state = this.reduce(state, lookahead, lookbehind);
			}
			
			/* add token to stack */
			if (token != null) {
				System.out.println("\n------------------------------------------------------------------------------------------------------");
				System.out.println("Adding \"" + token + "\" to the stack\n");
				stack.add(token);
			} else if (!lockedStack.isEmpty()) {
				System.out.println("Adding \"" + lockedStack.peekString() + "\" to the stack\n");
				stack = lockedStack.morph(stack);
			} else {
				repeat = false;
			}
			
			System.out.println("Current stack: " + stack + "\n");
				
			/* 
			 * walk backwards through the stack, constructing the state
			 * at each iteration and checking if it can be reduced
			 */
			for (int i = stack.size() - 1; i >= 0; i--) {
				state = stack.get(i) + " " + state;
				state = state.trim();
				
				if (i > 0) {
					lookbehind = stack.get(i-1).toString();
				} else {
					lookbehind = lockedStack.peekString();
				}
				
				/* 
				 * check if state can be reduced 
				 * if true, see if it SHOULD be reduced
				 * else, store the current stack and renew it
				 */
				state = this.reduce(state, lookahead, lookbehind);
				
				//System.out.println("\nstate: " + state + "\n");
				
				if (state.equals("program")) {
					parseTree = stack.get(0);
					return true;
				}
				
				if (canBeReduced(state)) {
					
				} else if (lookahead != null) {
					System.out.println("Locking current stack\n");
					Node tmp = stack.remove(stack.size()-1);
					lockedStack.push(stack);
					stack = new ArrayList<Node>();
					stack.add(tmp);
					System.out.println("stack is now: " + stack);
					
					lookbehind = null;
					state = stack.get(0).toString();
					state = this.reduce(state, lookahead, lookbehind);
					System.out.println("state is now: " + state);
					
					lookbehind = tmp.toString();
					break;
				}
			}
		}
		
		/* parsing is successful if program is last on the stack */
		parseTree = null;
		return false;
	}
}
