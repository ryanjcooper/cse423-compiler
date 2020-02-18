package edu.nmt.frontend;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.nmt.RuntimeSettings;

public class TokenLabelerTest {

	@Test
	public void testIsNumeric() {
		int num = (int) Math.random() * 2147483647;
		
		assertTrue(TokenLabeler.isNumeric(Integer.toString(num)));
		assertFalse(TokenLabeler.isNumeric(""));
		assertFalse(TokenLabeler.isNumeric(null));
		assertFalse(TokenLabeler.isNumeric("wrong"));
		assertFalse(TokenLabeler.isNumeric("42life"));
		assertTrue(TokenLabeler.isNumeric("0xFF"));
		assertFalse(TokenLabeler.isNumeric("0xabcdefg"));
	}

	@Test
	public void testIsIdentifier() {
		final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";
		//generate identifier strings
		StringBuilder builder1 = new StringBuilder();
		StringBuilder builder2 = new StringBuilder();
		
		//set first character to letter or underscore to create valid id
		int c = (int) (Math.random()*(ALPHA_NUMERIC_STRING.length() - 10));
		builder1.append(ALPHA_NUMERIC_STRING.charAt(c));
		
		//set first character to number to create invalid id
		c = (int) (Math.random()*10);
		builder2.append(c);
		
		//random length from 1-31
		int len = (int) (Math.random()*31);
		
		while(len-- != 0) {
			c = (int) (Math.random()*ALPHA_NUMERIC_STRING.length());
			builder1.append(ALPHA_NUMERIC_STRING.charAt(c));
			builder2.append(ALPHA_NUMERIC_STRING.charAt(c));
		}
		
		String validString = builder1.toString();
		String invalidString = builder2.toString();
		String nullString = null;
		
		assertTrue(TokenLabeler.isIdentifier(validString));
		assertFalse(TokenLabeler.isIdentifier(invalidString));
		assertFalse(TokenLabeler.isIdentifier(nullString));
	}

