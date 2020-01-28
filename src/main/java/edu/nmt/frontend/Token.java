package edu.nmt.frontend;

public class Token {

	private String tokenString;
	private TokenLabel tokenLabel;
	
	public Token(String token) {
		tokenString = token;
		tokenLabel = TokenLabeler.labelToken(tokenString);
	}
	
	public Token(String token, TokenLabel label) {
		tokenString = token;
		tokenLabel = label;
	}
	
	public String getTokenString() {
		return tokenString;
	}
	
	public TokenLabel getTokenLabel() {
		return tokenLabel;
	}
	
	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
	
	public void setTokenLabel(TokenLabel tokenLabel) {
		this.tokenLabel = tokenLabel;
	}
	
	@Override
	public String toString() {
		return "<\"" + tokenString + "\"," + tokenLabel + ">"; 
	}
	
	
}
