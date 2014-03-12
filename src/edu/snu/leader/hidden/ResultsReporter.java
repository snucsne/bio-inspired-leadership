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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

/**
 * ResultsReporter
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ResultsReporter
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ResultsReporter.class.getName() );

    /** Key for the results file */
    private static final String _RESULTS_FILE_KEY = "results-file";

    /** Key for the simulation log file flag */
    private static final String _USE_SIM_LOG_FILE_FLAG_KEY = "use-sim-log-file-flag";

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

    /** Flag denoting whether or not to use the sim log file */
    private boolean _useSimLogFile = false;

    /** The simulation state */
    private SimulationState _simState = null;

    /** The number of movements resulting each size */
    private int[] _movementCounts = new int[0];

    /** The final number of initiators */
    private int[] _finalInitiatorCounts = new int[0];

    /** The final number of initiators in a successful simulation */
    private int[] _finalSuccessfulInitiatorCounts = new int[0];

    /** The final number of initiators in a failed simulation */
    private int[] _finalFailedInitiatorCounts = new int[0];

    /** The max number of initiators */
    private int[] _maxInitiatorCounts = new int[0];

    /** The max number of initiators in a successful simulation */
    private int[] _maxSuccessfulInitiatorCounts = new int[0];

    /** The max number of initiators in a failed simulation */
    private int[] _maxFailedInitiatorCounts = new int[0];

    /** The number of successful simulations */
    private int _successfulSimulations = 0;



    /**
     * Initialize this reporter
     *
     * @param simState
     */
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Save the simulation state
        _simState = simState;

        // Grab the properties
        Properties props = simState.getProps();

        // Build some variables
        int individualCount = _simState.getIndividualCount();
        _movementCounts = new int[ individualCount + 1 ];
        _finalInitiatorCounts = new int[ individualCount + 1 ];
        _finalSuccessfulInitiatorCounts = new int[ individualCount + 1 ];
        _finalFailedInitiatorCounts = new int[ individualCount + 1 ];
        _maxInitiatorCounts = new int[ individualCount + 1 ];
        _maxSuccessfulInitiatorCounts = new int[ individualCount + 1 ];
        _maxFailedInitiatorCounts = new int[ individualCount + 1 ];

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
                _LOG.error( "Unable to open log file ["
                        + simLogFile
                        + "]", ioe );
                throw new RuntimeException( "Unable to open log file ["
                        + resultsFile
                        + "]", ioe );
            }
        }

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Gather the results from the just-finished simulation
     *
     * @param successful Flag signaling whether or not the initiation was
     *                   successful
     * @param finalInitiatorCount The final count of initiators
     * @param maxInitiatorCount The max count of initiators
     * @param departureHistory
     */
    public void gatherSimulationResults( boolean successful,
            int finalInitiatorCount,
            int maxInitiatorCount,
            List<DepartureEvent> departureHistory )
    {
        // Get the total number of individuals that departed
        _movementCounts[ _simState.getMaxDepartedCount() ]++;
        _finalInitiatorCounts[ finalInitiatorCount ]++;
        _maxInitiatorCounts[ maxInitiatorCount ]++;

        // Find out if the simulation was successful
        if( successful )
        {
            _successfulSimulations++;
            _finalSuccessfulInitiatorCounts[ finalInitiatorCount ]++;
            _maxSuccessfulInitiatorCounts[ maxInitiatorCount ]++;
        }
        else
        {
            _finalFailedInitiatorCounts[ finalInitiatorCount ]++;
            _maxFailedInitiatorCounts[ maxInitiatorCount ]++;
        }


        if( _useSimLogFile )
        {
            // Dump the simulation log
            StringBuilder builder = new StringBuilder();
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
                builder.append( String.format( "%06.4f", departed.getPersonality() ) );
                builder.append( "] " );
            }

            // Print it
            _logWriter.println( builder.toString() );
        }
    }

    /**
     * Report the final results
     */
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

        _writer.println( _SPACER );
        _writer.println( "# Initiation stats" );
        _writer.println( "initiations = " + totalInitiations );
        _writer.println( "successes = " + totalSuccesses );
        _writer.println( "total-simulations = " + _simState.getSimulationCount() );
        _writer.println( "total-successful-simulations = " + _successfulSimulations );
        _writer.println( "total-leadership-success = "
                    + (((float) _successfulSimulations) / ((float) _simState.getSimulationCount())) );
        _writer.println();
        _writer.println();

        // Print out the movement counts
        _writer.println( _SPACER );
        _writer.println( "# Movement counts" );
        for( int i = 0; i < _movementCounts.length; i++ )
        {
            _writer.println( "move."
                    + String.format( "%02d", i )
                    + " = "
                    + _movementCounts[i] );
        }
        _writer.println();
        _writer.println();

        // Print out the movement frequencies
        _writer.println( _SPACER );
        _writer.println( "# Movement frequencies" );
        for( int i = 0; i < _movementCounts.length; i++ )
        {
            _writer.println( "move-frequency."
                    + String.format( "%02d", i )
                    + " = "
                    + (_movementCounts[i] / (float) totalInitiations) );
        }
        _writer.println();
        _writer.println();

        // Print out the final initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Final initiator counts" );
        for( int i = 0; i < _finalInitiatorCounts.length; i++ )
        {
            _writer.println( "final-initiators."
                    + String.format( "%02d", i )
                    + " = "
                    + _finalInitiatorCounts[i] );
        }
        _writer.println();
        _writer.println();

        // Print out the final successful initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Final successful initiator counts" );
        for( int i = 0; i < _finalSuccessfulInitiatorCounts.length; i++ )
        {
            _writer.println( "final-successful-initiators."
                    + String.format( "%02d", i )
                    + " = "
                    + _finalSuccessfulInitiatorCounts[i] );
        }
        _writer.println();
        _writer.println();

        // Print out the final failed initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Final failed initiator counts" );
        for( int i = 0; i < _finalFailedInitiatorCounts.length; i++ )
        {
            _writer.println( "final-failed-initiators."
                    + String.format( "%02d", i )
                    + " = "
                    + _finalFailedInitiatorCounts[i] );
        }
        _writer.println();
        _writer.println();

        // Print out the max initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Max initiator counts" );
        for( int i = 0; i < _maxInitiatorCounts.length; i++ )
        {
            _writer.println( "max-initiators."
                    + String.format( "%02d", i )
                    + " = "
                    + _maxInitiatorCounts[i] );
        }
        _writer.println();
        _writer.println();

        // Print out the max successful initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Max successful initiator counts" );
        for( int i = 0; i < _maxSuccessfulInitiatorCounts.length; i++ )
        {
            _writer.println( "max-successful-initiators."
                    + String.format( "%02d", i )
                    + " = "
                    + _maxSuccessfulInitiatorCounts[i] );
        }
        _writer.println();
        _writer.println();

        // Print out the max failed initiator counts
        _writer.println( _SPACER );
        _writer.println( "# Max failed initiator counts" );
        for( int i = 0; i < _maxFailedInitiatorCounts.length; i++ )
        {
            _writer.println( "max-failed-initiators."
                    + String.format( "%02d", i )
                    + " = "
                    + _maxFailedInitiatorCounts[i] );
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

        // Close the results writer
        _writer.close();
    }
}
