package edu.nmt.frontend;

public class Token {

	private String tokenString;
	private String tokenLabel;
	
	public Token(String token) {
		tokenString = token;
		tokenLabel = TokenLabeler.labelToken(tokenString);
	}
	
	public Token(String token, String label) {
		tokenString = token;
		tokenLabel = label;
	}
	
	public String getTokenString() {
		return tokenString;
	}
	
	public String getTokenLabel() {
		return tokenLabel;
	}
	
	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
	
	public void setTokenLabel(String tokenLabel) {
		this.tokenLabel = tokenLabel;
	}
	
	@Override
	public String toString() {
		return "<\"" + tokenString + "\"," + tokenLabel + ">"; 
	}
	
	
}
