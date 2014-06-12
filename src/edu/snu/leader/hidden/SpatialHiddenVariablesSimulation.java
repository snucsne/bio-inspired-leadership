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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * SpatialHiddenVariablesSimulation
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SpatialHiddenVariablesSimulation
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SpatialHiddenVariablesSimulation.class.getName() );

    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the number of times to run the simulator */
    private static final String _SIMULATION_COUNT_KEY = "simulation-count";

    /** Key for the number of observers */
    private static final String _OBSERVER_COUNT_KEY = "observer-count";


    private class IndividualEvent {
        public final float time;
        public final SpatialIndividual individual;
        public IndividualEvent( float time, SpatialIndividual individual )
        {
            this.time = time;
            this.individual = individual;
        }
    }

    private class InitiatorData {
        public SpatialIndividual initiator = null;
        public int followCount = 0;
        public IndividualEvent followEvent = null;;
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
            // Build, initialize, run
            SpatialHiddenVariablesSimulation sim =
                new SpatialHiddenVariablesSimulation();
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

        // Run the simulation a number of times
        for( int i = 0; i< _simulationCount; i++ )
        {
            _LOG.debug( "Simulation ["
                    + i
                    + "]" );

            // Reset the simulation state
            _simState.reset();

            // Set the simulation index
            _simState.setSimIndex( i );

            // Set up the simulation
            setUpSimulationRun();

            executeSimulation( i );

            // Tear down the simulation
            tearDownSimulationRun();
        }

        // Report the final results
        _reporter.reportFinalResults();



        _LOG.trace( "Leaving run()" );
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
     * Executes a single run of the simulator
     *
     */
    private void executeSimulation( int simIndex )
    {
        _LOG.trace( "Entering executeSimulation()" );

//        if( simIndex >= 147 )
//        {
//            _LOG.getRootLogger().setLevel( Level.DEBUG );
//        }

        // Maintain the departure history
        List<DepartureEvent> departureHistory = new LinkedList<DepartureEvent>();

        // Determine the initiating individual
        List<InitiatorData> initiators = new ArrayList<InitiatorData>();
        IndividualEvent initiateEvent = determineNextInitiationEvent();
        _simState.initiate( initiateEvent.individual );
        initiators.add( new InitiatorData( initiateEvent.individual ) );
        int maxInitiatorCount = 1;
        departureHistory.add( new DepartureEvent( initiateEvent.individual,
                null,
                DepartureEvent.Type.INITIATE,
                initiateEvent.time ) );

        /* Continue the simulation until no individual is remaining or
         * the initiator(s) have cancelled. */
        while( (_simState.getRemainingCount() > 0)
                && (_simState.getDepartedCount() > 0) )
        {
            if( _LOG.isDebugEnabled() )
            {
                _LOG.debug( "Starting simulation step...   remaining=["
                        + _simState.getRemainingCount()
                        + "] departed=["
                        + _simState.getDepartedCount()
                        + "] initiators=["
                        + initiators.size()
                        + "]" );
            }


            // Process each initiator
            InitiatorData earliestInitiatorData = null;
            float earliestEventTime = Float.POSITIVE_INFINITY;
            Iterator<InitiatorData> initiatorDataIter = initiators.iterator();
            while( (_simState.getRemainingCount() > 0)
                    && initiatorDataIter.hasNext() )
            {
                InitiatorData current = initiatorDataIter.next();

                _LOG.debug( "Processing initiator ["
                        + current.initiator.getID()
                        + "]" );

                float currentEarliestEventTime = Float.POSITIVE_INFINITY;

                /* Get the total number of individuals that are following
                 * this initiator */
//                current.followCount = current.initiator.getTotalFollowerCount();
                current.followCount = current.initiator.getImmediateFollowerCount();

                /* Get the individual with the earliest follow time from the
                 * group of individuals that are neighbors with this initiator
                 * or those following this initiator  */
                current.followEvent = determineNextFollowEvent( current );
                if( current.followEvent.time < currentEarliestEventTime )
                {
                    currentEarliestEventTime = current.followEvent.time;
                }

                if( null != current.followEvent.individual )
                {
                    _LOG.debug( "FollowEvent: time=["
                            + current.followEvent.time
                            + "] ind=["
                            + current.followEvent.individual.getID()
                            + "]" );
                }

                // Get this individual's cancellation time
                current.cancelTime = calculateCancellationTime(
                        current.initiator,
                        (current.followCount + 1) );
                if( current.cancelTime < currentEarliestEventTime )
                {
                    currentEarliestEventTime = current.cancelTime;
                }

                _LOG.debug( "Cancellation time ["
                        + current.cancelTime
                        + "]" );

                // Does something for this initiator happen first?
                if( currentEarliestEventTime < earliestEventTime )
                {
                    // Yup
                    earliestEventTime = currentEarliestEventTime;
                    earliestInitiatorData = current;
                    _LOG.debug( "Earliest event ["
                            + earliestEventTime
                            + "]" );
                }
            }

            /* Get the individual with the earliest initiation time from
             * those remaining in the group. */
            initiateEvent = determineNextInitiationEvent();

            _LOG.debug( "InitiationEvent: time=["
                    + initiateEvent.time
                    + "]" );


            // Does a new initiation happen first?
            if( initiateEvent.time < earliestEventTime )
            {
                // Yup
                _simState.initiate( initiateEvent.individual );
                initiators.add( new InitiatorData( initiateEvent.individual ) );
                _LOG.debug( "New initiator ["
                        + initiateEvent.individual.getID()
                        + "]" );

                // Log it
                departureHistory.add( new DepartureEvent(
                        initiateEvent.individual,
                        null,
                        DepartureEvent.Type.INITIATE,
                        initiateEvent.time ) );


                // Update the max number of initiators if necessary
                if( maxInitiatorCount < initiators.size() )
                {
                    maxInitiatorCount = initiators.size();
                }
            }
            else
            {
                // Nope, check the initiator
                if( earliestInitiatorData.followEvent.time
                        < earliestInitiatorData.cancelTime )
                {
                    // A new individual follows this initiator
                    SpatialIndividual follower =
                            earliestInitiatorData.followEvent.individual;
                    _simState.follow( follower.getFirstMover().getIndividual(),
                            earliestInitiatorData.followEvent.individual );
                    _LOG.debug( "New follower" );

                    // Log it
                    departureHistory.add( new DepartureEvent(
                            follower,
                            follower.getFirstMover().getIndividual(),
                            DepartureEvent.Type.FOLLOW,
                            earliestInitiatorData.followEvent.time ) );

                }
                else if( earliestInitiatorData.cancelTime < Float.POSITIVE_INFINITY )
                {
                    // The initiator cancels
                    _simState.cancelInitiation( earliestInitiatorData.initiator );
                    initiators.remove( earliestInitiatorData );
                    _LOG.debug( "Cancelling" );

                    // Log it
                    departureHistory.add( new DepartureEvent(
                            earliestInitiatorData.initiator,
                            null,
                            DepartureEvent.Type.CANCEL,
                            earliestInitiatorData.cancelTime ) );
                }
            }

        }

        // Was the simulation successful?
        boolean successful = false;
        if( 0 == _simState.getRemainingCount() )
        {
//            _LOG.debug( "*** SUCCESSFUL! ***" );

            // Yup, notify all the initiators
            successful = true;
            Iterator<InitiatorData> initiatorDataIter = initiators.iterator();
            while( initiatorDataIter.hasNext() )
            {
                initiatorDataIter.next().initiator.signalInitiationSuccess( _simState );
            }
        }
//        else
//        {
//            _LOG.debug( "*** UNSUCCESSFUL *** ["
//                    + _simState.getRemainingCount()
//                    + "]" );
//        }

        // Gather the results from this run
        _reporter.gatherSimulationResults( successful,
                initiators.size(),
                maxInitiatorCount,
                departureHistory );

        _LOG.trace( "Leaving executeSimulation()" );
    }

    /**
     * Determine the next initiation event from the individuals remaining
     *
     * @return The initiation event
     */
    private IndividualEvent determineNextInitiationEvent()
    {
        SpatialIndividual initiator = null;
        float initiationTime = Float.POSITIVE_INFINITY;
        EventTimeCalculator eventTimeCalc = _simState.getEventTimeCalculator();

        /* Iterate through all the eligible initiators.  These are individuals
         * that are remaining and do NOT have a neighbor that is initiating
         * or following. */
        Iterator<SpatialIndividual> remainingIter =
                _simState.getEligibleInitiatorsIterator();
        while( remainingIter.hasNext() )
        {
            SpatialIndividual current = remainingIter.next();

            // Get the individual's initiation time
            float currentInitiationTime =
                    eventTimeCalc.calculateInitiationTime( current );

            // Is it the earliest initiator?
            if( currentInitiationTime < initiationTime )
            {
                // Yup
                initiationTime = currentInitiationTime;
                initiator = current;
            }
        }

        return new IndividualEvent( initiationTime, initiator );
    }

    /**
     * Determine the next follow event for the specified initiator
     *
     * @param initiatorData The initiator
     * @return The follow event
     */
    private IndividualEvent determineNextFollowEvent( InitiatorData initiatorData )
    {
        // Get some handy values we will reuse
        int followCount = initiatorData.initiator.getTotalFollowerCount();
        int groupSize = _simState.getInitiatorsGroupSize( initiatorData.initiator );
        EventTimeCalculator eventTimeCalc = _simState.getEventTimeCalculator();

        if( _LOG.isDebugEnabled() )
        {
            _LOG.debug( "followCount=["
                    + followCount
                    + "]" );
            _LOG.debug( "immediateFollowCount=["
                    + initiatorData.initiator.getImmediateFollowerCount()
                    + "]" );
            _LOG.debug( "groupSize=["
                    + groupSize
                    + "]" );
        }

        // Create some handy variables
        SpatialIndividual follower = null;
        float followTime = Float.POSITIVE_INFINITY;

        // Iterate through all the potential followers
        Iterator<SpatialIndividual> potentialFollowersIter =
                _simState.getPotentialFollowersIterator( initiatorData.initiator );
        while( potentialFollowersIter.hasNext() )
        {
            SpatialIndividual current = potentialFollowersIter.next();

            /* Do we use all the individuals that have departed or just the
             * nearest neighbors? */
            int departedCount = followCount + 1;
            if( !_simState.useAllDepartedIndividuals() )
            {
                departedCount = current.getNearestNeighborDepartedCount(
                        _simState );
            }

            // Don't forget the initiator!
            float currentFollowTime = eventTimeCalc.calculateFollowTime( current,
                    initiatorData.initiator,
                    departedCount,
                    _simState.getInitiatorsGroupSize( current ) );
// IS THIS RIGHT?

            _LOG.debug( "ind=["
                    + current.getID()
                    + "]  followTime=["
                    + currentFollowTime
                    + "]" );

            // Is it the earliest?
            if( currentFollowTime < followTime )
            {
                // Yup
                followTime = currentFollowTime;
                follower = current;

                _LOG.debug( "New earliest time" );
            }
        }

        return new IndividualEvent( followTime, follower );
    }

    /**
     * Calculates the cancelation time for the given individual and number
     * of individuals that have departed
     *
     * @param ind
     * @param departedCount
     * @return
     */
    private float calculateCancellationTime( SpatialIndividual ind,
            int departedCount )
    {
        float cancelTime = Float.POSITIVE_INFINITY;
        if( _simState.hasPotentialFollowers( ind ) )
        {
            EventTimeCalculator eventTimeCalc = _simState.getEventTimeCalculator();
            cancelTime = eventTimeCalc.calculateCancelTime( ind, departedCount );
        }

        return cancelTime;
    }

}
