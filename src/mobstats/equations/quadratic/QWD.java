package mobstats.equations.quadratic;

/**
 * QWD stands for Quadratic with defaults. It is simillar to regular quadratics but it has the support to multiply in the default values for each calculations.
 * 
 * @author Justin Stauch
 * @since May 25, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class QWD extends Quadratic {
    private double a, b, c, max, min;
    private boolean aDef, bDef, cDef;
    
    /**
     * Creates a new QWD with the given values.
     * 
     * @param a The a value of the equation.
     * @param b The b value of the equation.
     * @param c The c value of the equation.
     * @param max The maximum value.
     * @param min The minimum value.
     * @param aDef Whether or not to use the default value for determining the a part of the equation.
     * @param bDef Whether or not to use the default value for determining the b part of the equation.
     * @param cDef Whether or not to use the default value for determining the c part of the equation.
     */
    public QWD(double a, double b, double c, double max, double min, boolean aDef, boolean bDef, boolean cDef) {
        super(a, b, c, max, min);
        this.a = a;
        this.b = b;
        this.c = c;
        this.max = max;
        this.min = min;
        this.aDef = aDef;
        this.bDef = bDef;
        this.cDef = cDef;
    }
    
    /**
     * Solves the equation using x and the given default value.
     * 
     * @param x The value to solve with.
     * @param def The default value to use.
     * @return The solved value.
     */
    @Override
    public double solve(double x, double def) {
        double aNew = a, bNew = b, cNew = c;
        if (aDef) aNew *= def;
        if (bDef) bNew *= def;
        if (cDef) cNew *= def;
        double result = (aNew * (x * x)) + (bNew * x) + cNew;
        return result < min ? min : result > max ? max : result;
    }
}