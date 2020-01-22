package edu.nmt.frontend;

public class Token {

	private String tokenString;
	private TokenLabel tokenLabel;
	
	public Token(String token) {
		tokenString = token;
		tokenLabel = TokenLabeler.labelToken(tokenString);
	}
	
	@Override
	public String toString() {
		return "<\"" + tokenString + "\"," + tokenLabel + ">"; 
	}
	
	
}
