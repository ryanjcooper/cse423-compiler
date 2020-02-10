package edu.nmt.frontend;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
	public void printTree() throws IOException {
		File graphFile = new File("test.dot");
		graphFile.createNewFile();
		this.recursiveSetDepth();
		this.createDotFile(graphFile);
		
//		int maxDepth = this.getMaxDepth();
//		System.out.println("maxDepth: " + maxDepth);
//		System.out.println(this.printSubtree());
	}
	
	private void createDotFile(File graphFile) throws IOException {
		PrintWriter out = new PrintWriter(graphFile);
		out.write("graph {\n");
		out.write(this.createDotRanks());
		out.write("\n");
		out.write(this.buildGraph());
		out.write("}\n");
		out.close();
	}
	
	private String createDotRanks() {
		StringBuilder builder = new StringBuilder();
		int depth = this.getMaxDepth();
		for (int i = 0; i <= depth; i++) {
			builder.append("\t{ rank=same; ");
			builder.append(this.returnMatchingDepth(i));
			builder.deleteCharAt(builder.length() - 1);
			builder.deleteCharAt(builder.length() - 1);
			builder.append("}\n");
		}
		
		return builder.toString();
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
	
	private String buildGraph() {
		StringBuilder builder = new StringBuilder();
		if (!this.getChildren().isEmpty()) {
			for (Node x : this.getChildren()) {
				builder.append("\t" + this + " -- " + x + "\n");
			}
			if (this.getParent() == null) {
				builder.append("\n");
			}
			for (Node x : this.getChildren()) {
				builder.append(x.buildGraph());
			}
		}
		
		return builder.toString();
	}
	
	private String printSubtree() {
		StringBuilder builder = new StringBuilder();
		if (this.getChildren().isEmpty()) {
			return this.toString();
		}
		for (Node c : this.getChildren()) {
			builder.append(c.printSubtree() + "\t");
		}
		builder.deleteCharAt(builder.length() - 1).insert(0, this.toString() + "\n");
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return this.token.getTokenLabel();
	}
	
	
}
