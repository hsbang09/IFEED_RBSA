/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rbsa.eoss.dm;

import java.util.BitSet;

/**
 * An abstract class for a feature that explains the data
 *
 * @author nozomihitomi
 */
public abstract class AbstractFeature implements Feature {
    /**
     * The bitset for the observations this feature matches
     */
    private final BitSet matches;
    
    private final double support;
    private final double lift;
    private final double fconfidence;
    private final double rconfidence;
    private final double distance2UP;

    public AbstractFeature(BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        this.matches = matches;
        this.support = support;
        this.lift = lift;
        this.fconfidence = fconfidence;
        this.rconfidence = rconfidence;
        this.distance2UP = Math.sqrt(Math.pow(1-fconfidence,2)+Math.pow(1-rconfidence,2));
    }
    
    public BitSet getMatches() {
        return matches;
    }
    
    @Override
    public double getSupport() {
        return support;
    }

    @Override
    public double getFConfidence() {
        return fconfidence;
    }

    @Override
    public double getRConfidence() {
        return rconfidence;
    }
    
    @Override
    public double getDistance2UP() {
        return distance2UP;
    }
    

    @Override
    public double getLift() {
        return lift;
    }

}
