package edu.nmt.frontend;

import edu.nmt.frontend.scanner.TokenLabeler;

public class Token {

	private String tokenString;
	private String tokenLabel;
	private Integer lineNum;
	private Integer charPos;
	
	/**
	 * Generic constructor for given tokens
	 * @param token to add and auto-label
	 */
	public Token(String token) {
		this.tokenString = token;
		this.tokenLabel = TokenLabeler.labelToken(tokenString);
	}
	
	/**
	 * Constructor with specified label
	 * @param token to add
	 * @param label to use rather than auto-label
	 */
	public Token(String token, String label) {
		this.tokenString = token;
		this.tokenLabel = label;
	}
	
	/**
	 * Create token object with token and positionals
	 * @param token to record
	 * @param lineNum integer for position
	 * @param charPos integer for position in line
	 */
	public Token(String token, Integer lineNum, Integer charPos) {
		this.tokenString = token;
		this.tokenLabel = TokenLabeler.labelToken(tokenString);
		this.lineNum = lineNum;
		this.charPos = charPos;
	}
	
	/**
	 * Full constructor with all fields
	 * @param token to add
	 * @param label to override with
	 * @param lineNum integer for position
	 * @param charPos integer for position in line
	 */
	public Token(String token, String label, Integer lineNum, Integer charPos) {
		this.tokenString = token;
		this.tokenLabel = label;
		this.lineNum = lineNum;
		this.charPos = charPos;
	}
	
	public Token(Token token) {
		this(token.getTokenString(), token.getTokenLabel(), token.getLineNum(), token.getCharPos());
	}
	
	/**
	 * Returns the string of Token
	 * @return String type
	 */
	public String getTokenString() {
		return tokenString;
	}
	
	/**
	 * Label of token
	 * @return
	 */
	public String getTokenLabel() {
		return tokenLabel;
	}
	
	/**
	 * Replace token string
	 * @param tokenString for token field
	 */
	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}
	
	/**
	 * Set the label to override auto-labeling
	 * @param tokenLabel
	 */
	public void setTokenLabel(String tokenLabel) {
		this.tokenLabel = tokenLabel;
	}
	
	/**
	 * Return the line number of a token, allows for error output
	 * @return Integer for positional
	 */
	public Integer getLineNum() {
		return lineNum;
	}

	/**
	 * Replace line number field
	 * @param lineNum Integer object for positional
	 */
	public void setLineNum(Integer lineNum) {
		this.lineNum = lineNum;
	}

	/**
	 * Gets the position in line for error output
	 * @return Integer object in field
	 */
	public Integer getCharPos() {
		return charPos;
	}

	/**
	 * Override the character position in line
	 * @param charPos Object to replace with
	 */
	public void setCharPos(Integer charPos) {
		this.charPos = charPos;
	}
	
	/**
	 * Converts a token into the clang string format
	 */
	@Override
	public String toString() {
		return lineNum + ":" + charPos + " "  + tokenLabel + " '" + tokenString + "'";
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

		return (((Token) t).tokenString.equals(this.tokenString) && ((Token) t).tokenLabel.equals(this.tokenLabel) && ((Token) t).lineNum.equals(this.lineNum) && ((Token) t).charPos.equals(this.charPos));
	}
	
	
}
