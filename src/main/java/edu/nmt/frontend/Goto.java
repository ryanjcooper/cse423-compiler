package edu.nmt.frontend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Goto {
	public static Grammar grammar;
	public static HashMap<String, Goto> gotoTable; 
	
	private HashMap<String, Goto> transitions;
	private Token token;
	private boolean isEndState;
	private boolean repeat;
	
	Goto(Token tok) {
		this.transitions = new HashMap<String, Goto>();
		this.token = tok;
		this.isEndState = false;
		this.repeat = false;
	}	
	
	Goto(Token tok, boolean end) {
		this.transitions = new HashMap<String, Goto>();
		this.token = tok;
		this.isEndState = end;
		this.repeat = false;
	}
	
	public static void init(Grammar g) {
		grammar = g;
		gotoTable = new HashMap<String, Goto>();
		
		/* for each rule */
		for (Rule rule : grammar.getRules()) {
			String rhs = rule.getRightSide()[0];
			String lhs = rule.getLeftSide();

			/* begin with the RHS */
			if (gotoTable.keySet().contains(rhs)) {
				if (rhs.equals(lhs) && rule.getRightSide().length == 1) {
					gotoTable.get(rhs).setRepeat(true);
				} else {
					gotoTable.get(rhs).addTransition(rule.getRightSide(), rule.getLeftSide(), 1);
				}
			} else {
				Goto newStart = new Goto(new Token(null, rhs, null, null));
				
				if (rhs.equals(lhs) && rule.getRightSide().length == 1) {
					newStart.setRepeat(true);
				}
				
				gotoTable.put(rhs, newStart);
				newStart.addTransition(rule.getRightSide(), rule.getLeftSide(), 1);
			}		
		}
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	public HashMap<String, Goto> getTransitions() {
		return this.transitions;
	}
	
	public Token getToken() {
		return this.token;
	}
	
	private void addTransition(String[] syms, String finale, int index) {
		//System.out.println(finale);
		try {
			/* check if this already contains transition */
			if (this.transitions.keySet().contains(syms[index])) {
				this.transitions.get(syms[index]).addTransition(syms, finale, ++index);
			} else {
				Goto newTransition = new Goto(new Token(null, syms[index], null, null));
				
				this.transitions.put(syms[index], newTransition);
				newTransition.addTransition(syms, finale, ++index);
			}
		} catch (Exception e) {
			Goto finalState = new Goto(new Token(null, finale, null, null), true);
			this.transitions.put(finale, finalState);
		}
	}
	
	public Goto makeTransition(String dest) {
		return this.transitions.get(dest);
	}
	
	public boolean canRepeat() {
		try {
			return gotoTable.get(this.token.getTokenLabel()).repeat;
		} catch (NullPointerException npe) {
			return false;
		}
	}
	
	public boolean canTransition(String dest) {
		return this.transitions.keySet().contains(dest);
	}
	
	public boolean canEnd() {
		return this.isEndState;
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
	
	public Goto terminateTo() {
		for (Goto g : this.transitions.values()) {
			if (g.canEnd()) {
				return g;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return this.token.getTokenLabel();
	}
	
	public static void main(String[] argv) throws IOException {
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Goto.init(g);
		//System.out.println(gotoTable.get("type").getTransitions().get("identifier").getTransitions());
		System.out.println(gotoTable.get("statementList").repeat);
	}
}
