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
package edu.snu.leader.hidden.event;

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MathUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Properties;


/**
 * PersonalityEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityEventTimeCalculator extends AbstractEventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PersonalityEventTimeCalculator.class.getName() );

    /** Key for modifying the initiation rate flag */
    private static final String _MODIFY_INITIATION_RATE_KEY = "modify-initiation-rate";

    /** Key for modifying the following rate flag */
    private static final String _MODIFY_FOLLOWING_RATE_KEY = "modify-following-rate";

    /** Key for modifying the cancellation rate flag */
    private static final String _MODIFY_CANCELLATION_RATE_KEY = "modify-cancellation-rate";


//    /** Exponential constant alpha for k calculation */
//    private static final float _ALPHA = 3.321928f;
//
//    /** Multiplicative constant beta for k calculation */
//    private static final float _BETA = (float) Math.pow( 10.0f, (1.0f / _ALPHA) );


    /** Description of the algorithm */
    private String _description = "";

    /** Flag for modifying the initiation rate */
    private boolean _modifyInitiationRate = false;

    /** Flag for modifying the following rate */
    private boolean _modifyFollowingRate = false;

    /** Flag for modifying the cancellation rate */
    private boolean _modifyCancellationRate = false;


    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.event.AbstractEventTimeCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the initiation rate modification flag
        String modifyInitiationRateStr = props.getProperty( _MODIFY_INITIATION_RATE_KEY );
        Validate.notEmpty( modifyInitiationRateStr,
                "Modify initation rate (key="
                + _MODIFY_INITIATION_RATE_KEY
                + ") may not be empty" );
        _modifyInitiationRate = Boolean.parseBoolean( modifyInitiationRateStr );
        _LOG.info( "Using _modifyInitiationRate=[" + _modifyInitiationRate + "]" );

        // Get the following rate modification flag
        String modifyFollowingRateStr = props.getProperty( _MODIFY_FOLLOWING_RATE_KEY );
        Validate.notEmpty( modifyFollowingRateStr,
                "Modify following rate (key="
                + _MODIFY_FOLLOWING_RATE_KEY
                + ") may not be empty" );
        _modifyFollowingRate = Boolean.parseBoolean( modifyFollowingRateStr );
        _LOG.info( "Using _modifyFollowingRate=[" + _modifyFollowingRate + "]" );

        // Get the cancellation rate modification flag
        String modifyCancellationRateStr = props.getProperty( _MODIFY_CANCELLATION_RATE_KEY );
        Validate.notEmpty( modifyCancellationRateStr,
                "Modify cancellation rate (key="
                + _MODIFY_CANCELLATION_RATE_KEY
                + ") may not be empty" );
        _modifyCancellationRate = Boolean.parseBoolean( modifyCancellationRateStr );
        _LOG.info( "Using _modifyCancellationRate=[" + _modifyCancellationRate + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

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
            // Yup, get the individual's personality
            float personality = ind.getPersonality();

            // Modify the rate
            float k = calculateK( personality );
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
            // Yup, get the individual's personality
            float personality = ind.getPersonality();

            // Modify the rate
            float k = calculateK( 1.0f - personality );
            tau = tau / k;

//            _LOG.warn( "Follow: personality=["
//                    + personality
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
            // Yup, get the individual's personality
            float personality = ind.getPersonality();

            // Modify the rate
            float k = calculateK( 1.0f - personality );
            cRate = cRate * k;

//            _LOG.warn( "Cancel: personality=["
//                    + personality
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
                float personality = 0.1f * j;

                builder.append( "initiation-rate.personality." );
                builder.append( String.format( "%03.1f", personality ) );
                builder.append( " = " );

                float k = calculateK( personality );
                float tau = calculateInitiationRate( _simState.getIndividualCount() )
                        / k;

                builder.append( tau );
                builder.append( _NEWLINE );

//                _LOG.warn( "Initiate: personality=["
//                        + personality
//                        + "] k=["
//                        + k
//                        + "] tau=["
//                        + tau
//                        + "]" );
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
                    float personality = 0.1f * j;

                    builder.append( "follow-rate." );
                    builder.append( String.format( "%02d", i ) );
                    builder.append( ".personality." );
                    builder.append( String.format( "%03.1f", personality ) );
                    builder.append( " = " );

                    float k = calculateK( 1.0f - personality );
                    float tau = calculateFollowRate( groupSize, i ) / k;

                    builder.append( tau );
                    builder.append( _NEWLINE );

//                    _LOG.warn( "Follow: personality=["
//                            + personality
//                            + "] k=["
//                            + k
//                            + "] tau=["
//                            + tau
//                            + "]" );

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
                    float personality = 0.1f * j;

                    builder.append( "cancel-rate." );
                    builder.append( String.format( "%02d", i ) );
                    builder.append( ".personality." );
                    builder.append( String.format( "%03.1f", personality ) );
                    builder.append( " = " );

                    float k = calculateK( 1.0f - personality );
                    float cRate = calculateCancelRate( i ) * k;

                    builder.append( (1.0f / cRate ) );
                    builder.append( _NEWLINE );

//                    _LOG.warn( "Cancel: personality=["
//                            + personality
//                            + "] k=["
//                            + k
//                            + "] cRate=["
//                            + (1.0f/cRate)
//                            + "]" );

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
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) );
//        return 2.0f * value;
    }

    {
        // A description of the algorithm
        // NOTE:  This is put here to keep it near the calculation itself
//        _description = "k = 2.0f * value";
        _description = "k = 2.0f  * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) )";
    }

}
