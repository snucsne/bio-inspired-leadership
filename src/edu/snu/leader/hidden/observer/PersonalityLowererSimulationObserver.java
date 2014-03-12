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

import java.util.Properties;

import edu.snu.leader.hidden.SimulationState;

/**
 * PersonalityLowererSimulationObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityLowererSimulationObserver extends
        AbstractSimulationObserver
{
    /** The number of simulations after which the personalities will be lowered */
    private int _loweringSimulationCount = 0;

    /** The number of individuals whose personality will be lowered */
    private int _indCount = 0;



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
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simSetUp()
     */
    @Override
    public void simSetUp()
    {
        // Have the desired number of simulations taken place?
        if( _loweringSimulationCount == _simState.getSimulationCount() )
        {
            // Yup
        }
    }
}
