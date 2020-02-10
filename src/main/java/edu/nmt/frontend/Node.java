package edu.nmt.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
	private Token token;
	private Node parent;
	private List<Node> children;
	
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
	
	public static String printTree(Node node, String indent, Boolean last) {
       StringBuilder sb = new StringBuilder();
       sb.append(indent);
       if (last) {
    	   sb.append("\\-");
           indent += "  ";
       } else {
    	   sb.append("|-");
           indent += "| ";
       }
       sb.append(node.toString() + "\n");

       List<Node> children = node.getChildren();
       Collections.reverse(children);
       for (int i = 0; i < children.size(); i++) {
    	   sb.append(printTree(children.get(i), indent, i == children.size() - 1));
       }
       return sb.toString();
	}
	
	@Override
	public String toString() {
		return this.token.getTokenLabel();
	}
}
