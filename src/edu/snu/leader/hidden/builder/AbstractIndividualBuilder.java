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

// Imports
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * AbstractIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractIndividualBuilder implements IndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            AbstractIndividualBuilder.class.getName() );

    /** Key for the locations file */
    private static final String _LOCATIONS_FILE_KEY = "locations-file";

    /** Key for the max location radius */
    private static final String _MAX_RADIUS_KEY = "max-location-radius";


    /** Default personality value */
    protected static final float DEFAULT_PERSONALITY = 0.0f;

    /** Default assertiveness value */
    protected static final float DEFAULT_ASSERTIVENESS = 0.0f;

    /** Default preferred direction */
    protected static final float DEFAULT_PREFERRED_DIR = 0.0f;

    /** Default abstract conflict */
    protected static final float DEFAULT_RAW_CONFLICT = 0.0f;

    /** Default flag for describing initiation histories */
    protected static final boolean DEFAULT_DESCRIBE_INITIATION_HISTORY = false;


    /** The simulation state */
    protected SimulationState _simState = null;

    /** The predefined locations for individuals */
    private List<Vector2D> _locations = new LinkedList<Vector2D>();

    /** The maximum radius for generated locations */
    private float _maxRadius = 1.0f;


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Save the simulation state
        _simState = simState;

        // Get the properties
        Properties props = simState.getProps();

        // Get the max location radius
        String maxRadiusStr = props.getProperty( _MAX_RADIUS_KEY );
        Validate.notEmpty( maxRadiusStr,
                "Max radius (key="
                + _MAX_RADIUS_KEY
                + ") may not be empty" );
        _maxRadius = Float.parseFloat( maxRadiusStr );
        _LOG.info( "Using _maxRadius=[" + _maxRadius + "]" );

        // Was a locations file supplied?
        String locationsFileStr = props.getProperty( _LOCATIONS_FILE_KEY );
        if( (null != locationsFileStr) && !locationsFileStr.equals( "" ) )
        {
            // Yup
            loadLocations( locationsFileStr );
            _LOG.info( "Using locations file [" + locationsFileStr + "]" );
        }

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Generates a new unique ID
     *
     * @return The unique ID
     */
    protected Object generateUniqueIndividualID( int index )
    {
        return "Ind" + String.format( "%05d", index );
    }

    /**
     * Create a valid location for an individual
     *
     * @param index The index of the individual
     * @return The valid location
     */
    protected Vector2D createValidLocation( int index )
    {
        Vector2D location = null;

        // If we have a location, use it
        if( index < _locations.size() )
        {
            location = _locations.get( index );
        }
        // Otherwise, generate it
        else
        {
            // Generate a radius
            float radius = _simState.getRandom().nextFloat() * _maxRadius;

            // Generate an angle
            double angle = ( _simState.getRandom().nextDouble() * Math.PI * 2.0 )
                    - Math.PI;

            // Convert to cartesian
            float x = radius * (float) Math.cos( angle );
            float y = radius * (float) Math.sin( angle );

            location = new Vector2D( x, y );
        }

        return location;
    }

    /**
     * TODO Method description
     *
     * @param filename
     */
    protected void loadLocations( String filename )
    {
        _LOG.trace( "Entering loadLocations( filename )" );

        // Try to process the file
        try
        {
            // Create a reader
            BufferedReader reader = new BufferedReader(
                    new FileReader( filename ) );

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
                float x = Float.parseFloat( parts[0] );
                float y = Float.parseFloat( parts[1] );

                // Build the location and add it to the list
                _locations.add( new Vector2D( x, y ) );
            }

            // Close the file
            reader.close();
        }
        catch( Exception e )
        {
            _LOG.error( "Unable to read locations file ["
                    + filename
                    + "]", e );
            throw new RuntimeException( "Unable to read locations file ["
                    + filename
                    + "]" );
        }

        _LOG.trace( "Leaving loadLocations( filename )" );
    }

}
