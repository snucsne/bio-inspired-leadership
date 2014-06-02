/*
 * The Bio-inspired Leadership Toolkit is a set of tools used to simulate the
 * emergence of leaders in multi-agent systems. Copyright (C) 2014 Southern
 * Nazarene University This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package edu.snu.leader.discrete.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ec.util.MersenneTwisterFast;
import edu.snu.leader.discrete.behavior.Decision;
import edu.snu.leader.discrete.simulator.Agent;


/**
 * General Utils
 * 
 * @author Tim Solum
 */
public class Utils
{
    /* Used for creating unique ids */
    private static int uniqueIdCount = 0;

    // we shouldn't have an instance of this class
    private Utils()
    {
    };

    /**
     * Creates a shuffled list of agents
     * 
     * @param agents
     * @param random
     * @return
     */
    public static List<Agent> shuffleAgents( List<Agent> agents,
            MersenneTwisterFast random )
    {
        // get an unshuffled list of agents
        List<Agent> unshuffled = new LinkedList<Agent>();
        // our list that will be shuffled and returned
        List<Agent> shuffled = new ArrayList<Agent>( agents.size() );
        // add agents to the unshuffled list
        unshuffled.addAll( agents );
        // while we still have agents in the unshuffled list
        while( !unshuffled.isEmpty() )
        {
            // get an int between 0 and the size of unshuffled list
            int rand = random.nextInt( unshuffled.size() );
            // remove the agent from unshuffled at index rand and add it to
            // shuffle
            shuffled.add( unshuffled.remove( rand ) );
        }
        return shuffled;
    }

    /**
     * Draws a directional triangle on a graphics 2D
     * 
     * @param bbg The graphics2D object
     * @param heading The direction its heading in radians
     * @param x The x coordinate
     * @param y The y coordinate
     * @param sideLength The length of the longest two sides
     * @param fillColor The color of the body of the triangle
     * @param borderColor The border color of the triangle
     */
    public static void drawDirectionalTriangle( Graphics2D bbg,
            double heading,
            double x,
            double y,
            double sideLength,
            Color fillColor,
            Color borderColor )
    {
        Polygon triangle = new Polygon();

        // add a point straight ahead and slightly longer than side length
        triangle.addPoint( 0, (int) ( 1.5 * sideLength ) );
        // add the two side points slightly off to the right and left of center
        triangle.addPoint( (int) ( sideLength / 2 ), 0 );
        triangle.addPoint( (int) ( -sideLength / 2 ), 0 );

        // move the origin of drawing to the x and y coordinate to be drawn
        bbg.translate( x, y );
        // rotate the g2d object by heading
        bbg.rotate( heading );

        // set the fill color and draw
        bbg.setColor( fillColor );
        bbg.fill( triangle );
        // set the border color and draw
        bbg.setColor( borderColor );
        bbg.draw( triangle );

        // undo the transformations
        bbg.rotate( -heading );
        bbg.translate( -x, -y );
    }

    /**
     * Reads a locations file and creates an array of points
     * 
     * @param filename The locations filename
     * @param numLocations Number of locations wanted
     * @return The array of points
     */
    public static Point2D[] readPoints( String filename, int numLocations )
    {
        // array of points
        Point2D[] locations = new Point2D[numLocations];

        // create a scanner to read in file
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
        // close and return
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
        List<ProbabilityRange> forTheMath = new ArrayList<ProbabilityRange>();
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
                if( decision == null )
                {
                    System.out.println( "Something went wrong getting decision" );
                }
                break;
            }
            // keep track of the sum of probabilities (shouldn't be greater than
            // 1)
            sum += temp.getProbability();
        }
        // Debug output if something went wrong getting decision
        if( decision == null )
        {
            System.out.println( "ERROR Num decisions = "
                    + possibleDecisions.size() );
            System.out.println( "ERROR Rand = " + rand );
            for( int i = 0; i < possibleDecisions.size(); i++ )
            {
                System.out.println( possibleDecisions.get( i ).getConflict() );
            }
            System.out.println( Arrays.toString( possibleDecisions.toArray() ) );
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
        /** The minimum random number for selection */
        public double min = 0.0;

        /** The maximum random number for selection */
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
