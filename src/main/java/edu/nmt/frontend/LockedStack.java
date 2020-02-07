package edu.nmt.frontend;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class LockedStack {
	
	private ArrayDeque<ArrayList<Node>> stack;
	
	LockedStack() {
		this.stack = new ArrayDeque<ArrayList<Node>>();
	}
	
	public ArrayList<Node> pop() {
		return this.stack.pop();
	}
	
	public void push(ArrayList<Node> prevStack) {
		this.stack.push(prevStack);
	}
	
	public ArrayList<Node> peek() {
		return this.stack.peek();
	}
	
	public String peekString() {
		if (this.stack.isEmpty())
			return null;
		
		String peek = "";
		
		for (Node node : this.stack.peek()) {
			peek += " " + node;
		}
		
		return peek.trim();
	}
	
	public HashSet<String> peekFirsts(Grammar g) {
		return g.computeFirsts(this.peekString());
	}
	
	public ArrayList<Node> morph(ArrayList<Node> oldStack) {
		ArrayList<Node> tmp = this.pop();
		
		for (Node n : oldStack) {
			tmp.add(n);
		}		
		
		return tmp;
	}

	public static void main(String[] args) throws IOException {
		LockedStack ls = new LockedStack();
		ls.push(new ArrayList<Node>());
		ls.peek().add(new Node(new Token(null, "type", null, null)));
		ls.peek().add(new Node(new Token(null, "identifier", null, null)));
    	Grammar g = new Grammar("config/grammar.cfg");
    	g.loadGrammar();		
		System.out.println(ls.peekFirsts(g));
	}

	public boolean isEmpty() {
		return this.stack.isEmpty();
	}
}
