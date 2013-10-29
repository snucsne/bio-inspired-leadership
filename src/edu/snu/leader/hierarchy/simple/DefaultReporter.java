/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * DefaultReporter
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DefaultReporter implements Reporter
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DefaultReporter.class.getName() );

    /** Key for the statistics file */
    private static final String _STATS_FILE_KEY = "stats-file";

    /** Stats file spacer comment */
    private static final String _STATS_SPACER =
            "# =========================================================";


    /** The writer to which the results are reported */
    private PrintWriter _writer = null;

    /** The simulation state */
    private SimulationState _simState = null;


    /**
     * Initializes this reporter
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hierarchy.simple.Reporter#initialize(edu.snu.leader.hierarchy.simple.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( state )" );

        // Store the simulatino state
        _simState = simState;

        // Grab the properties
        Properties props = simState.getProps();

        // Load the statistics filename
        String statsFile = props.getProperty( _STATS_FILE_KEY );
        Validate.notEmpty( statsFile, "Statistics file may not be empty" );

        // Create the statistics writer
        try
        {
            _writer = new PrintWriter( new BufferedWriter(
                    new FileWriter( statsFile ) ) );
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to open stats file ["
                    + statsFile
                    + "]", ioe );
            throw new RuntimeException( "Unable to open stats file ["
                    + statsFile
                    + "]", ioe );
        }

        // Log the system properties to the stats file for future reference
        _writer.println( "# Started: " + (new Date()) );
        _writer.println( _STATS_SPACER );
        _writer.println( "# Simulation properties" );
        _writer.println( _STATS_SPACER );
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
        _writer.println( _STATS_SPACER );
        _writer.println();

        _LOG.trace( "Leaving initialize( state )" );
    }

    /**
     * Report the final results of the simulation
     *
     * @see edu.snu.leader.hierarchy.simple.Reporter#reportFinalResults()
     */
    @Override
    public void reportFinalResults()
    {
        // Create some handy variables
        long firstActiveTimestep = Long.MAX_VALUE;
        long lastActiveTimestep = Long.MIN_VALUE;
        int initiatorCount = 0;

        // Gather some statistics
        DescriptiveStatistics immediateFollowerStats = new DescriptiveStatistics();
        DescriptiveStatistics initiatorDistanceStats = new DescriptiveStatistics();
        DescriptiveStatistics activeTimestepStats = new DescriptiveStatistics();

        // Iterate through all the individuals
        Iterator<Individual> indIter = _simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            Individual ind = indIter.next();

            // Get some statistics
            immediateFollowerStats.addValue( ind.getImmediateFollowerCount() );
            initiatorDistanceStats.addValue( ind.getDistanceToInitiator() );
            activeTimestepStats.addValue( ind.getActiveTimestep() );

            // Build the prefix
            String prefix = "individual." + ind.getID() + ".";

            // Log out important information
            _writer.println( prefix + "group-id = "
                    + ind.getGroupID() );
            _writer.println( prefix + "active-timestep = "
                        + ind.getActiveTimestep() );
            _writer.println( prefix + "immediate-follower-count = "
                        + ind.getImmediateFollowerCount() );
            _writer.println( prefix + "total-follower-count = "
                        + ind.getTotalFollowerCount() );
            _writer.println( prefix + "distance-to-initiator = "
                    + ind.getDistanceToInitiator() );
            _writer.println( prefix + "location = "
                    + ind.getLocation().getX()
                    + " "
                    + ind.getLocation().getY() );
            _writer.println( prefix + "threshold = "
                    + ind.getThreshold() );
            _writer.println( prefix + "skill = "
                    + ind.getSkill() );
            _writer.println( prefix + "confidence = "
                    + ind.getConfidence() );
            _writer.println( prefix + "reputation = "
                    + ind.getReputation() );
            _writer.println( prefix + "boldness = "
                    + ind.getBoldness() );

            // Get the leader's ID, if it exists
            Object leaderID = "";
            if( null != ind.getLeader() )
            {
                leaderID = ind.getLeader().getIndividual().getID();
            }
            else
            {
                ++initiatorCount;
            }
            _writer.println( prefix + "leader = "
                    + leaderID );

            // Build the list of neighbor ID's
            StringBuilder builder = new StringBuilder();
            Iterator<Neighbor> neighborIter = ind.getNearestNeighbors().iterator();
            while( neighborIter.hasNext() )
            {
                builder.append( neighborIter.next().getIndividual().getID() );
                builder.append( " " );
            }
            _writer.println( prefix + "nearest-neighbors = "
                    + builder.toString() );

            // Build the list of follower ID's
            builder = new StringBuilder();
            neighborIter = ind.getFollowers().iterator();
            while( neighborIter.hasNext() )
            {
                builder.append( neighborIter.next().getIndividual().getID() );
                builder.append( " " );
            }
            _writer.println( prefix + "immediate-followers = "
                    + builder.toString() );

            // Check the activity time
            if( firstActiveTimestep > ind.getActiveTimestep() )
            {
                firstActiveTimestep = ind.getActiveTimestep();
            }
            if( lastActiveTimestep < ind.getActiveTimestep() )
            {
                lastActiveTimestep = ind.getActiveTimestep();
            }

            _writer.println();
        }

        // Log the simulation information
        _writer.println( "simulation.first-active-timestep = "
                + firstActiveTimestep );
        _writer.println( "simulation.last-active-timestep = "
                + lastActiveTimestep );
        _writer.println( "simulation.initiator-count = "
                + initiatorCount );

        // Log the stats
        _writer.println( "statistics.immediate-followers.mean = "
                + immediateFollowerStats.getMean() );
        _writer.println( "statistics.immediate-followers.std-dev = "
                + immediateFollowerStats.getStandardDeviation() );
        _writer.println( "statistics.immediate-followers.min = "
                + immediateFollowerStats.getMin() );
        _writer.println( "statistics.immediate-followers.max = "
                + immediateFollowerStats.getMax() );

        _writer.println( "statistics.initiator-distance.mean = "
                + initiatorDistanceStats.getMean() );
        _writer.println( "statistics.initiator-distance.std-dev = "
                + initiatorDistanceStats.getStandardDeviation() );
        _writer.println( "statistics.initiator-distance.min = "
                + initiatorDistanceStats.getMin() );
        _writer.println( "statistics.initiator-distance.max = "
                + initiatorDistanceStats.getMax() );

        _writer.println( "statistics.active-timestep.mean = "
                + activeTimestepStats.getMean() );
        _writer.println( "statistics.active-timestep.std-dev = "
                + activeTimestepStats.getStandardDeviation() );
        _writer.println( "statistics.active-timestep.min = "
                + activeTimestepStats.getMin() );
        _writer.println( "statistics.active-timestep.max = "
                + activeTimestepStats.getMax() );

        // Log out the stop time
        _writer.println();
        _writer.println( _STATS_SPACER );
        _writer.println( "# Finished: " + (new Date()) );

        // Close out the writer
        _writer.close();
    }

}
