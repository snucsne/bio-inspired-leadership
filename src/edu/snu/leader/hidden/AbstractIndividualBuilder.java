/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.awt.geom.Point2D;
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
    protected static final float DEFAULT_CONFLICT_DIR = 0.0f;

    /** Default flag for describing initiation histories */
    protected static final boolean DEFAULT_DESCRIBE_INITIATION_HISTORY = false;


    /** The simulation state */
    protected SimulationState _simState = null;

    /** The predefined locations for individuals */
    private List<Point2D> _locations = new LinkedList<Point2D>();

    /** The maximum radius for generated locations */
    private float _maxRadius = 1.0f;


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.IndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
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
    protected Point2D createValidLocation( int index )
    {
        Point2D location = null;

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

            location = new Point2D.Float( x, y );
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
                _locations.add( new Point2D.Float( x, y ) );
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
