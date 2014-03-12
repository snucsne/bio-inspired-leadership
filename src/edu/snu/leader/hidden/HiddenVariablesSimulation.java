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

// Imports
import ec.util.MersenneTwisterFast;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;


/**
 * HiddenVariablesSimulation
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class HiddenVariablesSimulation
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            HiddenVariablesSimulation.class.getName() );


    /** The size of the group */
    private int _groupSize = 0;

    /** Array of all the individual's information */
    private IndividualInfo[] _individuals = new IndividualInfo[0];

    /** Random number generator */
    private MersenneTwisterFast _random = null;

    /** The number of times to run the simulation */
    private int _simRunCount = 10000;




    /**
     * Initialize the simulation
     */
    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Get the number of individuals
        _groupSize = 10;
        _individuals = new IndividualInfo[ _groupSize ];

        // Initialize all the individuals
        float initiationRate = 1290 * _groupSize;
        float cancelAlpha = 0.009f;
        float cancelGamma = 2.0f;
        float cancelEpsilon = 2.3f;
        float followAlpha = 162.3f;
        float followBeta = 75.4f;
        for( int i = 0; i < _individuals.length; ++i )
        {
            _individuals[i] = new IndividualInfo( initiationRate,
                    cancelAlpha,
                    cancelGamma,
                    cancelEpsilon,
                    followAlpha,
                    followBeta );
        }

        // Set the random seed
        _random = new MersenneTwisterFast( 42 );

        _LOG.trace( "Leaving initialize()" );
    }

    /**
     * Runs the simulation
     */
    public void run()
    {
        _LOG.trace( "Entering run()" );

        // Create some handy variables
        int[] movementCounts = new int[ _individuals.length + 1 ];
        int[][] followCounts = new int[ _individuals.length ][ _individuals.length ];

        // Run the simulation a number of times
        for( int run = 0; run < _simRunCount; ++run )
        {
            List<Integer> remaining = new LinkedList<Integer>();
            List<Integer> departed = new LinkedList<Integer>();

            // Get the initiator
            int initiatorIdx = getInitiatorIdx();
            departed.add( initiatorIdx );
            _individuals[initiatorIdx].signalInitiationAttempt();

            _LOG.debug( "Ind["
                    + initiatorIdx
                    + "] initiated" );

            // Add all the others to the remaining list
            for( int i = 0; i < _individuals.length; i++ )
            {
                if( i != initiatorIdx )
                {
                    remaining.add( i );
                }
            }

            // Proceed until everyone follows or we cancel
            boolean active = true;
            while( active )
            {
                // Get the number remaining
                int remainingCount = remaining.size();
                int departedCount = _groupSize - remainingCount;

                _LOG.debug( "      remainingCount=["
                        + remainingCount
                        + "]" );
                _LOG.debug( "      departedCount=["
                        + departedCount
                        + "]" );

                // Calculate the follow times for the remaining individuals
                float followTime = Float.POSITIVE_INFINITY;
                int followerIdx = 0;
                for( int i = 0; i < _individuals.length; i++ )
                {
                    // Is it a remaining individual?
                    if( remaining.contains( i ) )
                    {
                        // Yup
                        float indFollowTime = generateRandomExponential(
                                1.0f / calculateFollowRate( _individuals[i],
                                        departedCount,
                                        _individuals.length ) );

                        // Is it the soonest?
                        if( followTime > indFollowTime )
                        {
                            // Yup again
                            followTime = indFollowTime;
                            followerIdx = i;
                        }
                    }
                }

                // Calculate the cancellation time
                float cancellationTime = generateRandomExponential(
                        1.0f / calculateCancelationRate( _individuals[initiatorIdx],
                                departedCount ) );

                _LOG.debug( "      Earliest follow time ["
                        + followTime
                        + "]" );
                _LOG.debug( "      Cancellation time ["
                        + cancellationTime
                        + "]" );

                // Does the follower follow before the initiator cancels?
                if( followTime <= cancellationTime )
                {
                    // Yup.  Move the individual from remaining to departed
                    boolean success = remaining.remove( new Integer( followerIdx ) );
                    departed.add( followerIdx );

                    _LOG.debug( "    Ind["
                            + followerIdx
                            + "] followed" );
                }
                else
                {
                    // Nope.  This run is done!
                    active = false;
                    _LOG.debug( "  Ind["
                            + initiatorIdx
                            + "] CANCELLED" );
                }


                // Are we done?
                if( 0 >= remaining.size() )
                {
                    // Yup
                    active = false;
                }
            }

            _LOG.debug( "Total departed=["
                    + departed.size()
                    + "]" );

            // Record how many individuals moved
            movementCounts[ departed.size() ]++;

            // Was the movement successful?
            if( 0 == remaining.size() )
            {
                // Yup
                _individuals[initiatorIdx].signalInitiationSuccess();
            }

            // Track the following counts
            if( 1 < departed.size() )
            {
                for( int leaderIdx = 1;
                        leaderIdx < (departed.size() - 1);
                        leaderIdx++ )
                {
                    int leader = departed.get( leaderIdx );
                    for( int followerIdx = (leaderIdx+1);
                            followerIdx < departed.size();
                            followerIdx++  )
                    {
                        int follower = departed.get( followerIdx );
                        followCounts[leader][follower]++;
                    }
                }
            }
        }

        // Calculate the total number of initiation attempts and successes
        int totalInitiations = 0;
        int totalSuccesses = 0;
        for( int i = 0; i < _individuals.length; i++ )
        {
            totalInitiations += _individuals[i].getInitiationAttempts();
            totalSuccesses += _individuals[i].getInitiationSuccesses();
        }
        System.out.println( "# ==========================================" );
        System.out.println( "# Initiation stats" );
        System.out.println( "initiations = " + totalInitiations );
        System.out.println( "successes = " + totalSuccesses );
        System.out.println();
        System.out.println();

        // Print out the stats
        System.out.println( "# ==========================================" );
        System.out.println( "# Individual initiation stats" );
        for( int i = 0; i < _individuals.length; i++ )
        {
            System.out.println( "initiation."
                    + String.format( "%02d", i )
                    + ".attempts = "
                    + _individuals[i].getInitiationAttempts() );
            System.out.println( "initiation."
                    + String.format( "%02d", i )
                    + ".successes = "
                    + _individuals[i].getInitiationSuccesses() );
            System.out.println( "initiation."
                    + String.format( "%02d", i )
                    + ".attempt-percentage = "
                    + ( ((float) _individuals[i].getInitiationAttempts())
                            / totalInitiations ) );
            System.out.println( "initiation."
                    + String.format( "%02d", i )
                    + ".success-percentage = "
                    + ( ((float) _individuals[i].getInitiationSuccesses())
                            / totalSuccesses ));
        }
        System.out.println();
        System.out.println();

        // Print out the movement counts
        System.out.println( "# ==========================================" );
        System.out.println( "# Movement counts" );
        for( int i = 0; i < movementCounts.length; i++ )
        {
            System.out.println( "move."
                    + String.format( "%02d", i )
                    + " = "
                    + movementCounts[i] );
        }
        System.out.println();
        System.out.println();

        // Print out the follower stats
        System.out.println( "# ==========================================" );
        System.out.println( "# Follower stats" );
        for( int i = 0; i < followCounts.length; i++ )
        {
            for( int j = 0; j < followCounts[i].length; j++ )
            {
                System.out.println( "leader."
                        + String.format( "%02d", i )
                        + ".follower."
                        + String.format( "%02d", j )
                        + " = "
                        + followCounts[i][j] );
            }
            System.out.println();
        }
        System.out.println();

        // Print out the rates
        System.out.println( "# ==========================================" );
        System.out.println( "# Rate information" );
        for( int i = 1; i < _groupSize; i++ )
        {
            System.out.println( "cancel-rate."
                    + String.format( "%02d", i )
                    + " = "
                    + (calculateCancelationRate( _individuals[0], i ) ) );
        }
        System.out.println();
        for( int i = 1; i < _groupSize; i++ )
        {
            System.out.println( "follow-rate."
                    + String.format( "%02d", i )
                    + " = "
                    + (calculateFollowRate( _individuals[0], i, _groupSize ) ) );
        }

        _LOG.trace( "Leaving run()" );
    }


    public static void main( String[] args )
    {
        // Build, initialize, run
        HiddenVariablesSimulation sim = new HiddenVariablesSimulation();
        sim.initialize();
        sim.run();
    }

    /**
     * Get the index of the initiator
     *
     * @return
     */
    private int getInitiatorIdx()
    {
        int initiatorIdx = 0;
        float earliestTime = Float.POSITIVE_INFINITY;

        // Generate an initiation time for each individual
        for( int i = 0; i < _individuals.length; i++ )
        {
            // Generate the individual's initiation time
            float initiationTime = generateRandomExponential(
                    1.0f / _individuals[i].getInitiationRate() );

            // Is it the earliest?
            if( earliestTime > initiationTime )
            {
                // Yup
                earliestTime = initiationTime;
                initiatorIdx = i;
            }
        }

        return initiatorIdx;
    }

    /**
     * Calculate the cancellation rate for a given individual and number of
     * departed individuals
     *
     * @param ind
     * @param departed
     * @return
     */
    private float calculateCancelationRate( IndividualInfo ind, int departed )
    {
        float denominator = 1.0f
                + (float) Math.pow( (departed / ind.getCancelGamma()),
                        ind.getCancelEpsilon() );
        float numerator = ind.getCancelAlpha();
//        _LOG.debug( "departed=[" + departed + "]" );
//        _LOG.debug( "denominator=[" + denominator + "]" );
//        _LOG.debug( "numerator=[" + numerator + "]" );
//        _LOG.debug( "rate=[" + (1.0f / ( numerator / denominator )) + "]" );
        return 1.0f / ( numerator / denominator );
    }

    /**
     * Calculate the follow rate for a given individual and number of departed individuals
     *
     * @param ind
     * @param departed
     * @param groupSize
     * @return
     */
    private float calculateFollowRate( IndividualInfo ind, int departed, int groupSize )
    {
        return ind.getFollowAlpha()
                + ( ( ind.getFollowBeta() * (groupSize - departed) ) / departed );
    }

    /**
     * Generates a random number from the exponential distribution with the
     * specified rate
     *
     * @param rate
     * @return
     */
    private float generateRandomExponential( float rate )
    {
        float randomUniform = _random.nextFloat();
        float next = (float) ( -1 * Math.log( 1 - randomUniform ) )
                / rate;

        return next;
    }
}
