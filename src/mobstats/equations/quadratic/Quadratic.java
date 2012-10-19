package mobstats.equations.quadratic;

import mobstats.equations.StatSolver;

/**
 * Represents a StatSolver that solves using a quadratic equation.
 * 
 * Quadratic equation is in the form of ⨍(x) = ax^2 + bx + c. It creates a parabola for a graph and the points at which y is zero can be found using quadratic formula.
 * By putting a as 0, a linear equation is created which has a straight line where two consecutive integer values for x put through the equation will have a difference of b.
 * The equation will always be c when x is 0.
 * Setting a and b as 0 will always give the same result.
 * The vertex is the point with the y value that can only be reached with one value for x.
 * For positive values of a, the vertex has the lowest possible y value for the equation and for negative values of a, the vertex has the highest possible y value for the equation.
 * When b is zero,the vertex has an x value of zero and a y value of c.
 * 
 * @author Justin Stauch
 * @since March 29, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class Quadratic implements StatSolver {
    private double a, b, c, max, min;
    
    /**
     * Creates a knew Quadratic formula with the given values.
     * 
     * @param a The a value of the equation.
     * @param b The b value of the equation.
     * @param c The c value of the equation.
     * @param max The maximum value.
     * @param min The minimum value.
     */
    public Quadratic(double a, double b, double c, double max, double min) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.max = max;
        this.min = min;
    }
    
    /**
     * Sets the equation to the given values.
     * 
     * @param a The a value of the equation.
     * @param b The b value of the equation.
     * @param c The c value of the equation.
     */
    public StatSolver setEquation(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
        return this;
    }
    
    /**
     * Solves the equation.
     * 
     * @param x The value to use as x.
     * @return The solution to the equation with the given value of x.
     */
    @Override
    public double solve(double x, double def) {
        double result = a*(x*x) + b*x + c;
        return result < min ? min : result > max ? max : result;
    }
}