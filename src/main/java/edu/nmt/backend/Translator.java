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
import edu.nmt.optimizer.CodeOptimizations;
import edu.nmt.optimizer.IR;
import edu.nmt.optimizer.Instruction;

public class Translator {

	private Map<String, Integer> typeSizes = new HashMap<String, Integer>();
	private Map<String, String> jumpLabels = new HashMap<String, String>();
 	
	{
		typeSizes.put("int", 4);	
		typeSizes.put("boolean", 4);
	}
	
	private IR ir;
	private ASTParser a;

	
	public Translator(IR ir, ASTParser a) {
		this.ir = ir;
		this.a = a;
//		this.asm = new ArrayList<String>();
	}
	
	public Integer getNextBaseOffset(Map<String, Integer> variableOffsets) {
		
		try {
			// Max offset.
			return Collections.min(variableOffsets.values());
		} catch (java.util.NoSuchElementException e) {
			return 0;
		}		
	}
	
	public String getSizeModifier(Integer size) {
		if (size == null)
			size = 4;
		
		switch (size) {
		case 4: 
			return "l";
		case 8:
			return "q";
		}
		return "l";
	}
	
	public String getRegisterModifier(Integer size) {
		switch (size) {
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
			asm.add(".LFB" + funcIndex + ":\n");
			asm.add("\t.cfi_startproc\n");
			asm.add("\tpushq	%rbp\n");
			asm.add("\t.cfi_def_cfa_offset 16\n");
			asm.add("\t.cfi_offset 6, -16\n");
			asm.add("\tmovq	%rsp, %rbp\n");
			asm.add("\t.cfi_def_cfa_register 6\n");
			
			// adjust stack pointer for necessary locals
			
			
//			asm.add("\tsubq	$16, %rsp\n");
//			asm.add("\tmovl	$7, -4(%rbp)\n");
//			asm.add("\tmovl	-4(%rbp), %eax\n");

			
			
			List<Instruction> funcInstr = ir.getFunctionIRs().get(funcName);
			
			for (Instruction inst : funcInstr) {
			
				System.out.println(inst);
				
				// since this is already linearized, just simply translate Instruction object to corresponding assembly command(s)
				if (inst.getOperation() == null) {
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					String instrValue = inst.getOperand1().getInstrID();
					String regModifier = getRegisterModifier(variableSizes.get(instrValue));
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					
					asm.add("\tmov" + sizeModifier + "\t" + variableOffsets.get(instrValue) + "(%rbp), %" + regModifier + "bx\n");
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("=")) {
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					String instrValue = inst.getInstrID();
					String regModifier = getRegisterModifier(variableSizes.get(instrValue));
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					
					asm.add("\tmov" + sizeModifier + "\t" + variableOffsets.get(instrValue) + "(%rbp), %" + regModifier + "bx\n");
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
				} else if (inst.getOperation().equals("numeric_constant")) {
					
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);

					asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t$" + inst.getOp1Name() + ", " + offset + "(%rbp)\n");
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
					
					System.out.println(returnValueSize);
					
					
					asm.add("\tmov" + getSizeModifier(typeSizes.get(inst.getType())) + "\t" + offset + "(%rbp), %" + getRegisterModifier(returnValueSize) + "ax\n");					
				} else if (inst.getOperation().equals("+") || inst.getOperation().equals("-") || inst.getOperation().equals("&") 
														   || inst.getOperation().equals("|") || inst.getOperation().equals("^")
														   || inst.getOperation().equals("<<") || inst.getOperation().equals(">>")) {	
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					/* move first operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					offset = variableOffsets.get(instrValue2);
					
					if (inst.getOperation().equals("+"))
						/* add second operand to register */
						asm.add("\tadd" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else if (inst.getOperation().equals("-"))
						/* sub second operand to register */
						asm.add("\tsub" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else if (inst.getOperation().equals("&"))
						/* and second operand to register */
						asm.add("\tand" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else if (inst.getOperation().equals("|"))
						/* or second operand to register */
						asm.add("\tor" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else if (inst.getOperation().equals("^"))
						/* xor second operand to register */
						asm.add("\txor" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else if (inst.getOperation().equals("<<"))
						/* left shift second operand to register */
						asm.add("\tshl" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					else if (inst.getOperation().equals(">>"))
						/* right shift second operand to register */
						asm.add("\tshr" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");			
					
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);;
					
					/* move register value to stack */
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("identifier")) {
					/* assigning the value of an identifier to a variable */
					System.out.println(inst.getOp1Name());
					String id = inst.getOp1Name();
					String instrValue = inst.getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(id));
					Integer idOffset = variableOffsets.get(id);
					Integer offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					System.out.println("identifier");
					System.out.println(id);
					System.out.println(idOffset);					
					
					/* move register value to stack */
					asm.add("\tmov" + sizeModifier + "\t" + idOffset + "(%rbp), %" + regModifier + "bx\n");
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "bx, " + offset + "(%rbp)\n");
					
					variableOffsets.put(instrValue, offset);
					variableSizes.put(instrValue, typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("*")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					/* move first operand into ax */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "ax\n");
					
					offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					/* empty rdx */
					asm.add("\tmov" + sizeModifier + "\t$0, %" + regModifier + "dx\n");
					
					/* multiply*/
					asm.add("\timul\t%" + regModifier + "bx\n");
					
					/* move result into dest */
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "ax, " + offset + "(%rbp)\n");
					
					variableOffsets.put(inst.getInstrID(), offset);
					variableSizes.put(inst.getInstrID(), typeSizes.get(inst.getType()));
				} else if (inst.getOperation().equals("/") || inst.getOperation().equals("%")) {
					String instrValue1 = inst.getOperand1().getInstrID();
					String instrValue2 = inst.getOperand2().getInstrID();
					String sizeModifier = getSizeModifier(typeSizes.get(inst.getType()));
					String regModifier = getRegisterModifier(variableSizes.get(instrValue2));
					Integer offset = variableOffsets.get(instrValue1);
					
					/* move first operand into ax */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "ax\n");
					
					offset = variableOffsets.get(instrValue2);
					
					/* move second operand into register */
					asm.add("\tmov" + sizeModifier + "\t" + offset + "(%rbp), %" + regModifier + "bx\n");
					
					/* empty rdx */
					asm.add("\tmov" + sizeModifier + "\t$0, %" + regModifier + "dx\n");
					
					/* divide */
					asm.add("\tidiv\t%" + regModifier + "bx\n");
					
					/* move result into dest */
					offset = getNextBaseOffset(variableOffsets) + (typeSizes.get(inst.getType()) * -1);
					
					/* div uses ax, mod uses dx */
					if (inst.getOperation().equals("/"))
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "ax, " + offset + "(%rbp)\n");
					else
						asm.add("\tmov" + sizeModifier + "\t%" + regModifier + "dx, " + offset + "(%rbp)\n");
					
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
							asm.add("\tadd" + sizeModifier + "\t$1," + regModifier + "bx\n");
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
							asm.add("\tsub" + sizeModifier + "\t$1," + regModifier + "bx\n");
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
						jumpLabels.put(inst.getInstrID(), inst.getInstrID() + inst.getOp1Name());
					}
					
					asm.add(jumpLabels.get(inst.getInstrID()) + ":\n");	
				}
			}
			
			// function footer
			asm.add("\tpopq	%rbp\n");
			asm.add("\t.cfi_def_cfa 7, 8\n");
			asm.add("\tret\n");
			asm.add("\t.cfi_endproc\n");			
		

			funcAsm.put(funcName, asm);
			fileAsm.addAll(asm);
		}
		
		System.out.println("\n");
		for(String s : fileAsm) {
			System.out.print(s);
		}
		
		
		
	}
	
	
	public static void main(String argv[]) throws IOException {
		Scanner s = new Scanner("test/test.c");
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
    	}
	
		
	}
	
}
