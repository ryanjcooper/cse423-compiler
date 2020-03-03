package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;

public class ReturnInstruction extends Instruction {

	public ReturnInstruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		super("return", operandList, lineNumber);
	}
	
}
