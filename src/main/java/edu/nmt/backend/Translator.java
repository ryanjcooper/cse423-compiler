package edu.nmt.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nmt.RuntimeSettings;
import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;
import edu.nmt.optimizer.CallInstruction;
import edu.nmt.optimizer.IR;
import edu.nmt.optimizer.Instruction;

public class Translator {

	private Map<String, Integer> typeSizes = new HashMap<String, Integer>();
	private Map<String, String> jumpLabels = new HashMap<String, String>();
 	
	{
		typeSizes.put("int", 4);	
		typeSizes.put("boolean", 4);
		typeSizes.put("char", 1);
		typeSizes.put("void", 4);
	}
	
	private IR ir;
	private ASTParser a;
	private List<String> fileAsm;

	
	public Translator(IR ir, ASTParser a) {
		this.ir = ir;
		this.a = a;
//		this.asm = new ArrayList<String>();
	}
	
	public Integer getNextBaseOffset(Map<String, Integer> variableOffsets) {
		
		try {
			// Max offset.
			int min = Collections.min(variableOffsets.values());
			return min >= 0 ? 0 : min;
		} catch (java.util.NoSuchElementException e) {
			return 0;
		}		
	}
	
	public Integer getPositiveBaseOffset(Map<String, Integer> variableOffsets) {
		
		try {
			// Max positive offset.
			return Collections.max(variableOffsets.values());
		} catch (java.util.NoSuchElementException e) {
			return 0;
		}		
	}
	
	public String getSizeModifier(Integer size) {
		if (size == null)
			size = 4;
		
		switch (size) {
		case 1:
			return "b";
		case 4: 
			return "l";
		case 8:
			return "q";
		}
		return "l";
	}
	
	public String getRegisterModifier(Integer size) {
		switch (size) {
		case 1: 
			return "";
		case 4: 
			return "e";
		case 8:
			return "r";
		}
		return null;
		
	}
	
	public String getShiftModifier(Integer size) {
		switch (size) {
		case 4: 
			return "$0x1F";
		case 8:
			return "$0x3F";
		}
		return null;
		
	}
	
	public void translate() {
		
		Integer funcIndex = 0;
		Map<String, Integer> variableOffsets;
		Map<String, Integer> variableSizes;
		Map<String, Node> funcScope;
		List<String> asm;
		Map<String, List<String>> funcAsm = new HashMap<String, List<String>>();
		List<String> fileAsm = new ArrayList<String>();
		
		// file header
		fileAsm.add("\t.file\t\"" + RuntimeSettings.sourceFilename + "\"\n");
		fileAsm.add("\t.text\n");
		fileAsm.add("\t.globl	main\n");
		fileAsm.add("\t.type	main, @function\n");		
	
		
		// translate each function
		for (String funcName : ir.getFunctionIRs().keySet()) {		
			asm = new ArrayList<String>();
			funcScope = a.getFunctionSymbolTable(funcName);
			
			variableOffsets = new HashMap<String, Integer>();
			variableSizes = new HashMap<String, Integer>();
			
			// function label
			asm.add(funcName + ":\n");
			
			// function preamble
			asm.add(".LFB" + funcIndex++ + ":\n");
			asm.add("\tpushq	%rbp\n");
			asm.add("\tmovq	%rsp, %rbp\n");
			
			// adjust stack pointer for necessary locals
			
//			asm.add("\tsubq	$16, %rsp\n");
//			asm.add("\tmovl	$7, -4(%rbp)\n");
//			asm.add("\tmovl	-4(%rbp), %eax\n");

			
			
			List<Instruction> funcInstr = ir.getFunctionIRs().get(funcName);
			
			for (Instruction inst : funcInstr) {
			
//				System.out.println(inst.getOperation() + " goes to: " + inst);
				
				// since this is already linearized, just simply translate Instruction object to corresponding assembly command(s)
				if (inst.getOperation() == null) {
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					String instrValue = null;
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					if(inst.getOperand1() == null) {
						// if var is declared without initializing, default value to 0
						asm.add("\tmov" + sizeModifier + "\t$0, " + offset + "(%rbp)\n");
					} else {
						// if var is declared and initialized
						instrValue = inst.getOperand1().getInstrID();
						
						String regModifier = getRegisterModifier(typeSizes.get(inst.getOperand1().getType()));
						String regSuffix = "";
						String regSuffix1 = "";
						
						if (inst.getType().equals("char")) {
							regSuffix = "bl";
							regSuffix1 = "al";
						} else {
							regSuffix = "bx";
							regSuffix1 = "ax";
						}
						
						if(inst.getOperand1() instanceof CallInstruction) {
							// move result of function call into memory
							Integer offset2 = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getOperand1().getType()) * - 1);
							asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix1 + ", " + offset2 + "(%rbp)\n");
							
							variableOffsets.put(inst.getOperand1().getOp1Name(), offset);
							variableSizes.put(inst.getOperand1().getOp1Name(), typeSizes.get(inst.getType()));
						}

						asm.add("\tmov" + sizeModifier + "\t" + variableOffsets.get(instrValue) + "(%rbp), %" + regModifier + regSuffix + "\n");
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix + ", " + offset + "(%rbp)\n");
						asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					}
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));

				} else if (inst.getOperation().equals("=")) {
					String instrValue1 = inst.getInstrID();
					String instrValue2 = inst.getOperand1().getInstrID();
					String regModifier = getRegisterModifier(variableSizes.get(instrValue1));
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					
					String regSuffix = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "al";
					} else {
						regSuffix = "ax";
					}		
					
					asm.add("\tmov" + sizeModifier + "\t" + variableOffsets.get(instrValue2) + "(%rbp), %" + regModifier + regSuffix + "\n");
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix + ", " + variableOffsets.get(instrValue1) + "(%rbp)\n");
					
				} else if (inst.getOperation().equals("numeric_constant") || inst.getOperation().equals("char_constant")) {
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);

					if (inst.getType() != null && inst.getType().equals("char")) {
						asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t$" + (int) inst.getOp1Name().charAt(1) + ", " + offset + "(%rbp)\n");
					} else {
						asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t$" + inst.getOp1Name() + ", " + offset + "(%rbp)\n");
					}
					
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
//					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
					
					
//					System.out.println("HERE: " + inst.getInstrID());
					
