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
package edu.snu.leader.hidden.observer;

// imports
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;

import java.util.Properties;


/**
 * ForcedPersonalityDistributionSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ForcedPersonalityDistributionSimObserver
    extends AbstractSimulationObserver
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ForcedPersonalityDistributionSimObserver.class.getName() );


    protected enum PersonalityAssignmentMethod {
        RANDOM,
        OBSERVERS,
        INVERSE_OBSERVERS
    };

    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Call the superclass implementation
        super.initialize( simState );

        // Get the simulation properties
        Properties props = simState.getProps();


    }

    /**
     * Prepares a simulation run for execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simRunSetUp()
     */
    @Override
    public void simRunSetUp()
    {
        /* Is it the beginning?  Note that we can't do it when we build the
         * individuals since we need them all built to begin with.
         */
        if( 0 == _simState.getSimulationCount() )
        {
            // Yup
        }
    }

}
