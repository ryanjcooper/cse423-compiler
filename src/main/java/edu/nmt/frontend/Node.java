package edu.nmt.frontend;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Node {
	Token token;
	Node parent;
	Integer level;
	List<Node> children;
	
	Node(Token t) {
		this.token = t;
		this.parent = null;
		this.level = 0;
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
	
	public void setLevel(Integer lvl) {
		this.level = lvl;
	}
	
	public Integer getLevel() {
		return this.level;
	}
	
	public void addChild(Node n) {
		n.setParent(this);
		this.children.add(n);
		
		//if no children, height is incremented
		if (this.children.isEmpty()) {
			this.setLevel(this.getLevel() + 1);
		} else {
			int maxHeight = 0;
			for (Node x : this.getChildren()) {
				if (x.getLevel() > maxHeight) {
					maxHeight = x.getLevel();
				}
			}
			//if has children, height is maxHeight among children + 1
			this.setLevel(maxHeight + 1);
		}
		
		Node p = this;
		//increment height of all parent nodes going up tree
		while (p.getParent() != null) {
			p = p.getParent();
			int maxHeight = 0;
			for (Node x : p.getChildren()) {
				if (x.getLevel() > maxHeight) {
					maxHeight = x.getLevel();
				}
			}
			p.setLevel(maxHeight + 1);
		}
	}
	
	private void setLeafLevel() {
		if (this.getLevel() != 0) {
			for (Node x : this.getChildren()) {
				x.setLeafLevel();
			}
		} else {
			this.setLevel(this.getParent().getLevel() - 1);
		}
	}
	
	// TODO
	/**
	 * using this node as root, pretty print the tree
	 * to console and to file
	 */
	public void printTree() {
		this.setLeafLevel();
		String graph = buildGraph(this);
		System.out.println(graph);
		
		for (int i = this.getLevel(); i >= 0; i--) {
			System.out.println("level " + (6 - i) + ":");
			printTreeLevel(this, i);
			System.out.println();
		}
		
	}
	
	private String buildGraph(Node n) {
		StringBuilder builder = new StringBuilder();
		if (!n.getChildren().isEmpty()) {
			for (Node x : n.getChildren()) {
				builder.append(n + "->" + x + ";");
				builder.append(buildGraph(x));
			}
		}
		
		return builder.toString();
	}
	
	private void printTreeLevel(Node n, Integer lvl) {
		if (n == null) {
			return;
		}
		if (n.getLevel().equals(lvl)) {
			System.out.print(n + "\t");
		} else {
			for (Node c : n.getChildren()) {
				printTreeLevel(c, lvl);
			}
		}
	}
	
	@Override
	public String toString() {
		return this.token.getTokenLabel();
	}
	
	
}
