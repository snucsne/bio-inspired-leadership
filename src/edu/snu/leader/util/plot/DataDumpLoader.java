/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * DataDumpLoader
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DataDumpLoader
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DataDumpLoader.class.getName() );

    /** The key for the world object's name */
    private static final String _WORLD_OBJ_NAME_KEY = "world-object-name";

    /** The key for the world object's team name */
    private static final String _WORLD_OBJ_TEAM_NAME_KEY = "team-name";

    /** The key for the collision bounding radius */
    private static final String _WORLD_OBJ_COLLISION_BOUNDING_RADIUS_KEY =
            "collision-bounding-radius";

    /** The key for a position */
    private static final String _WORLD_OBJ_POSITION = "position";

    /** Flag indicating that the name of the world object has been found */
    private boolean _nameFound = false;

    /** Flag indicating that the team name has been found */
    private boolean _teamNameFound = false;

    /** Flag indicating that the collision bounding radius has been found */
    private boolean _collisionBoundingRadiusFound = false;

    /**
     * Parses the specified file and returns the dump information inside
     *
     * @param filename The filename
     * @return The data dump information
     * @throws IOException
     * @throws FileNotFoundException
     */
    public DataDump loadDataDumpFile( String filename )
        throws IOException, FileNotFoundException
    {
        _LOG.trace( "Entering loadDataDumpFile( filename )" );

        // Determine if the file exits
        File file = new File( filename );
        if( !file.exists() )
        {
            throw new IllegalArgumentException( "File ["
                    + filename
                    + "] does not exist." );
        }

        DataDump dump = new DataDump();

        // Try to open it
        BufferedReader reader = new BufferedReader( new FileReader( file ) );

        // Parse all the lines
        String line = null;
        while( null != (line = reader.readLine()) )
        {
            // Parse the line
            if( line.length() > 0 )
            {
                // Split it up into a key-value pair
                String[] splitLine = line.split( "=" );
                if( splitLine.length != 2 )
                {
                    _LOG.error( "Illegal key-value pair ["
                            + line
                            + "] in file=["
                            + filename
                            + "]" );
                    reader.close();
                    throw new IllegalArgumentException(
                            "Line contains more than a key-value pair" );
                }
                parseLine( splitLine[0], splitLine[1], dump );
            }
        }

        // Close the file
        reader.close();

        _LOG.trace( "Leaving loadDataDumpFile( filename )" );

        return dump;
    }

    /**
     * Resets this loader
     */
    public void reset()
    {
        _nameFound = false;
        _teamNameFound = false;
        _collisionBoundingRadiusFound = false;
    }

    /**
     * Parses a line of the data dump file
     *
     * @param key The key part of the line
     * @param value The value part of the line
     * @param dump The data dump file
     */
    private void parseLine( String key, String value, DataDump dump )
    {
        // Determine what type of line it is
        // Is it a position?
        if( _WORLD_OBJ_POSITION.equals( key ) )
        {
            // Parse the position
            dump.addPosition( buildVector3D( value ) );
        }
        // Is it the name?
        else if( !_nameFound && _WORLD_OBJ_NAME_KEY.equals( key ) )
        {
            // Just put the name as-is in the dump
            dump.setName( value );
        }
        // Is it the team name?
        else if( !_teamNameFound && _WORLD_OBJ_TEAM_NAME_KEY.equals( key ) )
        {
            // Just put the team name as-is in the dump
            dump.setTeamName( value );
        }
        // Is it the collision bounding radius
        else if( !_collisionBoundingRadiusFound
                && _WORLD_OBJ_COLLISION_BOUNDING_RADIUS_KEY.equals( key ) )
        {
            // Parse it into a float
            try
            {
                dump.setCollisionBoundingRadius( Float.parseFloat( value ) );
            }
            catch( NumberFormatException nfe )
            {
                throw new IllegalArgumentException( "Invalid bounding radius value ["
                        + value
                        + "]" );
            }
        }
    }

    /**
     * Builds a Vector3f from a String value
     *
     * @param value The string value
     * @return The Vector3f
     */
    private Vector3D buildVector3D( String value )
    {
        Vector3D vec = null;

        // Split it up by commas
        String[] componentStrs = value.split( "," );
        if( componentStrs.length != 3 )
        {
            throw new IllegalArgumentException( "Invalid vector=["
                    + value
                    + "]" );
        }
        float[] components = new float[ componentStrs.length ];
        try
        {
            components[0] = Float.parseFloat( componentStrs[0] );
            components[1] = Float.parseFloat( componentStrs[1] );
            components[2] = Float.parseFloat( componentStrs[2] );

            vec = new Vector3D( components[0], components[1], components[2] );
        }
        catch( NumberFormatException nfe )
        {
            throw new IllegalArgumentException( "The vector definition ["
                    + value
                    + "] contains non-float values" );
        }

        return vec;
    }

}
