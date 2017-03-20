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
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.event.DepartureEvent;
import edu.snu.leader.hidden.event.EventTimeCalculator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * ResultsReporter
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DefaultResultsReporter implements ResultsReporter
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DefaultResultsReporter.class.getName() );

    /** Key for the results file */
    private static final String _RESULTS_FILE_KEY = "results-file";

    /** Key for the simulation log file flag */
    private static final String _USE_SIM_LOG_FILE_FLAG_KEY = "use-sim-log-file-flag";

    /** Key for the location log file flag */
    private static final String _USE_LOCATION_LOG_FILE_FLAG_KEY =
            "use-location-log-file-flag";

    /** Stats file spacer comment */
    private static final String _SPACER =
            "# =========================================================";

    private static class SimulationLog
    {
        public boolean successful = false;
        public int finalInitiatorCount = 0;
        public int maxInitiatorCount = 0;
        public List<DepartureEvent> departureHistory =
                new LinkedList<DepartureEvent>();
        /**
         * Builds this SimulationLog object
         *
         * @param successful
         * @param finalInitiatorCount
         * @param maxInitiatorCount
         * @param departureHistory
         */
        public SimulationLog( boolean successful,
                int finalInitiatorCount,
                int maxInitiatorCount,
                List<DepartureEvent> departureHistory )
        {
            this.successful = successful;
            this.finalInitiatorCount = finalInitiatorCount;
            this.maxInitiatorCount = maxInitiatorCount;
            this.departureHistory = departureHistory;
        }


    }



    /** The writer to which the results are reported */
    private PrintWriter _writer = null;

    /** The writer to which simulation logs are reported */
    private PrintWriter _logWriter = null;

    /** The writer to which location logs are reported */
    private PrintWriter _locationWriter = null;

    /** Flag denoting whether or not to use the sim log file */
    private boolean _useSimLogFile = false;

    /** Flag denoting whether or not to use the location log file */
    private boolean _useLocationLogFile = false;

    /** The simulation state */
    private SimulationState _simState = null;

    /** The number of movements resulting each size */
//    private int[] _movementCounts = new int[0];
    private Map<Task,int[]> _movementCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The final number of initiators */
//    private int[] _finalInitiatorCounts = new int[0];
    private Map<Task,int[]> _finalInitiatorCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The final number of initiators in a successful simulation */
//    private int[] _finalSuccessfulInitiatorCounts = new int[0];
    private Map<Task,int[]> _finalSuccessfulInitiatorCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The final number of initiators in a failed simulation */
//    private int[] _finalFailedInitiatorCounts = new int[0];
    private Map<Task,int[]> _finalFailedInitiatorCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The max number of initiators */
//    private int[] _maxInitiatorCounts = new int[0];
    private Map<Task,int[]> _maxInitiatorCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The max number of initiators in a successful simulation */
//    private int[] _maxSuccessfulInitiatorCounts = new int[0];
    private Map<Task,int[]> _maxSuccessfulInitiatorCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The max number of initiators in a failed simulation */
//    private int[] _maxFailedInitiatorCounts = new int[0];
    private Map<Task,int[]> _maxFailedInitiatorCounts =
            new EnumMap<Task,int[]>( Task.class );

    /** The number of successful simulations */
