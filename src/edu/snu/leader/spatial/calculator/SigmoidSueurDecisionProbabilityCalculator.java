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
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import edu.snu.leader.spatial.SimulationState;


/**
 * SigmoidSueurDecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SigmoidSueurDecisionProbabilityCalculator
        extends AbstractSueurDecisionProbabilityCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SigmoidSueurDecisionProbabilityCalculator.class.getName() );
    
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
    protected float calculateK( float value )
    {
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * _sigmoidSlopeValue) ) );
    }

}
