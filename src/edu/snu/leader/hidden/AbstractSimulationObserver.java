/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

/**
 * AbstractSimulationObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractSimulationObserver implements SimulationObserver
{
    /** The simulation state */
    protected SimulationState _simState = null;


    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hidden.SimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Save the simulation state
        _simState = simState;
    }

    /**
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.hidden.SimulationObserver#simSetUp()
     */
    @Override
    public void simSetUp()
    {
        // Do nothing
    }

    /**
     * Prepares a simulation run for execution
     *
     * @see edu.snu.leader.hidden.SimulationObserver#simRunSetUp()
     */
    @Override
    public void simRunSetUp()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.hidden.SimulationObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after the simulation has finished execution
     *
     * @see edu.snu.leader.hidden.SimulationObserver#simTearDown()
     */
    @Override
    public void simTearDown()
    {
        // Do nothing
    }

    /**
     * TODO Method description
     *
     * @see edu.snu.leader.hidden.SimulationObserver#describeResults()
     */
    @Override
    public void describeResults()
    {
        // Do nothing
    }

}
