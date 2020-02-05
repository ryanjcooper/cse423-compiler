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
		Scanner scanner = new Scanner("test/min.c");
		scanner.scan();
		Parser p = new Parser(new Grammar("config/grammar.cfg"), scanner.getTokens());
		p.grammar.loadGrammar();
		p.parse();
	}
	
	public String getSpacedArray(String[] strarr) {
		String output = "";
		
		for (String s : strarr) {
			output += s + " ";
		}
		
		return output.trim();		
	}
	
	/*
	 * reduces a state to a non-terminal based on lookahead
	 * @param state is the stack state at a specific iteration
	 * @param lookahead is the next symbol to be added to the stack
	 * @return state reduced to a non-terminal
	 */
	public String reduce(String state, Token lookahead, Token lookbehind) {
		boolean repeat = true;
		
		//System.out.println("state = " + state + "\n");
		
		while (repeat) {
			ArrayList<String> nts = new ArrayList<String>();	// list of possible non-terminals
			repeat = false;
			
			for (Rule rule : this.grammar.getRules()) {
				/* convert right side to space-spaced string */
				String rhs = getSpacedArray(rule.getRightSide());
				
				/* if the state is found in the RHS of grammar,
				 * add its corresponding LHS non-terminal
				 */
				if (rhs.equals(state)) {
					System.out.println("state \"" + rhs + "\" can become \"" + rule.getLeftSide() + "\"");
					nts.add(rule.getLeftSide());
				}
			}
			
			/* loop through nts, checking to see if the lookahead matches any of their follow sets */
			for (String nt : nts) {
				if (lookahead == null || state.contains("semi") || this.grammar.getFollowSets().get(nt).isEmpty() || 
					 this.grammar.getFollowSets().get(nt).contains(lookahead.getTokenLabel()) 					  ||
					 this.hasNonTerminal(this.grammar.getFollowSets().get(nt))) {
					//System.out.println("lookbehind: " + lookbehind.getTokenLabel());
					//System.out.println("lookbehind firsts: " + this.grammar.getFirstSets().get(lookbehind.getTokenLabel()));
					//System.out.println(this.grammar.getFirstSets().get(lookbehind.getTokenLabel()).contains(state));
					if (this.grammar.getFirstSets().get(lookbehind.getTokenLabel()).contains(state) ||
						(this.grammar.getFirstSets().get(lookbehind.getTokenLabel()).contains(state.split(" ")[0]) &&
						 !state.split(" ")[0].equals(nt))) {
						System.out.println("state \"" + state + "\" --> \"" + state + "\"\n");
						repeat = false;
					} else {
						System.out.println("state \"" + state + "\" --> \"" + nt + "\"\n");
						state = nt;
						repeat = true;
					}
					break;
				} else {
					System.out.println("state \"" + state + "\" --> \"" + state + "\"\n");
				}
			}					
		}		
		
		return state;
	}
	
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
	 * @param n is the number of symbols to be replaced by nt
	 * @param stack is the stack to be manipulated
	 * @return altered stack
	 */
	public ArrayList<String> replace(String nt, int n, ArrayList<String> stack) {
		/* pop n items off stack */
		for (int i = 0; i < n; i++) {
			stack.remove(stack.size()-1);
		}
		
		/* push nt onto the stack */
		stack.add(nt);
		
		return stack;
	}
	
	public void parse() {
		ArrayList<String> stack = new ArrayList<String>();
		Iterator<Token> tokenIt = tokens.iterator();
		int start = 0;
		Token lookahead = tokenIt.next();
		Token lookbehind = null;
		String state = "";
		String newState = "";
		boolean repeat = true;
		
		while (repeat) {
			Token token = lookahead;
			
			try {
				lookahead = tokenIt.next();
			} catch (Exception e) {
				lookahead = null;
			}
			
			if (token != null) {
				System.out.println("Adding \"" + token.getTokenLabel() + "\" to the stack\n");
				stack.add(token.getTokenLabel());
			} else {
				repeat = false;
			}
			
			System.out.println("Current stack: " + stack + "\n");
			
			state = "";
			newState = "";
			
			/* walk backwards through the stack, constructing the state
			 * at each iteration and checking if it can be reduced
			 */
			for (int i = stack.size() - 1; i >= 0; i--) {
				state = stack.get(i) + " " + state;
				state = state.trim();

				if (i > 0) {
					lookbehind = new Token(null, stack.get(i-1));
				}
				
				newState = this.reduce(state, lookahead, lookbehind);
				
				if (!state.equals(newState))
					stack = this.replace(newState, state.split(" ").length, stack);
				
				state = newState;
			}
		}
	}
}
