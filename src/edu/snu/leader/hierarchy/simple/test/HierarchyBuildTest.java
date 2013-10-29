/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple.test;

// Imports
import org.apache.log4j.Logger;

import ec.util.MersenneTwisterFast;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * HierarchyBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class HierarchyBuildTest
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            HierarchyBuildTest.Distribution.class.getName() );


    public enum Distribution {
        NORMAL,
        GAUSSIAN
    };

    /** The number of individuals to use */
    private int _individualCount = 0;

    /** The number of neighbors that are considered "nearest" */
    private int _nearestNeighborCount = 0;

    /** Amount to increment the motivation each timestep */
    private float _motivationTimeIncrement = 0.0f;

    /** Motivation increment multiplier for neighbors */
    private float _motivationNeighborIncrement = 0.0f;

    /** The motivation distribution */
    private Distribution _motivationDist = Distribution.NORMAL;

    /** The threshold distribution */
    private Distribution _thresholdDist = Distribution.NORMAL;

    /** The random number generator */
    private MersenneTwisterFast _rng = null;

    /** All the individuals */
    private List<TestIndividual> _allIndividuals = new LinkedList<TestIndividual>();

    /** The last timestep in the simulation */
    private long _lastTimestep = -1;

    /** The timestep in which the first individual activated */
    private long _firstActiveTimestep = -1;

    /**
     * Builds this HierarchyBuildTest object
     */
    public HierarchyBuildTest()
    {
        _individualCount = 750;
        _nearestNeighborCount = 7;
        _motivationTimeIncrement = 0.0005f;
        _motivationNeighborIncrement = 0.02f;
        _rng = new MersenneTwisterFast( 42 );
    }

    /**
     * Initializes the test
     */
    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Build all the individuals
        for( int i = 0; i < _individualCount; i++ )
        {
            // Generate an id
            String id = String.format( "id%04d", (i + 1) );

            // Generate a random location
            float x = _rng.nextFloat();
            float y = _rng.nextFloat();
            Point2D location = new Point2D.Float( x, y );

            // Generate a random motivation
            float motivation = 0.0f;
            if( Distribution.NORMAL.equals( _motivationDist ) )
            {
                motivation = _rng.nextFloat() / 2.0f;
            }
            else if( Distribution.GAUSSIAN.equals( _motivationDist ) )
            {
                motivation = (float) Math.min( 0.0,
                        Math.max( 0.5, ((_rng.nextGaussian() + 2.0) / 8.0) ) );
            }

            // Generate a random threshold
            float threshold = 0.0f;
            if( Distribution.NORMAL.equals( _thresholdDist ) )
            {
                threshold = ( _rng.nextFloat() / 2.0f ) + 0.5f;
            }
            else if( Distribution.GAUSSIAN.equals( _thresholdDist ) )
            {
                threshold = 0.5f + (float) Math.min( 0.0,
                        Math.max( 0.5, ((_rng.nextGaussian() + 2.0) / 8.0) ) );
            }

            // Create the individual and add it to the list
            _allIndividuals.add( new TestIndividual(
                    id,
                    motivation,
                    threshold,
                    location,
                    _nearestNeighborCount,
                    _motivationTimeIncrement,
                    _motivationNeighborIncrement ) );
        }

        // Initialize all the individuals
        Iterator<TestIndividual> indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            TestIndividual ind = indIter.next();
            ind.initialize( _allIndividuals );
        }

        _LOG.trace( "Leaving initialize()" );
    }

    /**
     * Run the test
     */
    public void run()
    {
        _LOG.trace( "Entering run()" );

        // Repeatedly update individuals until all are active
        boolean inactivesExist = true;
        long timestep = 0;
        TestIndividual ind = null;
        Iterator<TestIndividual> indIter = null;
        while( inactivesExist )
        {
            timestep++;

            // Default to everyone being active
            inactivesExist = false;

            indIter = _allIndividuals.iterator();
            while( indIter.hasNext() )
            {
                ind = indIter.next();
                ind.update( timestep );

                // Is the individual inactive?
                if( !ind.isActive() )
                {
                    inactivesExist = true;
                }
                else
                {
                    if( _firstActiveTimestep < 0 )
                    {
                        _firstActiveTimestep = timestep;
                    }
                }
            }

        }

        _lastTimestep = timestep;

        _LOG.trace( "Leaving run()" );
    }

    /**
     * Generates a report
     */
    public void report()
    {
        _LOG.trace( "Entering report()" );

        System.out.println( "digraph hierarchy {" );
        System.out.println( "  node [style=\"filled,solid\",color=\"#000000\"];");

        Iterator<TestIndividual> indIter = _allIndividuals.iterator();
        while( indIter.hasNext() )
        {
            TestIndividual ind = indIter.next();
            TestNeighbor leader = ind.getLeader();

            long activeTimestamp = ind.getActiveTimestep();
            float percent = (_lastTimestep - activeTimestamp)
                    / (float) (_lastTimestep - _firstActiveTimestep);
            int red = Math.round( (1 - percent) * 170 );
            int green = Math.round( percent * 170 );
            if( null != leader )
            {
                System.out.println( ind.getId()
                        + " [shape=circle,fillcolor=\"#"
                        + String.format( "%02X", red )
                        + String.format( "%02X", green)
                        + "00\",label=\"\"];" );
                System.out.println( leader.getIndividual().getId()
                        + " -> "
                        + ind.getId()
                        + " [dir=\"back\"];" );
//                System.out.println( ind.getId()
//                        + " -> "
//                        + leader.getIndividual().getId()
//                        + ";" );

//                        + " [ label = \""
//                        + ind.getActiveTimestep()
//                        + "\" ];" );
            }
            else
            {
                System.out.println( ind.getId()
                        + " [shape=diamond,height=1,width=1,fillcolor=\"#"
                        + String.format( "%02X", red )
                        + String.format( "%02X", green)
                        + "00\",label=\"\"];" );
            }
        }
        System.out.println( "}" );

        _LOG.trace( "Leaving report()" );
    }

    public static void main( String[] args )
    {
        HierarchyBuildTest test = new HierarchyBuildTest();
        test.initialize();
        test.run();
        test.report();
    }
}
