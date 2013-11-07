/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

import edu.snu.leader.hidden.SimulationState;

/**
 * SimulationObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface SimulationObserver
{
    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState );

    /**
     * Prepares the simulation for execution
     */
    public void simSetUp();

    /**
     * Prepares a simulation run for execution
     */
    public void simRunSetUp();

    /**
     * Performs any cleanup after a simulation run has finished execution
     */
    public void simRunTearDown();

    /**
     * Performs any cleanup after the simulation has finished execution
     */
    public void simTearDown();

    /**
     * Describes any actions taken or results observed
     */
    public void describeResults();
}
