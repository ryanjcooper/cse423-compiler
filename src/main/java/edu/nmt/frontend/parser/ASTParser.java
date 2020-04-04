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
import edu.nmt.frontend.Token;
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
	
	public Node getRoot() {
		return root;
	}

	// Parser constructs that dont add any semantic meaning
	private List<String> syntaxConstructs = new ArrayList<String>(Arrays.asList(
				"l_paren",
				"r_paren",
				"semi",
				"l_brace",
				"r_brace",
				"return",
				"l_bracket",
				"r_bracket",
				"if",
				"else",
				"for",
				"colon",
				"while"
			));

	// Token labels that should not be rolled up, even if only one child
	private List<String> ignoreRollup = new ArrayList<String>(Arrays.asList(
				"condition"
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
//				current.setType("function"); // removed per discussion with Terence.
				
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
//				current.setType("function"); // removed per discussion with Terence.
				
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
			
			// handle conditionals
			} else if (current.getToken().getTokenLabel().equals("ifStmt")) {
				tmp = current.getChildren();
				// search over child nodes for condition, change expression -> condition
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("expression")) {
						child.getToken().setTokenLabel("condition");
					}
				}
				
			// handle for loops
			} else if (current.getToken().getTokenLabel().equals("forLoop")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				Node assignNode = new Node(new Token("null", "null"));
				Node comparisonNode = new Node(new Token("null", "null"));
				Node incNode = new Node(new Token("null", "null"));
				Node bodyNode = new Node(new Token("null", "null"));
				
				// search over child nodes for 
				for (Node child : tmp) {
					// variable assignment node
					if (child.getToken().getTokenLabel().equals("statement")) {
						if (assignNode.getToken().getTokenLabel().equals("null")) {
							assignNode = child;
						} else if (comparisonNode.getToken().getTokenLabel().equals("null")) {
							comparisonNode = child;
						}
					} else if (child.getToken().getTokenLabel().equals("expression")) {
						incNode = child;
					} else if (child.getToken().getTokenLabel().equals("compoundStmt")) {
						bodyNode = child;
					}
				}
					
			
				// set strict order for body of forLoop
				tmp2.add(assignNode);
				tmp2.add(comparisonNode);
				tmp2.add(incNode);
				tmp2.add(bodyNode);
			
				current.setChildren(tmp2);
				
			// handle while loops
			} else if (current.getToken().getTokenLabel().equals("iterationStmt")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				Node comparisonNode = null;
				Node bodyNode = null;
				
				// search over child nodes for body and condition of the loop
				for (Node child : tmp) {					
					if (child.getToken().getTokenLabel().equals("whileLoop")) {
						for (Node child2 : child.getChildren()) {
							if (child2.getToken().getTokenLabel().equals("expression")) {
								comparisonNode = child2;
							}
						}
					} else if (child.getToken().getTokenLabel().equals("compoundStmt")) {
						bodyNode = child;
					}
				}
								
				if (comparisonNode != null && bodyNode != null) {
					
					current.getToken().setTokenLabel("whileLoop");
					
					// ordered children
					tmp = new ArrayList<Node>();
					tmp.add(comparisonNode);
					tmp.add(bodyNode);
					
					comparisonNode.setParent(current);
					bodyNode.setParent(current);
					
					current.setChildren(tmp);
				}
						
			// collapse single child nodes (non-terminals)
			} else if ((current.getChildren().size() == 1) && (!ignoreRollup.contains(current.getToken().getTokenLabel()))) {				
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
			} else if (current.getToken().getTokenLabel().equals("postIncExpr") || current.getToken().getTokenLabel().equals("preIncExpr")) {
				String incOrder = current.getToken().getTokenLabel();
				
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
				
				// case where ++ is part of another expression
				if (!current.getParent().getToken().getTokenLabel().equals("exprStmt") && !current.getParent().getToken().getTokenLabel().equals("forLoop")) {
					Node newPlaceholder = new Node(new Token("statementList", "statementList"));
					
					System.out.println(current.getParent());
					
					// set new placeholder up to capture stmtList
					newPlaceholder.setParent(current.getParent().getParent());
					tmp = new ArrayList<Node>();
					tmp.add(current.getParent());
					newPlaceholder.setChildren(tmp);
					
					// move children around
					tmp = current.getParent().getParent().getChildren();
					tmp.remove(current.getParent());
					tmp.add(newPlaceholder);
					current.getParent().getParent().setChildren(tmp);
					
					// set current node
					current.getParent().setParent(newPlaceholder);
					
					
					// if the increment is acting on just an identifier, collapse to two ordered commands			
					if (current.getChildren().get(0).getToken().getTokenLabel().equals("identifier")) {
						current.getToken().setTokenLabel("identifier");
						
						// capture id
						String identifierString = current.getChildren().get(0).getToken().getTokenString();
						
						// change current node to an identifier
						current.setName(identifierString);
						current.setChildren(new ArrayList<Node>());
						
						// setup new operation
						Node opNode = new Node(new Token("assignStmt", "assignStmt"));
						
						Node constantNode = new Node(new Token("1"));
						constantNode.setType("int");
						
						Node idNode = new Node(new Token(identifierString));
						idNode.setName(identifierString);
						
						if (current.getOp().equals("++")) {
							opNode.setOp("+=");
						} else if (current.getOp().equals("--")) {
							opNode.setOp("-=");
						} else {
							opNode.setOp("+=");
						}
						
						current.setOp(null);
						opNode.setParent(newPlaceholder);
						
						tmp = new ArrayList<Node>();
						tmp.add(idNode);
						tmp.add(constantNode);
						
						idNode.setParent(opNode);
						constantNode.setParent(opNode);
						
						opNode.setChildren(tmp);
						
						tmp2 = newPlaceholder.getChildren();
						
						if (incOrder.equals("postIncExpr")) {
							tmp2.add(opNode);
						} else {
							tmp2.add(0, opNode);
						}
						
						newPlaceholder.setChildren(tmp2);
					} else {
						;
						// TODO in the future when we support incrementing on expressions
					}
				}
				
				
			// label op in addExpression and binExpression
			} else if (current.getToken().getTokenLabel().equals("addExpression")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("add_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("min_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
					}				
				}
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
			} else if (current.getToken().getTokenLabel().equals("bitExpr")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("bit_op")) {
						current.setOp(child.getToken().getTokenString());
						current.getToken().setTokenLabel("bitExpression");
						tmp2.add(child);
					}
				}
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
			// label unary ops in expression and collapse goto statements
			} else if (current.getToken().getTokenLabel().equals("expression")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				Node gotoNode = null;
				Node identifierNode = null;
				
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("tilde")) {
						current.setOp(child.getToken().getTokenString());
						current.getToken().setTokenLabel("bitExpression");
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("exclaim")) {
						current.setOp(child.getToken().getTokenString());
						current.getToken().setTokenLabel("bitExpression");
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("goto")) {
						gotoNode = child;
					} else if (child.getToken().getTokenLabel().equals("identifier")) {
						identifierNode = child;
					}
				}				
				
				// found valid goto expression
				if (gotoNode != null && identifierNode != null) {
					current.setName(identifierNode.getToken().getTokenString());
					current.getToken().setTokenLabel(gotoNode.getToken().getTokenLabel());
					tmp = new ArrayList<Node>();
				}
				
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
				
			// label op in mulExpression and divExpression
			} else if (current.getToken().getTokenLabel().equals("term")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("mul_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
						current.setTokenLabel("mulExpression");
					} else if (child.getToken().getTokenLabel().equals("div_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
						current.setTokenLabel("divExpression");
					} else if (child.getToken().getTokenLabel().equals("mod_op")) {
						current.setOp(child.getToken().getTokenString());
						tmp2.add(child);
						current.setTokenLabel("modExpression");
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
				
			// label op in addExpression and binExpression
			} else if (current.getToken().getTokenLabel().equals("label")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("identifier")) {
						current.setName(child.getToken().getTokenString());
						tmp2.add(child);
					}		
				}
				tmp.removeAll(tmp2);
				current.setChildren(tmp);
			
			// collapse pointers
			} else if (current.getToken().getTokenLabel().equals("pointer")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
				
				for (Node child : tmp) {
					if (child.getToken().getTokenLabel().equals("mul_op")) {
						tmp2.add(child);
					} else if (child.getToken().getTokenLabel().equals("identifier")) {
						current.setName(child.getToken().getTokenString());
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

	/**
	 * determine if this ast is typed correctly or not
	 * @return true if typed correctly, false else
	 */
	public boolean isTypedCorrectly() {
		return isTypedCorrectly(this.root, this.root);
	}
	
	/**
	 * recursive method to determine type correctness.
	 * @param root is the root of the scope.
	 * @param parent is the root of the subtree.
	 * @return true if all children are correctly typed.
	 */
	private boolean isTypedCorrectly(Node root, Node parent)
	{
		/* all parent cases depend on children */
		for (Node node : parent.getChildren()) {
			if (node.isScopeNode()) {
				return isTypedCorrectly(node, node);
			} else if (!isTypedCorrectly(root, node)) {
				return false;
			}
		}
		
		for (Node child : parent.getChildren()) {
			if (child.getToken().getTokenLabel().equals("identifier") && child.getType() == null) {
				child.setType(root.getSymbolTable().get(child.getName()).getType());	
			}
		}
		
		/* base case */
		if (parent.typeCheckable()) {
			Node base = (parent.getType() == null) ? parent.getChildren().get(0) : parent;
			String type = base.getType();
			for (int i = 0; i < parent.getChildren().size(); i++) {
				if (!type.equals(parent.getChildren().get(i).getType())) {
					System.out.printf("Type mismatch '%s' and '%s'\n", type, parent.getChildren().get(i).getType());
					return false;
				}
			}
			
			parent.setType(type);
			return true;
		} else {
			return true;
		}
	}
	
	public static void main(String argv[]) throws Exception {
		Scanner scanner = new Scanner("test/base.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		if (p.parse()) {
//			System.out.println(Node.printTree(p.getParseTree(), " ", false));	
			
			
			ASTParser a = new ASTParser(p);
//			if (a.parse() && a.isTypedCorrectly()) {
			if (a.parse()) {
				a.printAST();
//				if (a.isTypedCorrectly()) {
//					System.out.println("Type check passed.");
//				}
			}
			
			a.printSymbolTable();
			
		} else {
			System.out.println("FAILED TO PARSE");
		}
		

		
		
	}
	
	
	
}
