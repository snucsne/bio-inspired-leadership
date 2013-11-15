package edu.snu.leader.discrete.utils;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ec.util.MersenneTwisterFast;
import edu.snu.leader.discrete.behavior.Decision;


/**
 * General Utils
 * 
 * @author Tim Solum
 */
public class Utils
{

    private static int uniqueIdCount = 0;

    private Utils()
    {
    };

    /**
     * Reads a locations file and creates an array of points
     * 
     * @param filename The locations filename
     * @param numLocations Number of locations wanted
     * @return The array of points
     */
    public static Point2D[] readPoints( String filename, int numLocations )
    {
        Point2D[] locations = new Point2D[numLocations];

        Scanner scanner;
        try
        {
            scanner = new Scanner( new File( filename ) );
        }
        catch( FileNotFoundException e )
        {
            throw new RuntimeException( "Could not find or load " + filename
                    + " locations file" );
        }

        int i = 0;
        // get rid of all of the text we don't need
        while( !scanner.hasNextDouble() )
        {
            scanner.next();
        }
        // okay, time to get the points now
        while( scanner.hasNextDouble() )
        {
            double tempx, tempy;
            tempx = scanner.nextDouble();
            tempy = scanner.nextDouble();
            locations[i] = new Point2D.Double( tempx, tempy );
            i++;
        }
        scanner.close();
        return locations;
    }

    /**
     * Generates a unique id
     * 
     * @return The unique id
     */
    public static String generateUniqueId( String type )
    {
        return type + uniqueIdCount++;
    }

    /**
     * Returns a random number between the min and max values given a
     * MersenneTwisterFast generator
     * 
     * @param random The generator
     * @param min The minimum value
     * @param max The maximum value
     * @return The probability
     */
    public static double getRandomNumber( MersenneTwisterFast random,
            double min,
            double max )
    {
        return ( random.nextDouble() * ( max - min ) ) + min;
    }

    /**
     * Returns a decision from a possibleDecisions list. Does not account for
     * DoNothing decisions
     * 
     * @param possibleDecisions The list of possible decisions
     * @param rand The random number
     * @return The decision
     */
    public static Decision getDecision( List<Decision> possibleDecisions,
            double rand )
    {
        // a list of probability ranges for each Decision [min, max]
        List<ProbabilityRange> forTheMath = new LinkedList<ProbabilityRange>();
        Decision decision = null;
        double sum = 0.0;

        // calculate the probability ranges for each decision
        for( int i = 0; i < possibleDecisions.size(); i++ )
        {
            Decision temp = possibleDecisions.get( i );
            forTheMath.add( new ProbabilityRange( sum, sum
                    + temp.getProbability() ) );
            // if the random number falls within the probability range the its
            // the decision we want
            if( forTheMath.get( i ).isThisTheOne( rand ) )
            {
                decision = possibleDecisions.get( i );
                break;
            }
            sum += temp.getProbability();
        }
        return decision;
    }

    /**
     * Class for getDecision method. It holds ranges of probabilities for each
     * associated with each decision and then allows for a check to see if a
     * random number falls between the min and max range
     * 
     * @author Tim Solum
     */
    private static class ProbabilityRange
    {
        public double min = 0.0;

        public double max = 0.0;

        public ProbabilityRange( double min, double max )
        {
            this.min = min;
            this.max = max;
        }

        /**
         * Checks to see if a probability falls within the min and max range
         * 
         * @param probability Probability to see if it is contained
         * @return True or false
         */
        public boolean isThisTheOne( double probability )
        {
            boolean isTheOne = false;
            if( min <= probability && probability < max )
            {
                isTheOne = true;
            }
            return isTheOne;
        }
    }
}
