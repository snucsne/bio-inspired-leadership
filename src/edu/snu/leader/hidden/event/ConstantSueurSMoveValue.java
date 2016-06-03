/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SimulationState;

/**
 * ConstantSueurSMoveValue
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ConstantSueurSMoveValue implements SueurSMoveValue
{
    /** The constant s-move value */
    private float _sMoveValue = 0.0f;


    /**
     * Builds this ConstantSueurSMoveValue object
     *
     * @param value The constant s-move value
     */
    public ConstantSueurSMoveValue( float value )
    {
        _sMoveValue = value;
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
        return _sMoveValue;
    }

}
