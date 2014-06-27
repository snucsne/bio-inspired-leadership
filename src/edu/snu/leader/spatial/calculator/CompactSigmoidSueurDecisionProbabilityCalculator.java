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
package edu.snu.leader.spatial.calculator;

// Imports
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.SimulationState;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.util.Properties;


/**
 * CompactSigmoidSueurDecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class CompactSigmoidSueurDecisionProbabilityCalculator extends
        AbstractSueurDecisionProbabilityCalculator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            CompactSigmoidSueurDecisionProbabilityCalculator.class.getName() );

    /** Key for modifying the sigmoid slope value */
    private final String _SIGMOID_SLOPE_VALUE_KEY = "sigmoid-slope-value";


    /** Sigmoid slope value */
    private float _sigmoidSlopeValue = 0.0f;


    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#initialize(edu.snu.leader.spatial.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProperties();

        // Get the sigmoid slope value
        String sigmoidSlopeValueStr = props.getProperty(_SIGMOID_SLOPE_VALUE_KEY);
        Validate.notEmpty(sigmoidSlopeValueStr,
                "Sigmoid slope value (Key ="
                + _SIGMOID_SLOPE_VALUE_KEY
                + ") may not be empty " );
        _sigmoidSlopeValue = Float.parseFloat(sigmoidSlopeValueStr);
        _LOG.info( "Using _sigmoidSlopeValue = [" + _sigmoidSlopeValue + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     * @see edu.snu.leader.spatial.calculator.AbstractSueurDecisionProbabilityCalculator#calculateK(float)
     */
    @Override
    protected float calculateK( float value, DecisionType type )
    {
        float k = 0.0f;

        // If it is initiation or following, use a range of [0.1,1.9]
        if( DecisionType.INITIATE.equals( type )
                || DecisionType.FOLLOW.equals( type ) )
        {
            k = 0.1f + ( 1.8f * ( 1.0f / (1.0f +
                    (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) ) );
        }
        // Otherwise, if it is a cancel, use a more compact range
        else if( DecisionType.CANCEL.equals( type ) )
        {
//            k = 0.6f + ( 0.8f * ( 1.0f / (1.0f +
//                    (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) ) );
            k = 0.59623023f + ( 0.80753954f * ( 1.0f / (1.0f +
                    (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) ) );
        }

        return k;
    }

}
