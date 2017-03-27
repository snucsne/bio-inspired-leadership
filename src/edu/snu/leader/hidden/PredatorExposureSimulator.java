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

//Imports
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

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import edu.snu.leader.util.MiscUtils;


public class PredatorExposureSimulator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PredatorExposureSimulator.class.getName() );

    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the results file */
    protected static final String _RESULTS_FILE_KEY = "results-file";

    /** Stats file spacer comment */
    private static final String _SPACER =
            "# =========================================================";

    /** Newline string */
    protected static final String _NEWLINE = System.lineSeparator();

    /** X-axis vector */
    private static Vector2D _X_AXIS = new Vector2D( 1, 0 );
    
    
    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** The writer to which the results are reported */
    private PrintWriter _writer = null;

    /** The distance at which the predator appears */
    private float _predDistance = 30.0f;

    
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
            PredatorExposureSimulator sim =
                new PredatorExposureSimulator();
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
    
    /**
     * Run the simulation
     */
    public void run()
    {
        _LOG.trace( "Entering run()" );
        
        int angleCount = 16;
        for( int i = 0; i < angleCount; i++ )
        {
            double angle = i * Math.PI * 2.0 / (double) angleCount;
            Vector2D vec = convertPolarToCartesian( 1, angle );
            double computedAngle = calculateAngle( vec );
            
            _LOG.warn( "angle=["
                    + angle
                    + "] computedAngle=["
                    + computedAngle
                    + "] vec=["
                    + vec
                    + "]" );
        }
        
        
        _LOG.trace( "Leaving run()" );
    }

    /**
     * Calculates the angle formed by the specified vector and the
     * x axis.  This essential gets the angle from converting it to
     * polar coordinates.
     *
     * @param vec The vector for which the angle will be calculated 
     * @return The angle
     */
    private double calculateAngle( Vector2D vec )
    {
        // Compute the angle
        double angle = Vector2D.angle( _X_AXIS, vec );
        
        // The angle computation finds only goes from [0,pi]
        if( vec.getY() < 0 )
        {
            // Subtract 2PI to make it right
            angle = 2 * Math.PI - angle;
        }
        
        return angle;
    }
    
    /**
     * Converts the given polar coordinates to a cartesian vector
     *
     * @param r The radius
     * @param theta The angle
     * @return The cartesian vector
     */
    private Vector2D convertPolarToCartesian( double r, double theta )
    {
        double x = r * Math.cos( theta );
        double y = r * Math.sin( theta );
        
        return new Vector2D( x, y );
    }
}
