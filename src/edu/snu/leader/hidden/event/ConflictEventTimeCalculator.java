/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MathUtils;
import org.apache.log4j.Logger;


/**
 * ConflictEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ConflictEventTimeCalculator extends AbstractEventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ConflictEventTimeCalculator.class.getName() );


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
            float conflict = ind.getConflict();

            // Modify the rate
            float k = calculateK( conflict );
            tau = tau / k;

//            _LOG.warn( "Initiate: personality=["
//                    + personality
//                    + "] k=["
//                    + k
//                    + "] tau=["
//                    + tau
//                    + "]" );
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
            float conflict = ind.getConflict();

            // Modify the rate
            float k = calculateK( 1.0f - conflict );
            tau = tau / k;

//            _LOG.warn( "Follow: conflict=["
//                    + conflict
//                    + "] k=["
//                    + k
//                    + "] tau=["
//                    + tau
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
            float conflict = ind.getConflict();

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
     * Returns a string description of the initiation time calculations
     *
     * @return A string description of the initiation time calculations
     * @see edu.snu.leader.hidden.event.AbstractEventTimeCalculator#describeInitiation()
     */
    @Override
    public String describeInitiation()
    {
        String description = super.describeInitiation();
        if( _modifyInitiationRate )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( "# " );
            builder.append( _description );
            builder.append( _NEWLINE );

            // Iterate through different personalities
            for( int j = 0; j <= 10; j++ )
            {
                float conflict = 0.1f * j;

                builder.append( "initiation-rate.conflict." );
                builder.append( String.format( "%03.1f", conflict ) );
                builder.append( " = " );

                float k = calculateK( conflict );
                float tau = calculateInitiationRate( _simState.getIndividualCount() )
                        / k;

                builder.append( tau );
                builder.append( _NEWLINE );

                _LOG.warn( "Initiate: conflict=["
                        + conflict
                        + "] k=["
                        + k
                        + "] tau=["
                        + tau
                        + "]" );
            }

            description = builder.toString();
        }

        return description;
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
                // Also go through different personalities
                for( int j = 0; j <= 10; j++ )
                {
                    float conflict = 0.1f * j;

                    builder.append( "follow-rate." );
                    builder.append( String.format( "%02d", i ) );
                    builder.append( ".conflict." );
                    builder.append( String.format( "%03.1f", conflict ) );
                    builder.append( " = " );

                    float k = calculateK( 1.0f - conflict );
                    float tau = calculateFollowRate( groupSize, i ) / k;

                    builder.append( tau );
                    builder.append( _NEWLINE );

                    _LOG.warn( "Follow: conflict=["
                            + conflict
                            + "] k=["
                            + k
                            + "] tau=["
                            + tau
                            + "]" );

                }
                builder.append( _NEWLINE );
            }
            builder.append( _NEWLINE );

            description = builder.toString();
        }

        return description;
    }

    /**
     * Returns a string description of the cancellation time calculations
     *
     * @return A string description of the cancellation time calculations
     * @see edu.snu.leader.hidden.event.AbstractEventTimeCalculator#describeCancellation()
     */
    @Override
    public String describeCancellation()
    {
        String description = super.describeCancellation();
        if( _modifyCancellationRate )
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
                // Also go through different personalities
                for( int j = 0; j <= 10; j++ )
                {
                    float conflict = 0.1f * j;

                    builder.append( "cancel-rate." );
                    builder.append( String.format( "%02d", i ) );
                    builder.append( ".conflict." );
                    builder.append( String.format( "%03.1f", conflict ) );
                    builder.append( " = " );

                    float k = calculateK( 1.0f - conflict );
                    float cRate = calculateCancelRate( i ) * k;

                    builder.append( (1.0f / cRate ) );
                    builder.append( _NEWLINE );

                    _LOG.warn( "Cancel: conflict=["
                            + conflict
                            + "] k=["
                            + k
                            + "] cRate=["
                            + (1.0f/cRate)
                            + "]" );

                }
                builder.append( _NEWLINE );
            }
            builder.append( _NEWLINE );

            description = builder.toString();
        }

        return description;
    }

    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    private float calculateK( float value )
    {
//        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) );
        return 2.0f * value;
    }

    {
        // A description of the algorithm
        // NOTE:  This is put here to keep it near the calculation itself
        _description = "k = 2.0f * value";
    }

}
