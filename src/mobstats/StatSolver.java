package mobstats;

/**
 * Used to solve for certain stats. Can be turned into different things for different types of equations.
 * 
 * @author Justin Stauch
 * @since March 29, 2012
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public interface StatSolver {
    
    public double solve(double x, double def);
}
