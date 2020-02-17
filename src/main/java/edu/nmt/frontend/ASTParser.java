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
				"unary_op",
				"l_bracket",
				"r_bracket"
			));
	
	public ASTParser(Parser p) {
		this.p = p;
	}
	
	public Boolean parse() {
		root = p.getParseTree();
		Stack<Node> stack = new Stack<Node>();
		List<Node> tmp;
		List<Node> tmp2;
		
		stack.addAll(root.getChildren());
		root.setName("global");
		
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
				current.setType("function");
				try {
					current.getScopeNode().addSymbol(current.getName(), current);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					return false;
				}
				
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
				
				// check for redefinition error
				try {
					current.getScopeNode().addSymbol(current.getName(), current);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					return false;
				}
			
			// label identifiers and try and get type
			} else if (current.getToken().getTokenLabel().equals("identifier")) {
				current.setName(current.getToken().getTokenString());
				
				// TODO: solve undeclared identifiers
//				if (!current.getScopeNode().containsSymbol(current.getToken().getTokenString())) {
//					System.err.println("error: use of undeclared identifier '" + current.getToken().getTokenString() + "'");
//				}		
			}
			
			stack.addAll(current.getChildren());
		}
		
		return true;
	}

	public void printAST() {
		System.out.println(Node.printTree(this.root, " ", false));
	} 
	
	
	public void printSymbolTable() {	
		Stack<Node> stack = new Stack<Node>();
		stack.add(this.root);
		
		while(!stack.empty()) {
			Node cur = stack.pop();
			stack.addAll(cur.getChildren());
			
			if (cur.isScopeNode()) {
				System.out.println(cur.getName());		
				System.out.println(new String(new char[cur.getName().length()]).replace("\0", "-"));
				
				System.out.println(cur.getSymbolTableString() + "\n");
			}
			
			
		}
		
	}
	
	public static void main(String argv[]) throws Exception {
		Scanner scanner = new Scanner("test/test.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		p.parse();
//		System.out.println(Node.printTree(p.getParseTree(), " ", false));
		ASTParser a = new ASTParser(p);
		if (a.parse()) {
			a.printAST();	
		}
		
		a.printSymbolTable();
		
		
	}
	
	
	
}
