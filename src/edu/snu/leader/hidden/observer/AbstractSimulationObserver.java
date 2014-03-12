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

import edu.snu.leader.hidden.SimulationState;

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
     * @see edu.snu.leader.hidden.observer.SimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
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
     * @see edu.snu.leader.hidden.observer.SimulationObserver#simSetUp()
     */
    @Override
    public void simSetUp()
    {
        // Do nothing
    }

    /**
     * Prepares a simulation run for execution
     *
     * @see edu.snu.leader.hidden.observer.SimulationObserver#simRunSetUp()
     */
    @Override
    public void simRunSetUp()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.hidden.observer.SimulationObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after the simulation has finished execution
     *
     * @see edu.snu.leader.hidden.observer.SimulationObserver#simTearDown()
     */
    @Override
    public void simTearDown()
    {
        // Do nothing
    }

    /**
     * TODO Method description
     *
     * @see edu.snu.leader.hidden.observer.SimulationObserver#describeResults()
     */
    @Override
    public void describeResults()
    {
        // Do nothing
    }

}
