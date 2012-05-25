package mobstats;

/**
 * Represents a StatSolver that solves using a quadratic equation.
 * 
 * Quadratic equation is in the form of ⨍(x) = ax^2 + bx + c. It creates a parabola for a graph and the points at which y is zero can be found using quadratic formula.
 * By putting a as 0, a linear equation is created which has a straight line where two consecutive integer values for x put through the equation will have a difference of b.
 * The equation will always be c when x is 0.
 * Setting a and b as 0 will always give the same result.
 * The vertex is the point that can only be reached with one value for x.
 * For positive values of a, the vertex has the lowest possible y value for the equation and for negative values of a, the vertex has the highest possible y value for the equation.
 * When b is zero,the vertex has an x value of zero and a y value of c.
 * 
 * @author Justin Stauch
 * @since March 29, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class Quadratic implements StatSolver {
    private double a, b, c;
    private boolean aDef, bDef, cDef
    
    /**
     * Creates a knew Quadratic formula with the given values.
     * 
     * @param a The a value of the equation.
     * @param b The b value of the equation.
     * @param c The c value of the equation.
     */
    public Quadratic(double a, double b, double c, boolean aDef, boolean bDef, boolean cDef) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.aDef = aDef;
        this.bDef = bDef;
        this.cDef = cDef;
    }
    
    /**
     * Sets the equation to the given values.
     * 
     * @param a The a value of the equation.
     * @param b The b value of the equation.
     * @param c The c value of the equation.
     */
    public void setEquation(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    /**
     * Gets the a value in the equation.
     * 
     * @return The a value in the equation.
     */
    public double getA() {
        return a;
    }
    
    /**
     * Gets the b value in the equation.
     * 
     * @return The b value in the equation.
     */
    public double getB() {
        return b;
    }
    
    /**
     * Gets the c value in the equation.
     * 
     * @return The c value in the equation.
     */
    public double getC() {
        return c;
    }
    
    /**
     * Solves the equation.
     * 
     * @param x The value to use as x.
     * @return The solution to the equation with the given value of x.
     */
    @Override
    public double solve(double x, double def) {
        double aVal, bVal, cVal;
        if (aDef) aVal = a*def*()
    }
    
}