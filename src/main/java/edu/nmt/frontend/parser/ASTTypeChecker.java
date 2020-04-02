/**
 * 
 */
package edu.nmt.frontend.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.scanner.Scanner;

/**
 * @author Terence
 *
 */
public class ASTTypeChecker {
	
	public static Map<String, Node> symbolTable;

	public static void main(String[] args) throws IOException {
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
		
		ASTTypeChecker.symbolTable = a.getRoot().getChildren().get(0).getSymbolTable();
		System.out.println(ASTTypeChecker.symbolTable);
		System.out.println(isTypedCorrectly(a.getRoot()));
	}
	
	public static boolean isTypedCorrectly(Node root)
	{
		/* all parent cases depend on children */
		for (Node node : root.getChildren()) {
			if (!isTypedCorrectly(node)) {
				return false;
			}
		}
		
		for (Node child : root.getChildren()) {
			if (child.getToken().getTokenLabel().equals("identifier") && child.type == null) {
				child.type = symbolTable.get(child.getToken().getTokenString()).type;	
			}
		}
		
		/* base case */
		if (root.typeCheckable()) {
			String type = (root.type == null) ? root.getChildren().get(0).type : root.type;
			System.out.println(root);
			System.out.println(type);
			for (int i = 0; i < root.getChildren().size(); i++) {
				System.out.println(root.getChildren().get(i).type + "\n\n");
				if (!type.equals(root.getChildren().get(i).type)) {
					return false;
				}
			}
			
			root.setType(type);
			return true;
		} else {
			return true;
		}
	}

}
