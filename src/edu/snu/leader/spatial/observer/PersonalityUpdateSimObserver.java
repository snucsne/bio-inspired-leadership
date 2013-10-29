/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.observer;

// Imports
import java.util.Iterator;
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.PersonalityTrait;
import org.apache.log4j.Logger;

/**
 * PersonalityUpdateSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityUpdateSimObserver extends AbstractSimObserver
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PersonalityUpdateSimObserver.class.getName() );


    /** Flag indicating that an agent made a decision */
    private boolean _anAgentMadeADecision = false;

    /** The last simulation step in which the personality was updated */
    private long _lastSimStepUpdate = -1l;

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
        // We only need to update personalities after decisions
        _anAgentMadeADecision = true;
    }

    /**
     * Performs any cleanup after a simulation run step has finished execution
     *
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#simRunStepTearDown()
     */
    @Override
    public void simRunStepTearDown()
    {
        // Only check for updates if an agent made a decision
        if( _anAgentMadeADecision )
        {
            // Update the personality traits
            updatePersonalities();

            // Reset the flag
            _anAgentMadeADecision = false;

            // Log the simulation time step
            _lastSimStepUpdate = _simState.getCurrentSimulationStep();
        }
    }


    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        _LOG.debug( "Last simulation step update=["
                + _lastSimStepUpdate
                + "] current=["
                + _simState.getCurrentSimulationStep()
                + "]" );

        // Only update if we didn't just update
        if( _lastSimStepUpdate != _simState.getCurrentSimulationStep() )
        {
            // Update the personality traits
            updatePersonalities();

            // Reset the flag
            _anAgentMadeADecision = false;
        }

        _lastSimStepUpdate = -1l;
    }

    /**
     * Update each agent's personality trait
     */
    private void updatePersonalities()
    {
        // Iterate over all the agents
        Iterator<Agent> iter = _simState.getAgentIterator();
        while( iter.hasNext() )
        {
            // Get the agent's personality trait
            Agent current = iter.next();
            PersonalityTrait trait = current.getPersonalityTrait();

//            _LOG.debug( "Updating personality for agent ["
//                    + current.getID()
//                    + "]" );

            // Update it
            trait.update();
        }
    }
}
