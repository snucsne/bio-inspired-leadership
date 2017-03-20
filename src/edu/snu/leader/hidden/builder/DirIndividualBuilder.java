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
package edu.snu.leader.hidden.builder;

import java.io.BufferedReader;
import java.io.FileReader;
//Imports
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.MetricSpatialIndividual;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;


/**
 * DirIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DirIndividualBuilder extends AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DirIndividualBuilder.class.getName() );

    /** Key for the directions file */
    public static final String DIRECTIONS_FILE_KEY = "directions-file";
    
    /** Key for the type of local communication */
    private static final String _LOCAL_COMMUNICATION_TYPE_KEY = "local-communication-type";

    /** Map of direction overrides */
    private Map<Object, Float> _directions = new HashMap<Object, Float>();

    /** Flag denoting whether or not metric local communication is used */
    private boolean _useMetric = false;

    
    
    
    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();
        
        // Load the directions
        String directionsFile = props.getProperty( DIRECTIONS_FILE_KEY );
        Validate.notEmpty( directionsFile,
                "Directions file (key="
                + DIRECTIONS_FILE_KEY
                + ") is required" );
//        _LOG.warn( "Using directions file [" + directionsFile + "]" );
        
        // Try to process the file
        try
        {
            // Create a reader
            BufferedReader reader = new BufferedReader(
                    new FileReader( directionsFile ) );

            // Process each line
            String line;
            while( null != (line = reader.readLine()) )
            {
                // Is it a comment or empty?
                if( (0 == line.length()) || line.startsWith( "#" ) )
                {
                    // Yup
                    continue;
                }

                // Split it
                String[] parts = line.split("\\s+");

                // Parse it
                String indID = parts[0];
                float direction = Float.parseFloat(  parts[1] );
                
                // Store it
                _directions.put( indID, new Float( direction ) );
//                _LOG.warn( "Individual=[" + indID + "] direction=[" + direction + "]" );
            }

            // Close the reader
            reader.close();
        }
        catch( Exception e )
        {
            _LOG.error( "Unable to read directions file ["
                    + directionsFile
                    + "]", e );
            throw new RuntimeException( "Unable to directions file ["
                    + directionsFile
                    + "]" );
        }

        // Get the type of local communication
        String localCommunicationType = props.getProperty(
                _LOCAL_COMMUNICATION_TYPE_KEY );
        if( "metric".equals( localCommunicationType ) )
        {
            _useMetric = true;
        }
        _LOG.info( "Using _useMetric=[" + _useMetric + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }



    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The individual
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create a valid location
        Vector2D location = createValidLocation( index );

        // Create the individual's ID
        Object id = generateUniqueIndividualID( index );
        
        // Create the individual's direction
        Float directionObj = _directions.get( id );
        float direction = directionObj.floatValue();
        
        // Create the individual
        SpatialIndividual ind = null;

        // Determine which to build
        if( _useMetric )
        {
            ind = new MetricSpatialIndividual( id,
                    location,
                    DEFAULT_PERSONALITY,
                    1.0f,
                    direction,
                    DEFAULT_RAW_CONFLICT,
                    true );
        }
        else
        {
            ind = new SpatialIndividual( id,
                    location,
                    DEFAULT_PERSONALITY,
                    1.0f,
                    direction,
                    DEFAULT_RAW_CONFLICT,
                    true );
        }

        return ind;

    }

}
