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
package edu.snu.leader.hidden.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

// imports
import org.apache.log4j.Logger;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;

import edu.snu.leader.hidden.Neighbor;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MiscUtils;


/**
 * TODO Class description
 *
 * @author Brent Eskridge
 */
public class PositionAnalyzer
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PositionAnalyzer.class.getName() );
    
    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the results file */
    protected static final String _RESULTS_FILE_KEY = "results-file";

    /** Stats file spacer comment */
    private static final String _SPACER =
            "# =========================================================";

    /** Newline string */
    protected static final String _NEWLINE = System.lineSeparator();

    
    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** The writer to which the results are reported */
    private PrintWriter _writer = null;

    /** Flag indicating that the mean resultant vector should be scaled by the
     *  number of nearest neighbors */
    private boolean _scaleMeanResultantVector = true;

    
    /**
     * Main entry into the analyzer
     *
     * @param args
     */
    public static void main( String[] args )
    {
        try
        {
            PositionAnalyzer analyzer = new PositionAnalyzer();
            analyzer.initialize();
            analyzer.run();
        }
        catch( Exception e )
        {
            _LOG.error( "Unknown error", e );
        }
    }

    /**
     * Initialize the analyzer
     */
    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Load the properties
        _props = MiscUtils.loadProperties( _PROPS_FILE_KEY );

        // Initialize the simulation state
        _simState.initialize( _props );
        
        // Load the results filename
        String resultsFile = _props.getProperty( _RESULTS_FILE_KEY );
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
                _props.stringPropertyNames() );
        Collections.sort( keyList );
        Iterator<String> iter = keyList.iterator();
        while( iter.hasNext() )
        {
            String key = iter.next();
            String value = _props.getProperty( key );

            _writer.println( "# " + key + " = " + value );
        }
        _writer.println( _SPACER );
        _writer.println();
        _writer.flush();
        
        _LOG.trace( "Leaving initialize()" );
    }
    
    public void run()
    {
        // Process all the individuals
        for( SpatialIndividual ind : _simState.getAllIndividuals() )
        {
            // Create our builder and prefix
            StringBuilder builder = new StringBuilder();
            String prefix = "individual." + ind.getID() + ".";

            // Log the individual's the location
            builder.append( prefix );
            builder.append( "location = " );
            builder.append( String.format( "%06.4f", ind.getLocation().getX() )
                        + " "
                        + String.format( "%06.4f", ind.getLocation().getY() ) );
            builder.append( _NEWLINE );
            
            // Log all the nearest neighbors
            builder.append( prefix );
            builder.append( "nearest-neighbors =" );
            for( Neighbor neighbor : ind.getNearestNeighbors() )
            {
                builder.append( " " );
                builder.append( neighbor.getIndividual().getID() );
            }
            builder.append( _NEWLINE );
            
            // Calculate the relative positions of all the neighbors
            List<Vector2D> relPositions = 
                    calculateRelativePositionsOfNeighbors( ind );
            builder.append( prefix );
            builder.append( "rel-positions-nearest-neighbors =" );
            for( Vector2D position : relPositions )
            {
                builder.append( " {" );
                builder.append( String.format( "%06.4f", position.getX() )
                        + ","
                        + String.format( "%06.4f", position.getY() )
                        + "}" );
            }
            builder.append( _NEWLINE );
            
            // Calculate the mean relative distance
            float meanRelDistance = calculateMeanRelativeDistanceOfNeighbors( ind );
            builder.append( prefix );
            builder.append( "mean-relative-distance-to-neighbors = " );
            builder.append( String.format( "%06.4f", meanRelDistance ) );
            builder.append( _NEWLINE );
            
            // Calculate the mean relative positions
            Vector2D meanRelPosition = calculateMeanRelativePositionsOfNeighbors( ind );
            builder.append( prefix );
            builder.append( "mean-relative-position-of-neighbors.cartesian = " );
            builder.append( String.format( "%06.4f", meanRelPosition.getX() )
                    + " "
                    + String.format( "%06.4f", meanRelPosition.getY() ) );
            builder.append( _NEWLINE );
            builder.append( prefix );
            builder.append( "mean-relative-position-of-neighbors.length = " );
            builder.append( String.format( "%06.4f",
                    meanRelPosition.distance( Vector2D.ZERO ) ) );
            builder.append( _NEWLINE );
            builder.append( prefix );
            double angle = Vector2D.angle( meanRelPosition,
                    new Vector2D( 1.0f, 0.0f ) );
            if( 0.0 > meanRelPosition.getY() )
            {
                angle = 2 * Math.PI - angle;
            }
            builder.append( "mean-relative-position-of-neighbors.angle = " );
            builder.append( String.format( "%06.4f", angle ) );
            builder.append( _NEWLINE );

            // Calculate the mean resultant vector
            Vector2D meanResultantVector = calculateMeanResultantVector( ind );
            builder.append( prefix );
            builder.append( "mean-resultant-vector-to-neighbors.cartesian = " );
            builder.append( String.format( "%06.4f", meanResultantVector.getX() )
                    + " "
                    + String.format( "%06.4f", meanResultantVector.getY() ) );
            builder.append( _NEWLINE );
            builder.append( prefix );
            builder.append( "mean-resultant-vector-to-neighbors.length = " );
            builder.append( String.format( "%06.4f",
                    meanResultantVector.distance( Vector2D.ZERO ) ) );
            builder.append( _NEWLINE );
            builder.append( prefix );
            angle = Vector2D.angle( meanResultantVector,
                    new Vector2D( 1.0f, 0.0f ) );
            if( 0.0 > meanResultantVector.getY() )
            {
                angle = 2 * Math.PI - angle;
            }
            builder.append( "mean-resultant-vector-to-neighbors.angle = " );
            builder.append( String.format( "%06.4f", angle ) );
            builder.append( _NEWLINE );

            // Write out all the data
            _writer.println( builder.toString() );
            _writer.flush();
        }
        
        // Close the writer
        _writer.close();
    }

    private List<Vector2D> calculateRelativePositionsOfNeighbors( SpatialIndividual ind )
    {
        // Get the individual's location
        Vector2D indLocation = ind.getLocation();
        
        // Iterate over all the nearest neighbors
        List<Vector2D> relativePositions = new LinkedList<Vector2D>();
        for( Neighbor neighbor : ind.getNearestNeighbors() )
        {
            // Get the relative position of the neighbor
            Vector2D neighborLocation = neighbor.getIndividual().getLocation();
            Vector2D relativePosition = neighborLocation.subtract( indLocation );
            
            // Add it to the list
            relativePositions.add( relativePosition );
        }

        return relativePositions;
    }
    
    private float calculateMeanRelativeDistanceOfNeighbors( SpatialIndividual ind )
    {
        // Iterate over all the nearest neighbors
        float totalDistance = 0.0f;
        for( Vector2D relPosition : calculateRelativePositionsOfNeighbors( ind ) )
        {
            // Add it to the total distance
            totalDistance += relPosition.distance( Vector2D.ZERO );
        }
        
        // Compute the mean
        float meanRelativeDistance = totalDistance / ind.getNearestNeighborCount();
        
        return meanRelativeDistance;
    }

    private Vector2D calculateMeanRelativePositionsOfNeighbors( SpatialIndividual ind )
    {
        // Iterate over all the nearest neighbors
        Vector2D meanRelativePosition = Vector2D.ZERO;
        for( Vector2D relPosition : calculateRelativePositionsOfNeighbors( ind ) )
        {
            // Add it to the mean relative position
            meanRelativePosition = meanRelativePosition.add(
                    relPosition );
        }
        
        // Scale it
        meanRelativePosition = meanRelativePosition.scalarMultiply(
                1.0f / ind.getNearestNeighborCount() );
        
        return meanRelativePosition;
    }
    
    /**
     * Calculates the circle statistic for the given individual
     *
     * @param ind The individual
     * @return The circle statistic
     */
    private Vector2D calculateMeanResultantVector( SpatialIndividual ind )
    {
        // Iterate over all the nearest neighbors
        Vector2D meanResultantVector = Vector2D.ZERO;
        for( Vector2D relPosition : calculateRelativePositionsOfNeighbors( ind ) )
        {
            // Normalize it and add it to the statistic
            meanResultantVector = meanResultantVector.add(
                    relPosition.normalize() );
        }
        
        // Do we scale it?
        if( _scaleMeanResultantVector )
        {
            // Yup
            meanResultantVector = meanResultantVector.scalarMultiply(
                    1.0f / ind.getNearestNeighborCount() );
        }

        return meanResultantVector;
    }
    
}
