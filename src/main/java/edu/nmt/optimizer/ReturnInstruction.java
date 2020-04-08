package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;

/**
 * @author	mattadik123
 * @dated	03/02/2020
 *
 */
public class ReturnInstruction extends Instruction {

	public ReturnInstruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		super("return", node, operandList, lineNumber);
		this.op1Name = this.operand1.getInstrID();
	}

	@Override
	public String toString() {
		return "return " + this.op1Name;
	}
	
	
}
