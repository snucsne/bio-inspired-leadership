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
import edu.snu.leader.spatial.AgentSpatialState;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.PersonalityTrait;
import edu.snu.leader.spatial.PersonalityUpdateEvent;
import edu.snu.leader.spatial.SimulationRunHaltReason;
import edu.snu.leader.spatial.SimulationState;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ResultsReporterSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ResultsReporterSimObserver extends AbstractSimObserver
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ResultsReporterSimObserver.class.getName() );

    /** Key for the results file */
    private static final String _RESULTS_FILE_KEY = "results-file";

    /** Stats file spacer comment */
    private static final String _SPACER =
            "# =========================================================";


    private class AgentInitiationHistory
    {
        private int _initiations = 0;
        private int _successes = 0;
        private int _failures = 0;

        public void addSuccess()
        {
            _initiations++;
            _successes++;
        }

        public void addFailure()
        {
            _initiations++;
            _failures++;
        }

        /**
         * Returns the initiations for this object
         *
         * @return The initiations
         */
        public int getInitiations()
        {
            return _initiations;
        }

        /**
         * Returns the successes for this object
         *
         * @return The successes
         */
        public int getSuccesses()
        {
            return _successes;
        }

        /**
         * Returns the failures for this object
         *
         * @return The failures
         */
        public int getFailures()
        {
            return _failures;
        }
    }


    /** The writer to which the results are reported */
    private PrintWriter _writer = null;

    /** The reason the last simulation ended */
    private SimulationRunHaltReason _lastSimHaltReason = null;

    private long _initiationCount = 0;
    private long _successCount = 0;
    private Map<Agent, List<PersonalityUpdateEvent>> _agentPersonalityUpdates =
            new HashMap<Agent, List<PersonalityUpdateEvent>>();
    private Map<Agent, AgentInitiationHistory> _agentInitiationHistories =
            new HashMap<Agent, AgentInitiationHistory>();
    private List<Agent> _currentInitiators = new LinkedList<Agent>();



    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @param keyPrefix Prefix for configuration property keys
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#initialize(edu.snu.leader.spatial.SimulationState, java.lang.String)
     */
    @Override
    public void initialize( SimulationState simState, String keyPrefix )
    {
        _LOG.trace( "Entering initialize( simState, keyPrefix )" );

        // Call the superclass implementation
        super.initialize( simState, keyPrefix );

        // Grab the properties
        Properties props = simState.getProperties();

        // Load the results filename
        String resultsFile = props.getProperty( _RESULTS_FILE_KEY );
        Validate.notEmpty( resultsFile, "Results file may not be empty" );
        _LOG.info( "Sending results to ["
                + resultsFile
                + "]" );

        // Create the statistics writer
        try
        {
            _writer = new PrintWriter( new BufferedWriter(
                    new FileWriter( resultsFile ) ) );
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to open results file ["
                    + resultsFile
                    + "]", ioe );
            throw new RuntimeException( "Unable to open results file ["
                    + resultsFile
                    + "]", ioe );
        }

        // Log the system properties to the stats file for future reference
        _writer.println( "# Started: " + (new Date()) );
        _writer.println( _SPACER );
        _writer.println( "# Simulation properties" );
        _writer.println( _SPACER );
        List<String> keyList = new ArrayList<String>(
                props.stringPropertyNames() );
        Collections.sort( keyList );
        Iterator<String> iter = keyList.iterator();
        while( iter.hasNext() )
        {
            String key = iter.next();
            String value = props.getProperty( key );

            _writer.println( "# " + key + " = " + value );
        }
        _writer.println( _SPACER );
        _writer.println();
        _writer.flush();


        _LOG.trace( "Leaving initialize( simState, keyPrefix )" );
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        // Clear out all the initiators
        for( Agent agent : _currentInitiators )
        {
            AgentInitiationHistory history = _agentInitiationHistories.get( agent );
            if( null == history )
            {
                history = new AgentInitiationHistory();
                _agentInitiationHistories.put( agent, history );
            }
            history.addSuccess();
        }
        _currentInitiators.clear();

        // Reset the last sim halt reason
        _lastSimHaltReason = null;
    }

    /**
     * Performs any cleanup after the simulation has finished execution
     *
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#simTearDown()
     */
    @Override
    public void simTearDown()
    {
        // Write out all the initiation results
        _writer.println( _SPACER );
        _writer.println( "# Initiation stats" );
        _writer.println( "initiations = " + _initiationCount );
        _writer.println( "successes = " + _successCount );
        _writer.println( "total-simulations = " + _simState.getSimulationRunCount() );
        _writer.println( "total-successful-simulations = " + _successCount );
        _writer.println( "total-leadership-success = "
                    + (((float) _successCount) / ((float) _simState.getSimulationRunCount())) );
        _writer.println();
        _writer.println();

        // Print out the agent initiation stats
        _writer.println( _SPACER );
        _writer.println( "# Agent initiation stats" );
        Iterator<Agent> agentIter = _simState.getAgentIterator();
        while( agentIter.hasNext() )
        {
            // Get the agent
            Agent agent = agentIter.next();

            // Default to no initiations
            int initiations = 0;
            int successes = 0;
            int failures = 0;

            // See if there is actually data
            AgentInitiationHistory history = _agentInitiationHistories.get( agent );
            if( null != history )
            {
                initiations = history.getInitiations();
                successes = history.getSuccesses();
                failures = history.getFailures();
            }

            _writer.println( "initiation."
                    + agent.getID()
                    + ".attempts = "
                    + initiations );
            _writer.println( "initiation."
                    + agent.getID()
                    + ".successes = "
                    + successes );
            _writer.println( "initiation."
                    + agent.getID()
                    + ".failures = "
                    + failures );
            _writer.println( "initiation."
                    + agent.getID()
                    + ".attempt-percentage = "
                    + ( ((float) initiations )
                            / _initiationCount ) );
            _writer.println( "initiation."
                    + agent.getID()
                    + ".success-percentage = "
                    + ( ((float) successes)
                            / _successCount ));

        }
        _writer.println();
        _writer.println();

        // Print out the information about each agent
        _writer.println( _SPACER );
        _writer.println( "# Agent data" );
        agentIter = _simState.getAgentIterator();
        while( agentIter.hasNext() )
        {
            describeAgent( agentIter.next() );
            _writer.println();
        }
        _writer.println();

        // Log the stop time
        _writer.println( "# Stopped: " + (new Date()) );

        // Close the results writer
        _writer.close();
    }

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
        // Log initiations
        if( DecisionType.INITIATE.equals( event.getDecision().getType() ) )
        {
            // Keep track of how many initiations there are
            _initiationCount++;

            // Save the agent for later processing upon success/failure
            _currentInitiators.add( agent );
        }

        // Log cancelations
        if( DecisionType.CANCEL.equals( event.getDecision().getType() ) )
        {
            // Increment the agent's cancel count
            AgentInitiationHistory history = _agentInitiationHistories.get( agent );
            if( null == history )
            {
                history = new AgentInitiationHistory();
                _agentInitiationHistories.put( agent, history );
            }
            history.addFailure();

            // Update the agent's initiation status
            _currentInitiators.remove( agent );
        }
    }

    /**
     * Performs any processing necessary to handle a personality update
     *
     * @param event The personality update
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#personalityUpdated(edu.snu.leader.spatial.PersonalityUpdateEvent)
     */
    @Override
    public void personalityUpdated( PersonalityUpdateEvent event )
    {
        // Log the agent's update
        List<PersonalityUpdateEvent> updates = _agentPersonalityUpdates.get(
                event.getAgent() );
        if( null == updates )
        {
            updates = new LinkedList<PersonalityUpdateEvent>();
            _agentPersonalityUpdates.put( event.getAgent(), updates );
        }
        updates.add( event );
    }

    /**
     * Performs any processing necessary to handle the simulation halting
     *
     * @param reason The reason for the halt
     * @see edu.snu.leader.spatial.observer.AbstractSimObserver#simulationRunHalted(edu.snu.leader.spatial.SimulationRunHaltReason)
     */
    @Override
    public void simulationRunHalted( SimulationRunHaltReason reason )
    {
        // Save the reason the simulation run was halted
        _lastSimHaltReason = reason;

        // Was it successful?
        if( SimulationRunHaltReason.ALL_AGENTS_DEPARTED.equals( reason ) )
        {
            // Yup
            _successCount++;

            // Mark all the non-canceling initiators as successful
        }
    }

    private void describeAgent( Agent agent )
    {
        // Build the prefix
        String prefix = "individual." + agent.getID() + ".";

        // Get some data from the agent
        AgentSpatialState spatialState = agent.getSpatialState();
        PersonalityTrait personalityTrait = agent.getPersonalityTrait();


        // Describe the agent's personality
        _writer.println( prefix
                + "personality = "
                + personalityTrait.getPersonality() );

        // Describe agent's initial location
        _writer.println( prefix
                + "location = "
                + spatialState.getInitialPosition().getX()
                + " "
                + spatialState.getInitialPosition().getY() );

        // Describe agent's preferred destination
        _writer.println( prefix
                + "preferred-destination = "
                + spatialState.getPreferredDestination().getX()
                + " "
                + spatialState.getPreferredDestination().getY() );

        // Describe the agent's personality history
        _writer.print( prefix + "personality-history =" );
        List<PersonalityUpdateEvent> updates =
                _agentPersonalityUpdates.get( agent );
        if( null != updates )
        {
            Iterator<PersonalityUpdateEvent> updateIter = updates.iterator();
            while( updateIter.hasNext() )
            {
                PersonalityUpdateEvent event = updateIter.next();
                _writer.print( " ["
                        + event.getSimRun()
                        + " "
                        + event.getSimRunStep()
                        + " "
                        + event.getPreviousPersonality()
                        + " "
                        + event.getUpdatedPersonality()
                        + "]" );
            }
        }
        _writer.println();

        // Describe the agent's nearest neighbors
        _writer.print( prefix + "nearest-neighbors =" );
        List<Agent> neighbors = agent.getCurrentNearestNeighbors();
        for( Agent neighbor : neighbors )
        {
            _writer.print( " " + neighbor.getID() );
        }
        _writer.println();
        _writer.println( prefix + "nearest-neighbor-count = "
                + neighbors.size() );

        // Add placeholders for the social network analysis
        _writer.println( prefix
                + "eigenvector-centrality = %%%"
                + agent.getID()
                + "-EIGENVECTOR-CENTRALITY%%%" );
        _writer.println( prefix
                + "betweenness = %%%"
                + agent.getID()
                + "-BETWEENNESS%%%" );
        _writer.println( prefix
                + "sna-todo = %%%"
                + agent.getID()
                + "-sna-todo%%%" );
    }

}
