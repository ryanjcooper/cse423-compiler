package edu.nmt.frontend;

import static org.junit.Assert.*;

import org.junit.Test;

public class TokenTest {

	/**
	 * Test if constructor assigns correct string and detects correct label
	 */
	@Test
	public void testTokenString() {
		Token subject = new Token("float");
		assertTrue(subject.getTokenString().contentEquals("float"));
		assertTrue(subject.getTokenLabel().contentEquals("identifier"));
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
		Token subject = new Token("int");
		String baseline = new String("type 'int'");
		assertTrue(subject.toString().contentEquals(baseline));
	}

	/**
	 * Check if the comparison function is correct
	 */
	@Test
	public void testEqualsObject() {
		Token baseline = new Token("int");
		Token subject = new Token("int");
		Token falsehood = new Token("return");
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
		
	}

}
