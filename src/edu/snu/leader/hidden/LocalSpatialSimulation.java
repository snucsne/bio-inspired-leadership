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
package edu.snu.leader.hidden;

// Imports
import edu.snu.leader.hidden.event.DepartureEvent;
import edu.snu.leader.hidden.event.EventTimeCalculator;
import edu.snu.leader.hidden.observer.SimulationObserver;
import edu.snu.leader.util.MiscUtils;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * LocalSpatialSimulation
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class LocalSpatialSimulation
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            LocalSpatialSimulation.class.getName() );

    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the number of observers */
    private static final String _OBSERVER_COUNT_KEY = "observer-count";


    private class InitiatorData {
        public SpatialIndividual initiator = null;
        public int followCount = 0;
        public DepartureEvent followEvent = null;;
        public float cancelTime = 0.0f;
        public InitiatorData( SpatialIndividual initiator )
        {
            this.initiator = initiator;
        }
    }

    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** The results reporter */
    private ResultsReporter _reporter = new ResultsReporter();

    /** The number of times to run the simulator */
    private int _simulationCount = 0;

    /** The simulation observers */
    private List<SimulationObserver> _simObservers =
            new LinkedList<SimulationObserver>();


    /**
     * Main entry into the simulation
     *
     * @param args
     */
    public static void main( String[] args )
    {
        try
        {
            _LOG.debug( "Starting simulation..." );

            // Build, initialize, run
            LocalSpatialSimulation sim =
                new LocalSpatialSimulation();
            sim.initialize();
            sim.run();
        }
        catch( Exception e )
        {
            _LOG.error( "Unknown error", e );
        }
    }


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

        // Initialize the results reporter
        _reporter.initialize( _simState );

        // Get the simulation count
        _simulationCount = _simState.getSimulationCount();

        // Get the number of observers
        int observerCount = 0;
        String observerCountStr = _props.getProperty( _OBSERVER_COUNT_KEY );
        if( null != observerCountStr )
        {
            observerCount = Integer.parseInt( observerCountStr );
        }

        // Get the observers
        for( int i = 0; i < observerCount; i++ )
        {
            // Get the class name
            String key = "observer."
                    + String.format( "%02d", i )
                    + ".class";
            String observerClassStr = _props.getProperty( key );

            // Instantiate and initialize the observer
            SimulationObserver observer = (SimulationObserver)
                    MiscUtils.loadAndInstantiate(
                            observerClassStr,
                            "Simulation observer class" );
            observer.initialize( _simState );

            // Save it
            _simObservers.add( observer );
        }

        _LOG.trace( "Leaving initialize()" );
    }

    /**
     * Runs the simulation
     */
    public void run()
    {
        _LOG.trace( "Entering run()" );

        // Set up the simulation
        setUpSimulation();

        // Run the simulation a number of times
        for( int i = 0; i < _simulationCount; i++ )
        {
            _LOG.warn( "Simulation ["
                    + i
                    + "]" );

            // Reset the simulation state
            _simState.reset();

            // Set the simulation index
            _simState.setSimIndex( i );

            // Set up the simulation run
            setUpSimulationRun();

            // Execute the simulation
            executeSimulation( i );

            // Tear down the simulation run
            tearDownSimulationRun();
        }

        // Tear down the simulation
        tearDownSimulation();

        // Report the final results
        _reporter.reportFinalResults();

        _LOG.trace( "Leaving run()" );
    }

    /**
     * Iterate over all the observers and have them set up the simulation
     */
    private void setUpSimulation()
    {
        Iterator<SimulationObserver> iter = _simObservers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simSetUp();
        }
    }

    /**
     * Iterate over all the observers and have them set up a simulation run
     */
    private void setUpSimulationRun()
    {
        Iterator<SimulationObserver> iter = _simObservers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simRunSetUp();
        }
    }

    /**
     * Iterate over all the observers and have them tear down a simulation run
     */
    private void tearDownSimulationRun()
    {
        Iterator<SimulationObserver> iter = _simObservers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simRunTearDown();
        }
    }

    /**
     * Iterate over all the observers and have them tear down the simulation
     */
    private void tearDownSimulation()
    {
        Iterator<SimulationObserver> iter = _simObservers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simTearDown();
        }
    }

    /**
     * Executes a single run of the simulator
     *
     */
    private void executeSimulation( int simIndex )
    {
        _LOG.trace( "Entering executeSimulation()" );

        /* Maintain a map of each individual's current time until a decision
         * is made.  It is reset if anything should happen that would change
         * the decision. */
        Map<Object, DepartureEvent> indEvents =
                new HashMap<Object, DepartureEvent>();

        // Maintain the departure history
        List<DepartureEvent> departureHistory = new LinkedList<DepartureEvent>();

        // Maintain a map of individual IDs and their departure times
        Map<Object,Float> departureTimes = new HashMap<Object, Float>();

        // Maintain a map of initiators
        Map<SpatialIndividual, InitiatorData> initiators =
                new HashMap<SpatialIndividual, InitiatorData>();
        int maxInitiatorCount = 0;

        // Keep track of the previous event's time
        float previousEventsTime = 0;

        // Keep track of the total simulation time
        float totalSimulationTime = 0;

        // Run the simulation until it isn't active
        while( isSimulationActive() || (0 == maxInitiatorCount) )
        {
            // Create some handy variables
            DepartureEvent earliestEvent = new DepartureEvent(
                    null,
                    null,
                    null,
                    Float.POSITIVE_INFINITY );

            // Iterate through every individual
            Iterator<SpatialIndividual> individualsIter =
                    _simState.getAllIndividuals().iterator();
            while( individualsIter.hasNext() )
            {
                // Get the current individual
                SpatialIndividual currentInd = individualsIter.next();
                _LOG.debug( "Processing currentInd=["
                        + currentInd.getID()
                        + "]" );

                /* If they are already following, then ignore them.
                 * Note that this would need to change if they can change their
                 * mind. */
                if( (null != currentInd.getGroupID())
                        && (null != currentInd.getLeader() ) )
                {
                    _LOG.debug( "Ind=["
                            + currentInd.getID()
                            + "] is already following ["
                            + currentInd.getLeader().getIndividual().getID()
                            + "]" );
                    continue;
                }

                // Do they have a current event?
                DepartureEvent currentEvent = indEvents.get( currentInd.getID() );
                _LOG.debug( "CurrentEvent=[" + currentEvent + "]" );
                if( null != currentEvent )
                {
                    // Yup, process it using the last event's time
                    currentEvent.updateTime( currentEvent.getTime()
                            - previousEventsTime );

                    // Sanity check
                    if( currentEvent.getTime() < 0.0f )
                    {
                        _LOG.error( "Current event should have happened before this: time=["
                                + currentEvent.getTime()
                                + "] ind=["
                                + currentEvent.getDeparted().getID()
                                + "]" );
                        throw new RuntimeException( "Invalid event time" );
                    }
                }
                else
                {
                    // Nope, build one
                    currentEvent = buildDepartureEvent( currentInd, departureTimes );
                    indEvents.put( currentInd.getID(), currentEvent );
                }

                _LOG.debug( "Current event: departed=["
                        + currentEvent.getDeparted().getID()
                        + "] leader=["
                        + currentEvent.getLeaderID()
                        + "] type=["
                        + currentEvent.getType()
                        + "] time=["
                        + currentEvent.getTime()
                        + "]" );


                // Is it the new earliest event?
                if( currentEvent.getTime() < earliestEvent.getTime() )
                {
                    // Yup
                    earliestEvent = currentEvent;
                }
            }

            if( null == earliestEvent )
            {
                // Something went foobar

                throw new RuntimeException( "No earliest event found!" );
            }

            // Log the earliest event
            _LOG.debug( "Earliest event: departed=["
                    + earliestEvent.getDeparted().getID()
                    + "] leader=["
                    + earliestEvent.getLeaderID()
                    + "] type=["
                    + earliestEvent.getType()
                    + "] time=["
                    + earliestEvent.getTime()
                    + "]" );

            // Was the earliest event an initiation?
            DepartureEvent.Type earliestType = earliestEvent.getType();
            if( DepartureEvent.Type.INITIATE.equals( earliestType ) )
            {
                // Yup, process it
                _simState.initiate( earliestEvent.getDeparted() );
                initiators.put( earliestEvent.getDeparted(),
                        new InitiatorData( earliestEvent.getDeparted() ) );
                departureTimes.put( earliestEvent.getDeparted().getID(),
                        new Float( earliestEvent.getTime() + totalSimulationTime ) );
                _LOG.debug( "New initiator ["
                        + earliestEvent.getDeparted().getID()
                        + "]" );

                // Update the max number of initiators if necessary
                if( maxInitiatorCount < initiators.size() )
                {
                    maxInitiatorCount = initiators.size();
                }
            }
            // How about a follow?
            else if( DepartureEvent.Type.FOLLOW.equals( earliestType ) )
            {
                // Yup, process it
                _simState.follow( earliestEvent.getLeader(),
                        earliestEvent.getDeparted() );
                departureTimes.put( earliestEvent.getDeparted().getID(),
                        new Float( earliestEvent.getTime() + totalSimulationTime ) );
                _LOG.debug( "New follower ["
                        + earliestEvent.getDeparted().getID()
                        + "] leader=["
                        + earliestEvent.getLeader().getID()
                        + "] total=["
                        + earliestEvent.getLeader().getTotalFollowerCount()
                        + "]" );
            }
            // How about a cancel?
            else if( DepartureEvent.Type.CANCEL.equals( earliestType ) )
            {
                // Yup, grab all the initiator's followers and delete their time
                SpatialIndividual canceler = earliestEvent.getDeparted();
                Iterator<Neighbor> followerIter = canceler.getAllFollowers().iterator();
                while( followerIter.hasNext() )
                {
                    Neighbor current = followerIter.next();
                    departureTimes.remove( current.getIndividual().getID() );

                    // Reset all the event times for the neighbor's mimics
                    Iterator<SpatialIndividual> mimickingNeighborsIter =
                            current.getIndividual().getMimickingNeighbors().iterator();
                    while( mimickingNeighborsIter.hasNext() )
                    {
                        SpatialIndividual mimickingNeighbor = mimickingNeighborsIter.next();
                        indEvents.remove( mimickingNeighbor.getID() );
                        _LOG.debug( "Reseting event time for ind=["
                                + mimickingNeighbor.getID()
                                + "]" );
                    }
                }

                // Process the cancellation
                _simState.cancelInitiation( canceler );
                initiators.remove( canceler );
                departureTimes.remove( canceler.getID() );
                _LOG.debug( "Cancelling ["
                        + canceler.getID()
                        + "]" );
            }
            // I have no idea
            else
            {
                _LOG.error( "Unknown event type ["
                        + earliestType
                        + "]" );
                throw new RuntimeException( "Invalid event type" );
            }

            // Log the event
            departureHistory.add( earliestEvent );

            /* All the events for the individuals observing the decision-making
             * individual need to be recalculated from scratch. */
            Iterator<SpatialIndividual> mimickingNeighborsIter =
                    earliestEvent.getDeparted().getMimickingNeighbors().iterator();
            while( mimickingNeighborsIter.hasNext() )
            {
                SpatialIndividual mimickingNeighbor = mimickingNeighborsIter.next();
                indEvents.remove( mimickingNeighbor.getID() );
                _LOG.debug( "Reseting event time for ind=["
                        + mimickingNeighbor.getID()
                        + "]" );
            }

            // Don't forget the individual itself
            indEvents.remove( earliestEvent.getDeparted().getID() );
            _LOG.debug( "Reseting event time for ind=["
                    + earliestEvent.getDeparted().getID()
                    + "]" );

            // Save the event's time
            previousEventsTime = earliestEvent.getTime();
            totalSimulationTime += earliestEvent.getTime();
        }

        // Was the simulation successful?
        boolean successful = false;
        if( 0 == _simState.getRemainingCount() )
        {
            // Yup, notify all the initiators
            successful = true;
            Iterator<InitiatorData> initiatorDataIter = initiators.values().iterator();
            while( initiatorDataIter.hasNext() )
            {
                initiatorDataIter.next().initiator.signalInitiationSuccess( _simState );
            }

            _LOG.debug( "Simulation was SUCCESSFUL" );
        }
        else
        {
            _LOG.debug( "Simulation FAILED" );
        }

        // Gather the results from this run
        _reporter.gatherSimulationResults( successful,
                initiators.keySet(),
                maxInitiatorCount,
                departureHistory );


        _LOG.trace( "Leaving executeSimulation()" );
    }

    /**
     * Determines if the simulation is still active
     *
     * @return <code>true</code> if active, otherwise, <code>false</code>
     */
    private boolean isSimulationActive()
    {
        return (_simState.getRemainingCount() > 0)
                && (_simState.getDepartedCount() > 0);
    }

    /**
     * Builds the next departure event for the given individual
     *
     * @param ind The individual
     * @param departureTimes The departure times of all individuals
     * @return The next departure event
     */
    private DepartureEvent buildDepartureEvent( SpatialIndividual ind,
            Map<Object,Float> departureTimes )
    {
        _LOG.trace( "Entering buildDepartureEvent( ind, departureTimes )" );

        DepartureEvent event = null;

        _LOG.debug( "Building departure event ind=[" + ind.getID() + "]" );

        // Get the event time calculator
        EventTimeCalculator eventTimeCalc = _simState.getEventTimeCalculator();

        // Is the individual already departed?
        if( null != ind.getGroupID() )
        {
            // Yup, check if they are a leader
            if( null == ind.getLeader() )
            {
                _LOG.debug( "Individual is a leader" );

                // Yup, they are a leader, get their cancellation time
                float cancelTime = Float.POSITIVE_INFINITY;
                if( ind.getImmediateFollowerCount() < ind.getNearestNeighborCount() )
                {
                    _LOG.debug( "immediateFollowerCount=["
                            + ind.getImmediateFollowerCount()
                            + "] < nearestNeighborCount=["
                            + ind.getNearestNeighborCount()
                            + "]" );
                    cancelTime = eventTimeCalc.calculateCancelTime(
                            ind,
                            ind.getNearestNeighborsFollowingCount() + 1 );
                }
                else
                {
                    _LOG.debug( "Leader will not cancel: followerCount=["
                            + ind.getImmediateFollowerCount()
                            + "] nearestNeighborCount=["
                            + ind.getNearestNeighborCount()
                            + "]" );
                }

                // Build the departure event
                event = new DepartureEvent( ind,
                        null,
                        DepartureEvent.Type.CANCEL,
                        cancelTime );

                _LOG.debug( "Built new event ["
                        + event
                        + "]" );
            }
            else
            {
                // No, they aren't a leader
                // IGNORE THIS SITUATION FOR NOW
            }
        }
        else
        {
            // Nope, find out if there is anyone already departed to follow
            Map<Object,List<Neighbor>> groupMemberships =
                    new HashMap<Object,List<Neighbor>>();
            List<Neighbor> neighbors = ind.getNearestNeighbors();
            Iterator<Neighbor> neighborIter = neighbors.iterator();
            while( neighborIter.hasNext() )
            {
                Neighbor currentNeighbor = neighborIter.next();

                // Do they belong to a group?
                Object groupID = currentNeighbor.getIndividual().getGroupID();
                if( null != groupID )
                {
                    // Yup, get the list of neighbors in this group
                    List<Neighbor> groupNeighbors = groupMemberships.get( groupID );
                    if( null == groupNeighbors )
                    {
                        groupNeighbors = new LinkedList<Neighbor>();
                        groupMemberships.put( groupID, groupNeighbors );
                    }

                    // Add it to the list
                    groupNeighbors.add( currentNeighbor );

                    _LOG.debug( "Neighbor ["
                            + currentNeighbor.getIndividual().getID()
                            + "] is a member of group ["
                            + groupID
                            + "]" );
                }
            }

            // Is there anyone to follow?
            if( groupMemberships.isEmpty() )
            {
                // Nope, the individual can initiate
                float initiationTime = eventTimeCalc.calculateInitiationTime( ind );

                // Build the departure event
                event = new DepartureEvent( ind,
                        null,
                        DepartureEvent.Type.INITIATE,
                        initiationTime );
            }
            else
            {
                // Yup, find out which follow event is first
                event = new DepartureEvent(
                        null,
                        null,
                        null,
                        Float.POSITIVE_INFINITY );
                Iterator<Object> groupIDIter = groupMemberships.keySet().iterator();
                while( groupIDIter.hasNext() )
                {
                    // Get the group and its observed members
                    Object groupID = groupIDIter.next();
                    List<Neighbor> groupNeighbors = groupMemberships.get( groupID );

                    // Find out how many members there are
                    int groupSize = groupNeighbors.size();
//                    _LOG.debug( "Group ["
//                            + groupID
//                            + "] has ["
//                            + groupSize
//                            + "] observed members" );

                    // Find out who the first observed member is
                    SpatialIndividual firstMover = null;
                    float earliestDepartureTime = Float.POSITIVE_INFINITY;
                    neighborIter = groupNeighbors.iterator();
                    while( neighborIter.hasNext() )
                    {
                        Neighbor currentNeighbor = neighborIter.next();

                        // Get the departure time
                        Float departureTime = departureTimes.get(
                                currentNeighbor.getIndividual().getID() );

                        // Is it the earliest?
                        if( (null != departureTime)
                                && (departureTime.floatValue() < earliestDepartureTime) )
                        {
                            // Yup
                            firstMover = currentNeighbor.getIndividual();
                            earliestDepartureTime = departureTime.floatValue();
//                            _LOG.debug( "Earliest observed group member: ind=["
//                                    + firstMover
//                                    + "] time=["
//                                    + earliestDepartureTime
//                                    + "]" );
                        }
                    }

                    // Calculate the time
                    float followTime = eventTimeCalc.calculateFollowTime(
                            ind,
                            firstMover,
                            groupSize,
                            ind.getNearestNeighborCount() );

                    // Is it the earliest one?
                    if( followTime < event.getTime() )
                    {
                        // Yup
                        event = new DepartureEvent( ind,
                                firstMover,
                                DepartureEvent.Type.FOLLOW,
                                followTime );
                    }
                }
            }
        }

        _LOG.trace( "Leaving buildDepartureEvent( ind, departureTimes )" );

        return event;
    }
}
