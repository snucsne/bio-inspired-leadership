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
package edu.snu.leader.spatial;

// Imports
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * SimulationState
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SimulationState
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SimulationState.class.getName() );

    /** Key for the random number seed */
    private static final String _RANDOM_SEED_KEY = "random-seed";

    /** Key for the number of agents in the simulation */
    private static final String _AGENT_COUNT = "agent-count";

    /** Key for the simulation run count */
    private static final String _SIMULATION_RUN_COUNT = "simulation-run-count";

    /** Key for the simulation run step count */
    private static final String _SIMULATION_RUN_STEP_COUNT = "simulation-run-step-count";

    /** Key for flag specifying that only one initiator is allowed */
    private static final String _SINGLE_INITIATOR_ONLY = "single-initiator-only";


    /** The simulation properties */
    private Properties _props = null;

    /** Random number generator */
    private MersenneTwisterFast _random = null;

    /** The observer manager */
    private ObserverManager _obsManager = null;

    /** The number of agents in the simulation */
    private int _agentCount = 0;

    /** The number of simulation runs to perform */
    private int _simulationRunCount = 0;

    /** The number of simulation steps to perform */
    private long _simulationRunStepCount = 0;

    /** The current simulation run */
    private int _currentSimulationRun = 0;

    /** The current simulation step */
    private long _currentSimulationStep = 0;

    /** All the agents in the simulation */
    private List<Agent> _agents = new LinkedList<Agent>();

    /** Number of new groups created */
    private int _newGroupCount = 0;

    /** Flag specifying that only one initiator is allowed */
    private boolean _singleInitiatorOnly = false;

    /** Flag denoting that something has requested the simulation to halt */
    private boolean _haltSimulation = false;


    /**
     * Initialize the simulation state
     *
     * @param props
     */
    public void initialize( Properties props )
    {
        _LOG.trace( "Entering initialize( props )" );

        // Save the properties
        _props = props;

        // Get the random number generator seed
        String randomSeedStr = props.getProperty( _RANDOM_SEED_KEY );
        Validate.notEmpty( randomSeedStr, "Random seed is required" );
        long seed = Long.parseLong( randomSeedStr );
        _random = new MersenneTwisterFast( seed );
        _LOG.debug( "Using random seed ["
                + seed
                + "]" );

        // Get the agent count
        String agentCountStr = props.getProperty( _AGENT_COUNT );
        Validate.notEmpty( agentCountStr,
                "Agent count (key=["
                        + _AGENT_COUNT
                        + "] may not be empty" );
        _agentCount = Integer.parseInt( agentCountStr );
        _LOG.debug( "Using agent count ["
                + _agentCount
                + "]" );

        // Get the simulation run count
        String simulationRunCountStr = props.getProperty( _SIMULATION_RUN_COUNT );
        Validate.notEmpty( simulationRunCountStr,
                "Simulation run count (key=["
                        + _SIMULATION_RUN_COUNT
                        + "] may not be empty" );
        _simulationRunCount = Integer.parseInt( simulationRunCountStr );
        _LOG.debug( "Using simulation run count ["
                + _simulationRunCount
                + "]" );

        // Get the simulation run step count
        String simulationRunStepCountStr = props.getProperty( _SIMULATION_RUN_STEP_COUNT );
        Validate.notEmpty( simulationRunStepCountStr,
                "Simulation run step count (key=["
                        + simulationRunStepCountStr
                        + "] may not be empty" );
        _simulationRunStepCount = Integer.parseInt( simulationRunStepCountStr );
        _LOG.debug( "Using simulation run step count ["
                + _simulationRunStepCount
                + "]" );

        // Get the single initiator flag
        String singleInitiatorStr = props.getProperty( _SINGLE_INITIATOR_ONLY );
        Validate.notEmpty( singleInitiatorStr,
                "Single initator only flag (key=["
                        + _SINGLE_INITIATOR_ONLY
                        + "]) may not be null" );
        _singleInitiatorOnly = Boolean.parseBoolean( singleInitiatorStr );
        _LOG.debug( "Using _singleInitiatorOnly=["
                + _singleInitiatorOnly
                + "]" );

        // Build and initialize the observer manager
        _obsManager = new ObserverManager();
        _obsManager.initialize( this );

        _LOG.trace( "Leaving initialize( props )" );
    }

    /**
     * Returns the configuration properties of the simulation
     *
     * @return The configuration properties
     */
    public Properties getProperties()
    {
        return _props;
    }


    /**
     * Returns the observer manager for the simulation
     *
     * @return The observer manager
     */
    public ObserverManager getObserverManager()
    {
        return _obsManager;
    }

    /**
     * Sets up the simulation for the next run
     */
    public void setupNextSimulationRun()
    {
        // Increment the simulation run
        _currentSimulationRun++;

        // Reset the simulation run step count
        _currentSimulationStep = 0;

        // Reset the halt simulation flag
        _haltSimulation = false;

        // Reset the group data
        _newGroupCount = 0;
        Group.NONE.reset();

        // Reset all the agents
        for( Agent agent : _agents )
        {
            agent.reset();
        }
    }

    /**
     * Sets up the simulation for the next run step
     */
    public void setupNextSimulationRunStep()
    {
        // Increment the simulation run step count
        _currentSimulationStep++;
    }

    /**
     * Returns the number of simulation runs to perform
     *
     * @return The number of simulation runs to perform
     */
    public int getSimulationRunCount()
    {
        return _simulationRunCount;
    }

    /**
     * Returns the number of simulation run steps to perform
     *
     * @return The number of simulation run steps to perform
     */
    public long getSimulationRunStepCount()
    {
        return _simulationRunStepCount;
    }

    /**
     * Returns the current simulation run
     *
     * @return The current simulation run
     */
    public int getCurrentSimulationRun()
    {
        return _currentSimulationRun;
    }

    /**
     * Returns the currentSimulationStep for this object
     *
     * @return The currentSimulationStep
     */
    public long getCurrentSimulationStep()
    {
        return _currentSimulationStep;
    }

    /**
     * Returns the random number generator
     *
     * @return The random number generator
     */
    public MersenneTwisterFast getRandom()
    {
        return _random;
    }

    /**
     * Adds the specified agent to the simulation
     *
     * @param agent The agent to add
     */
    public void addAgent( Agent agent )
    {
        _agents.add( agent );
    }

    /**
     * Returns an iterator over all the simulated agents
     *
     * @return An iterator over all the simulated agents
     */
    public Iterator<Agent> getAgentIterator()
    {
        return _agents.iterator();
    }

    /**
     * Returns the number of agents in the simulation
     *
     * @return The number of agents in the simulation
     */
    public int getAgentCount()
    {
        return _agentCount;
    }

    /**
     * Returns the newGroupCount for this object
     *
     * @return The newGroupCount
     */
    public int getNewGroupCount()
    {
        return _newGroupCount;
    }

    /**
     * Builds the next new group ID
     *
     * @return A new group ID
     */
    public Object buildNextNewGroupID()
    {
        return "Group" + String.format( "%02d", (_newGroupCount++) + 1 );
    }

    /**
     * Indicates whether or not initiation decision are possible
     *
     * @return <code>true</code> if multiple initiations are possible or no
     * initiations have been made, otherwise, <code>false</code>
     */
    public boolean isInitiationPossible()
    {
        return !(_singleInitiatorOnly && (1 <= _newGroupCount));
    }

    /**
     * Determines if the simulation run has been halted
     *
     * @return <code>true</code> if the simulation run has been halted,
     * otherwise, <code>false</code>
     */
    public boolean hasSimulationRunBeenHalted()
    {
        return _haltSimulation;
    }

    /**
     * Signals the simulator that the simulation run should be halted
     */
    public void haltSimulationRun()
    {
        _haltSimulation = true;
    }
}
