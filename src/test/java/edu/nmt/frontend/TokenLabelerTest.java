package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TokenLabelerTest {

	@Test
	public void testIsNumeric() {
		int num = (int) Math.random() * 2147483647;
		
		assertEquals(true, TokenLabeler.isNumeric(Integer.toString(num)));
		assertEquals(false, TokenLabeler.isNumeric(""));
		assertEquals(false, TokenLabeler.isNumeric(null));
		assertEquals(false, TokenLabeler.isNumeric("wrong"));
		assertEquals(false, TokenLabeler.isNumeric("42life"));
	}
/*
	@Test
	public void testLabelToken() {
		//generate identifier string
		final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";
		StringBuilder builder = new StringBuilder();
		//set first character to letter or underscore to create valid id
		int c = (int) (Math.random()*ALPHA_NUMERIC_STRING.length() - 10);
		builder.append(ALPHA_NUMERIC_STRING.charAt(c));
		//random length from 1-31
		int len = (int) (Math.random()*31); 
		while(len-- != 0) {
			c = (int) (Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(c));
		}
		String genString = builder.toString();
		
		//variableModifier auto const unsigned signed volatile extern static register
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("auto"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("const"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("unsigned"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("signed"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("volatile"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("extern"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("static"));
		assertEquals(TokenLabel.variableModifier, TokenLabeler.labelToken("register"));
		//typeSpecifier double float int short long void char
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("double"));
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("float"));
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("int"));
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("short"));
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("long"));
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("void"));
		assertEquals(TokenLabel.typeSpecifier, TokenLabeler.labelToken("char"));
		//controlSpecifier break return continue goto
		assertEquals(TokenLabel.controlSpecifier, TokenLabeler.labelToken("break"));
		assertEquals(TokenLabel.controlSpecifier, TokenLabeler.labelToken("return"));
		assertEquals(TokenLabel.controlSpecifier, TokenLabeler.labelToken("continue"));
		assertEquals(TokenLabel.controlSpecifier, TokenLabeler.labelToken("goto"));
		//tagSpecifier ' " [ ] { } ( )
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("'"));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("\""));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("["));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("]"));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("{"));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("}"));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken("("));
		assertEquals(TokenLabel.tagSpecifier, TokenLabeler.labelToken(")"));
		//assignmentSpecifier = += -= /= *= %= |= &= >>= <<= ^=
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("+="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("-="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("/="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("*="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("%="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("|="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("&="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken(">>="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("<<="));
		assertEquals(TokenLabel.assignmentSpecifier, TokenLabeler.labelToken("^="));
		//variableOperator + - * / % ++ --
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("+"));
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("-"));
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("*"));
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("/"));
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("%"));
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("++"));
		assertEquals(TokenLabel.variableOperator, TokenLabeler.labelToken("--"));
		//booleanOperator && == || <= >= < > !=
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken("&&"));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken("=="));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken("||"));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken("<="));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken(">="));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken("<"));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken(">"));
		assertEquals(TokenLabel.booleanOperator, TokenLabeler.labelToken("!="));
		//conditionalStmt if else
		assertEquals(TokenLabel.conditionalStmt, TokenLabeler.labelToken("if"));
		assertEquals(TokenLabel.conditionalStmt, TokenLabeler.labelToken("else"));
		//iterationStmt while do for
		assertEquals(TokenLabel.iterationStmt, TokenLabeler.labelToken("while"));
		assertEquals(TokenLabel.iterationStmt, TokenLabeler.labelToken("do"));
		assertEquals(TokenLabel.iterationStmt, TokenLabeler.labelToken("for"));
		//structStmt struct . ->
		assertEquals(TokenLabel.structStmt, TokenLabeler.labelToken("struct"));
		assertEquals(TokenLabel.structStmt, TokenLabeler.labelToken("."));
		assertEquals(TokenLabel.structStmt, TokenLabeler.labelToken("->"));
		//switchStmt switch case default
		assertEquals(TokenLabel.switchStmt, TokenLabeler.labelToken("switch"));
		assertEquals(TokenLabel.switchStmt, TokenLabeler.labelToken("case"));
		assertEquals(TokenLabel.switchStmt, TokenLabeler.labelToken("default"));
		//enumSpecifier enum
		assertEquals(TokenLabel.enumSpecifier, TokenLabeler.labelToken("enum"));
		//unionSpecifier union
		assertEquals(TokenLabel.unionSpecifier, TokenLabeler.labelToken("union"));
		//typedefSpecifier typedef
		assertEquals(TokenLabel.typedefSpecifier, TokenLabeler.labelToken("typedef"));
		//delimiter , ;
		assertEquals(TokenLabel.delimiter, TokenLabeler.labelToken(","));
		assertEquals(TokenLabel.delimiter, TokenLabeler.labelToken(";"));
		//bitOperator ^ & | ! << >>
		assertEquals(TokenLabel.bitOperator, TokenLabeler.labelToken("^"));
		assertEquals(TokenLabel.bitOperator, TokenLabeler.labelToken("&"));
		assertEquals(TokenLabel.bitOperator, TokenLabeler.labelToken("|"));
		assertEquals(TokenLabel.bitOperator, TokenLabeler.labelToken("!"));
		assertEquals(TokenLabel.bitOperator, TokenLabeler.labelToken("<<"));
		assertEquals(TokenLabel.bitOperator, TokenLabeler.labelToken(">>"));
		//preprocessorSpecifier #
		assertEquals(TokenLabel.preprocessorSpecifier, TokenLabeler.labelToken("#"));
		//identifier
//		System.out.println("generated string = " + genString); //print generated string
		assertEquals(TokenLabel.identifier, TokenLabeler.labelToken(genString));
	}
*/
}