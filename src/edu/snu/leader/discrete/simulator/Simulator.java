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

package edu.snu.leader.discrete.simulator;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.snu.leader.discrete.behavior.Decision.DecisionType;
import edu.snu.leader.util.MiscUtils;


/**
 * The Simulator
 * 
 * @author Tim Solum
 */
public class Simulator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( Simulator.class.getName() );

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

    /** For adhesion time limits */
    private static int _lastJoinedAgentTime = 0;

    /** Adhesion time limit */
    private int _adhesionTimeLimit = 0;

    /** The number of times an initiator reached his cancellation threshold */
    private static int _successCount = 0;

    /** The number of occurrences of each group size at the end of a simulation */
    private static int[] _groupSizeCounts = null;

    private static long _randomSeedOverride = -1;

    public static int[] getGroupSizeCounts()
    {
        return _groupSizeCounts;
    }

    public Simulator()
    {
        _successCount = 0;
        _randomSeedOverride = -1;
    }

    public Simulator( long randomSeedOverride )
    {
        this();
        _randomSeedOverride = randomSeedOverride;
    }

    public static long getRandomSeedOverride()
    {
        return _randomSeedOverride;
    }

    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Load the properties
        _props = MiscUtils.loadProperties( _PROPS_FILE_KEY );

        // Initialize the simulation state
        _simState.initialize( _props );

        String adhesionTimeLimit = _simState.getProperties().getProperty(
                "adhesion-time-limit" );
        Validate.notEmpty( adhesionTimeLimit,
                "Adhesion time limit may not be empty" );
        _adhesionTimeLimit = Integer.parseInt( adhesionTimeLimit );

        // add the adhesion time limit info to root directory
//        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY
//                + "adhesion-time-limit=" + _adhesionTimeLimit + "_" );

        // Load and instantiate the agent builder
        String agentBuilderClassName = _props.getProperty( _AGENT_BUILDER_CLASS );
        Validate.notEmpty( agentBuilderClassName,
                "Agent builder class name (key=" + _AGENT_BUILDER_CLASS
                        + ") may not be empty" );
        _agentBuilder = (AgentBuilder) MiscUtils.loadAndInstantiate(
                agentBuilderClassName, "Agent builder class name" );

        _agentBuilder.initialize( _simState );

        // Build the agents
        buildAgents();
        
        // create the predator
        Predator predator = new Predator("PredDebug");
        predator.initialize( _simState );
        _simState.setPredator( predator );

        _groupSizeCounts = new int[_simState.getAgentCount() + 1];

        _LOG.trace( "Exiting initialize()" );
    }

    /**
     * Run the simulation after initialization
     */
    public void execute()
    {
        _LOG.trace( "Entering execute()" );

        while( _simState.getCurrentSimulationRun() < _simState.getSimulationRunCount() )
        {
            executeRun();
        }

        _LOG.trace( "Leaving execute()" );
    }

    private void executeRun()
    {
        _LOG.trace( "Entering executeRun()" );
        Iterator<Agent> agentIterator = null;
        while( isSimActive() )
        {

            // _LOG.trace("Making decisions");
            // make decisions
            agentIterator = _simState.getAgentIterator();
            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();
                if(temp.isAlive()){
                    temp.makeDecision();
                }
            }
            // _LOG.trace("Finished making decisions");

            // _LOG.trace("Executing decisions");
            // execute decisions
            agentIterator = _simState.getAgentIterator();
            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();
                if(temp.isAlive()){
                    temp.execute();
                }
            }
            // _LOG.trace("Finished executing decisions");

            // update traits
            agentIterator = _simState.getAgentIterator();
            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();
                if(temp.isAlive()){
                    temp.update();
                }
            }
            
            if(_simState.isPredatorEnabled()){
                _simState.getPredator().hunt();
            }

            // _LOG.trace("Setting up next simulation run step");
            // setup next run step
            _simState.setupNextSimulationRunStep();
            // _LOG.trace("Finished setting up next simulation run step");
        }

        _LOG.trace( "Setting up next simulation run" );
        // setup next simulation run
        _simState.setupNextSimulationRun();
        _LOG.trace( "Finished setting up next simulation run" );

        _lastJoinedAgentTime = 0;

        _LOG.trace( "Leaving executeRun()" );
    }

    private void buildAgents()
    {
        _LOG.trace( "Entering buildAgents()" );

        List<Agent> agents = _agentBuilder.build();

        for( int i = 0; i < agents.size(); i++ )
        {
            _simState.addAgent( agents.get( i ) );
        }

        _LOG.trace( "Leaving buildAgents()" );
    }

    /**
     * This is used to reset lastJoinedAgentTime when using an adhesion time
     * limit
     * 
     * @param joined Whether an agent joined or not
     */
    public static void agentMoved()
    {
        _lastJoinedAgentTime = 0;
    }

    /**
     * Gets the number of agents that were able to reach their cancellation
     * threshold after initiating
     * 
     * @return The success count
     */
    public static int getSuccessCount()
    {
        return _successCount;
    }

    private Agent initiationAgent = null;

    /**
     * Returns a flag denoting whether or not the simulation run is still active
     * 
     * @return A flag denoting whether or not the simulation run is still active
     */
    private boolean isSimActive()
    {
        boolean isActive = false;

        // if global and only one can initiate then use adhesion time limit
        if( _simState.getCommunicationType().equals( "global" )
                && !Agent.canMultipleInitiate() )
        {
            isActive = true;
            int groupCount = 0;
            Iterator<Agent> agentIterator = _simState.getAgentIterator();

            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();

                if( temp.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.FOLLOW ) )
                {
                    groupCount++;
                }
                else if( temp.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.INITIATION ) )
                {
                    groupCount++;
                    initiationAgent = temp;
                }
                // if an agent is canceling then this simulation is finished
                else if( temp.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.CANCELLATION ) )
                {
                    // temp.endOfInitiation( false, groupCount );
                    // isActive = false;
                    // break;
                    groupCount++;
                }
            }
            // if we have an initiator
            if( initiationAgent != null )
            {
                // if last joined time is greater than the adhesion time limit
                // then
                // this run is done
                if( _lastJoinedAgentTime > _adhesionTimeLimit )
                {
                    initiationAgent.endOfInitiation( false, groupCount );
                    isActive = false;
                }
                if( initiationAgent.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.CANCELLATION ) )
                {
                    initiationAgent.endOfInitiation( false, groupCount );
                    isActive = false;
                }
                if( groupCount / (float) _simState.getAgentCount() >= initiationAgent.getCancelThreshold() )
                {
                    _successCount++;
                    initiationAgent.endOfInitiation( true, groupCount );
                    isActive = false;
                }
                _lastJoinedAgentTime++;
            }
            if( groupCount >= _simState.getAgentCount() )
            {
                isActive = false;
            }

            if( !isActive )
            {
                _groupSizeCounts[groupCount]++;
            }
        }
        // do the simulation for as many time steps as there are
        else if( _simState.getSimulationTime() < _simState.getMaxSimulationTimeSteps() )
        {
            isActive = true;
            if(Agent.numReachedDestination >= _simState.getAgentCount() - _simState.getPredator().getTotalAgentsEaten()){
                _successCount++;
                isActive = false;
            }
        }

        return isActive;
    }
}
