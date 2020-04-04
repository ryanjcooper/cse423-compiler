package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

public class JumpInstruction extends Instruction {
	/**
	 * operand1 is the boolean expression (can be null for unconditional jumps)
	 * operand2 is the destination of the jump statement (assumed to be jumpIfFalse for conditional jumps)
	 * 
	 * if the ifStmt does not have a condition, then it is an else statement, and does not have a destination
	 */

	public JumpInstruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		super("jump", node, operandList, lineNumber);
	}
	
	public String toString() {
		String out = "jump " + this.operand2.getInstrID();
		if (this.operand1 != null) {
			out += " if " + operand1.getInstrID() + " is false";
		}
		return out;
	}
}
