/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SimulationState;
import org.apache.commons.lang.Validate;


/**
 * FunctionSueurSMoveValue
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class FunctionSueurSMoveValue implements SueurSMoveValue
{
    protected enum SMoveFunction {
        /** Petit function described in Sueur paper */
        PETIT() {
            @Override
            protected float execute( SimulationState simState,
                    int departed,
                    int groupSize,
                    float alphaMove,
                    float betaMove )
            {
                return groupSize - departed;
            }
        },
        /** Corrected Petit function that matches original paper */
        PETITCORRECTED() {
            @Override
            protected float execute( SimulationState simState,
                    int departed,
                    int groupSize,
                    float alphaMove,
                    float betaMove )
            {
                return (departed
                        / ( ( (1 / betaMove) / (( 1 / alphaMove ) + ( ( (1 / betaMove) * (groupSize - departed) ) / departed )) )
                                - ( alphaMove / betaMove )))
                        - departed;
            }
        };

        protected abstract float execute( SimulationState simState,
                int departed,
                int groupSize,
                float alphaMove,
                float betaMove );
    };

    private SMoveFunction _function = null;


    /**
     * Builds this FunctionSueurSMoveValue object
     *
     * @param functionDesc A description of the function
     */
    public FunctionSueurSMoveValue( String functionDesc )
    {
        // Determine which function we use
        _function = SMoveFunction.valueOf( functionDesc.toUpperCase().trim() );
        Validate.notNull( _function,
                "Unknown SMoveFunction of type ["
                + functionDesc
                + "]" );
    }

    /**
     * Returns the s-move value used in the Sueur collective movement model
     *
     * @param simState The simulation state
     * @param departed The number of individuals who have already departed
     * @param groupSize The size of the group
     * @param alphaMove Intrinsic probability that an individual starts moving
     * @param betaMove Mimetic coefficient for joining movement
     * @return The s-move value
     * @see edu.snu.leader.hidden.event.SueurSMoveValue#getValue(edu.snu.leader.hidden.SimulationState, int, int)
     */
    @Override
    public float getValue( SimulationState simState,
            int departed,
            int groupSize,
            float alphaMove,
            float betaMove )
    {
        return _function.execute( simState, departed, groupSize, alphaMove, betaMove );
    }

}
