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
package edu.snu.leader.hidden.personality;

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;


/**
 * DirectionUpdateRuleWithDecayPersonalityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DirectionUpdateRuleWithDecayPersonalityCalculator
        extends StandardUpdateRuleWithDecayPersonalityCalculator
        implements PersonalityCalculator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DirectionUpdateRuleWithDecayPersonalityCalculator.class.getName() );

    /** Class to store loaded direction changes */
    protected static class DirectionChange {
        protected final int simIndex;
        protected final float direction;
        protected final float sigma;
        protected DirectionChange( int simIndex,
                float direction,
                float sigma )
        {
            this.simIndex = simIndex;
            this.direction = direction;
            this.sigma = sigma;
        }
    }

    /** Key for the directions file */
    private static final String _DIRECTIONS_FILE_KEY = "directions-file";

    /** Key for the flag indicating that the winner discount should be modified */
    private static final String _MODIFY_WINNER_DISCOUNT_KEY = "modify-winner-discount";

    /** Key for the flag indicating that the winner reward should be modified */
    private static final String _MODIFY_WINNER_REWARD_KEY = "modify-winner-reward";


    /** The simulation state */
    private SimulationState _simState = null;

    /** The scheduled changes in direction */
    private Queue<DirectionChange> _directionChanges =
            new LinkedList<DirectionChange>();

    /** The current direction */
    private float _direction = 0.0f;

    /** The current sigma */
    private float _sigma = 0.0f;

    /** The maximum gaussian value */
    private float _maxGaussianValue = 0.0f;

    /** The current Gaussian used to calculate winner reward */
    private Gaussian _gaussian = null;

    /** Flag denoting whether the direction difference should modify
     *  the update winner discount value */
    private boolean _modifyWinnerDiscount = false;

    /** Flag denoting whether the direction difference should modify
     *  the update winner reward value */
    private boolean _modifyWinnerReward = false;


    /**
     * Initializes the updater
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        _simState = simState;
        Properties props = simState.getProps();

        // Import the preferred directions
        String directionsFileStr = props.getProperty( _DIRECTIONS_FILE_KEY );
        Validate.notEmpty( directionsFileStr,
                "Directions file (key=["
                + _DIRECTIONS_FILE_KEY
                + "]) may not be empty" );
        _LOG.info( "Using directionsFileStr=["
                + directionsFileStr
                + "]" );
        loadDirectionChanges( directionsFileStr );

        // Get the first direction
        DirectionChange first = _directionChanges.peek();
        if( (first != null) && (first.simIndex <= 0) )
        {
            // Save the data
            _direction = first.direction;
            _sigma = first.sigma;
            _gaussian = new Gaussian( 0.0f, _sigma );
            _maxGaussianValue = 1.0f / (_sigma * (float) Math.sqrt( 2.0f * Math.PI ) );
            _LOG.debug( "Initial direction ["
                    + _direction
                    + "]" );

            // Pull it off the queue
            _directionChanges.poll();
        }

        // Get the modify winner discount flag
        String modifyWinnerDiscountStr = props.getProperty(
                _MODIFY_WINNER_DISCOUNT_KEY );
        Validate.notEmpty( modifyWinnerDiscountStr,
                "Modify winner discount flag (key=["
                        + _MODIFY_WINNER_DISCOUNT_KEY
                        + "]) may not be empty" );
        _modifyWinnerDiscount = Boolean.parseBoolean( modifyWinnerDiscountStr );
        _LOG.info( "Using _modifyWinnerDiscount=["
                + _modifyWinnerDiscount
                + "]" );

        // Get the modify winner reward flag
        String modifyWinnerRewardStr = props.getProperty(
                _MODIFY_WINNER_REWARD_KEY );
        Validate.notEmpty( modifyWinnerRewardStr,
                "Modify winner discount flag (key=["
                        + _MODIFY_WINNER_REWARD_KEY
                        + "]) may not be empty" );
        _modifyWinnerReward = Boolean.parseBoolean( modifyWinnerRewardStr );
        _LOG.info( "Using _modifyWinnerReward=["
                + _modifyWinnerReward
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }


    /**
     * Calculate the new personality value
     *
     * @param currentPersonality The individual's current personality
     * @param updateType The type of update being applied
     * @param followers The number of followers in the initiation
     * @return The updated personality value
     * @see edu.snu.leader.hidden.personality.StandardUpdateRulePersonalityCalculator#calculatePersonality(edu.snu.leader.hidden.SpatialIndividual, edu.snu.leader.hidden.personality.PersonalityUpdateType, int)
     */
    @Override
    public float calculatePersonality( SpatialIndividual individual,
            PersonalityUpdateType updateType,
            int followers )
    {
        // Do we change the direction?
        DirectionChange next = _directionChanges.peek();
        if( (next != null) && (next.simIndex <= _simState.getSimIndex()) )
        {
            // Save the data
            _direction = next.direction;
            _sigma = next.sigma;
            _gaussian = new Gaussian( 0.0f, _sigma );
            _maxGaussianValue = 1.0f / (_sigma * (float) Math.sqrt( 2.0f * Math.PI ) );
            _LOG.warn( "New direction=["
                    + _direction
                    + "] for next.simIndex=["
                    + next.simIndex
                    + "] simStateSimIdx=["
                    + _simState.getSimIndex()
                    + "] sigma=["
                    + _sigma
                    + "] maxGaussianValue=["
                    + _maxGaussianValue
                    + "]" );

            // Pull it off the queue
            _directionChanges.poll();
        }

        // Go ahead and use the superclass implementation
        return super.calculatePersonality( individual, updateType, followers );
    }



    /**
     * Calculates the true winner result for the given individual and number
     * of followers
     *
     * @param individual The winning, initiating individual
     * @param followers The number of followers
     * @return The true winner result
     * @see edu.snu.leader.hidden.personality.StandardUpdateRulePersonalityCalculator#calculateTrueWinnerResult(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    protected float calculateTrueWinnerResult( SpatialIndividual individual,
            int followers )
    {
        // Get the default result
        float result = super.calculateTrueWinnerResult( individual, followers );

        // Do we modify the winner reward?
        if( _modifyWinnerReward )
        {
            // Calculate the direction difference
            float dirDiff = calculateDirectionDifference( individual );

            // Use the Gaussian to calculate the result multiplier
            float multiplier = (float) _gaussian.value( dirDiff ) / _maxGaussianValue;
            result *= multiplier;

            _LOG.debug( "multiplier=["
                    + multiplier
                    + "] result=["
                    + result
                    + "] dirDiff=["
                    + dirDiff
                    + "] gaussianValue=["
                    + _gaussian.value( dirDiff )
                    + "]" );
        }

        return result;
    }

    /**
     * Calculates the true winner discount for the given individual and number
     * of followers
     *
     * @param individual The winning, initiating individual
     * @param followers The number of followers
     * @return The true winner discount
     * @see edu.snu.leader.hidden.personality.StandardUpdateRulePersonalityCalculator#calculateTrueWinnerDiscount(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    protected float calculateTrueWinnerDiscount( SpatialIndividual individual,
            int followers )
    {
        // Get the default discount
        float discount =  super.calculateTrueWinnerDiscount( individual, followers );

        // Do we modify it?
        if( _modifyWinnerDiscount )
        {
            // Calculate the direction difference
            float dirDiff = calculateDirectionDifference( individual );

            // Use the Gaussian to calculate the result multiplier
            float multiplier = (float) _gaussian.value( dirDiff ) / _maxGaussianValue;
            discount *= multiplier;

            _LOG.debug( "multiplier=["
                    + multiplier
                    + "] discount=["
                    + discount
                    + "] dirDiff=["
                    + dirDiff
                    + "] gaussianValue=["
                    + _gaussian.value( dirDiff )
                    + "]");
        }

        return discount;
    }


    /**
     * Load the direction changes from the specified file
     *
     * @param filename
     */
    protected void loadDirectionChanges( String filename )
    {
        _LOG.trace( "Entering loadDirectionChanges( filename )" );

        // Try to process the file
        try
        {
            // Create a reader
            BufferedReader reader = new BufferedReader(
                    new FileReader( filename ) );

            // Process each line
            String line;
            while( null != (line = reader.readLine()) )
            {
                // Is it a comment or empty?
                if( (0 == line.length()) || line.startsWith( "#" ) )
                {
                    // Yup
                    continue;
                }

                // Split it
                String[] parts = line.split("\\s+");

                // Parse it
                int simIndex = Integer.parseInt( parts[0] );
                float direction = Float.parseFloat( parts[1] );
                float sigma = Float.parseFloat( parts[2] );

                // Multiply the sim index by the group size
                simIndex *= _simState.getIndividualCount();

                // Create the direction change
                _directionChanges.offer( new DirectionChange(
                        simIndex, direction, sigma ) );
            }

            // Close the reader
            reader.close();
        }
        catch( Exception e )
        {
            _LOG.error( "Unable to read direction changes file ["
                    + filename
                    + "]", e );
            throw new RuntimeException( "Unable to direction changes file ["
                    + filename
                    + "]" );
        }

        _LOG.trace( "Leaving loadDirectionChanges( filename )" );
    }

    /**
     * Calculates the direction difference between an individual and the
     * environment
     *
     * @param individual The individual
     * @return The normalized difference in direction
     */
    protected float calculateDirectionDifference( SpatialIndividual individual )
    {
        // Get the absolute value of the difference
        float dirDiff = Math.abs( individual.getPreferredDirection()
                - _direction ) % 2.0f;

        // If it is over 180 degrees, recalculate to get just the raw diffference
        if( 1.0f < dirDiff )
        {
            dirDiff = 2.0f - dirDiff;
        }

        _LOG.debug( "dirDiff=["
                + dirDiff
                + "] preferredDir=["
                + individual.getPreferredDirection()
                + "] dir=["
                + _direction
                + "]" );

        return dirDiff;
    }
}
