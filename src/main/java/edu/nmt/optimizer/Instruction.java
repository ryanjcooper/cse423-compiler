package edu.nmt.optimizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.nmt.frontend.*;
import edu.nmt.frontend.scanner.TokenLabeler;

/**
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
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Instruction getOperand1() {
		return operand1;
	}

	public void setOperand1(Instruction operand1) {
		this.operand1 = operand1;
	}

	public String getOp1Name() {
		return op1Name;
	}

	public void setOp1Name(String op1Name) {
		this.op1Name = op1Name;
	}

	public Instruction getOperand2() {
		return operand2;
	}

	public void setOperand2(Instruction operand2) {
		this.operand2 = operand2;
	}

	public List<String> getSpecialInstructions() {
		return specialInstructions;
	}

	public void setSpecialInstructions(List<String> specialInstructions) {
		this.specialInstructions = specialInstructions;
	}

	public Instruction(String instrType, Node node, List<Instruction> operandList, Integer lineNumber) {
		String label = null;
		if (node != null) {
			label = node.getToken().getTokenLabel();
		} else if (instrType.contentEquals("jump")) {
			label = "jump";
		}
		// first condition handles non-special instructions
		if (!this.specialInstructions.contains(instrType)) {
			this.lineNumber = lineNumber;
			this.instrID = "_" + lineNumber;
			this.type = node.getType();
			
			if (label.contentEquals("label")) {

				if (node.getName() == null) {
					this.op1Name = node.getToken().getTokenString();
				} else {
					this.op1Name = node.getName();
				}

				this.operation = this.type = "label";
			} else if (label.contentEquals("param")) {
				this.instrID = node.getName();
				this.operation = "funcParam";
			} else if (label.contentEquals("identifier")) {
				this.operation = label;
				this.op1Name = node.getToken().getTokenString();
				this.type = node.getScope().get(node.getName()).getType();
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
					if (node.getToken().getTokenLabel().contentEquals("boolExpr")) {
						this.type = "boolean";
					}
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
				if (label.contentEquals("ifStmt") || label.contentEquals("loopBody")) {
					this.type = "conditionalJump";
					this.operand1 = operandList.get(0);
					this.operand2 = operandList.get(1);
				} else {
					this.type = "unconditionalJump";
					this.operand1 = null;
					this.operand2 = operandList.get(0);
				}
			} else if (instrType.contentEquals("call")) {
				this.instrID = this.op1Name = node.getToken().getTokenString();
				this.type = node.getScope().get(node.getName()).getType();
			}
		}
		
		
	}
	
	public Instruction(Integer ln, String id, String op, String typ, Instruction op1, String name1, Instruction op2) {
		this.lineNumber = ln;
		this.instrID = id;
		this.operation = op;
		this.type = typ;
		this.operand1 = op1;
		this.op1Name = name1;
		this.operand2 = op2;
	}
	
	public Instruction() {
		
	}
	
	/**
	 * copy of constructor for already existing object
	 * @param ln is the line number
	 * @param id is the instruction id
	 * @param op is the operation
	 * @param typ is the type
	 * @param op1 is the first operand of the instruction
	 * @param name1 is the name stored for operand 1
	 * @param op2 is the second operand of the instruction
	 */
	public void Init(Integer ln, String id, String op, String typ, Instruction op1, String name1, Instruction op2) {
		this.lineNumber = ln;
		this.instrID = id;
		this.operation = op;
		this.type = typ;
		this.operand1 = op1;
		this.op1Name = name1;
		this.operand2 = op2;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public boolean equals(Instruction i2) {
		if (!(this.instrID == i2.instrID || this.instrID.equals(i2.instrID))) {
			System.out.println("Instruction id do not match");
			return false;
		}
		
		if (!(this.lineNumber == i2.lineNumber)) {
			System.out.println("Line numbers do not match");
			return false;
		}
		
		if (!(this.operation == i2.operation || this.operation.equals(i2.operation))) {
			System.out.println("Operations do not match");
			System.out.println(this.operation);
			System.out.println(i2.operation);
			return false;
		}
		
		if (!(this.type == i2.type || this.type.equals(i2.type))) {
			System.out.println("Types do not match");
			System.out.println(this.type);
			System.out.println(i2.type);
			return false;
		}		
		
		if (!(this.op1Name == i2.op1Name || this.op1Name.equals(i2.op1Name))) {
			System.out.println("Op1 name do not match");
			return false;
		}
		
		if (!(this.operand1 == i2.operand1 || this.operand1.equals(i2.operand1))) {
			System.out.println("Operand 1 do not match");
			return false;
		}
		
		if (!(this.operand2 == i2.operand2 || this.operand2.equals(i2.operand2))) {
			System.out.println("Operand 2 do not match");
			return false;
		}
		
		return true;
	}
	
	public void copy(Instruction i2) {
		this.instrID = i2.instrID;
		this.lineNumber = i2.lineNumber;
		this.type = i2.type;
		this.operand1 = i2.operand1;
		this.operand2 = i2.operand2;
		this.operation = i2.operation;
		this.op1Name = i2.op1Name;
	}
	
	/**
	 * converts a line of an IR output to an object
	 * @param line is the string to convert
	 * @param op1 is the first operand
	 * @param op2 is the second operand
	 * @return Instruction object representation of line
	 */
	public static Instruction strToInstr(String line, Instruction op1, Instruction op2) {
		String[] lineSplit = line.split(" ");
		
		/* convert string nulls to actual nulls */
		for (int i = 0; i < lineSplit.length; i++) {
			if (lineSplit[i].trim().equals("null")) {
				lineSplit[i] = null;
			}
		}
		
		return new Instruction(Integer.parseInt(lineSplit[0]), lineSplit[1], lineSplit[2], lineSplit[3], op1, lineSplit[5], op2);
	}
	
	public String instrToStr() {
		String op1 = null;
		String op2 = null;
		
		if (operand1 != null)
			op1 = operand1.getLineNumber().toString();
		
		if (operand2 != null)
			op2 = operand2.getLineNumber().toString();
	
		return lineNumber + " " + instrID + " " + operation + " " 
						  + type + " " + op1 + " " 
						  + op1Name + " " + op2 + " null";
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(instrID);
		if (operation != null && (operation.contentEquals("identifier") || operation.contains("constant") || operation.contentEquals("label"))) {
			builder.append(" = " + op1Name);
		} else if (operation != null && operation.contentEquals("=")) {
			builder.append(" = ");
			
			if (operand1 != null) {
				builder.append(operand1.getInstrID() + " ");
			}
		} else {
			builder.append(" =");
			if (operand1 == null && operand2 == null && operation == null) {
				builder.append(" null");
			} else if (operation != null && (operation.contentEquals("!") || operation.contentEquals("~"))) {
				builder.append(operation + operand1.getInstrID());
			} else {
				if (operand1 != null)
					builder.append(" " + operand1.getInstrID());
				
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
		if (this.instrID.contains("_")) {
			this.setInstrID("_" + lineNumber);
		}
	}
}
