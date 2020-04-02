package edu.nmt.optimizer;
import java.util.List;

import edu.nmt.frontend.*;

/**
 * 
 * @author	mattadik123
 * @dated	03/02/2020
 *
 */
public class Instruction {
	protected Integer lineNumber;
	protected String instrID;
	protected String operation;
	protected String type;
	protected Instruction operand1;
	protected String op1Name;
	protected Instruction operand2;
	
	public Instruction(Node node, List<Instruction> operandList, Integer lineNumber) {
		this.lineNumber = lineNumber;
		this.instrID = "_" + lineNumber;
		String label = node.getToken().getTokenLabel();
		this.type = node.getType();
		
		if (label.contentEquals("identifier")) {
			this.operation = label;
			this.op1Name = node.getToken().getTokenString();
			this.type = node.getScopeNode().getSymbolTable().get(node.getName()).getType();
		} else if (label.contains("constant")) {
			this.operation = label;
			this.op1Name = node.getToken().getTokenString();
			if (this.type == null)
				this.type = label.replace("_constant", "");
		} else {
			if (label.contentEquals("varDeclaration")) {
				this.instrID = node.getName();
				this.operation = null;
			} else if (label.contentEquals("assignStmt")) {
				this.instrID = node.getChildren().get(1).getName();
				this.operation = "=";
				this.type = node.getChildren().get(1).getType();
			} else {
				this.operation = node.getOp();
			}
			
			if (!operandList.isEmpty()){
				this.op1Name = node.getName();
				if (operandList.size() > 1) {
					this.operand1 = operandList.get(1);
					this.operand2 = operandList.get(0);
					if (type == null)
						this.type = operandList.get(1).getType();
				} else {
					this.operand1 = operandList.get(0);
					if (type == null)
						this.type = operandList.get(0).getType();
				}
			}
		}
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Instruction(String type, List<Instruction> operandList, Integer lineNumber) {
		this.lineNumber = lineNumber;
		this.operation = type;
		if (type.contentEquals("return")) {
			this.operand1 = operandList.get(0);
			this.type = operand1.getType();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(instrID);
		if (operation != null && (operation.contentEquals("identifier") || operation.contains("constant"))) {
			builder.append(" = " + op1Name);
		} else if (operation != null && operation.contentEquals("=")) {
			builder.append(" = ");
			
			if (operand1 != null) {
				builder.append(operand1.getInstrID() + " ");
			}
		} else {
			builder.append(" = ");
			if (operand1 == null && operand2 == null && operation == null) {
				builder.append("null");
			} else if (operation != null && (operation.contentEquals("!") || operation.contentEquals("~"))) {
				builder.append(operation + operand1.getInstrID());
			} else {
				if (operand1 != null)
					builder.append(operand1.getInstrID());
				
				if (operation != null)
					builder.append(" " + operation);
				
				if (operand2 != null)
					builder.append(" " + operand2.getInstrID());
			}
		}
		return  builder.toString();
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
