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
	
	private List<String> syntaxConstructs = new ArrayList<String>(Arrays.asList(
				"l_paren",
				"r_paren",
				"semi",
				"l_brace",
				"r_brace",
				"return"
			));
	
	public ASTParser(Parser p) {
		this.p = p;
	}
	
	public void parse() {
		Node ptRoot = p.getParseTree();
		Stack<Node> stack = new Stack<Node>();
		List<Node> tmp;
		List<Node> tmp2;
		
		stack.addAll(ptRoot.getChildren());
		
		while(!stack.empty()) {
			Node current = stack.pop();
//			System.out.println(current.token + " " + current.getParent());
			if (syntaxConstructs.contains(current.getToken().getTokenLabel())) {
				tmp = current.getParent().getChildren();
				tmp.remove(current);
				tmp.addAll(current.getChildren());
				current.getParent().setChildren(tmp);
			} else if (current.getToken().getTokenLabel().equals("funcDefinition")) {
				tmp = current.getChildren();
				tmp2 = new ArrayList<Node>();
//				astRoot.addChild(current);
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
			} else if ((current.getChildren().size() == 1)) {
				// remove node from parent
				tmp = current.getParent().getChildren();
				tmp.remove(current);
				current.getParent().setChildren(tmp);
				// set only childs parent to current node
				Node tmpNode = current.getChildren().get(0);
				tmpNode.setParent(current.getParent());
				current.getParent().addChild(tmpNode);
			}
			
			stack.addAll(current.getChildren());
		}
//		System.out.println(astRoot.getASTChildren());
		
		System.out.println(Node.printTree(ptRoot, " ", false));
	}
	
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/base.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		p.parse();
//		System.out.println(Node.printTree(p.getParseTree(), " ", false));
		ASTParser a = new ASTParser(p);
		a.parse();
		

	} 
	
	
	
}
