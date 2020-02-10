package edu.nmt.frontend;

import java.util.Arrays;
import java.util.Objects;

public class Rule {
	protected String leftSide;
    protected String[] rightSide;

    /**
     * Constructor for rules
     * @param leftSide
     * @param rightSide
     */
    public Rule(String leftSide, String[] rightSide) {
        this.rightSide = rightSide;
        this.leftSide = leftSide;
    }
    
    /**
     * Gets left side of rule
     * @return String
     */
    public String getLeftSide() {
        return leftSide;
    }

    /**
     * List of strings from right side
     * @return List of strings
     */
    public String[] getRightSide() {
        return rightSide;
    }
    
    /**
     * Creates a hash for the children
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.leftSide);
        hash = 29 * hash + Arrays.deepHashCode(this.rightSide);
        return hash;
    }

    /**
     * Allow for comparing of nodes
     * @return true if equivalent, else false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rule other = (Rule) obj;
        if (!Objects.equals(this.leftSide, other.leftSide)) {
            return false;
        }
        if (!Arrays.deepEquals(this.rightSide, other.rightSide)) {
            return false;
        }
        return true;
    }

    /**
     * Allow printing of Nodes and their children
     */
    @Override
    public String toString() {
        String str = leftSide + " -> ";
        for (int i = 0; i < rightSide.length; i++) {
            str += rightSide[i] + " ";
        }
        return str;
    }

}
