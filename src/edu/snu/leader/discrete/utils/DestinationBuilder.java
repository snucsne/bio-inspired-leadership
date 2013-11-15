package edu.snu.leader.discrete.utils;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ec.util.MersenneTwisterFast;

public class DestinationBuilder
{
    /** Directory to store the destinations files in */
    private String directory = "cfg/sim/destinations/";

    /** Number of destinations to create */
    private int _destinationCount = 0;

    /** The random seed */
    private long _randomSeed = 0;

    private Point2D[] _destinations;

    /** The random number generator */
    private MersenneTwisterFast _rng = null;

    public static void main( String[] args )
    {
        int count = 10;
        DestinationBuilder db = new DestinationBuilder( count, 1 );
        db.generatePoles( 50, 100, .50 );
        db.generateSplitNorth( 20, 50, 50, 75, .50 );
        db.generateSides( 25, 50, .50 );

        double[] horizontalPercentages = { 1 };
        db.generateHorizontalNorth( 60, 25, 1, horizontalPercentages );
        
        db.generateCircle( 55, count );
        
        db.generateOneNorth( 70 );
    }

    DestinationBuilder( int destinationCount, long seed )
    {
        _destinationCount = destinationCount;
        _randomSeed = seed;
        _rng = new MersenneTwisterFast( _randomSeed );
    }

    /**
     * Generates two destinations that are directly above or below the origin.
     * They are placed at a distance between min and max. A percentage can be
     * defined for how many destinations are to north or south.
     * 
     * @param minDistOrigin Minimum distance from the origin
     * @param maxDistOrigin Maximum distance from the origin
     * @param percentNorth Percentage of destinations that are going north
     */
    public void generatePoles( int minDistOrigin,
            int maxDistOrigin,
            double percentNorth )
    {
        _destinations = new Point2D[_destinationCount];
        // The number of north destinations
        int numberNorth = (int) Math.round( _destinationCount * percentNorth );

        // The y coordinate for the north destination
        double northYCoord = Utils.getRandomNumber( _rng, minDistOrigin,
                maxDistOrigin );
        Point2D north = new Point2D.Double( 0, -northYCoord );

        // The y coordinate for the south destination
        double southYCoord = Utils.getRandomNumber( _rng, minDistOrigin,
                maxDistOrigin );
        Point2D south = new Point2D.Double( 0, southYCoord );

        // fill destinations array
        for( int i = 0; i < _destinationCount; i++ )
        {
            // add north destinations until we don't need anymore
            if( numberNorth > 0 )
            {
                _destinations[i] = north;
                numberNorth--;
            }
            else
            {
                _destinations[i] = south;
            }
        }

        String filename = directory + "destinations-poles-" + _destinationCount
                + "-per-" + percentNorth + "-seed-" + _randomSeed + ".dat";
        saveToFile( filename );
    }

    /**
     * Generates two destinations that are directly beside the origin. They are
     * placed at a distance between min and max. A percentage can be defined for
     * how many destinations are to left or right.
     * 
     * @param minDistOrigin Minimum distance from the origin
     * @param maxDistOrigin Maximum distance from the origin
     * @param percentLeft Percentage of destinations that are going left
     */
    public void generateSides( int minDistOrigin,
            int maxDistOrigin,
            double percentLeft )
    {
        _destinations = new Point2D[_destinationCount];
        // The number of left destinations
        int numberLeft = (int) Math.round( _destinationCount * percentLeft );

        // The y coordinate for the left destination
        double leftXCoord = Utils.getRandomNumber( _rng, minDistOrigin,
                maxDistOrigin );
        Point2D left = new Point2D.Double( -leftXCoord, 0 );

        // The y coordinate for the right destination
        double rightXCoord = Utils.getRandomNumber( _rng, minDistOrigin,
                maxDistOrigin );
        Point2D right = new Point2D.Double( rightXCoord, 0 );

        // fill destinations array
        for( int i = 0; i < _destinationCount; i++ )
        {
            // add left destinations until we don't need anymore
            if( numberLeft > 0 )
            {
                _destinations[i] = left;
                numberLeft--;
            }
            else
            {
                _destinations[i] = right;
            }
        }

        String filename = directory + "destinations-sides-" + _destinationCount
                + "-per-" + percentLeft + "-seed-" + _randomSeed + ".dat";
        saveToFile( filename );
    }

