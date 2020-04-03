package edu.nmt.optimizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.nmt.frontend.*;
import edu.nmt.frontend.scanner.TokenLabeler;

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
	private List<String> specialInstructions = new ArrayList<String>(Arrays.asList(
			"call",
			"jump",
			"return"
		));
	
	public Instruction(String instrType, Node node, List<Instruction> operandList, Integer lineNumber) {
		String label = node.getToken().getTokenLabel();
		// first condition handles non-special instructions
		if (!this.specialInstructions.contains(instrType)) {
			this.lineNumber = lineNumber;
			this.instrID = "_" + lineNumber;
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
		} else {
			this.lineNumber = lineNumber;
			this.operation = instrType;
			if (instrType.contentEquals("return")) {
				this.operand1 = operandList.get(0);
				this.type = operand1.getType();
			} else if (instrType.contentEquals("jump")) {
				if (label.contentEquals("ifStmt")) {
					/**
					 * procedure:
					 * check if else/elif statement exists (nested ifStmt)
					 * if no, recursively check if parent is an ifStmt then choose following sibling of uppermost statement as dest (if this exists)
					 * if yes, dest is next nested ifStmt
					 * 
					 */
				}
			}
		}
		
		
	}
	
	public Instruction(String lineNum, String id, String op, String type, String constant, String op1, String op2) {
		this.lineNumber = Integer.parseInt(lineNum);
		this.instrID = id;
		this.operation = op;
		this.type = type;
		this.op1Name = constant;
		
		if (op1 != null) {
			this.operand1 = IR.variableMap.get(op1);
		}
		
		if (op2 != null) {
			this.operand2 = IR.variableMap.get(op2);
		}
		
		if (IR.variableMap.get(instrID) == null) {
			IR.variableMap.put(id, this);
		}
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * converts a line of an IR output to an object
	 * @param line is the string to convert
	 * @return Instruction object representation of line
	 */
	public static Instruction strToInstr(String line) {
		String[] lineSplit = line.split(" ");
		String[] maxSplit = new String[8];
		int diff = maxSplit.length - lineSplit.length;
		boolean constant = isConstant(lineSplit[3]);
		
		for (int i = 0; i < lineSplit.length; i++) {
			if (i == 0) {
				/* index 0 is line number */
				maxSplit[0] = lineSplit[i].substring(0, lineSplit[i].length() - 1); // remove the semicolon
			} else if (i >= lineSplit.length - 2) {
				/* last two indexes remain as last two */
				maxSplit[i + diff] = lineSplit[i];
			} else {
				/* rest of indexes match */
				maxSplit[i] = lineSplit[i];
			}
		}
		
		if (constant) {
			return new Instruction(maxSplit[0], maxSplit[1], "varDeclaration", maxSplit[7], maxSplit[3], null, null);
		} else {
			
			return new Instruction(maxSplit[0], maxSplit[1], "varDeclaration", maxSplit[7], null, maxSplit[3], maxSplit[5]);
		}
	}
	
	private static boolean isConstant(String str) {
		if (TokenLabeler.isNumeric(str)) {
			return true;
		} else {
			return false;
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
