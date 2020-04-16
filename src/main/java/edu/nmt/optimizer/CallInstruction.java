package edu.nmt.optimizer;

import java.util.ArrayList;
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
	
	public CallInstruction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String instrToStr() {
		String op1 = null;
		String op2 = null;
		
		if (operand1 != null)
			op1 = operand1.getLineNumber().toString();
		
		if (operand2 != null)
			op2 = operand2.getLineNumber().toString();
				
		StringBuilder sb = new StringBuilder();
		
		for (Instruction param : paramList) {
			sb.append(param.getInstrID() + " ");
		}
		
		
	
		return lineNumber + " " + instrID + " " + operation + " " 
						  + type + " " + op1 + " " 
						  + op1Name + " " + op2 + " " + sb;
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
	
	public void setParamList(ArrayList<Instruction> paramList) {
		this.paramList = paramList;
	}

}
