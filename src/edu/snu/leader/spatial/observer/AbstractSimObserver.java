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

import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.PersonalityUpdateEvent;
import edu.snu.leader.spatial.SimObserver;
import edu.snu.leader.spatial.SimulationRunHaltReason;
// Imports
import edu.snu.leader.spatial.SimulationState;

/**
 * AbstractSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class AbstractSimObserver implements SimObserver
{
    /** The state of the simulation */
    protected SimulationState _simState = null;

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
        // Save the simulation state
        _simState = simState;
    }

    /**
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simSetup()
     */
    @Override
    public void simSetup()
    {
        // Do nothing
    }

    /**
     * Prepares a simulation step for execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunSetup()
     */
    @Override
    public void simRunSetup()
    {
        // Do nothing
    }

    /**
     * Prepares a simulation step for execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunStepSetup()
     */
    @Override
    public void simRunStepSetup()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after a simulation run step has finished execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunStepTearDown()
     */
    @Override
    public void simRunStepTearDown()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        // Do nothing
    }

    /**
     * Performs any cleanup after the simulation has finished execution
     *
     * @see edu.snu.leader.spatial.SimObserver#simTearDown()
     */
    @Override
    public void simTearDown()
    {
        // Do nothing
    }

    /**
     * Performs any processing necessary to handle an agent making a decision
     *
     * @param agent The agent making the decision
     * @param event The decision
     * @see edu.snu.leader.spatial.SimObserver#agentDecided(edu.snu.leader.spatial.Agent, edu.snu.leader.spatial.DecisionEvent)
     */
    @Override
    public void agentDecided( Agent agent, DecisionEvent event )
    {
        // Do nothing
    }

    /**
     * Performs any processing necessary to handle a personality update
     *
     * @param event The personality update
     * @see edu.snu.leader.spatial.SimObserver#personalityUpdated(edu.snu.leader.spatial.PersonalityUpdateEvent)
     */
    @Override
    public void personalityUpdated( PersonalityUpdateEvent event )
    {
        // Do nothing
    }

    /**
     * Performs any processing necessary to handle the simulation halting
     *
     * @param reason The reason for the halt
     * @see edu.snu.leader.spatial.SimObserver#simulationRunHalted(edu.snu.leader.spatial.SimulationRunHaltReason)
     */
    @Override
    public void simulationRunHalted( SimulationRunHaltReason reason )
    {
        // Do nothing
    }


}
