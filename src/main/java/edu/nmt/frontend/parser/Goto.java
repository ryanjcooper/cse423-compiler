package edu.nmt.frontend.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

public class Goto implements Cloneable {
	public static Grammar grammar;
	public static HashMap<String, Goto> gotoTable; 
	
	private HashMap<String, Goto> transitions;
	private Node token;
	private boolean start;
	private boolean end;
	private boolean repeat;
	
	Goto () {
		this.transitions = new HashMap<String, Goto>();
		this.token = null;
		this.end = false;
		this.repeat = false;		
	}
	
	Goto(Token tok) {
		this.transitions = new HashMap<String, Goto>();
		this.token = new Node(tok);
		this.start = false;
		this.end = false;
		this.repeat = false;
	}	
	
	Goto(Token tok, boolean start, boolean end) {
		this.transitions = new HashMap<String, Goto>();
		this.token = new Node(tok);
		this.start = start;
		this.end = end;
		this.repeat = false;		
	}
	
	Goto(Token tok, boolean end) {
		this.transitions = new HashMap<String, Goto>();
		this.token = new Node(tok);
		this.start = false;
		this.end = end;
		this.repeat = false;
	}
	
	/**
	 * initialize automata from grammar
	 * @param g is the grammar from which the automata spawns
	 */
	public static void init(Grammar g) {
		grammar = g;
		gotoTable = new HashMap<String, Goto>();
		
		/* loop through all rules, adding states and transitions */
		for (Rule rule : grammar.getRules()) {
			String rhs = rule.getRightSide()[0];
			String lhs = rule.getLeftSide();
			boolean secondState = rhs.equals(".");

			if (secondState) {
				rhs = rule.getRightSide()[1];
			}

			/* begin with the RHS */
			if (gotoTable.keySet().contains(rhs)) {
				if (rhs.equals(lhs) && rule.getRightSide().length == 1) {
					gotoTable.get(rhs).setRepeat(true);
				} else {
					gotoTable.get(rhs).addTransition(rule.getRightSide(), rule.getLeftSide(), 1);
				}
			} else {
				Goto newStart = new Goto(new Token(null, rhs, null, null), !secondState, false);
				
				if (rhs.equals(lhs) && rule.getRightSide().length == 1) {
					newStart.setRepeat(true);
					gotoTable.put(rhs, newStart);
					continue;
				}
				
				gotoTable.put(rhs, newStart);
				
				newStart.addTransition(rule.getRightSide(), rule.getLeftSide(), secondState ? 2 : 1);
			}		
		}
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	public HashMap<String, Goto> getTransitions() {
		return this.transitions;
	}
	
	public Node getToken() {
		return this.token;
	}
	
	public void setToken(Node tok) {
		this.token = tok;
	}
	
	private void addTransition(String[] syms, String lhs, int index) {
		try {
			/* check if this already contains transition */
			if (this.transitions.keySet().contains(syms[index])) {
				this.transitions.get(syms[index]).addTransition(syms, lhs, ++index);
			} else {
				Goto newTransition = new Goto(new Token(null, syms[index], null, null));
				
				this.transitions.put(syms[index], newTransition);
				newTransition.addTransition(syms, lhs, ++index);
			}
		} catch (Exception e) {
			Goto finalState = new Goto(new Token(null, lhs, null, null), true);
			this.transitions.put(lhs, finalState);
		}
	}
	
	public static Goto getLock() {
		return new Goto(new Token(null, "$", null, null));
	}
	
	public Goto nextState() {
		for (Goto g : this.transitions.values()) {
			return g;
		}
		
		return null;
	}
	
	public Goto getEnd() {
		Goto tmp = this;
		
		while (!tmp.isEndState()) {
			tmp = tmp.nextState(true);
		}
		
		return tmp;
	}
	
	public Goto nextState(boolean nt) {
		if (this.transitions.size() == 1) {
			return this.transitions.values().iterator().next();
		} else {
			for (String key : this.transitions.keySet()) {
				if (nt) {
					if (!grammar.isTerminal(key)) {
						return this.makeTransition(key);
					}				
				} else {
					return this.makeTransition(key);
				}
			}			
		}
		
		return null;		
	}
	
	public void addTransition(Goto dest) {
		this.transitions.put(dest.toString(), dest);
	}
	
	public Goto makeTransition(String dest) {
		return this.transitions.get(dest);
	}
	
	public boolean canRepeat() {
		try {
			return gotoTable.get(this.token.toString()).repeat;
		} catch (NullPointerException npe) {
			return false;
		}
	}
	
	public static boolean canStart(String terminal) {
		try {
			return gotoTable.get(terminal).start;
		} catch (NullPointerException npe) {
			return false;
		}
	}
	
	public boolean canTransition(String dest) {
		return this.transitions.keySet().contains(dest);
	}
	
	public boolean isEndState() {
		return this.end;
	}
	
	public boolean isStartState() {
		return this.start;
	}
	
	public List<Goto> getNonTerminalTransitions() {
		List<Goto> nts = new ArrayList<Goto>();
		
		for (String key : this.transitions.keySet()) {
			if (!grammar.isTerminal(key)) {
				nts.add(this.transitions.get(key));
			}
		}
		
		return nts;
	}
	
	public Goto getEpsilonTransition() {
		Goto org = gotoTable.get(this.toString());
		
		try {
			for (Goto g : org.transitions.values()) {
				if (g.end) {
					return g;
				}
			}
		} catch (NullPointerException npe) {
			return null;
		}
		
		return null;
	}
	
	public Goto terminateTo() {
		for (Goto g : this.transitions.values()) {
			if (g.isEndState()) {
				return g;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return this.token.toString();
	}
	
	public static Goto getEnd(String goal) {
		return new Goto(new Token(null, goal, null, null));
	}
	
	public static Goto get(String sym) {
		try { 
			return gotoTable.get(sym).clone();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	@Override
	public Goto clone() {
		Goto newGoto = new Goto(new Token(null, this.token.toString(), null, null));

		newGoto.setRepeat(this.repeat);
		newGoto.end = this.end;
		
		for (Goto g : this.transitions.values()) {
			newGoto.addTransition(g.clone());
		}
		
		return newGoto;
	}
	
	public static void main(String[] argv) throws IOException {
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Goto.init(g);
		Goto test = Goto.get("identifier");
		test = test.makeTransition("l_paren");
		//Goto test2 = Goto.get("expression");
		//test2.nextState().addTransition(new Goto(new Token(null, "hell", null, null)));
		System.out.println(test.getEnd());
	}
}
