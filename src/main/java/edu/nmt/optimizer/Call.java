package edu.nmt.optimizer;

import java.util.List;

public class Call extends Instruction {
	private Instruction funcLabel;
	private List<Instruction> paramList;
	private Instruction returnInstr;
}
