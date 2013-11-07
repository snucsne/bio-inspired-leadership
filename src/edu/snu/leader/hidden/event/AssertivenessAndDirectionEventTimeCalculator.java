/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MathUtils;
import org.apache.log4j.Logger;


/**
 * AssertivenessAndDirectionEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class AssertivenessAndDirectionEventTimeCalculator extends
        AbstractEventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            AssertivenessAndDirectionEventTimeCalculator.class.getName() );

    /** Description of the algorithm */
    private String _description = "";



    /**
     * Calculates the time at the specified individual will initiate movement
     *
     * @param ind The individual
     * @return The initiation time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateInitiationTime(edu.snu.leader.hidden.SpatialIndividual)
     */
    @Override
    public float calculateInitiationTime( SpatialIndividual ind )
    {
        // Get the initation rate
        float tau = calculateInitiationRate(
                _simState.getIndividualCount() );

        // Do we modify it?
        if( _modifyInitiationRate )
        {
            // Yup, get the individual's conflict
            float assertiveness = ind.getAssertiveness();
            float dirDiff = 0.0f;
            float conflict = calculateConflict( assertiveness, dirDiff );

            // Modify the rate
            float k = calculateK( conflict );
            tau = tau / k;
        }

        return MathUtils.generateRandomExponential(
                1.0f / tau,
                _simState.getRandom() );
    }

    /**
     * Calculates the time at the specified individual will follow an
     * initiator
     *
     * @param ind The individual
     * @param initiator The initiator
     * @param departed The number of individuals who have already departed
     * @param groupSize The size of the group
     * @return The follow time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateFollowTime(edu.snu.leader.hidden.SpatialIndividual, edu.snu.leader.hidden.SpatialIndividual, int, int)
     */
    @Override
    public float calculateFollowTime( SpatialIndividual ind,
            SpatialIndividual initiator,
            int departed,
            int groupSize )
    {
        float tau = calculateFollowRate( groupSize, departed );

        // Do we modify it?
        if( _modifyFollowingRate )
        {
            // Yup, get the individual's conflict
            float assertiveness = ind.getAssertiveness();
            float dirDiff = Math.abs( ind.getPreferredDirection()
                    - initiator.getPreferredDirection() ) % 2.0f;
            if( 1.0f < dirDiff )
            {
                dirDiff = 2.0f - dirDiff;
            }
            float conflict = calculateConflict( assertiveness, dirDiff );

            // Modify the rate
            float k = calculateK( 1.0f - conflict );
            tau = tau / k;

//            _LOG.warn( "Follow: conflict=["
//                    + conflict
//                    + "] dirDiff=["
//                    + dirDiff
//                    + "] assert=["
//                    + assertiveness
//                    + "] k=["
//                    + k
//                    + "] tau=["
//                    + tau
//                    + "] preferredDir=["
//                    + ind.getPreferredDirection()
//                    + "] initiatorDir=["
//                    + initiator.getPreferredDirection()
//                    + "] departed=["
//                    + departed
//                    + "]" );
        }

        return MathUtils.generateRandomExponential( 1.0f / tau,
                _simState.getRandom());
    }

    /**
     * Calculates the time at the specified individual will cancel an initiation
     *
     * @param ind The individual
     * @param departed The number of individuals who have already departed
     * @return The cancellation time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateCancelTime(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    public float calculateCancelTime( SpatialIndividual ind, int departed )
    {
        float cRate = calculateCancelRate( departed );

        // Do we modify it?
        if( _modifyCancellationRate )
        {
            // Yup, get the individual's conflict
            float assertiveness = ind.getAssertiveness();
            float dirDiff = 0.0f;
            float conflict = calculateConflict( assertiveness, dirDiff );

            // Modify the rate
            float k = calculateK( 1.0f - conflict );
            cRate = cRate * k;

//            _LOG.warn( "Cancel: conflict=["
//                    + conflict
//                    + "] k=["
//                    + k
//                    + "] cRate=["
//                    + cRate
//                    + "]" );
        }

        return MathUtils.generateRandomExponential( cRate,
                _simState.getRandom() );
    }


    /**
     * Returns a string description of the following time calculations
     *
     * @return A string description of the following time calculations
     * @see edu.snu.leader.hidden.event.AbstractEventTimeCalculator#describeFollow()
     */
    @Override
    public String describeFollow()
    {
        String description = super.describeFollow();
        if( _modifyFollowingRate )
        {
            // Get the group size
            int groupSize = _simState.getInitiatorsGroupSize(
                    _simState.getAllIndividuals().get( 0 ) );

            StringBuilder builder = new StringBuilder();
            builder.append( "# " );
            builder.append( _description );
            builder.append( _NEWLINE );

            // Display the cancel rate for all the potential follower counts
            for( int i = 1; i < groupSize; i++ )
            {
                // Also go through different conflicts
                for( int j = 0; j <= 10; j++ )
                {
                    float assertiveness = 0.1f * j;

                    for( int k = 0; k <= 10; k++ )
                    {
                        float dirDiff = 0.1f * k;

                        float conflict = calculateConflict( assertiveness, dirDiff );

                        builder.append( "follow-rate." );
                        builder.append( String.format( "%02d", i ) );
                        builder.append( ".assertiveness." );
                        builder.append( String.format( "%03.1f", assertiveness ) );
                        builder.append( ".dir-diff." );
                        builder.append( String.format( "%03.1f", dirDiff ) );
                        builder.append( " = " );

                        float kConstant = calculateK( 1.0f - conflict );
                        float tau = calculateFollowRate( groupSize, i ) / kConstant;

                        builder.append( tau );
                        builder.append( _NEWLINE );

                        _LOG.warn( "Follow: conflict=["
                                + conflict
                                + "] k=["
                                + kConstant
                                + "] tau=["
                                + tau
                                + "]" );
                    }
                    builder.append( _NEWLINE );
                }
                builder.append( _NEWLINE );
            }
            builder.append( _NEWLINE );

            description = builder.toString();
        }

        return description;
    }


    /**
     * Calculates the conflict for a given assertiveness and direction difference
     *
     * @param assertiveness
     * @param dirDiff
     * @return The conflict value
     */
    private float calculateConflict( float assertiveness, float dirDiff )
    {
        return (float) (Math.pow( assertiveness, 0.5f )
                * Math.pow( dirDiff, 0.5f ));
    }

    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    private float calculateK( float value )
    {
        return 2.0f * value;
    }

    {
        // A description of the algorithm
        // NOTE:  This is put here to keep it near the calculation itself
        _description = "k = 2.0f * value";
    }

}
