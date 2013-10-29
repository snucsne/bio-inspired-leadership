/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

/**
 * Reporter
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface Reporter
{
    /**
     * Initializes this reporter
     *
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState );

    /**
     * Report the final results of the simulation
     */
    public void reportFinalResults();

}
