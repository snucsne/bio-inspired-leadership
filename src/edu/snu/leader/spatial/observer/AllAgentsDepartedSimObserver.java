/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.observer;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.Group;
import edu.snu.leader.spatial.SimulationRunHaltReason;
import org.apache.log4j.Logger;

/**
 * AllAgentsDepartedSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class AllAgentsDepartedSimObserver extends AbstractSimObserver
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( AllAgentsDepartedSimObserver.class.getName() );

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
        // Did the last agent follow?
        if( DecisionType.FOLLOW.equals( event.getDecision().getType() )
                && (0 == Group.NONE.getSize()) )
        {
            // Yup.  Send the signal
            _simState.getObserverManager().signalHaltSimulationRun(
                    SimulationRunHaltReason.ALL_AGENTS_DEPARTED );
        }
    }

}
