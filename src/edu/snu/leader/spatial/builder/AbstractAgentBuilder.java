/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.builder;

// Imports
import edu.snu.leader.spatial.AgentBuilder;
import edu.snu.leader.spatial.SimulationState;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * AbstractAgentBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractAgentBuilder implements AgentBuilder
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            AbstractAgentBuilder.class.getName() );

    /** Key for the locations file */
    private static final String _LOCATIONS_FILE_KEY = "locations-file";

    /** The agent ID prefix */
    private static final String _AGENT_ID_PREFIX = "Ind";


    /** The current simulation state */
    protected SimulationState _simState = null;

    /** The predefined locations for individuals */
    protected List<Vector2D> _locations = new LinkedList<Vector2D>();


    /**
     * Initializes the agent builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.spatial.AgentBuilder#initialize(edu.snu.leader.spatial.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Validate and store the simulation state
        Validate.notNull( simState, "Simulation state may not be null" );
        _simState = simState;

        // Get the simulation properties
        Properties props = simState.getProperties();

        // Ensure that a file detailing the locations was supplied
        String locationsFileStr = props.getProperty( _LOCATIONS_FILE_KEY );
        Validate.notEmpty( locationsFileStr,
                "Locations file (key=["
                + _LOCATIONS_FILE_KEY
                + "] may not be empty" );
        loadLocations( locationsFileStr );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Generates a new unique ID
     *
     * @return The unique ID
     */
    protected Object generateUniqueIndividualID( int index )
    {
        return _AGENT_ID_PREFIX + String.format( "%05d", index );
    }

    /**
     * Loads the locations of the agents from a file
     *
     * @param filename
     */
    protected void loadLocations( String filename )
    {
        _LOG.trace( "Entering loadLocations( filename )" );

        _LOG.info( "Loading locations from file ["
                + filename
                + "]" );

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
