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
package edu.snu.leader.util;

//Imports
import ec.util.MersenneTwisterFast;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.log4j.Logger;



/**
 * LocationBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class LocationBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            LocationBuilder.class.getName() );

    /** Simple file spacer comment */
    private static final String _SPACER =
            "# =========================================================";

    /** Delta to apply to separations to prevent rounding errors */
    private static final float _DELTA = 0.01f;


    /** The number of locations to build */
    private int _locationCount = 0;

    /** The min neighbor separation */
    private float _minNeighborSeparation = 0.0f;

    /** The max neighbor separation */
    private float _maxNeighborSeparation = 0.0f;

    /** The random seed */
    private long _randomSeed = 0;

    /** The random number generator */
    private MersenneTwisterFast _rng = null;




    /**
     * Builds this LocationBuilder object
     *
     * @param locationCount
     * @param minNeighborSeparation
     * @param maxNeighborSeparation
     * @param randomSeed
     */
    public LocationBuilder( int locationCount,
            float minNeighborSeparation,
            float maxNeighborSeparation,
            long randomSeed )
    {
        // Store the necessary parameters
        _locationCount = locationCount;
        _maxNeighborSeparation = maxNeighborSeparation;
        _minNeighborSeparation = minNeighborSeparation;
        _randomSeed = randomSeed;

        // Create the random number generator
        _rng = new MersenneTwisterFast( randomSeed );
    }

    public void reseedRNG( long randomSeed )
    {
        _randomSeed = randomSeed;
        _rng = new MersenneTwisterFast( randomSeed );
    }

    public Point2D[] buildLocations()
    {
        // Set up some data
        Point2D[] locations = new Point2D[ _locationCount ];

        // The first location defaults to the origin
        locations[0] = new Point2D.Float( 0.0f, 0.0f );

        // Build the rest of the locations
        for( int i = 1; i < _locationCount; i++ )
        {
            // Loop around until the location is valid
            boolean valid = false;
            do
            {
                // Pick a random, existing location as a neighbor
                int neighborIdx = _rng.nextInt( i );

                // Generate the relative coordinates
                float radius = ( _rng.nextFloat()
                        * (_maxNeighborSeparation - _minNeighborSeparation - ( 2 * _DELTA )) )
                        + _minNeighborSeparation + _DELTA;
                float angle = _rng.nextFloat() * 2.0f * (float) Math.PI;
                float x = radius * (float) Math.cos( angle );
                float y = radius * (float) Math.sin( angle );

                // Build the location
                locations[i] = new Point2D.Float(
                        x + (float) locations[neighborIdx].getX(),
                        y + (float) locations[neighborIdx].getY() );

                // See if it is valid
                valid = isValid( locations[i], locations, i );

                _LOG.debug( "Location ["
                        + i
                        + "]=("
                        + String.format( "%+08.4f", locations[i].getX() )
                        + ","
                        + String.format( "%+08.4f", locations[i].getY() )
                        + ") is ["
                        + (valid ? "valid" : "INVALID" )
                        + "]" );

            } while( !valid );
        }

        return locations;
    }

    public void sendToSimpleFile( String filename, Point2D[] locations )
    {
        // Create the writer
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter( new BufferedWriter(
                    new FileWriter( filename ) ) );
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to open simple file ["
                    + filename
                    + "]", ioe );
            throw new RuntimeException( "Unable to open simple file ["
                    + filename
                    + "]", ioe );
        }

        writer.println( _SPACER );
        writer.println( "# Location count ["
                + _locationCount
                + "]" );
        writer.println( "# Max neighbor separation ["
                + _maxNeighborSeparation
                + "]" );
        writer.println( "# Min neighbor separation ["
                + _minNeighborSeparation
                + "]" );
        writer.println( "# Random seed ["
                + _randomSeed
                + "]" );
        writer.println( _SPACER );
        writer.println();

        for( int i = 0; i < locations.length; i++ )
        {
            writer.println( String.format( "%+08.4f", locations[i].getX() )
                    + "    "
                    + String.format( "%+08.4f", locations[i].getY() ) );
        }

        // Close the writer
        writer.close();
    }

    private boolean isValid( Point2D location, Point2D[] locations, int maxIdx )
    {
        boolean valid = true;
        for( int i = 0; (i < maxIdx) && valid; i++ )
        {
            if( _minNeighborSeparation > location.distance( locations[i] ) )
            {
                valid = false;
            }
        }

        return valid;
    }

    /**
     * TODO Method description
     *
     * @param args
     */
    public static void main( String[] args )
    {
//        // Get the number of locations to build
//        int locationCount = Integer.parseInt( args[0] );
//
//        // Get the minimum neighbor distance
//        float minNeighborDistance = Float.parseFloat( args[1] );
//
//        // Get the maximum neighbor distance
//        float maxNeighborDistance = Float.parseFloat( args[2] );
//
//        // Get the random seed
//        long randomSeed = Long.parseLong( args[3] );
//
//        // Get the number of files to build
//        int fileCount = Integer.parseInt( args[4] );
        
        int locationCount = 30;
        float minNeighborDistance = 1.0f;
        float maxNeighborDistance = 4.0f;
        long randomSeed = 1;
        int fileCount = 1;

//        // Get the file prefix
//        String filePrefix = args[5];
        String filePrefix = "valid-metric-loc-";

        // Create the builder
        LocationBuilder builder = new LocationBuilder( locationCount,
                minNeighborDistance,
                maxNeighborDistance,
                randomSeed );

        // Build the specified number of files
        for( int i = 0; i < fileCount; i++, randomSeed++ )
        {
            // Reseed the RNG
            builder.reseedRNG( randomSeed );

            // Build the locations
            Point2D[] locations = builder.buildLocations();

            // Build the output file name
            String fileName = filePrefix
                    + String.format( "%05d", randomSeed )
                    + ".dat";

            // Send the locations to the file
            builder.sendToSimpleFile( fileName, locations );
        }
    }

}
