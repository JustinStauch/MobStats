package mobstats.equations.quadratic;

/**
 * QMD stands for Quadratic multiplied by default. This works like a normal quadratic but it multiplies the result by the default value.
 * 
 * @author Justin Stauch
 * @since May 25, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class QMD extends Quadratic {
    
    /**
     * Creates a new QMD with the given values.
     * 
     * @param a The a value to use.
     * @param b The b value to use.
     * @param c The c value to use.
     * @param max The maximum value.
     * @param min The minimum value.
     */
    public QMD(double a, double b, double c, double max, double min) {
        super(a, b, c, max, min);
    }
    
    /**
     * Solves the equation using x and the default value.
     * 
     * @param x The value to solve with.
     * @param def The default value of what is being solved.
     * @return The solved value.
     */
    @Override
    public double solve(double x, double def) {
        return super.solve(x, def) * def;
    }
}
