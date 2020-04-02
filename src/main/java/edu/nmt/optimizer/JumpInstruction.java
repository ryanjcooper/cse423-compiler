package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

public class JumpInstruction extends Instruction {
	/**
	 * operand1 is the destination of a false statement
	 * operand2 is the boolean expression
	 * 
	 * note when making constructor: if the destination is an else/elif statement, then the destination is the next nested if statement
	 * otherwise, the destination is the ifStmt's following sibling node within its parent's child list
	 * 
	 * if the ifStmt does not have a condition, then it is an else statement, and does not have a destination
	 * note: this design means that there is no logical end to the if-elseif-else block, as it bleeds directly into the next set of
	 * statements following the conditional block
	 */

	public JumpInstruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		super("jump", node, operandList, lineNumber);
	}
	
	public String toString() {
		return null;
	}
}
