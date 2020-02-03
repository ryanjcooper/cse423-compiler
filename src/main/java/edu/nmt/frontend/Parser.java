package edu.nmt.frontend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.nmt.RuntimeSettings;

public class Parser {
	
	
	public static void main(String argv[]) throws IOException {
		Scanner scanner = new Scanner("test/base.c");
		scanner.scan();
		Iterator<Token> tokens = scanner.getTokens().iterator();
		String tokenString = "";
		Node node;
		
		while (tokens.hasNext()) {
			/* first get new token */
			Token token = tokens.next();
			
			/* create a new node for the token */
			node = new Node(token);
			
			/* add token label to token string */
			tokenString += token.getTokenLabel();
			
			//System.out.println(tokenString);
			
			/* split the token string into an array */
			String[] tokenSplit = tokenString.split(" ");
			
			/* this will be used to save the state of each right-to-left test */
			String tokenLabels = "";
			
			/* go from right to left, checking if grammar map has a non-terminal for the token string
			 * if true, give the node a non-terminal parent
			 */
			for (int i = tokenSplit.length - 1; i >= 0; i--) {
				tokenLabels = tokenSplit[i] + " " + tokenLabels;
				tokenLabels = tokenLabels.trim();
			
				while (RuntimeSettings.grammarMap.containsKey(tokenLabels)) {
					/* create a new node with the non-terminal symbol from the grammar map 
					 * and set it as the parent of this node 
					 */
					String nonTerminal = RuntimeSettings.grammarMap.get(tokenLabels);
					Node newParent = new Node(new Token("non-terminal", nonTerminal));
					node.setParent(newParent);
					newParent.addChild(node);
					
					tokenString = tokenString.replace(tokenLabels, nonTerminal);
					tokenLabels = nonTerminal;
				}
				
				System.out.println(tokenLabels);
			}
			
			tokenString += " ";
		}
	}
}
