/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

// Imports
import edu.snu.leader.spatial.SimulationState;


/**
 * SimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface SimObserver
{
    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @param keyPrefix Prefix for configuration property keys
     */
    public void initialize( SimulationState simState, String keyPrefix );

    /**
     * Prepares the simulation for execution
     */
    public void simSetup();

    /**
     * Prepares a simulation run for execution
     */
    public void simRunSetup();

    /**
     * Prepares a simulation step for execution
     */
    public void simRunStepSetup();

    /**
     * Performs any cleanup after a simulation run step has finished execution
     */
    public void simRunStepTearDown();

    /**
     * Performs any cleanup after a simulation run has finished execution
     */
    public void simRunTearDown();

    /**
     * Performs any cleanup after the simulation has finished execution
     */
    public void simTearDown();

    /**
     * Performs any processing necessary to handle an agent making a decision
     *
     * @param agent The agent making the decision
     * @param event The decision
     */
    public void agentDecided( Agent agent, DecisionEvent event );

    /**
     * Performs any processing necessary to handle a personality update
     *
     * @param event The personality update
     */
    public void personalityUpdated( PersonalityUpdateEvent event );

    /**
     * Performs any processing necessary to handle the simulation halting
     *
     * @param reason The reason for the halt
     */
    public void simulationRunHalted( SimulationRunHaltReason reason );
}
