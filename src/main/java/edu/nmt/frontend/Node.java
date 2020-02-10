package edu.nmt.frontend;

import java.util.ArrayList;
import java.util.List;

public class Node {
	Token token;
	Node parent;
	Integer depth;
	List<Node> children;
	
	Node(Token t) {
		this.token = t;
		this.parent = null;
		this.depth = 0;
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
	
	public void setDepth(Integer depth) {
		this.depth = depth;
	}
	
	public Integer getDepth() {
		return this.depth;
	}
	
	public void addChild(Node n) {
		n.setParent(this);
		n.setDepth(this.getDepth() + 1); //for some reason, this doesn't properly set the depth of all children, so recursiveSetDepth was made
		this.children.add(n);
	}
	
	// TODO
	/**
	 * using this node as root, pretty print the tree
	 * to console and to file
	 * @throws IOException 
	 */
	public void printTree() {
		this.recursiveSetDepth();
		
		int maxDepth = this.getMaxDepth();
		System.out.println("maxDepth: " + maxDepth);
		System.out.println(this.printSubtree());
	}
	
	private String returnMatchingDepth(int depth) {
		if (this.getDepth() == depth) {
			return this.toString() + "; ";
		} else if (this.getDepth() < depth) {
			StringBuilder builder = new StringBuilder();
			for (Node c : this.getChildren()) {
				builder.append(c.returnMatchingDepth(depth));
			}
			return builder.toString();
		} else {
			return null;
		}
	}
	
	private void recursiveSetDepth() {
		if (this.getChildren().isEmpty()) {
			return;
		} else {
			int depth = this.getDepth() + 1;
			for (Node c : this.getChildren()) {
				c.setDepth(depth);
				c.recursiveSetDepth();
			}
		}
	}
	
	private int getMaxDepth() {
		if (this.getChildren().isEmpty()) {
			return this.getDepth();
		} else {
			int maxDepth = 0;
			for (Node x : this.getChildren()) {
				int xDepth = x.getMaxDepth();
				if (maxDepth < xDepth) {
					maxDepth = xDepth;
				}
			}
			return maxDepth;
		}
	}
	
	private String printSubtree() {
		if (this.getChildren().isEmpty()) {
			return this.toString();
		}
		StringBuilder builder = new StringBuilder();
		
		for (Node c : this.getChildren()) {
			builder.append(c.printSubtree());
		}
		builder.deleteCharAt(builder.length() - 1).insert(0, this.toString() + "\n");
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return this.token.getTokenLabel();
	}
	
	
}