	@Test
	public void testLabelToken() {
		//type int void char unsigned
		assertEquals("type", TokenLabeler.labelToken("int"));
		assertEquals("type", TokenLabeler.labelToken("void"));
		assertEquals("type", TokenLabeler.labelToken("char"));
		assertEquals("type", TokenLabeler.labelToken("unsigned"));
		//return return
		assertEquals("return", TokenLabeler.labelToken("return"));
		//break break
		assertEquals("break", TokenLabeler.labelToken("break"));
		//goto goto
		assertEquals("goto", TokenLabeler.labelToken("goto"));
		//single_quote '
		assertEquals("single_quote", TokenLabeler.labelToken("'"));
		//double_quote "
		assertEquals("double_quote", TokenLabeler.labelToken("\""));
		//l_paren (
		assertEquals("l_paren", TokenLabeler.labelToken("("));
		//r_paren )
		assertEquals("r_paren", TokenLabeler.labelToken(")"));
		//l_brace {
		assertEquals("l_brace", TokenLabeler.labelToken("{"));
		//r_brace }
		assertEquals("r_brace", TokenLabeler.labelToken("}"));
		//l_bracket [
		assertEquals("l_bracket", TokenLabeler.labelToken("["));
		//r_bracket ]
		assertEquals("r_bracket", TokenLabeler.labelToken("]"));
		//assign_op = += -= /= *= %= |= &= >>= <<= ^=
		assertEquals("assign_op", TokenLabeler.labelToken("="));
		assertEquals("assign_op", TokenLabeler.labelToken("+="));
		assertEquals("assign_op", TokenLabeler.labelToken("-="));
		assertEquals("assign_op", TokenLabeler.labelToken("/="));
		assertEquals("assign_op", TokenLabeler.labelToken("*="));
		assertEquals("assign_op", TokenLabeler.labelToken("%="));
		assertEquals("assign_op", TokenLabeler.labelToken("|="));
		assertEquals("assign_op", TokenLabeler.labelToken("&="));
		assertEquals("assign_op", TokenLabeler.labelToken(">>="));
		assertEquals("assign_op", TokenLabeler.labelToken("<<="));
		assertEquals("assign_op", TokenLabeler.labelToken("^="));
		//add_op + 
		assertEquals("add_op", TokenLabeler.labelToken("+"));
		//min_op -
		assertEquals("min_op", TokenLabeler.labelToken("-"));
		//mul_op *
		assertEquals("mul_op", TokenLabeler.labelToken("*"));
		//mod_op %
		assertEquals("mod_op", TokenLabeler.labelToken("%"));		
		//div_op /
		assertEquals("div_op", TokenLabeler.labelToken("/"));
		//unary_op ++ --
		assertEquals("unary_op", TokenLabeler.labelToken("++"));
		assertEquals("unary_op", TokenLabeler.labelToken("--"));
		//exclaim !
		assertEquals("exclaim", TokenLabeler.labelToken("!"));
		//tilde ~
		assertEquals("tilde", TokenLabeler.labelToken("~"));
		//dot .
		assertEquals("dot", TokenLabeler.labelToken("."));
		//arrow ->
		assertEquals("arrow", TokenLabeler.labelToken("->"));
		//bool_op && == || <= >= < > != 
		assertEquals("bool_op", TokenLabeler.labelToken("&&"));
		assertEquals("bool_op", TokenLabeler.labelToken("=="));
		assertEquals("bool_op", TokenLabeler.labelToken("||"));
		assertEquals("bool_op", TokenLabeler.labelToken("<="));
		assertEquals("bool_op", TokenLabeler.labelToken(">="));
		assertEquals("bool_op", TokenLabeler.labelToken("<"));
		assertEquals("bool_op", TokenLabeler.labelToken(">"));
		assertEquals("bool_op", TokenLabeler.labelToken("!="));
		//if if
		assertEquals("if", TokenLabeler.labelToken("if"));
		//else else
		assertEquals("else", TokenLabeler.labelToken("else"));
		//while while
		assertEquals("while", TokenLabeler.labelToken("while"));
		//do do
		assertEquals("do", TokenLabeler.labelToken("do"));
		//for for
		assertEquals("for", TokenLabeler.labelToken("for"));
		//switch switch
		assertEquals("switch", TokenLabeler.labelToken("switch"));
		//struct struct
		assertEquals("struct", TokenLabeler.labelToken("struct"));
		//enum enum
		assertEquals("enum", TokenLabeler.labelToken("enum"));
		//case case
		assertEquals("case", TokenLabeler.labelToken("case"));
		//default default
		assertEquals("default", TokenLabeler.labelToken("default"));
		//comma ,
		assertEquals("comma", TokenLabeler.labelToken(","));
		//semi ;
		assertEquals("semi", TokenLabeler.labelToken(";"));
		// colon :
		assertEquals("colon", TokenLabeler.labelToken(":"));		
		//bit_op ^ & | ! << >>
		assertEquals("bit_op", TokenLabeler.labelToken("^"));
		assertEquals("bit_op", TokenLabeler.labelToken("&"));
		assertEquals("bit_op", TokenLabeler.labelToken("|"));
		assertEquals("bit_op", TokenLabeler.labelToken("<<"));
		assertEquals("bit_op", TokenLabeler.labelToken(">>"));
		//exclaim
		assertEquals("exclaim", TokenLabeler.labelToken("!"));
		//numeric_constant
		int c = (int) Math.random() * 2147483647;
		assertEquals("numeric_constant", TokenLabeler.labelToken(Integer.toString(c)));
		//identifier
		final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";
		//generate identifier strings
		StringBuilder builder1 = new StringBuilder();
		StringBuilder builder2 = new StringBuilder();
		
		//set first character to letter or underscore to create valid id
		c = (int) (Math.random()*(ALPHA_NUMERIC_STRING.length() - 10));
		builder1.append(ALPHA_NUMERIC_STRING.charAt(c));
		
		//set first character to number to create invalid id
		c = (int) (Math.random()*10);
		builder2.append(c);
		
		//random length from 2-32
		int len = (int) (Math.random()*31) + 1;
		
		while(len-- != 0) {
			c = (int) (Math.random()*ALPHA_NUMERIC_STRING.length());
			builder1.append(ALPHA_NUMERIC_STRING.charAt(c));
			builder2.append(ALPHA_NUMERIC_STRING.charAt(c));
		}
		
		String validString = builder1.toString();
		String invalidString = builder2.toString();
		
		assertEquals("identifier", TokenLabeler.labelToken(validString));
		//unknown
		assertEquals("unknown", TokenLabeler.labelToken(invalidString));
	}

}
