package edu.nmt.optimizer;
import edu.nmt.frontend.*;

public class Instruction {
	private String operation;
	private Instruction operand1;
	private String op1Name;
	private Instruction operand2;
	private String op2Name;
	
	public Instruction(Node node) {
		String label = node.getName();
		
	}

	@Override
	public String toString() {
		if (operation.contentEquals("identifier") || operation.contentEquals("numeric_constant")) {
			return op1Name;
		} else {
			return operation + operand1 + operand2;
		}
	}
}
