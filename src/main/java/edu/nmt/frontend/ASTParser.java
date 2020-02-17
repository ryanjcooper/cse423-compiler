package edu.nmt.frontend;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class ASTParser {
	
	private Parser p;
	private Node root;
	
	private List<String> syntaxConstructs = new ArrayList<String>(Arrays.asList(
				"l_paren",
				"r_paren",
				"semi",
				"l_brace",
				"r_brace",
				"return",
				"assign_op",
				"add_op",
				"unary_op"
			));
	
	public ASTParser(Parser p) {
		this.p = p;
	}
	
	public void parse() {
		root = p.getParseTree();
		Stack<Node> stack = new Stack<Node>();
		List<Node> tmp;
		List<Node> tmp2;
		
		stack.addAll(root.getChildren());
		
		// search over tree in dfs fashion
		while(!stack.empty()) {
			Node current = stack.pop();

			// remove node if token doesnt contribute to semantics
			if (syntaxConstructs.contains(current.getToken().getTokenLabel())) {
				tmp = current.getParent().getChildren();
				tmp.remove(current);
				tmp.addAll(current.getChildren());
				current.getParent().setChildren(tmp);
				
			// handle collapsing functions
			} else if (current.getToken().getTokenLabel().equals("funcDefinition")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("type")) {
						current.setType(child.getToken().getTokenString());
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("identifier")) {
						current.setName(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);			
				
			// collapse single child nodes (non-terminals)
			} else if ((current.getChildren().size() == 1)) {
				// remove node from parent
				tmp = current.getParent().getChildren();
				tmp.remove(current);
				current.getParent().setChildren(tmp);
				// set only childs parent to current node
				Node tmpNode = current.getChildren().get(0);
				tmpNode.setParent(current.getParent());
				current.getParent().addChild(tmpNode);
				
			// label numeric_constant with type
			} else if (current.getToken().getTokenLabel().equals("numeric_constant")) {
				current.setType("int");
			
			// handle variable declarations
			} else if (current.getToken().getTokenLabel().equals("varDeclaration")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("type")) {
						current.setType(child.getToken().getTokenString());
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("identifier")) {
						current.setName(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}	
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
			
			// label identifiers and try and get type
			} else if (current.getToken().getTokenLabel().equals("identifier")) {
				current.setName(current.getToken().getTokenString());
			}
			
			
			stack.addAll(current.getChildren());
		}
		
		
	}
	
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/add.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		p.parse();
//		System.out.println(Node.printTree(p.getParseTree(), " ", false));
		ASTParser a = new ASTParser(p);
		a.parse();
		
		System.out.println(Node.printTree(a.root, " ", false));
		

	} 
	
	
	
}
