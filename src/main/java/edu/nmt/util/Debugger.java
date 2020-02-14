package edu.nmt.util;

import java.util.Stack;

import edu.nmt.frontend.ActionType;

public class Debugger {
	
	private boolean isActive;
	
	public Debugger(boolean active) {
		this.isActive = active;
	}
	
	public void print(Object o) {
		if (this.isActive) {
			System.out.println(o);
		}
	}
	
	public void printStackTrace(Exception e) {
		if (this.isActive) {
			e.printStackTrace();
		}
	}
	
	public void printPhase(ActionType at) {
		if (this.isActive) {
			System.out.println("\n----------------------------------------------");
			switch (at) {
			case SHIFT:
			case REPEAT:
				System.out.println("SHIFT PHASE");
				break;
			case REDUCE:
				System.out.println("REDUCE PHASE");
				break;
			case REJECT:
				System.out.println("REJECTED");
				break;
			case ACCEPT:
				System.out.println("ACCEPTED");
				break;				
			}
			System.out.println("----------------------------------------------\n");	
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
