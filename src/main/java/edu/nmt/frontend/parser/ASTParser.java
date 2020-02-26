package edu.nmt.frontend.parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.scanner.Scanner;


/**
 * Reduce a parse tree to an abstract syntax tree following the form of that in clang.
 * Based on the idea of DFS traversal, reducing specific nodes following their function 
 * * (i.e. variable declarations will always have an identifier and a type).
 * Secondly, nodes with single children can always be removed with no loss of semantics.
 * @author rcooper
 * @dated 02/15/20
 *
 */
public class ASTParser {
	
	private Parser p;
	private Node root;
	
	// Parser constructs that dont add any semantic meaning
	private List<String> syntaxConstructs = new ArrayList<String>(Arrays.asList(
				"l_paren",
				"r_paren",
				"semi",
				"l_brace",
				"r_brace",
				"return",
				"l_bracket",
				"r_bracket"
			));
	
	/**
	 * Constructor
	 * @param p Parser with parse tree built and accepted
	 */
	public ASTParser(Parser p) {
		this.p = p;
	}
	
	
	/**
	 * Single-pass parse tree reduction algorithm
	 * @return true iff parse tree is reduced to ast
	 */
	public Boolean parse() {
		root = p.getParseTree();
		Stack<Node> stack = new Stack<Node>();
		List<Node> tmp;
		List<Node> tmp2;
		
		// add parse tree root as this will always be program
		stack.addAll(root.getChildren());
		root.setName("global");
		
		
		// dfs over tree in 
		while(!stack.empty()) {
			Node current = stack.pop();
			
			// remove node if token doesnt contribute to semantics
			if (syntaxConstructs.contains(current.getToken().getTokenLabel())) {
				tmp = current.getParent().getChildren();
				tmp.remove(current);
				tmp.addAll(current.getChildren());
				current.getParent().setChildren(tmp);
				
				for (Node child : current.getChildren()) {
					child.setParent(current.getParent());
				}
				
				
			// handle collapsing functions
			} else if (current.getToken().getTokenLabel().equals("funcDefinition")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				// search over child nodes for type and identifier, collapse into funcDefinition node
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
				
				// add function to current node's parent scope (supports nested functions!)
				try {
					current.getScopeNode().addSymbol(current.getName(), current);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					return false;
				}
				
				// bring param nodes up and label their identifier and type
				for (Node child : current.getChildren()) {
					if (child.getToken().getTokenLabel().equals("params")) {
						for (Node child2 : child.getChildren()) {
							if (child2.getToken().getTokenLabel().equals("paramList")) {
								for (Node child3 : child2.getChildren()) { // param objects
									tmp = child3.getChildren();
									tmp2 = new ArrayList<Node>();
									if (child3.getToken().getTokenLabel().equals("param")) {
										for (Node paramFeatures : child3.getChildren()) {
											if (paramFeatures.getToken().getTokenLabel().equals("type")) {
												child3.setType(paramFeatures.getToken().getTokenString());
												tmp2.add(paramFeatures);
											} else if (paramFeatures.getToken().getTokenLabel().equals("identifier")) {
												child3.setName(paramFeatures.getToken().getTokenString());
												tmp2.add(paramFeatures);
											}
										}
									}
									tmp.removeAll(tmp2);
									child3.setChildren(tmp);
								}
							}
						}
					}
				}
				
			// collapse function declarations
			} else if (current.getToken().getTokenLabel().equals("funcDeclaration")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				// search over child nodes for type and identifier, collapse into funcDeclaration node
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
				
				// bring param nodes up and label their identifier and type
				for (Node child : current.getChildren()) {
					if (child.getToken().getTokenLabel().equals("params")) {
						for (Node child2 : child.getChildren()) {
							if (child2.getToken().getTokenLabel().equals("paramList")) {
								for (Node child3 : child2.getChildren()) { // param objects
									tmp = child3.getChildren();
									tmp2 = new ArrayList<Node>();
									if (child3.getToken().getTokenLabel().equals("param")) {
										for (Node paramFeatures : child3.getChildren()) {
											if (paramFeatures.getToken().getTokenLabel().equals("type")) {
												child3.setType(paramFeatures.getToken().getTokenString());
												tmp2.add(paramFeatures);
											} else if (paramFeatures.getToken().getTokenLabel().equals("identifier")) {
												child3.setName(paramFeatures.getToken().getTokenString());
												tmp2.add(paramFeatures);
											}
										}
									}
									tmp.removeAll(tmp2);
									child3.setChildren(tmp);
								}
							}
						}
					}
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
				
			// collapse declarationList
			} else if (current.getToken().getTokenLabel().equals("declarationList")) {
				tmp = current.getParent().getChildren();
				tmp.remove(current);
				tmp.addAll(current.getChildren());
				
				for (Node child : current.getChildren()) {
					child.setParent(current.getParent());
				}
				
				current.getParent().setChildren(tmp);
				
			// label numeric_constant with type
			} else if (current.getToken().getTokenLabel().equals("numeric_constant")) {
				current.setType("int");
			
			// handle variable declarations
			} else if (current.getToken().getTokenLabel().equals("varDeclaration")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				// get type, identifier, and assign_op from child nodes and label varDeclaration node
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("type")) {
						current.setType(child.getToken().getTokenString());
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("identifier")) {
						current.setName(child.getToken().getTokenString());
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("assign_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}	
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
				// add symbol to current nodes parent scope
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
			
			// label op in assignStmt
			} else if (current.getToken().getTokenLabel().equals("assignStmt")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("assign_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
			// label op in simpleExpression
			} else if (current.getToken().getTokenLabel().equals("simpleExpression")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("bool_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
			// label op in incExpr
			} else if (current.getToken().getTokenLabel().equals("incExpr")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("unary_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
			// label op in addExpression
			} else if (current.getToken().getTokenLabel().equals("addExpression")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("add_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
			
			// label op in boolExpr
			} else if (current.getToken().getTokenLabel().equals("boolExpr")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("bool_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}
				}				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
			}
			
			stack.addAll(current.getChildren());
		}

		return true;
	}
	
	
	/**
	 * Print out ast of this parser
	 */
	public void printAST() {
		System.out.println(Node.printTree(this.root, " ", false));
	} 
	
	
	/**
	 * Print out all symbol tables, descends scope bfs
	 */
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
		if (p.parse()) {
			;
//			System.out.println(Node.printTree(p.getParseTree(), " ", false));	
		}
		
		ASTParser a = new ASTParser(p);
		if (a.parse()) {
			a.printAST();	
		}
		
		a.printSymbolTable();
		
		
	}
	
	
	
}