    /**
     * Generates two destinations that are north of the origin. They are
     * symmetrically placed on the y-axis a min-max distance as well as placed a
     * min-max distance away from the x-axis. A percent is given for the percent
     * going to each group.
     * 
     * @param xMinOrigin Minimum distance away from the y-axis on either side
     * @param xMaxOrigin Maximum distance away from the y-axis on either side
     * @param yMinOrigin Minimum distance away from the x-axis
     * @param yMaxOrigin Maximum distance away from the x-axis
     * @param percentLeft Percent going to the left group
     */
    public void generateSplitNorth( int xMinOrigin,
            int xMaxOrigin,
            int yMinOrigin,
            int yMaxOrigin,
            double percentLeft )
    {
        _destinations = new Point2D[_destinationCount];
        int numberLeft = (int) Math.round( _destinationCount * percentLeft );

        // get the x and y coords
        double xCoord = Utils.getRandomNumber( _rng, xMinOrigin, xMaxOrigin );
        double yCoord = Utils.getRandomNumber( _rng, yMinOrigin, yMaxOrigin );

        // create the left and right points
        Point2D left = new Point2D.Double( -xCoord, -yCoord );
        Point2D right = new Point2D.Double( xCoord, -yCoord );

        // fill destinations array
        for( int i = 0; i < _destinationCount; i++ )
        {
            if( numberLeft > 0 )
            {
                numberLeft--;
                _destinations[i] = left;
            }
            else
            {
                _destinations[i] = right;
            }
        }

        String filename = directory + "destinations-split-" + _destinationCount
                + "-per-" + percentLeft + "-seed-" + _randomSeed + ".dat";
        saveToFile( filename );
    }

    public void generateHorizontalNorth( int distOrigin,
            int distOther,
            int count,
            double[] percentages )
    {
        _destinations = new Point2D[_destinationCount];
        int currentIndex = 0;
        // center index of array
        int indexCenter = -1;
        int tempCount = 0;
        // if there are an odd amount of destinations wanted
        if( count % 2 != 0 )
        {
            indexCenter = ( count / 2 );
            // calculate number going to the center first
            int numberCenter = (int) Math.round( _destinationCount
                    * percentages[indexCenter] );
            // create destinations going to center
            while( tempCount < numberCenter )
            {
                _destinations[tempCount] = new Point2D.Double( 0, -distOrigin );
                tempCount++;
                currentIndex++;
            }
        }

        // calculate half of size of array (rounded down)
        int half = count / 2;
        // set the distance modifier to half (used for placing destinations away
        // from y-axis)
        int distanceModifier = half;
        for( int i = 0; i < half; i++ )
        {
            // calculate number going left and right, and the x and y coords
            int numberGoingLeft = (int) Math.round( _destinationCount
                    * percentages[i] );
            int numberGoingRight = (int) Math.round( _destinationCount
                    * percentages[count - 1 - i] );
            double xCoord = ( distanceModifier ) * distOther;
            double yCoord = distOrigin;

            // place destinations to this y and -x pair
            while( numberGoingLeft > 0 )
            {
                _destinations[currentIndex] = new Point2D.Double( -xCoord,
                        -yCoord );
                numberGoingLeft--;
                currentIndex++;
            }

            // place destinations to this y and x pair
            while( numberGoingRight > 0 )
            {
                _destinations[currentIndex] = new Point2D.Double( xCoord,
                        -yCoord );
                numberGoingRight--;
                currentIndex++;
            }
            distanceModifier--;
        }

        // if there are fewer destinations then wanted (because of rounding)
        // then add a few extra
        while( currentIndex < _destinationCount )
        {
            _destinations[currentIndex] = _destinations[currentIndex - 1];
            currentIndex++;
        }

        // create file name
        String string_percentages = "percents-";
        for( int i = 0; i < percentages.length; i++ )
        {
            string_percentages += percentages[i] + "-";
        }
        String filename = directory + "destinations-horizontal-"
                + _destinationCount + "-count-" + count + "-"
                + string_percentages + "seed-" + _randomSeed + ".dat";
        // save the file
        saveToFile( filename );
    }
    
    public void generateCircle(int distOrigin, int count){
        _destinations = new Point2D[_destinationCount];
        double angleBetweenEach = 360.0 / count;
        
        for(int i = 0; i < count; i++){
            double x = distOrigin * Math.cos( Math.toRadians( angleBetweenEach * i ));
            double y = distOrigin * Math.sin(Math.toRadians( angleBetweenEach * i ));
            _destinations[i] = new Point2D.Double(x,y);
        }
        
        String filename = directory +"destinations-circle-count-" + count + "-seed-" + _randomSeed + ".dat";
        
        saveToFile( filename );
    }
    
    public void generateOneNorth(int distOrigin){
        _destinations = new Point2D[_destinationCount];
        
        double x = 0;
        double y = -distOrigin;
        
        for(int i = 0; i < _destinationCount; i++){
            _destinations[i] = new Point2D.Double(x,y);
        }
        
        String filename = directory + "destinations-one-" + _destinationCount + "-seed-" + _randomSeed + ".dat";
        
        saveToFile( filename );
    }

    /**
     * Saves the destination points to a file
     * 
     * @param filename The filename to save to
     */
    private void saveToFile( String filename )
    {
        // Create the writer
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter( new BufferedWriter( new FileWriter(
                    filename ) ) );
        }
        catch( IOException ioe )
        {
            throw new RuntimeException( "Unable to open simple file ["
                    + filename + "]", ioe );
        }

        writer.println( "# Location count [" + _destinationCount + "]" );
        writer.println( "# Random seed [" + _randomSeed + "]" );
        writer.println();

        for( int i = 0; i < _destinations.length; i++ )
        {
            writer.println( String.format( "%+08.4f", _destinations[i].getX() )
                    + "    "
                    + String.format( "%+08.4f", _destinations[i].getY() ) );
        }

        // Close the writer
        writer.close();
    }

}
