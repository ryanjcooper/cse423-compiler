//package edu.nmt.frontend;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class ASTNode extends Node {
//	
//	private Node parseNode;
//	private String type;
//	private String name;
//	List<ASTNode> children;
////	private String identifier;
//	
//	public ASTNode() {
//		super(null);
//		children = new ArrayList<ASTNode>();
//	}
//	
//	public ASTNode(Node n) {
//		super(n.getToken());
//		this.parseNode = n;
//		children = new ArrayList<ASTNode>();
//	}
//	
//	public ASTNode(Node n, String type) {
//		super(n.getToken());
//		this.parseNode = n;
//		this.type = type;
//		children = new ArrayList<ASTNode>();
//	}
//
//	public void addChild(ASTNode n) {
//		n.setParent(this);
//		n.setDepth(this.getDepth() + 1); //for some reason, this doesn't properly set the depth of all children, so recursiveSetDepth was made
//		this.children.add(n);
//	}
//
//	public void setType(String type) {
//		this.type = type;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//	
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		if (this.parseNode != null)
//			sb.append(this.parseNode.token.getTokenLabel());
//		if (this.name != null)
//			sb.append(" " + this.name);
//		if (this.type != null)
//			sb.append(" " + this.type +  " ");
//
//		return sb.toString();
//	}
//	
//	public List<ASTNode> getASTChildren() {
//		return this.children;
//	}
//}
