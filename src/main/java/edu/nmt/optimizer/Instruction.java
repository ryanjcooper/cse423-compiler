package edu.nmt.optimizer;
import java.util.List;

import edu.nmt.frontend.*;

public class Instruction {
	protected Integer lineNumber;
	protected String instrID;
	protected String operation;
	protected Instruction operand1;
	protected String op1Name;
	protected Instruction operand2;
	
	public Instruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		this.lineNumber = lineNumber;
		this.instrID = "_" + lineNumber;
		String label = node.getToken().getTokenLabel();
		if (label.contentEquals("identifier") || label.contentEquals("numeric_constant")) {
			this.operation = label;
			this.op1Name = node.getToken().getTokenString();
		} else {
			if (label.contentEquals("varDeclaration")) {
				this.instrID = node.getName();
				this.operation = null;
				
			} else {
				this.operation = node.getOp();
			}
			
			if (!operandList.isEmpty()){
				this.op1Name = node.getName();
				this.operand1 = operandList.get(0);
				if (operandList.size() > 1) {
					this.operand2 = operandList.get(1);
				}
			}
		}
	}
	
	public Instruction(String type, List<Instruction> operandList, Integer lineNumber) {
		this.lineNumber = lineNumber;
		this.operation = type;
		if (type.contentEquals("return")) {
			this.operand1 = operandList.get(0);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(instrID + " = ");
		if (operation != null && (operation.contentEquals("identifier") || operation.contentEquals("numeric_constant"))) {
			builder.append(op1Name);
			return builder.toString();
		} else {
			if (operand1 != null) {
				builder.append(operand1.getInstrID() + " ");
			}
			
			if (operation != null) {
				builder.append(operation);
			}
			
			if (operand2 != null) {
				builder.append(" " + operand2.getInstrID());
			}
			return  builder.toString();
		}
	}

	public String getInstrID() {
		return instrID;
	}

	public void setInstrID(String instrID) {
		this.instrID = instrID;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}
}
