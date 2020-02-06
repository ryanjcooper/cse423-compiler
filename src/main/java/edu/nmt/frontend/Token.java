package edu.nmt.frontend;

public class Token {

	private String tokenString;
	private String tokenLabel;
	private Integer lineNum;
	private Integer charPos;
	
	public Token(String token) {
		this.tokenString = token;
		this.tokenLabel = TokenLabeler.labelToken(tokenString);
	}
	
	public Token(String token, String label) {
		this.tokenString = token;
		this.tokenLabel = label;
	}
	
	public Token(String token, Integer lineNum, Integer charPos) {
		this.tokenString = token;
		this.tokenLabel = TokenLabeler.labelToken(tokenString);
		this.lineNum = lineNum;
		this.charPos = charPos;
	}
	
	public Token(String token, String label, Integer lineNum, Integer charPos) {
		this.tokenString = token;
		this.tokenLabel = label;
		this.lineNum = lineNum;
		this.charPos = charPos;
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
	
	/**
	 * Converts a token into the clang string format
	 */
	@Override
	public String toString() {
		return tokenLabel + " '" + tokenString + "'";
	}
	
	/**
	 * Compares Token to another object
	 * @param t
	 * @return true if true
	 */
	@Override
	public boolean equals(Object t) {
		if (t == null)
			return false;
		if (this.getClass() != t.getClass())
			return false;

		return (((Token) t).tokenString.equals(this.tokenString) && ((Token) t).tokenLabel.equals(this.tokenLabel));
	}
	
	
}
