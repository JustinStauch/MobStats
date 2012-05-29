package mobstats.equations.exponential;

/**
 * EMD stands for exponential multiplied by default. This takes an exponential equation and then multiplies it by the default value.
 * 
 * @author Justin Stauch
 * @since May 25, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class EMD extends Exponential {
    
    public EMD(double a, double b, double c, double d, double f) {
        super(a, b, c, d, f);
    }
    
    @Override
    public double solve(double x, double def) {
        return super.solve(x, def) * def;
    }
}
