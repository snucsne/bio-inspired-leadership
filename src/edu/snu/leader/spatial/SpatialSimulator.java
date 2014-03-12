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
import edu.snu.leader.util.MiscUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.Properties;


/**
 * SpatialSimulator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SpatialSimulator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SpatialSimulator.class.getName() );

    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the agent builder class name */
    private static final String _AGENT_BUILDER_CLASS = "agent-builder";



    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** The agent builder */
    private AgentBuilder _agentBuilder = null;

//    /** The observer manager */
//    private ObserverManager _obsManager = null;


    /**
     * Initialize the simulation
     */
    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Load the properties
        _props = MiscUtils.loadProperties( _PROPS_FILE_KEY );

        // Initialize the simulation state
        _simState.initialize( _props );

        // Load and instantiate the agent builder
        String agentBuilderClassName = _props.getProperty(
                _AGENT_BUILDER_CLASS );
        Validate.notEmpty( agentBuilderClassName,
                "Agent builder class name (key="
                + _AGENT_BUILDER_CLASS
                + ") may not be empty" );
        _agentBuilder = (AgentBuilder) MiscUtils.loadAndInstantiate(
                agentBuilderClassName,
                "Agent builder class name" );
        _agentBuilder.initialize( _simState );

        // Build the agents
        buildAgents();

//        // Build and initialize the observer manager
//        _obsManager = new ObserverManager();
//        _obsManager.initialize( _simState );

        _LOG.trace( "Leaving initialize()" );
    }

    /**
     * Executes a series of simulation runs
     */
    public void execute()
    {
        _LOG.trace( "Entering execute()" );

        // Signal the listeners that we are ready to begin
        _simState.getObserverManager().signalSimSetup();

        // Perform a series of simulation runs
        while( _simState.getCurrentSimulationRun()
                < _simState.getSimulationRunCount() )
        {
            if( 0 == (_simState.getCurrentSimulationRun() % 250) )
            {
                _LOG.info( "Simulation run ["
                        + _simState.getCurrentSimulationRun()
                        + "]" );
            }

            // Execute a single run
            executeRun();
        }

        // Signal the listeners that we are finished
        _simState.getObserverManager().signalSimTearDown();

        _LOG.trace( "Leaving execute()" );
    }

    /**
     * Executes a single simulation run
     */
    private void executeRun()
    {
        _LOG.trace( "Entering executeRun()" );

        // Tell the simulator state that the next simulation run is ready
        _simState.setupNextSimulationRun();

        // Signal the listeners
        _simState.getObserverManager().signalSimRunSetup();

        // Execute while the simulation isn't finished
        while( isSimActive() )
        {
            // Tell the simulator state that the next simulation step is ready
            _simState.setupNextSimulationRunStep();

            // Signal the listeners
            _simState.getObserverManager().signalSimRunStepSetup();

            // Have all the agents make their decisions
            Iterator<Agent> iter = _simState.getAgentIterator();
            while( iter.hasNext() )
            {
                iter.next().makeDecision();
            }

            // Have all the agents execute their decisions
            iter = _simState.getAgentIterator();
            while( iter.hasNext() )
            {
                iter.next().execute();
            }

            // Signal the listeners that the step is done
            _simState.getObserverManager().signalSimRunStepTearDown();
        }

        // Signal the listeners that we are done with a run
        _simState.getObserverManager().signalSimRunTearDown();

        _LOG.trace( "Leaving executeRun()" );
    }

    /**
     * Builds all the agents used in the simulations
     */
    private void buildAgents()
    {
        _LOG.trace( "Entering buildAgents()" );

        // Build the agents
        for( int i = 0; i < _simState.getAgentCount(); i++ )
        {
            Agent agent = _agentBuilder.build( i );
            _simState.addAgent( agent );
        }

        // Initialize all the agents
        Iterator<Agent> agentIter = _simState.getAgentIterator();
        while( agentIter.hasNext() )
        {
            agentIter.next().initialize( _simState );
        }

        _LOG.trace( "Leaving buildAgents()" );
    }

    /**
     * Returns a flag denoting whether or not the simulation run is still active
     *
     * @return A flag denoting whether or not the simulation run is still active
     */
    private boolean isSimActive()
    {
        // Default to true
        boolean active = true;

        // Has something signaled a halt?
        if( _simState.hasSimulationRunBeenHalted() )
        {
            // Yup
            active = false;
        }

        // Have we exceeded the maximum number of simulation steps?
        if( _simState.getCurrentSimulationStep()
                > _simState.getSimulationRunStepCount() )
        {
            active = false;
            _simState.getObserverManager().signalHaltSimulationRun(
                    SimulationRunHaltReason.MAX_SIM_RUN_STEPS_REACHED );
        }

        /* Have we exceeded the "adhesion" time limit?  This is just the max
         * number of time steps between decisions and/or movement.
         * Basically, nothing happened for a long time. */
        // TODO

        return active;
    }


    /**
     * Main entry into the simulation
     *
     * @param args
     */
    public static void main( String[] args )
    {
        try
        {
            // Build, initialize, run
            SpatialSimulator sim = new SpatialSimulator();
            sim.initialize();
            sim.execute();
        }
        catch( Exception e )
        {
            _LOG.error( "Unknown error", e );
        }
    }

}
