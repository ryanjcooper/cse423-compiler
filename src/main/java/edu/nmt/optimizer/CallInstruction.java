package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

public class CallInstruction extends Instruction {
	private List<List<Instruction>> paramList;
	
	public CallInstruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		super("call", node, operandList, lineNumber);
	}

}
