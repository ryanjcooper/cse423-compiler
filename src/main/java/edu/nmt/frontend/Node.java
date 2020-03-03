package edu.nmt.frontend;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Node {
	Token token;
	Node parent;
	Integer depth;
	String type;
	String name;
	String op;
	List<Node> children;
	private Map<String, Node> symbol_table;  // only valid in funcDefinition and program
	
	
	public Node(Token t) {
		this.token = t;
		this.parent = null;
		this.depth = 0;
		this.children = new ArrayList<Node>();
		
		if ((t != null) && isScopeNode()) {
			symbol_table = new HashMap<String, Node>();
		}
		
		
	}
	
	public Boolean isScopeNode() {
		return this.token.getTokenLabel().equals("program") || this.token.getTokenLabel().equals("funcDefinition");
	}
	
	public Node getScopeNode() {
		Node current = this.parent;
		while(!current.isScopeNode()) {
			current = current.parent;
		}
		return current;
	}
	
	public Token getToken() {
		return this.token;
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
	
	public static String printTree(Node node, String indent, Boolean last) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(indent);
		
		if (node.getParent() != null) {
			if (last) {
				sb.append("`-");
				indent += "  ";
			} else {
				sb.append("|-");
				indent += "| ";
			}
		}
		
		sb.append(node.toString() + "\n");

		List<Node> children = node.getChildren();
		Collections.reverse(children);
		
		for (int i = 0; i < children.size(); i++) {
			sb.append(printTree(children.get(i), indent, i == children.size() - 1));
		}
		
		return sb.toString();
	}
	
	public static void writeFile(String fileName, Node node) throws IOException {
		String out = printTree(node, " ", false);
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		writer.write(out);
		writer.close();
	}
	
//	private String returnMatchingDepth(int depth) {
//		if (this.getDepth() == depth) {
//			return this.toString() + "; ";
//		} else if (this.getDepth() < depth) {
//			StringBuilder builder = new StringBuilder();
//			for (Node c : this.getChildren()) {
//				builder.append(c.returnMatchingDepth(depth));
//			}
//			return builder.toString();
//		} else {
//			return null;
//		}
//	}
	
	public void recursiveSetDepth() {
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
	
	public int getMaxDepth() {
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
	
	public void setType(String type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}
	
//	private String printSubtree() {
//		if (this.getChildren().isEmpty()) {
//			return this.toString();
//		}
//		StringBuilder builder = new StringBuilder();
//		
//		for (Node c : this.getChildren()) {
//			builder.append(c.printSubtree());
//		}
//		builder.deleteCharAt(builder.length() - 1).insert(0, this.toString() + "\n");
//		return builder.toString();
//	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.token.getTokenLabel());
		
		if (this.name != null) {
			sb.append(" <" + this.name + "> ");
		}
		
		if (this.type != null) {
			sb.append(" <" + this.type  + "> ");
		}
		
		if (this.op != null) {
			sb.append(" <" + this.op  + "> ");
		}
		
		
		return sb.toString();
	}

	public void setChildren(List<Node> tmp) {
		this.children = tmp;
	}

	public void addSymbol(String key, Node n) throws Exception {
		if (isScopeNode()) {
			if (!symbol_table.containsKey(key)) {
				symbol_table.put(key, n);
			} else {
				throw new Exception("error: redefinition of '" + key + "'");
			}
		}
		
	}

	public String getName() {
		return this.name;
	}

	public Boolean containsSymbol(String key) {
		if (isScopeNode()) {
			return symbol_table.containsKey(key);
		}
		return false;
	}

	public Map<String, Node> getSymbolTable() {
		return this.symbol_table;
	}

	public String getSymbolTableString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, Node>> iter = this.symbol_table.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Node> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=').append('"');
            sb.append(entry.getValue().type);
            sb.append('"');
            if (iter.hasNext()) {
                sb.append('\n');
            }
        }
        return sb.toString();
	}

	public void setOp(String op) {
		this.op = op;	
	}
}
