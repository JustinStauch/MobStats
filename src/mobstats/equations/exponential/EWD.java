/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobstats.equations.exponential;

/**
 * EWD stands for exponential with defaults. This is an exponential equation where each value can be multiplied or not by the items default value.
 * 
 * @author Justin Stauch
 * @since May 25, 2012
 * 
 * copyright 2012© Justin Stauch, All Rights Reserved
 */
public class EWD extends Exponential {
    private double a, b, c, d, f, max, min;
    private boolean aDef, bDef, cDef, dDef, fDef;
    
    public EWD(double a, double b, double c, double d, double f, double max, double min, boolean aDef, boolean bDef, boolean cDef, boolean dDef, boolean fDef) {
        super(a, b, c, d, f, max, min);
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.f = f;
        this.max = max;
        this.min = min;
        this.aDef = aDef;
        this.bDef = bDef;
        this.cDef = cDef;
        this.dDef = dDef;
        this.fDef = fDef;
    }
    
    @Override
    public double solve(double x, double def) {
        double aNew = a, bNew = b, cNew = c, dNew = d, fNew = f;
        if (aDef) aNew *= def;
        if (bDef) bNew *= def;
        if (cDef) cNew *= def;
        if (dDef) dNew *= def;
        if (fDef) fNew *= def;
        double powered = Math.pow(bNew, cNew * (x - dNew));
        double result = (aNew * powered) + fNew;
        return result < min ? min : result > max ? max : result;
    }
}
