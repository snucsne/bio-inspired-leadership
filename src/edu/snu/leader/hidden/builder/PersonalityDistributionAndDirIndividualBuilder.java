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
import ec.util.MersenneTwisterFast;
import edu.snu.leader.hidden.SimulationState;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Properties;


/**
 * PersonalityDistributionAndDirIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityDistributionAndDirIndividualBuilder
        extends PersonalityDistributionIndividualBuilder
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PersonalityDistributionAndDirIndividualBuilder.class.getName() );

    /** Key for the direction delta */
    protected static final String _DIR_DELTA_KEY = "direction-delta";

    /** Key for the positive delta probability  */
    protected static final String _POS_DELTA_PROB_KEY = "positive-delta-probability";

    /** Key for the mean preferred direction */
    protected static final String _DIR_MEAN_KEY = "direction-mean";

    /** Key for the preferred direction std dev */
    protected static final String _DIR_STD_DEV_KEY = "direction-std-dev";

    /** Key for the minimum preferred direction */
    protected static final String _MIN_DIR_KEY = "min-direction";

    /** Key for the maximum preferred direction */
    protected static final String _MAX_DIR_KEY = "max-direction";



    /** Mean preferred direction */
    protected float _directionMean = 0.0f;

    /** Preferred direction standard deviation */
    protected float _directionStdDev = 0.0f;

    /** Minimum preferred direction */
    protected float _minDirection = 0.0f;

    /** Maximum preferred direction */
    protected float _maxDirection = 0.0f;

    /** The maximum number of tries to get a gaussian random number in the
     * correct range */
    protected int _maxTries = 5;

    /** Delta to apply to directions */
    protected float _dirDelta = 0.0f;

    /** Probability of group to apply a positive delta */
    protected float _positiveDeltaProbability = 0.0f;


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

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

        // Get the direction delta value
        String dirDeltaStr = props.getProperty( _DIR_DELTA_KEY );
        Validate.notEmpty( dirDeltaStr,
                "Direction delta (key="
                + _DIR_DELTA_KEY
                + ") may not be empty" );
        _dirDelta = Float.parseFloat( dirDeltaStr );
        _LOG.info( "Using _dirDelta=[" + _dirDelta + "]" );

        // Get the positive delta probability
        String posDeltaProbabilityStr = props.getProperty( _POS_DELTA_PROB_KEY );
        Validate.notEmpty( posDeltaProbabilityStr,
                "Positive delta percentage (key="
                + _POS_DELTA_PROB_KEY
                + ") may not be empty" );
        _positiveDeltaProbability = Float.parseFloat( posDeltaProbabilityStr );
        _LOG.info( "Using _positiveDeltaProbability=[" + _positiveDeltaProbability + "]" );

    }

    /**
     * Creates the preferred direction for an individual
     *
     * @return The preferred direction
     * @see edu.snu.leader.hidden.builder.PersonalityDistributionIndividualBuilder#createPreferredDir()
     */
    @Override
    protected float createPreferredDir()
    {
        // Random number generator
        MersenneTwisterFast random = _simState.getRandom();

        int tries = 0;
        float direction = Float.MAX_VALUE;
        while( ((_minDirection > direction) || (_maxDirection < direction))
                && (_maxTries > tries) )
        {
            direction = _directionMean
                    + ( (float) random.nextGaussian() * _directionStdDev );

            // Apply either a positive or negative delta
            float delta = _dirDelta;
            if( random.nextFloat() > _positiveDeltaProbability )
            {
                delta *= -1.0f;
            }
            direction += delta;

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

        _LOG.debug( "Created direction ["
                + direction
                + "]" );

        return direction;
    }


}
