/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.observer;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.PersonalityUpdateEvent;
import edu.snu.leader.spatial.SimulationRunHaltReason;
import edu.snu.leader.spatial.SimulationState;
import org.apache.log4j.Logger;

/**
 * DebugSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DebugSimObserver extends AbstractSimObserver
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DebugSimObserver.class.getName() );


    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @param keyPrefix Prefix for configuration property keys
     * @see edu.snu.leader.spatial.SimObserver#initialize(edu.snu.leader.spatial.SimulationState, java.lang.String)
     */
    @Override
    public void initialize( SimulationState simState, String keyPrefix )
    {
        // Call the superclass instantiation
        super.initialize( simState, keyPrefix );

        _LOG.debug( "Initializing sim observer with key prefix ["
                + keyPrefix
                + "]" );
    }

    /**
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simSetup()
     */
    @Override
    public void simSetup()
    {
        _LOG.debug( "Simulation setup called" );
    }

    /**
     * Prepares a simulation step for execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunSetup()
     */
    @Override
    public void simRunSetup()
    {
        _LOG.debug( "Simulation run setup called: run=["
                + _simState.getCurrentSimulationRun()
                + "]" );
    }

    /**
     * Prepares a simulation step for execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunStepSetup()
     */
    @Override
    public void simRunStepSetup()
    {
        _LOG.debug( "Simulation run step setup called: run=["
                + _simState.getCurrentSimulationRun()
                + "] step=["
                + _simState.getCurrentSimulationStep()
                + "]" );
    }

    /**
     * Performs any cleanup after a simulation run step has finished execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunStepTearDown()
     */
    @Override
    public void simRunStepTearDown()
    {
        _LOG.debug( "Simulation run step tear down called: run=["
                + _simState.getCurrentSimulationRun()
                + "] step=["
                + _simState.getCurrentSimulationStep()
                + "]" );
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        _LOG.debug( "Simulation run tear down called: run=["
                + _simState.getCurrentSimulationRun()
                + "]" );
    }

    /**
     * Performs any cleanup after the simulation has finished execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simTearDown()
     */
    @Override
    public void simTearDown()
    {
        _LOG.debug( "Simulation tear down called" );
    }

    /**
     * Performs any processing necessary to handle an agent making a decision
     *
     * @param agent The agent making the decision
     * @param event The decision
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#agentDecided(edu.snu.leader.spatial.Agent, edu.snu.leader.spatial.DecisionEvent)
     */
    @Override
    public void agentDecided( Agent agent, DecisionEvent event )
    {
        _LOG.debug( "Agent decided: agentID=["
                + agent.getID()
                + "] decisionType=["
                + event.getDecision().getType()
                + "] simRunStep=["
                + event.getTime()
                + "]" );
    }

    /**
     * Performs any processing necessary to handle a personality update
     *
     * @param event The personality update
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#personalityUpdated(edu.snu.leader.spatial.PersonalityUpdateEvent)
     */
    @Override
    public void personalityUpdated( PersonalityUpdateEvent event )
    {
        _LOG.debug( "Personality updated: agentID=["
                + event.getAgent().getID()
                + "] updateType=["
                + event.getType()
                + "] previousPersonality=["
                + event.getPreviousPersonality()
                + "] updatedPersonality=["
                + event.getUpdatedPersonality()
                + "] simRunStep=["
                + event.getSimRunStep()
                + "]" );
    }

    /**
     * Performs any processing necessary to handle the simulation halting
     *
     * @param reason The reason for the halt
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#simulationRunHalted(edu.snu.leader.spatial.SimulationRunHaltReason)
     */
    @Override
    public void simulationRunHalted( SimulationRunHaltReason reason )
    {
        _LOG.debug( "Simulation run halted: reason=["
                + reason
                + "]" );
    }


}
