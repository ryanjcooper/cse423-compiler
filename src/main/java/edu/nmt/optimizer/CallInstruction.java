package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;

/**
 * Special Instruction object that represents call instructions
 * @author	mattadik123
 * @dated	03/02/2020
 */
public class CallInstruction extends Instruction {
	private List<Instruction> paramList;
	
	public CallInstruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		super("call", node, operandList, lineNumber);
		this.paramList = operandList;
	}
	
	public String toString() {
		String returnStr = "push ";
		for (Instruction i : this.paramList) {
			returnStr += i.getInstrID() + ", ";
		}
		
		returnStr = returnStr.substring(0, returnStr.length() - 2);
		returnStr += ", then call function " + this.op1Name;
		
		return returnStr;
	}

}
