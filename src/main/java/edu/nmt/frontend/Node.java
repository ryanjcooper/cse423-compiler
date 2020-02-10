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
