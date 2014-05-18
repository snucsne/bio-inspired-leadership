/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

/**
 * SigmoidPersonalityEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SigmoidPersonalityEventTimeCalculator extends
        PersonalityEventTimeCalculator
{


    /**
     * Builds this SigmoidPersonalityEventTimeCalculator object
     *
     */
    public SigmoidPersonalityEventTimeCalculator()
    {
        _description = "k = 2.0f  * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) )";
    }

    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    @Override
    protected float calculateK( float value )
    {
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) );
    }

}
