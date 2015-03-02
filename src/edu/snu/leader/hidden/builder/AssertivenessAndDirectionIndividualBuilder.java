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
import edu.snu.leader.hidden.SpatialIndividual;

import java.util.Properties;


/**
 * AssertivenessAndDirectionIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class AssertivenessAndDirectionIndividualBuilder
        extends AbstractIndividualBuilder
{
    /** Our logger */
    protected static final Logger _LOG = Logger.getLogger(
            AssertivenessAndDirectionIndividualBuilder.class.getName() );

    /** Key for the mean assertiveness value */
    protected static final String _ASSERT_MEAN_KEY = "assertiveness-mean";

    /** Key for the assertiveness value std dev */
    protected static final String _ASSERT_STD_DEV_KEY = "assertiveness-std-dev";

    /** Key for the minimum assertiveness value */
    protected static final String _MIN_ASSERT_KEY = "min-assertiveness";

    /** Key for the maximum assertiveness value */
    protected static final String _MAX_ASSERT_KEY = "max-assertiveness";

    /** Key for the type of random number distribution to use for assertiveness */
    protected static final String _ASSERT_RNG_DIST_KEY = "assertiveness-rng-dist";

    /** Key for the mean preferred direction */
    protected static final String _DIR_MEAN_KEY = "direction-mean";

    /** Key for the preferred direction std dev */
    protected static final String _DIR_STD_DEV_KEY = "direction-std-dev";

    /** Key for the minimum preferred direction */
    protected static final String _MIN_DIR_KEY = "min-direction";

    /** Key for the maximum preferred direction */
    protected static final String _MAX_DIR_KEY = "max-direction";

    /** Key for the type of random number distribution to use for direction */
    protected static final String _DIR_RNG_DIST_KEY = "direction-rng-dist";




    /** Types of random number distributions */
    protected enum RNDistribution {
        UNIFORM,
        GAUSSIAN
    };

    /** Mean assertiveness value */
    protected float _assertivenessMean = 0.0f;

    /** Assertiveness value standard deviation */
    protected float _assertivenessStdDev = 0.0f;

    /** Minimum assertiveness value */
    protected float _minAssertiveness = 0.0f;

    /** Maximum assertiveness value */
    protected float _maxAssertiveness = 0.0f;

    /** The type random number distribution to use for assertiveness */
    protected RNDistribution _assertivenessRNDist = null;


    /** Mean preferred direction */
    protected float _directionMean = 0.0f;

    /** Preferred direction standard deviation */
    protected float _directionStdDev = 0.0f;

    /** Minimum preferred direction */
    protected float _minDirection = 0.0f;

    /** Maximum preferred direction */
    protected float _maxDirection = 0.0f;

    /** The type random number distribution to use for direction */
    protected RNDistribution _directionRNDist = null;

    /** The maximum number of tries to get a gaussian random number in the
     * correct range */
    protected int _maxTries = 5;

    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the mean assertiveness value
        String assertivenessMeanStr = props.getProperty( _ASSERT_MEAN_KEY );
        Validate.notEmpty( assertivenessMeanStr,
                "Assertiveness mean (key="
                + _ASSERT_MEAN_KEY
                + ") may not be empty" );
        _assertivenessMean = Float.parseFloat( assertivenessMeanStr );
        _LOG.info( "Using _assertivenessMean=[" + _assertivenessMean + "]" );

        // Get the assertiveness value standard deviation
        String assertivenessStdDevStr = props.getProperty( _ASSERT_STD_DEV_KEY );
        Validate.notEmpty( assertivenessStdDevStr,
                "Assertiveness std dev (key="
                + _ASSERT_STD_DEV_KEY
                + ") may not be empty" );
        _assertivenessStdDev = Float.parseFloat( assertivenessStdDevStr );
        _LOG.info( "Using _assertivenessStdDev=[" + _assertivenessStdDev + "]" );

        // Get the min assertiveness
        String minAssertivenessStr = props.getProperty( _MIN_ASSERT_KEY );
        Validate.notEmpty( minAssertivenessStr,
                "Minimum Assertiveness value (key="
                + _MIN_ASSERT_KEY
                + ") may not be empty" );
        _minAssertiveness = Float.parseFloat( minAssertivenessStr );
        _LOG.info( "Using _minAssertiveness=[" + _minAssertiveness + "]" );

        // Get the max assertiveness
        String maxAssertivenessStr = props.getProperty( _MAX_ASSERT_KEY );
        Validate.notEmpty( maxAssertivenessStr,
                "Maximum Assertiveness value (key="
                + _MAX_ASSERT_KEY
                + ") may not be empty" );
        _maxAssertiveness = Float.parseFloat( maxAssertivenessStr );
        _LOG.info( "Using _maxAssertiveness=[" + _maxAssertiveness + "]" );

        // Get the random number distribution for
        String assertivenessRNDistStr = props.getProperty( _ASSERT_RNG_DIST_KEY );
        Validate.notEmpty( assertivenessRNDistStr,
                "Random number distribution for assertiveness (key="
                + _ASSERT_RNG_DIST_KEY
                + ") may not be empty" );
        _assertivenessRNDist = RNDistribution.valueOf( assertivenessRNDistStr.toUpperCase() );
        _LOG.info( "Using _assertivenessRNDist=[" + _assertivenessRNDist + "]" );


        // Get the mean direction value
        String directionMeanStr = props.getProperty( _DIR_MEAN_KEY );
        Validate.notEmpty( directionMeanStr,
                "Direction mean (key="
                + _DIR_MEAN_KEY
                + ") may not be empty" );
        _directionMean = Float.parseFloat( directionMeanStr );
        _LOG.info( "Using _directionMean=[" + _directionMean + "]" );

        // Get the  value standard deviation
        String directionStdDevStr = props.getProperty( _DIR_STD_DEV_KEY );
        Validate.notEmpty( directionStdDevStr,
                "Direction std dev (key="
                + _DIR_STD_DEV_KEY
                + ") may not be empty" );
        _directionStdDev = Float.parseFloat( directionStdDevStr );
        _LOG.info( "Using _directionStdDev=[" + _directionStdDev + "]" );

        // Get the min direction
        String minDirectionStr = props.getProperty( _MIN_DIR_KEY );
        Validate.notEmpty( minDirectionStr,
                "Minimum Direction (key="
                + _MIN_DIR_KEY
                + ") may not be empty" );
        _minDirection = Float.parseFloat( minDirectionStr );
        _LOG.info( "Using _minDirection=[" + _minDirection + "]" );

        // Get the max direction
        String maxDirectionStr = props.getProperty( _MAX_DIR_KEY );
        Validate.notEmpty( maxDirectionStr,
                "Maximum direction (key="
                + _MAX_DIR_KEY
                + ") may not be empty" );
        _maxDirection = Float.parseFloat( maxDirectionStr );
        _LOG.info( "Using _maxDirection=[" + _maxDirection + "]" );

        // Get the random number distribution for the direction
        String directionRNDistStr = props.getProperty( _DIR_RNG_DIST_KEY );
        Validate.notEmpty( directionRNDistStr,
                "Random number distribution for direction (key="
                + _DIR_RNG_DIST_KEY
                + ") may not be empty" );
        _directionRNDist = RNDistribution.valueOf( directionRNDistStr.toUpperCase() );
        _LOG.info( "Using _directionRNDist=[" + _directionRNDist + "]" );


        _LOG.trace( "Leaving initialize( simState )" );
    }


    /**
     * TODO Method description
     *
     * @param index
     * @return
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create the assertiveness
        float assertiveness = 0.0f;
        if( RNDistribution.GAUSSIAN.equals( _assertivenessRNDist ) )
        {
            assertiveness = createGaussianAssertiveness();
        }
        else if( RNDistribution.UNIFORM.equals( _assertivenessRNDist ) )
        {
            assertiveness = createUniformAssertiveness();
        }
        else
        {
            _LOG.error( "Unknown assertiveness distribution ["
                    + _assertivenessRNDist
                    + "]" );
            throw new RuntimeException( "Unknown assertiveness distribution ["
                    + _assertivenessRNDist
                    + "]" );
        }

        // Create the preferred direction
        float preferredDirection = 0.0f;
        if( RNDistribution.GAUSSIAN.equals( _directionRNDist ) )
        {
            preferredDirection = createGaussianDirection();
        }
        else if( RNDistribution.UNIFORM.equals( _directionRNDist ) )
        {
            preferredDirection = createUniformDirection();
        }
        else
        {
            _LOG.error( "Unknown direction distribution ["
                    + _directionRNDist
                    + "]" );
            throw new RuntimeException( "Unknown direction distribution ["
                    + _directionRNDist
                    + "]" );
        }

        // Create a valid location
        Vector2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                DEFAULT_PERSONALITY,
                assertiveness,
                preferredDirection,
                DEFAULT_RAW_CONFLICT,
                false );

        return ind;
    }


    /**
     * Creates a assertiveness using a random value drawn from a
     * Gaussian distribution
     *
     * @return The assertiveness value
     */
    protected float createGaussianAssertiveness()
    {
        int tries = 0;
        float assertiveness = Float.MAX_VALUE;
        while( ((_minAssertiveness > assertiveness)
                    || (_maxAssertiveness < assertiveness))
                && (_maxTries > tries) )
        {
            assertiveness = _assertivenessMean
                    + ( (float) _simState.getRandom().nextGaussian() * _assertivenessStdDev );
            tries++;
        }
        if( _maxAssertiveness < assertiveness )
        {
            assertiveness = _maxAssertiveness;
        }
        else if (_minAssertiveness > assertiveness )
        {
            assertiveness = _minAssertiveness;
        }

        return assertiveness;
    }

    /**
     * Creates a assertiveness using a random value drawn from a
     * Uniform distribution
     *
     * @return The assertiveness value
     */
    protected float createUniformAssertiveness()
    {
        float assertiveness = ( _simState.getRandom().nextFloat()
                * (_maxAssertiveness - _minAssertiveness) )
                + _minAssertiveness;

        return assertiveness;
    }

    /**
     * Creates a preferred direction using a random value drawn from a
     * Gaussian distribution
     *
     * @return The direction
     */
    protected float createGaussianDirection()
    {
        int tries = 0;
        float direction = Float.MAX_VALUE;
        while( ((_minDirection > direction) || (_maxDirection < direction))
                && (_maxTries > tries) )
        {
            direction = _directionMean
                    + ( (float) _simState.getRandom().nextGaussian() * _directionStdDev );
            tries++;
        }
        if( _maxDirection < direction )
        {
            direction = _maxDirection;
        }
        else if (_minDirection > direction )
        {
            direction = _minDirection;
        }

        return direction;
    }

    /**
     * Creates a direction using a random value drawn from a
     * Uniform distribution
     *
     * @return The direction
     */
    protected float createUniformDirection()
    {
        float direction = ( _simState.getRandom().nextFloat()
                * (_maxDirection - _minDirection) )
                + _minDirection;

        return direction;
    }

}
