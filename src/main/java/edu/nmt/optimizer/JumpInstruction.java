package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

public class JumpInstruction extends Instruction {
	/**
	 * operand1 is the boolean expression (can be null for unconditional jumps)
	 * operand2 is the destination of the jump statement
	 * 
	 * if the ifStmt does not have a condition, then it is an else statement, and does not have a destination
	 * @param jumpType TODO
	 */

	public JumpInstruction(Node node, List<Instruction> operandList, Integer lineNumber, String jumpType) {
		super("jump", node, operandList, lineNumber);
		this.op1Name = jumpType;
	}
	
	public String toString() {
		String out = "jump " + this.operand2.getInstrID();
		if (this.operand1 != null) {
			out += " if " + operand1.getInstrID() + " is " + op1Name;
		}
		return out;
	}
}
