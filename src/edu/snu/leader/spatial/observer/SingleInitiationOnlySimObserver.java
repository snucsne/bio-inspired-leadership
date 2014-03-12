/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
