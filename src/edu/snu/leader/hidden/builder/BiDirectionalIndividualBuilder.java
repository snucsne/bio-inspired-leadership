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
import java.util.Properties;


/**
 * BiDirectionalIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class BiDirectionalIndividualBuilder
        extends AssertivenessAndDirectionIndividualBuilder
{
    /** Key for the direction delta */
    protected static final String _DIR_DELTA_KEY = "direction-delta";

    /** Key for the positive delta probability  */
    protected static final String _POS_DELTA_PROB_KEY = "positive-delta-probability";


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
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

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
        _LOG.info( "Using _dirDelta=[" + _positiveDeltaProbability + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Creates a preferred direction using a random value drawn from a
     * Gaussian distribution
     *
     * @return The direction
     * @see edu.snu.leader.hidden.builder.AssertivenessAndDirectionIndividualBuilder#createGaussianDirection()
     */
    @Override
    protected float createGaussianDirection()
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

        return direction;
    }

}
