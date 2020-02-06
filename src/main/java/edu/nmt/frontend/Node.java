package edu.nmt.frontend;

import java.util.ArrayList;
import java.util.List;

public class Node {
	Token token;
	Node parent;
	List<Node> children;
	
	Node(Token t) {
		this.token = t;
		this.parent = null;
		this.children = new ArrayList<Node>();
	}
	
	public Node getParent() {
		return this.parent;
	}
	
	public void setParent(Node p) {
		this.parent = p;
	}
	
	public List<Node> getChildren() {
		return this.children;
	}
	
	public void addChild(Node n) {
		this.children.add(n);
	}
	
	// TODO
	/**
	 * using this node as root, pretty print the tree
	 * to console and to file
	 */
	public void printTree() {
		
	}
	
	@Override
	public String toString() {
		return this.token.getTokenLabel();
	}
}