//					System.out.println(inst.getType());
//					System.out.println(typeSizes.get(inst.getType()));

					
				} else if (inst.getOperation().equals("return")) {					
					String returnValueName = inst.getOp1Name();
					Integer returnValueSize = variableSizes.get(returnValueName);

					Integer offset = variableOffsets.get(returnValueName);				
					String regSuffix = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "al";
					} else {
						regSuffix = "ax";
					}					
					
					asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t" + offset + "(%rbp), %" + getRegisterModifier(returnValueSize) + regSuffix + "\n");					
				} else if (inst.getOperation().equals("+") || inst.getOperation().equals("-") || inst.getOperation().equals("&") 
														   || inst.getOperation().equals("|") || inst.getOperation().equals("^")
														   || inst.getOperation().equals("<<") || inst.getOperation().equals(">>")) {	
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					System.out.println(instrValue2);
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					String regSuffix = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "bl";
					} else {
						regSuffix = "bx";
					}
					
					/* move first operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					
					offset = variableOffsets.get(instrValue2);
					
					if (inst.getOperation().equals("+"))
						/* add second operand to register */
						asm.add("\tadd" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					else if (inst.getOperation().equals("-"))
						/* sub second operand to register */
						asm.add("\tsub" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					else if (inst.getOperation().equals("&"))
						/* and second operand to register */
						asm.add("\tand" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					else if (inst.getOperation().equals("|"))
						/* or second operand to register */
						asm.add("\tor" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					else if (inst.getOperation().equals("^"))
						/* xor second operand to register */
						asm.add("\txor" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					else if (inst.getOperation().equals("<<"))
						/* left shift second operand to register */
						asm.add("\tshl" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					else if (inst.getOperation().equals(">>"))
						/* right shift second operand to register */
						asm.add("\tshr" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");			
					
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);;
					
					/* move register value to stack */
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix + ", " + offset + "(%rbp)\n");
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("identifier")) {
					/* assigning the value of an identifier to a variable */
					String id = inst.getOp1Name();
					String instrValue = inst.getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(id));
					Integer idOffset = variableOffsets.get(id);
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);			
					
					String regSuffix = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "bl";
					} else {
						regSuffix = "bx";
					}
					
					/* move register value to stack */
					asm.add("\tmov" + sizeModifier + "\t" + idOffset + "(%rbp), %" + regModifier + regSuffix + "\n");
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix + ", " + offset + "(%rbp)\n");
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					
					variableOffsets.put(instrValue, offset);
					variableSizes.put(instrValue, typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("*")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					String regSuffix = "";
					String regSuffix1 = "";
					String regSuffix2 = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "bl";
						regSuffix1 = "al";
						regSuffix2 = "dl";
					} else {
						regSuffix = "bx";
						regSuffix1 = "ax";
						regSuffix2 = "dx";
					}
					
					/* move first operand into ax */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix1 + "\n");
					
					offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					
					/* empty rdx */
					asm.add("\tmov" + sizeModifier + "\t$0, %" + regModifier + regSuffix2 + "\n");
					
					/* multiply*/
					asm.add("\timul\t%" + regModifier + regSuffix + "\n");
					
					/* move result into dest */
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix1 + ", " + offset + "(%rbp)\n");
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("/") || inst.getOperation().equals("%")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);

					String regSuffix = "";
					String regSuffix1 = "";
					String regSuffix2 = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "bl";
						regSuffix1 = "al";
						regSuffix2 = "dl";
					} else {
						regSuffix = "bx";
						regSuffix1 = "ax";
						regSuffix2 = "dx";
					}
					
					
					/* move first operand into ax */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix1 + "\n");
					
					offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + regSuffix + "\n");
					
					/* empty rdx */
					asm.add("\tmov" + sizeModifier + "\t$0, %" + regModifier + regSuffix2 + "\n");
					
					/* divide */
					asm.add("\tidiv\t%" + regModifier + regSuffix + "\n");
					
					/* move result into dest */
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					/* div uses ax, mod uses dx */
					if (inst.getOperation().equals("/"))
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix1 + ", " + offset + "(%rbp)\n");
					else
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix2 + ", " + offset + "(%rbp)\n");
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
					
				} else if (inst.getType().equals("boolean")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getOperand1().getType()));
					String shiftModifier = getShiftModifier(typeSizes.get(inst.getOperand1().getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					/* && and || assume both operand values are either 1 or 0 */
					if (inst.getOperation().equals("&&")) {
						offset = variableOffsets.get(instrValue1);
						asm.add("\tand" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
						asm.add("\tand" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
						offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
					} else if (inst.getOperation().equals("||")) {
						offset = variableOffsets.get(instrValue1);
						asm.add("\tor" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
						asm.add("\tand" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
						offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");						
					} else {
						/* x > y is converted to x >= y + 1 */
						if (inst.getOperation().equals(">")) {
							asm.add("\tadd" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
							inst.setOperation(">=");
						}
						
						/* flip with value with twos complement */
						asm.add("\tnot" + sizeModifier + "\t%" + regModifier + "bx\n");
						asm.add("\tadd" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
						
						offset = variableOffsets.get(instrValue1);
						
						/* add the first operand to the register */
						asm.add("\tadd" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
						
						/* x <= y is converted to x - 1 < y */
						if (inst.getOperation().equals("<=")) {
							asm.add("\tsub" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
							inst.setOperation("<");
						}
						
						offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getOperand1().getType()) * -1);
						
						if (inst.getOperation().equals("==")) {
							/* use ax as a tmp register */
							asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, %" + regModifier + "ax\n");
							asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "ax, %" + regModifier + "bx\n");
							asm.add("\tshr" + sizeModifier + "\t" + shiftModifier + ", %" + regModifier + "bx\n");
							asm.add("\tnot" + sizeModifier + "\t%" + regModifier + "ax\n");
							asm.add("\tadd" + sizeModifier + "\t$1, %" + regModifier + "ax\n");
							asm.add("\tshr" + sizeModifier + "\t" + shiftModifier + ", %" + regModifier + "ax\n");
							asm.add("\tor" + sizeModifier + "\t%" + regModifier + "bx, %" + regModifier + "ax\n");
							asm.add("\tadd" + sizeModifier + "\t$1, %" + regModifier + "ax\n");
							asm.add("\tmov" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
							asm.add("\tnot" + sizeModifier + "\t%" + regModifier + "bx\n");
							asm.add("\txor" + sizeModifier + "\t%" + regModifier + "bx, %" + regModifier + "ax\n");
							asm.add("\tand" + sizeModifier + "\t$1, %" + regModifier + "ax\n");
							asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "ax, " + offset + "(%rbp)\n");
						} else if (inst.getOperation().equals("<")) {
							asm.add("\tshr" + sizeModifier + "\t" + shiftModifier + ", %" + regModifier + "bx\n");
							asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
						} else if (inst.getOperation().equals(">=")) {
							asm.add("\tshr" + sizeModifier + "\t" + shiftModifier + ", %" + regModifier + "bx\n");
							asm.add("\tnot" + sizeModifier + "\t%" + regModifier + "bx\n");
							asm.add("\tand" + sizeModifier + "\t$1, %" + regModifier + "bx\n");
							asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
						}				
					}
					
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getType().equals("conditionalJump")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					Integer offset = variableOffsets.get(instrValue1);
					
					jumpLabels.put(instrValue2, instrValue2 + "conditionalJump");
					asm.add("\tcmp" + sizeModifier + "\t$0, " + offset + "(%rbp)\n");
					
					if (inst.getOp1Name().equals("false")) {
						asm.add("\tje\t" + jumpLabels.get(instrValue2) + "\n");
					} else {
						asm.add("\tjne\t" + jumpLabels.get(instrValue2) + "\n");
					}
				} else if (inst.getType().equals("label")) {
					if (jumpLabels.get(inst.getInstrID()) == null) {
						jumpLabels.put(inst.getInstrID(), inst.getInstrID() + "conditionalJump");
					}
					
					asm.add(jumpLabels.get(inst.getInstrID()) + ":\n");	
				} else if (inst.getOperation().equals("call")) {
					Integer totalParamOffset = 0;
					
					
					CallInstruction call = (CallInstruction) inst;
					// push parameters specified by the callInstruction to stack in right-to-left order
					Collections.reverse(call.getParamList());
					for (Instruction i : call.getParamList()) {
						String instrValue = i.getInstrID();
						String regModifier = getRegisterModifier(typeSizes.get(i.getType()));
						String sizeModifier = getSizeModifier(typeSizes.get(i.getType()));
						Integer offset2 = variableOffsets.get(instrValue);
						Integer offset3 = getNextBaseOffset(variableOffsets) + (typeSizes.get(i.getType()) * -1);
						totalParamOffset += (typeSizes.get(i.getType()));
						
						String regSuffix = "";
						
						if (i.getType().equals("char")) {
							regSuffix = "cl";
						} else {
							regSuffix = "cx";
						}
						
						
//						asm.add("\tpushq\t" + offset2 + "(%rbp)\n");
						asm.add("\tmov" + sizeModifier + "\t" + offset2 + "(%rbp), %" + regModifier + regSuffix + "\n");
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix + ", " + offset3 + "(%rbp)\n");
						asm.add("\tsubq\t$" + typeSizes.get(i.getType()) + ", %rsp\n");
						
						variableOffsets.put(i.getInstrID() + "Param", offset3);
						variableSizes.put(i.getInstrID() + "Param", typeSizes.get(inst.getType()));
					}
					Collections.reverse(call.getParamList());
					
					// call <functionLabel>
					asm.add("\tcall\t" + inst.getOp1Name() + "\n");
					
					// clean up stack after the call (add offset based on number of params to "pop" all pushed params)
					asm.add("\taddl\t$" + (totalParamOffset + 8) + ", %esp\n");
					
				} else if (inst.getOperation().equals("funcParam")) {
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					Integer paramOffset = getPositiveBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()));
					
					String regModifier = getRegisterModifier(typeSizes.get(inst.getType()));
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					
					String regSuffix = "";
					
					if (inst.getType().equals("char")) {
						regSuffix = "bl";
					} else {
						regSuffix = "bx";
					}
					
					asm.add("\tmov" + sizeModifier + "\t" + (paramOffset + 12) + "(%rbp), %" + regModifier + regSuffix + "\n");
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + regSuffix + ", " + offset + "(%rbp)\n");
					asm.add("\tsubq\t$" + typeSizes.get(inst.getType()) + ", %rsp\n");
					
					variableOffsets.put(inst.getInstrID() + "Param", paramOffset);
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				}  else if(inst.getType().equals("unconditionalJump")) {
					String instrValue2 = inst.getOperand2().getInstrID();
					jumpLabels.put(instrValue2, instrValue2 + "unconditionalJump");
					asm.add("\tjmp" + "\t" + jumpLabels.get(instrValue2) + "\n");
				}
			}
			
			// function footer
			if (funcName.equals("main")) {
				asm.add("\tmovq\t%rax, %rdi\n");
				asm.add("\tmovq\t$60, %rax\n");
				asm.add("\tsyscall\n");
			} else {
				asm.add("\tmovq	%rbp, %rsp\n");
				asm.add("\tpopq	%rbp\n");
				asm.add("\tret\n");	
			}
		

			funcAsm.put(funcName, asm);
			fileAsm.addAll(asm);
		}
		
		this.fileAsm = fileAsm;
	}
	
	
	public static void main(String argv[]) throws IOException {
		Scanner s = new Scanner("test/test2.c");
    	s.scan();
    
//		s.printTokens();

    	// Initialize grammar
    	Grammar grammar = new Grammar(RuntimeSettings.grammarFile);
    	grammar.loadGrammar();
    	
    	// Start parser
    	Parser p = new Parser(grammar, s);  

    	if (p.parse()) {
    		
//    		p.printParseTree();
        	        	
        	// Start AST Parser
    		ASTParser a = new ASTParser(p);
    		
    		if (a.parse()) {
//   			a.printAST();
    			
	    		a.printSymbolTable();
    		}
    		
			IR ir = new IR(a);
			IR.printIR(ir);
			
			Translator t = new Translator(ir, a);
			t.translate();
			System.out.println(t.getAsmString());
    	}
	
		
	}

	public String getAsmString() {
		StringBuilder sb = new StringBuilder();
		
		for(String s : this.fileAsm) {
			sb.append(s);
		}
		sb.append("\n");
		
		return sb.toString();
	}
	
}
