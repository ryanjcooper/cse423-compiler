package edu.nmt.frontend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	
	public Parser(Grammar g, List<Token> tok) {
		grammar = g;
		tokens = tok;
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/conditions.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
		p.grammar.loadGrammar();
		System.out.println(p.parse());
	}
	
	public String getSpacedArray(String[] strarr) {
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
	private boolean canReduce(String nt, String state, Node lookahead, Node lookbehind) {
		HashSet<String> ntFollowSet = this.grammar.getFollowSets().get(nt);
		/* 
		 * state will only reduce if:
		 * 	(1) if this state contains the last token
		 * 	(2) if this state contains a semi
		 * 	(3) if the follow set of the non-terminal contains the lookahead
		 * 	(4) if the follow set of the non-terminal contains a non-terminal
		 */
		if (lookahead == null 
				|| state.contains("semi") 
				|| state.contains("r_brace")
				|| ntFollowSet.isEmpty() 
				|| ntFollowSet.contains(lookahead.toString()) 					  
				|| this.hasNonTerminal(ntFollowSet)) {
			
			HashSet<String> lbFirstSets = (lookbehind != null) ? this.grammar.getFirstSets().get(lookbehind.toString()) : null;	
			
			/* 
			 * state will only reduce for real if:
			 * 	(1) lookbehind exists and
			 *  (2) the firstsSet of lookbehind contains state or
			 *  (3) the firstsSet of lookbehind contains the first element of the state and does not only consist of it
			 */
			if (!state.equals("else ifStmt") && (lbFirstSets != null 
					&& (lbFirstSets.contains(state) 
					|| (lbFirstSets.contains(state.split(" ")[0]) && !state.split(" ")[0].equals(nt))))
					|| (nt.equals("program") && lookahead != null)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	/*
	 * reduces a state to a non-terminal based on lookahead
	 * @param state is the stack state at a specific iteration
	 * @param lookahead is the next symbol to be added to the stack
	 * @param stack is the stack of tokens
	 * @return state reduced to a non-terminal
	 */
	public String reduce(String state, Node lookahead, Node lookbehind, ArrayList<Node> stack) {
		boolean repeat = true;
		
		//System.out.println("state = " + state + "\n");
		
		while (repeat) {
			ArrayList<String> nts = new ArrayList<String>();	// list of possible non-terminals
			repeat = false;
			
			for (Rule rule : this.grammar.getRules()) {
				/* convert right side to space-spaced string */
				String rhs = getSpacedArray(rule.getRightSide());
				
				/* get all possible non-terminals the currrent state can become */
				if (rhs.equals(state)) {
					System.out.println("state \"" + rhs + "\" can become \"" + rule.getLeftSide() + "\"");
					nts.add(rule.getLeftSide());
				}
			}
			
			/* loop through nts, checking to see if to change the state or not */
			for (String nt : nts) {
					if (!this.canReduce(nt, state, lookahead, lookbehind)) {
						System.out.println("state \"" + state + "\" --> \"" + state + "\"\n");
						repeat = false;
					} else {
						System.out.println("state \"" + state + "\" --> \"" + nt + "\"\n");
						this.replace(nt, state, stack);
						state = nt;
						repeat = true;
						break;
					}
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
		Node node = new Node(new Token(null, nt, null, null));	// new non-terminal node
		
		/* pop n nodes off stack and add them to parent node */
		for (int i = 0; i < n.split(" ").length; i++) {
			node.addChild(stack.remove(stack.size()-1));
		}
		
		/* push nt node onto the stack */
		stack.add(node);
		
		return stack;
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
		ArrayList<Node> stack = new ArrayList<Node>();		// stores each token as a node as they are read in
		ArrayList<Node> nodes = tokensToNodes();
		Iterator<Node> tokenIt = nodes.iterator();			// used to iterate through list of tokens
		Node lookahead = tokenIt.next();					// looks ahead to next token to be read
		boolean repeat = true;
		
		while (repeat) {
			Node token = lookahead;							// current token to be added to stack
			Node lookbehind = null;							// looks behind at the previous read token
			String state = "";								// represents the stack at each inverse iteration
			
			try {
				lookahead = tokenIt.next();
			} catch (Exception e) {
				lookahead = null;
			}
			
			/* add token to stack */
			if (token != null) {
				System.out.println("Adding \"" + token + "\" to the stack\n");
				stack.add(token);
			} else {
				repeat = false;
			}
			
			System.out.println("Current stack: " + stack + "\n");
			
			/* 
			 * walk backwards through the stack, constructing the state
			 * at each iteration and checking if it can be reduced
			 */
			for (int i = stack.size() - 1; i >= 0; i--) {
				String newState; // tmp variable used to represent the result from reduce
				state = stack.get(i) + " " + state;
				state = state.trim();

				if (i > 0) {
					lookbehind = stack.get(i-1);
				} else {
					lookbehind = null;
				}
				
				newState = this.reduce(state, lookahead, lookbehind, stack);
				state = newState;
			}
		}
		
		return stack.size() == 1 && stack.get(0).toString().equals("program");
	}
}