//    private int _successfulSimulations = 0;
    private Map<Task,Integer> _successfulSimulations =
            new EnumMap<Task,Integer>( Task.class );



    /**
     * TODO Method description
     *
     * @param simState
     * @see edu.snu.leader.hidden.ResultsReporter#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Save the simulation state
        _simState = simState;

        // Grab the properties
        Properties props = simState.getProps();

        // Build some variables
        int individualCount = _simState.getIndividualCount();
        for( Task task : Task.values() )
        {
            _movementCounts.put( task, new int[ individualCount + 1 ] );
            _finalInitiatorCounts.put( task, new int[ individualCount + 1 ] );
            _finalSuccessfulInitiatorCounts.put( task, new int[ individualCount + 1 ] );
            _finalFailedInitiatorCounts.put( task, new int[ individualCount + 1 ] );
            _maxInitiatorCounts.put( task, new int[ individualCount + 1 ] );
            _maxSuccessfulInitiatorCounts.put( task, new int[ individualCount + 1 ] );
            _maxFailedInitiatorCounts.put( task, new int[ individualCount + 1 ] );
            _successfulSimulations.put( task, new Integer(0) );
        }

        // Load the results filename
        String resultsFile = props.getProperty( _RESULTS_FILE_KEY );
        Validate.notEmpty( resultsFile, "Results file may not be empty" );

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

        // Do we log simulations?
        String useSimLogFileStr = props.getProperty( _USE_SIM_LOG_FILE_FLAG_KEY );
        if( null != useSimLogFileStr )
        {
            _useSimLogFile = Boolean.parseBoolean( useSimLogFileStr );
            _LOG.info( "Using _useSimLogFile=["
                    + _useSimLogFile
                    + "]" );
        }

        if( _useSimLogFile )
        {
            // Build the compressed simulation log file
            int lastDotIdx = resultsFile.lastIndexOf( '.' );
            String simLogFile = resultsFile.substring( 0, lastDotIdx )
                    + ".log.gz";
            _LOG.warn( "Sending simulation log to [" + simLogFile + "]" );

            // Build the log writer
            try
            {
                _logWriter = new PrintWriter( new BufferedWriter(
                        new OutputStreamWriter(
                                new GZIPOutputStream(
                                        new FileOutputStream( simLogFile ) ) ) ) );
            }
            catch( IOException ioe )
            {
                _LOG.error( "Unable to open simulation log file ["
                        + simLogFile
                        + "]", ioe );
                throw new RuntimeException( "Unable to open simulation log file ["
                        + simLogFile
                        + "]", ioe );
            }
        }

        // Do we log locations?
        String useLocationLogFileStr = props.getProperty( _USE_LOCATION_LOG_FILE_FLAG_KEY );
        if( null != useLocationLogFileStr )
        {
            _useLocationLogFile = Boolean.parseBoolean( useLocationLogFileStr );
            _LOG.info( "Using _useLocationLogFile=["
                    + _useLocationLogFile
                    + "]" );
        }

        if( _useLocationLogFile )
        {
            // Build the compressed location log file
            int lastDotIdx = resultsFile.lastIndexOf( '.' );
            String simLocationFile = resultsFile.substring( 0, lastDotIdx )
                    + ".locations.gz";
            _LOG.warn( "Sending location log to [" + simLocationFile + "]" );

            // Build the location log writer
            try
            {
                _locationWriter = new PrintWriter( new BufferedWriter(
                        new OutputStreamWriter(
                                new GZIPOutputStream(
                                        new FileOutputStream( simLocationFile ) ) ) ) );
                // );
            }
            catch( IOException ioe )
            {
                _LOG.error( "Unable to open location log file ["
                        + simLocationFile
                        + "]", ioe );
                throw new RuntimeException( "Unable to open location log file ["
                        + simLocationFile
                        + "]", ioe );
            }
        }

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * TODO Method description
     *
     * @param successful
     * @param finalInitiators
     * @param maxInitiatorCount
     * @param departureHistory
     * @see edu.snu.leader.hidden.ResultsReporter#gatherSimulationResults(boolean, java.util.Set, int, java.util.List)
     */
    @Override
    public void gatherSimulationResults( boolean successful,
            Set<SpatialIndividual> finalInitiators,
            int maxInitiatorCount,
            List<DepartureEvent> departureHistory )
    {
        Task task = _simState.getCurrentTask();
        
        // Get the total number of individuals that departed
        (_movementCounts.get( task ))[ _simState.getMaxDepartedCount() ]++;
        int finalInitiatorCount = finalInitiators.size();
        (_finalInitiatorCounts.get( task ))[ finalInitiatorCount ]++;
        (_maxInitiatorCounts.get( task ))[ maxInitiatorCount ]++;

        // Find out if the simulation was successful
        if( successful )
        {
            int successfulSims = _successfulSimulations.get( task ).intValue();
            _successfulSimulations.put( task, new Integer( successfulSims + 1 ) );
            (_finalSuccessfulInitiatorCounts.get( task ))[ finalInitiatorCount ]++;
            (_maxSuccessfulInitiatorCounts.get( task ))[ maxInitiatorCount ]++;
        }
        else
        {
            (_finalFailedInitiatorCounts.get( task ))[ finalInitiatorCount ]++;
            (_maxFailedInitiatorCounts.get( task ))[ maxInitiatorCount ]++;
        }


        if( _useSimLogFile )
        {
            // Dump the simulation log
            StringBuilder builder = new StringBuilder();
            builder.append( String.format( "%10S ", task.name() ) );
            builder.append( (successful ? "S  " : "F  " ) );
            builder.append( String.format( "%03d  %03d ",
                    finalInitiatorCount,
                    maxInitiatorCount ) );

            Iterator<DepartureEvent> historyIter = departureHistory.iterator();
            while( historyIter.hasNext() )
            {
                DepartureEvent currentEvent = historyIter.next();
                SpatialIndividual departed = currentEvent.getDeparted();
                builder.append( " [" );
                builder.append( departed.getID() );
                builder.append( "  " );
                builder.append( currentEvent.getLeaderID() );
                builder.append( "  " );
                builder.append( currentEvent.getType().getShortCode() );
                builder.append( "  " );
                builder.append( String.format( "%09.3f", currentEvent.getTime() ) );
                builder.append( "  " );
                builder.append( String.format( "%06.4f", departed.getPersonalityTrait( PersonalityTrait.BOLD_SHY ) ) );
                builder.append( "  " );
                builder.append( String.format( "%06.4f", departed.getPersonalityTrait( PersonalityTrait.ACTIVE_LAZY ) ) );
                builder.append( "  " );
                builder.append( String.format( "%06.4f", departed.getPersonalityTrait( PersonalityTrait.SOCIAL_SOLITARY ) ) );
                builder.append( "  " );
                builder.append( String.format( "%06.4f", departed.getPersonalityTrait(  PersonalityTrait.FEARFUL_ASSERTIVE ) ) );
                builder.append( "  " );
                builder.append( String.format( "%03d", currentEvent.getFollowerCount() ) );
                builder.append( "  " );
                builder.append( String.format( "%03d", currentEvent.getPotentialFollowerCount() ) );
                builder.append( "] " );
            }

            // Print it
            _logWriter.println( builder.toString() );
        }

        if( _useLocationLogFile )
        {
            // Dump a log of all the locations
            StringBuilder builder = new StringBuilder();
            builder.append( String.format( "%10S ", task.name() ) );
            builder.append( (successful ? "S [" : "F [" ) );

            Iterator<SpatialIndividual> initiatorIter = null;
            if( successful )
            {
                // Use the final initiators
                initiatorIter = finalInitiators.iterator();
            }
            else
            {
                // Use the canceled initiators
                initiatorIter = _simState.getCanceledInitiators().iterator();
            }

            // List all the initiator IDs
            while( initiatorIter.hasNext() )
            {
                builder.append( initiatorIter.next().getID() );
                if( initiatorIter.hasNext() )
                {
                    builder.append( ":" );
                }
            }
            builder.append( "] " );

            // Get all the individuals
            Iterator<SpatialIndividual> indIter = _simState.getAllIndividuals().iterator();
            while( indIter.hasNext() )
            {
                SpatialIndividual ind = indIter.next();

                // Get the individual's location
                Vector2D location = ind.getLocation();
                builder.append( " [" );
                builder.append( ind.getID() );
                builder.append( String.format( ":(%+010.5f,%+010.5f)",
                        location.getX(),
                        location.getY()) );

                // Get the individual's nearest neighbors
                Iterator<Neighbor> neighborIter =  ind.getNearestNeighbors().iterator();
                while( neighborIter.hasNext() )
                {
                    builder.append( ":" );
                    builder.append( neighborIter.next().getIndividual().getID() );
                }
                builder.append( "]" );
            }

            // Print it
            _locationWriter.println( builder.toString() );
            _locationWriter.flush();
        }
    }

    /**
     * TODO Method description
     *
     * @see edu.snu.leader.hidden.ResultsReporter#reportFinalResults()
     */
    @Override
    public void reportFinalResults()
    {
        // Calculate the total number of initiation attempts and successes
        int totalInitiations = 0;
        int totalSuccesses = 0;
        Iterator<SpatialIndividual> indIter = _simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            SpatialIndividual current = indIter.next();
            totalInitiations += current.getInitiationAttempts();
            totalSuccesses += current.getInitiationSuccesses();
        }

        // Calculate the total number of successful simulations
        int successfulSimulations = 0;
        for( Task task : Task.values() )
        {
            successfulSimulations += _successfulSimulations.get( task ).intValue();
        }
        
        _writer.println( _SPACER );
        _writer.println( "# Initiation stats" );
        _writer.println( "initiations = " + totalInitiations );
        _writer.println( "successes = " + totalSuccesses );
        _writer.println( "total-simulations = " + _simState.getSimulationCount() );
        _writer.println( "total-successful-simulations = " + successfulSimulations );
        _writer.println( "total-leadership-success = "
                    + (((float) successfulSimulations) / ((float) _simState.getSimulationCount())) );
        _writer.println();
        _writer.println();

        // Print out the movement counts
        _writer.println( _SPACER );
        _writer.println( "# Movement counts" );
        for( Task task : Task.values() )
        {
            int[] movementCounts = _movementCounts.get( task );
            for( int i = 0; i < movementCounts.length; i++ )
            {
                _writer.println( "move."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + movementCounts[i] );
            }
        }
        _writer.println();
        _writer.println();

        // Print out the movement frequencies
        _writer.println( _SPACER );
        _writer.println( "# Movement frequencies" );
        for( Task task : Task.values() )
        {
            int[] movementCounts = _movementCounts.get( task );
            for( int i = 0; i < movementCounts.length; i++ )
            {
                _writer.println( "move-frequency."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + (movementCounts[i] / (float) totalInitiations) );
            }
        }
        _writer.println();
        _writer.println();

        // Print out the final initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Final initiator counts" );
        for( Task task : Task.values() )
        {
            int[] finalInitiatorCounts = _finalInitiatorCounts.get( task );
            for( int i = 0; i < finalInitiatorCounts.length; i++ )
            {
                _writer.println( "final-initiators."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + finalInitiatorCounts[i] );
            }
        }
        _writer.println();
        _writer.println();

        // Print out the final successful initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Final successful initiator counts" );
        for( Task task : Task.values() )
        {
            int[] finalSuccessfulInitiatorCounts = _finalSuccessfulInitiatorCounts.get( task );
            for( int i = 0; i < finalSuccessfulInitiatorCounts.length; i++ )
            {
                _writer.println( "final-successful-initiators."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + finalSuccessfulInitiatorCounts[i] );
        }
        }
        _writer.println();
        _writer.println();

        // Print out the final failed initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Final failed initiator counts" );
        for( Task task : Task.values() )
        {
            int[] finalFailedInitiatorCounts = _finalFailedInitiatorCounts.get( task );
            for( int i = 0; i < finalFailedInitiatorCounts.length; i++ )
            {
                _writer.println( "final-failed-initiators."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + finalFailedInitiatorCounts[i] );
            }
        }
        _writer.println();
        _writer.println();

        // Print out the max initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Max initiator counts" );
        for( Task task : Task.values() )
        {
            int[] maxInitiatorCounts = _maxInitiatorCounts.get( task );
            for( int i = 0; i < maxInitiatorCounts.length; i++ )
            {
                _writer.println( "max-initiators."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + maxInitiatorCounts[i] );
            }
        }
        _writer.println();
        _writer.println();

        // Print out the max successful initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Max successful initiator counts" );
        for( Task task : Task.values() )
        {
            int[] maxSuccessfulInitiatorCounts = _maxSuccessfulInitiatorCounts.get( task );
            for( int i = 0; i < maxSuccessfulInitiatorCounts.length; i++ )
            {
                _writer.println( "max-successful-initiators."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + maxSuccessfulInitiatorCounts[i] );
            }
        }
        _writer.println();
        _writer.println();

        // Print out the max failed initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Max failed initiator counts" );
        for( Task task : Task.values() )
        {
            int[] maxFailedInitiatorCounts = _maxFailedInitiatorCounts.get( task );
            for( int i = 0; i < maxFailedInitiatorCounts.length; i++ )
            {
                _writer.println( "max-failed-initiators."
                        + task.name().toLowerCase()
                        + "."
                        + String.format( "%02d", i )
                        + " = "
                        + maxFailedInitiatorCounts[i] );
            }
        }
        _writer.println();
        _writer.println();


        // Print out the individual initiation stats
        _writer.println( _SPACER );
        _writer.println( "# Individual initiation stats" );
        indIter = _simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            SpatialIndividual current = indIter.next();
            _writer.println( "initiation."
                    + current.getID()
                    + ".attempts = "
                    + current.getInitiationAttempts() );
            _writer.println( "initiation."
                    + current.getID()
                    + ".successes = "
                    + current.getInitiationSuccesses() );
            _writer.println( "initiation."
                    + current.getID()
                    + ".attempt-percentage = "
                    + ( ((float) current.getInitiationAttempts())
                            / totalInitiations ) );
            _writer.println( "initiation."
                    + current.getID()
                    + ".success-percentage = "
                    + ( ((float) current.getInitiationSuccesses())
                            / totalSuccesses ));
        }
        _writer.println();
        _writer.println();


        // Print out the information about each individual
        _writer.println( _SPACER );
        _writer.println( "# Individual data" );
        indIter = _simState.getAllIndividuals().iterator();
        float xTotal = 0.0f;
        float yTotal = 0.0f;
        while( indIter.hasNext() )
        {
            SpatialIndividual current = indIter.next();
            _writer.println( current.describe() );
            _writer.println();

            xTotal += current.getLocation().getX();
            yTotal += current.getLocation().getY();
        }
        _writer.println();
        _writer.println();

        // Get the mean position
        _writer.println( _SPACER );
        _writer.println( "# Aggregate data" );

        float meanX = xTotal / _simState.getIndividualCount();
        float meanY = yTotal / _simState.getIndividualCount();
        _writer.println( "mean.position = "
                + String.format( "%06.4f", meanX )
                + " "
                + String.format( "%06.4f", meanY ) );
        _writer.println();
        _writer.println();

        // Get the event time calculator
        EventTimeCalculator eventTimeCalc = _simState.getEventTimeCalculator();

        // Print rate header
        _writer.println( _SPACER );
        _writer.println( "# Rate information" );

        // Printout the initiation description
        _writer.println( eventTimeCalc.describeInitiation() );
        _writer.println();

        // Print out the follow description
        _writer.println( eventTimeCalc.describeFollow() );
        _writer.println();

        // Print out the cancel description
        _writer.println( eventTimeCalc.describeCancellation() );
        _writer.println();

        if( _useSimLogFile )
        {
            // Close the log writer
            _logWriter.close();
        }

        if( _useLocationLogFile )
        {
            // Close the locations write
            _locationWriter.close();
        }

        // Close the results writer
        _writer.close();
    }
}
