package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

public class CallInstruction extends Instruction {
	public CallInstruction(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	private List<Instruction> paramList;
}
