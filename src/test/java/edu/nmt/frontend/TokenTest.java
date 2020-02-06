package edu.nmt.frontend;

import static org.junit.Assert.*;

import org.junit.Test;

public class TokenTest {
	
	/**
	 * Test the token constructor with 4 fields
	 */
	@Test
	public void testfourcon() {
		Token subject = new Token("int", "type", 1, 2);
		assertTrue(subject.getTokenString().contentEquals("int"));
		assertTrue(subject.getTokenLabel().contentEquals("type"));
		assertTrue(subject.getLineNum() == 1);
		assertTrue(subject.getCharPos() == 2);
	}

	/**
	 * Test if constructor assigns correct string and detects correct label
	 */
	@Test
	public void testTokenString() {
		Token subject = new Token("int");
		assertTrue(subject.getTokenString().contentEquals("int"));
		assertTrue(subject.getTokenLabel().contentEquals("type"));
	}

	/**
	 * Test if the token string and label can be set manually
	 */
	@Test
	public void testTokenStringString() {
		Token subject = new Token("float", "same");
		assertTrue(subject.getTokenString().contentEquals("float"));
		assertTrue(subject.getTokenLabel().contentEquals("same"));
		assertFalse(subject.getTokenLabel().contentEquals("different"));
	}

	/**
	 * Test if the expected token string is returned
	 */
	@Test
	public void testGetTokenString() {
		Token subject = new Token("i");
		assertTrue(subject.getTokenString().contentEquals("i"));
	}

	/**
	 * Test if the expected Token label is returned.
	 */
	@Test
	public void testGetTokenLabel() {
		Token subject = new Token("i");
		assertTrue(subject.getTokenLabel().contentEquals("identifier"));
	}

	/**
	 * Test if setTokenString changes the token string
	 */
	@Test
	public void testSetTokenString() {
		Token subject = new Token("int");
		subject.setTokenString("Dogs");
		assertTrue(subject.getTokenString().contentEquals("Dogs"));
	}

	/**
	 * Test if the function changes the token label permanently
	 */
	@Test
	public void testSetTokenLabel() {
		Token subject = new Token("int");
		subject.setTokenLabel("Banana");
		assertTrue(subject.getTokenLabel().contentEquals("Banana"));
	}

	/**
	 * Check if toString is functioning as expected
	 */
	@Test
	public void testToString() {
		Token subject = new Token("int", 1, 1);
		String baseline = new String("1:1 type 'int'");
		assertTrue(subject.toString().contentEquals(baseline));
	}

	/**
	 * Check if the comparison function is correct
	 */
	@Test
	public void testEqualsObject() {
		Token baseline = new Token("int", 1 ,1);
		Token subject = new Token("int", 1, 1);
		Token falsehood = new Token("return", 1, 1);
		Token wrongline = new Token("int", 2 ,1);
		Token wrongspot = new Token("int", 1 ,5);
		Token empty = null;
		String dog = new String("Mastiff");
		assertTrue(baseline.equals(subject));
		assertFalse(baseline.equals(falsehood));
		// Test if null
		assertFalse(baseline.equals(empty));
		// Test if different class
		assertFalse(baseline.equals(dog));
		// Same string
		subject.setTokenLabel("incorrect");
		assertFalse(baseline.equals(subject));
		assertFalse(baseline.equals(wrongline));
		assertFalse(baseline.equals(wrongspot));
		
	}
	
	/**
	 * Test if the lineNum is permanently changed
	 */
	@Test
	public void testSetLineNum() {
		Token subject = new Token("int", "type", 1, 2);
		subject.setLineNum(3);
		assertTrue(subject.getLineNum() == 3);
		
	}
	
	/**
	 * Test GetLineNum
	 */
	@Test
	public void testGetLineNum() {
		Token subject = new Token("int", "type", 1, 2);
		assertTrue(subject.getLineNum() == 1);
		
	}
	
	/**
	 * Test SetCharPos
	 */
	@Test
	public void testSetCharPos() {
		Token subject = new Token("int", "type", 1, 2);
		subject.setCharPos(5);
		assertTrue(subject.getCharPos() == 5);
	}
	
	/**
	 * Tests if getCharPos is returning the expected value
	 */
	@Test
	public void TestGetCharPos() {
		Token subject = new Token("int", "type", 1, 2);
		assertTrue(subject.getCharPos() == 2);
	}

}
