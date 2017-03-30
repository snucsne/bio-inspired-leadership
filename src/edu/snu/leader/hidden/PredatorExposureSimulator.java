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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import ec.util.MersenneTwisterFast;
import edu.snu.leader.hidden.util.PositionAnalyzer;
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

    /** The number of times to run the simulator */
    private int _simulationCount = 0;
    
    /** The random number generator */
    MersenneTwisterFast _rng = null;

    /** The distance at which the predator appears */
    private float _predDistance = 30.0f;

    /** Max learning rate */
    private double _alphaMax = 0.02;
    
    /** Sigma squared of normal distribution */
    private double _sigmaSquared = 0.08;
    
    /** Fearfulness history */
    private Map<Object,List<Double>> _fearfulnessHistory =
            new HashMap<Object,List<Double>>();
    
    /** Min fearfulness */
    private double _minFearfulness = 0.1;

    /** Max fearfulness */
    private double _maxFearfulness = 0.9;
    
    /** Default fearfulness */
    private double _defaultFearfulness = 0.1;

    
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
        
        // Get the simulation count
        _simulationCount = _simState.getSimulationCount();

        // Get our random number generator
        _rng = _simState.getRandom();

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
        
        // Create the default fearfulness for every individual
        for( SpatialIndividual ind : _simState.getAllIndividuals() )
        {
            List<Double> indHistory = new LinkedList<Double>();
            indHistory.add( new Double( _defaultFearfulness ) );
            _fearfulnessHistory.put( ind.getID(), indHistory );
        }

        _LOG.trace( "Leaving initialize()" );
    }
    
    /**
     * Run the simulation
     */
    public void run()
    {
        _LOG.trace( "Entering run()" );
        
        // Run the simulation
        for( int i = 0; i < _simulationCount; i++ )
        {
            // Generate a random location for the predator
            Vector2D predator = generatePredatorLocation();
            
            // Process each individual
            for( SpatialIndividual ind : _simState.getAllIndividuals() )
            {
                // Get the individual's location and mean resultant vector
                Vector2D indLoc = ind.getLocation();
                Vector2D indMRV = PositionAnalyzer.calculateMeanResultantVector( ind );
                
                // Get the relative position of the predator
                Vector2D predRelLoc = predator.subtract( indLoc );
                
                // Get the angle of the predator and MRV
                double predAngle = calculateAngle( predRelLoc );
                double mrvAngle = calculateAngle( indMRV );
                
                // Compute the learning rate alpha
                double angleDiff = Math.abs( predAngle - mrvAngle );
                if( angleDiff > Math.PI )
                {
                    angleDiff = Math.PI * 2.0 - angleDiff;
                }
                double x = (Math.PI - angleDiff) / (Math.PI / 2.0 );
                double alpha = 0.0;
                if( Math.abs( x ) <= 1.0 )
                {
                    alpha = _alphaMax * Math.exp( (-1.0 * x * x )
                            / (2.0 * _sigmaSquared ) );
                }
                
                // Get the old value for the individual and update it
                List<Double> indFearfulnessHistory = _fearfulnessHistory.get( ind.getID() );
                int count = indFearfulnessHistory.size();
                double currentFearfulness = indFearfulnessHistory.get( count - 1 );
                
                // Compute the "error"
                double error = indMRV.distance( Vector2D.ZERO );
                
                // Compute the new fearfulness
                double nextFearfulness = currentFearfulness * (1.0 - alpha)
                        + (alpha * error);
                if( nextFearfulness < _minFearfulness )
                {
                    nextFearfulness = _minFearfulness;
                }
                else if( nextFearfulness > _maxFearfulness )
                {
                    nextFearfulness = _maxFearfulness;
                }
                indFearfulnessHistory.add( new Double( nextFearfulness ) );
                
//                _LOG.warn( "sim=["
//                        + String.format( "%4d", i )
//                        + "] ind=["
//                        + ind.getID()
//                        + "]:  predRelLoc=["
//                        + String.format( "%+6.2f", predRelLoc.getX() )
//                        + ","
//                        + String.format( "%+6.2f", predRelLoc.getY() )
//                        + "]  predAngle=["
//                        + String.format( "%6.4f", predAngle )
//                        + "]  mrvAngle=["
//                        + String.format( "%6.4f", mrvAngle )
//                        + "]  (p-m)=["
//                        + String.format( "%6.4f", angleDiff )
//                        + "]  x=["
//                        + String.format( "%6.4f", x )
//                        + "]  alpha=["
//                        + String.format( "%6.4f", alpha )
//                        + "]  mrvDist=["
//                        + String.format( "%6.4f", indMRV.distance( Vector2D.ZERO ) )
//                        + "]  error=["
//                        + String.format( "%+6.4f", error )
//                        + "]  currentFearfulness=["
//                        + String.format( "%6.4f", currentFearfulness )
//                        + "]  nextFearfulness=["
//                        + String.format( "%6.4f", nextFearfulness )
//                        + "]" );
            }
        }
        
//        int angleCount = 16;
//        for( int i = 0; i < angleCount; i++ )
//        {
//            double angle = i * Math.PI * 2.0 / (double) angleCount;
//            Vector2D vec = convertPolarToCartesian( 1, angle );
//            double computedAngle = calculateAngle( vec );
//            
//            _LOG.warn( "angle=["
//                    + angle
//                    + "] computedAngle=["
//                    + computedAngle
//                    + "] vec=["
//                    + vec
//                    + "]" );
//        }
        
        // Only log the last data for now
        for( SpatialIndividual ind : _simState.getAllIndividuals() )
        {
            List<Double> indFearfulnessHistory = _fearfulnessHistory.get( ind.getID() );
            int count = indFearfulnessHistory.size();
            double currentFearfulness = indFearfulnessHistory.get( count - 1 );
            _writer.println( String.format( "%6.4f", currentFearfulness ) );
        }
        
        // Clean up
        _writer.flush();
        _writer.close();
        
        _LOG.trace( "Leaving run()" );
    }

    /**
     * Generate the predator at a random location
     *
     * @return The location of the predator
     */
    private Vector2D generatePredatorLocation()
    {
        // Generate a random angle
        double angle = 2.0 * Math.PI * _rng.nextDouble();
        
        // Get the cartesian vector of the angle and radius
        Vector2D predator = convertPolarToCartesian( _predDistance, angle );
        
//        _LOG.warn( "Predator: angle=["
//                + String.format( "%6.4f", angle )
//                + "] location=["
//                + String.format( "%+6.2f", predator.getX() )
//                + ","
//                + String.format( "%+6.2f", predator.getY() )
//                + "]" );
        
        return predator;
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
