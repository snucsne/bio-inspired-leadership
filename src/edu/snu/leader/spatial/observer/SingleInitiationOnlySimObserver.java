/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.observer;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.SimulationRunHaltReason;


/**
 * SingleInitiationOnlySimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SingleInitiationOnlySimObserver extends AbstractSimObserver
{

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
        // Did the agent cancel?
        if( DecisionType.CANCEL.equals( event.getDecision().getType() ) )
        {
            // Yup.  Send the signal
            _simState.getObserverManager().signalHaltSimulationRun(
                    SimulationRunHaltReason.ONLY_INITIATOR_CANCELED );
        }
    }

}
