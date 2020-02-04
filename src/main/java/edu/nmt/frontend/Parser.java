package edu.nmt.frontend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.nmt.RuntimeSettings;

public class Parser {
	
	private Grammar grammar;
	private List<Token> tokens;
	
	public Parser(Grammar g, List<Token> tok) {
		grammar = g;
		tokens = tok;
	}
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/base.c");
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
	public String reduce(String state, Token lookahead) {
		boolean repeat = true;
		
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
					//System.out.println("rhs:" + rhs + " lhs: " + rule.getLeftSide());
					nts.add(rule.getLeftSide());
				}
			}
			
			/* loop through nts, checking to see if the lookahead matches any of their follow sets */
			for (String nt : nts) {
				if (lookahead == null || this.grammar.getFollowSets().get(nt).contains(lookahead.getTokenLabel())) {
					state = nt;
					repeat = true;
					break;
				}
			}					
		}		
		
		return state;
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
		Token lookahead = tokenIt.next();
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
				stack.add(token.getTokenLabel());
			} else {
				repeat = false;
			}
			
			System.out.println(stack);
			
			/* check if current can become a NT 
			 * and gather possible NTs in a list 
			 */
			state = "";
			newState = "";
			
			for (int i = stack.size() - 1; i >= 0; i--) {
				state = stack.get(i) + " " + state;
				state = state.trim();
				newState = this.reduce(state, lookahead);
				if (!state.equals(newState))
					stack = this.replace(newState, state.split(" ").length, stack);
				state = newState;
			}
		}
	}
}
