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
	
	/**
	 * Gets the parent of a Node
	 * @return returns parent Node type
	 */
	public Node getParent() {
		return this.parent;
	}
	
	/**
	 * Alter the parent of a node
	 * @param p Node to set as parent
	 */
	public void setParent(Node p) {
		this.parent = p;
	}
	
	/**
	 * Returns list of Nodes that are Children
	 * @return List of Nodes
	 */
	public List<Node> getChildren() {
		return this.children;
	}
	
	/**
	 * Adds a Node as a child
	 * @param n is Node to add
	 */
	public void addChild(Node n) {
		this.children.add(n);
	}
}
