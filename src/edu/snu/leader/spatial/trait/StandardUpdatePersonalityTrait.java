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
package edu.snu.leader.spatial.trait;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionEvent;
import edu.snu.leader.spatial.DecisionType;
import edu.snu.leader.spatial.PersonalityTrait;
import edu.snu.leader.spatial.PersonalityUpdateEvent;
import edu.snu.leader.spatial.PersonalityUpdateType;
import edu.snu.leader.spatial.SimulationState;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Properties;


/**
 * StandardUpdatePersonalityTrait
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class StandardUpdatePersonalityTrait implements PersonalityTrait
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            StandardUpdatePersonalityTrait.class.getName() );

    /** Key for the discount */
    private static final String _DISCOUNT_KEY = "personality-discount";

    /** Key for the true winner discount */
    private static final String _TRUE_WINNER_DISCOUNT_KEY = "true-winner-discount";

    /** Key for the true loser discount */
    private static final String _TRUE_LOSER_DISCOUNT_KEY = "true-loser-discount";

    /** Key for the bystander winner discount */
    private static final String _BYSTANDER_WINNER_DISCOUNT_KEY = "bystander-winner-discount";

    /** Key for the bystander loser discount */
    private static final String _BYSTANDER_LOSER_DISCOUNT_KEY = "bystander-loser-discount";

    /** Key for the winner reward */
    private static final String _WINNER_REWARD_KEY = "winner-reward";

    /** Key for the loser penalty */
    private static final String _LOSER_PENALTY_KEY = "loser-penalty";

    /** Key for the minimum personality value */
    private static final String _MIN_PERSONALITY_KEY = "min-personality";

    /** Key for the maximum personality value */
    private static final String _MAX_PERSONALITY_KEY = "max-personality";

    /** Key for the flag indicating that true winner effects are active */
    private static final String _TRUE_WINNER_EFFECTS_ACTIVE_KEY = "true-winner-effects-active";

    /** Key for the flag indicating that true loser effects are active */
    private static final String _TRUE_LOSER_EFFECTS_ACTIVE_KEY = "true-loser-effects-active";

    /** Key for the flag indicating that bystander winner effects are active */
    private static final String _BYSTANDER_WINNER_EFFECTS_ACTIVE_KEY = "bystander-winner-effects-active";

    /** Key for the flag indicating that bystander loser effects are active */
    private static final String _BYSTANDER_LOSER_EFFECTS_ACTIVE_KEY = "bystander-loser-effects-active";



    /** The simulation state */
    private SimulationState _simState = null;

    /** The agent whose personality this is */
    private Agent _agent = null;

    /** The current personality */
    private float _personality = 0.0f;

    /** The update rule's discount */
    private float _discount = 0.0f;

    /** The update rule's true winner discount */
    private float _trueWinnerDiscount = 0.0f;

    /** The update rule's true loser discount */
    private float _trueLoserDiscount = 0.0f;

    /** The update rule's bystander winner discount */
    private float _bystanderWinnerDiscount = 0.0f;

    /** The update rule's bystander loser discount */
    private float _bystanderLoserDiscount = 0.0f;

    /** The update rule's true winner reward */
    private float _winnerReward = 0.0f;

    /** The update rule's true loser reward */
    private float _loserPenalty = 0.0f;

    /** Flag indicating whether or not true winner effects are active */
    private boolean _trueWinnerEffectActive = false;

    /** Flag indicating whether or not true loser effects are active */
    private boolean _trueLoserEffectActive = false;

    /** Flag indicating whether or not bystander winner effects are active */
    private boolean _bystanderWinnerEffectActive = false;

    /** Flag indicating whether or not bystander loser effects are active */
    private boolean _bystanderLoserEffectActive = false;

    /** The minimum allowable personality value */
    private float _minPersonality = 0.0f;

    /** The maximum allowable personality value */
    private float _maxPersonality = 1.0f;

    /** The group ID corresponding to the last update in which a cancel
     *  threshold was reached.  This is recorded to ensure that we update only
     *  once for a given group. */
    private Object _lastCancelThresholdReachedGroupID = null;



    /**
     * Builds this StandardUpdatePersonalityTrait object
     *
     * @param initialPersonality
     */
    public StandardUpdatePersonalityTrait( float initialPersonality )
    {
        // Store the personality for now, validate it once initialized
        _personality = initialPersonality;
    }

    /**
     * Initializes the trait
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.spatial.PersonalityTrait#initialize(edu.snu.leader.spatial.SimulationState, edu.snu.leader.spatial.Agent)
     */
    @Override
    public void initialize( SimulationState simState,
            Agent agent )
    {
        _LOG.trace( "Entering initialize( simState, agent )" );

        // Save the simulation state
        Validate.notNull( simState, "Simulation state may not be null" );
        _simState = simState;

        // Save the agent
        Validate.notNull( agent, "Agent may not be null" );
        _agent = agent;

        // Get the properties
        Properties props = simState.getProperties();

        // Get the discount value
        String discountStr = props.getProperty( _DISCOUNT_KEY );
        Validate.notEmpty( discountStr,
                "Personality discount (key=["
                + _DISCOUNT_KEY
                + "]) may not be empty" );
        _discount = Float.parseFloat( discountStr );
        _LOG.info( "Using _discount=[" + _discount + "]" );

        // Get the true winner discount value
        String trueWinnerDiscountStr = props.getProperty( _TRUE_WINNER_DISCOUNT_KEY );
        if( null != trueWinnerDiscountStr )
        {
            _trueWinnerDiscount = Float.parseFloat( trueWinnerDiscountStr );
        }
        else
        {
            _trueWinnerDiscount = _discount;
        }
        _LOG.info( "Using _trueWinnerDiscount=[" + _trueWinnerDiscount + "]" );

        // Get the true loser discount value
        String trueLoserDiscountStr = props.getProperty( _TRUE_LOSER_DISCOUNT_KEY );
        if( null != trueLoserDiscountStr )
        {
            _trueLoserDiscount = Float.parseFloat( trueLoserDiscountStr );
        }
        else
        {
            _trueLoserDiscount = _discount;
        }
        _LOG.info( "Using _trueLoserDiscount=[" + _trueLoserDiscount + "]" );

        // Get the bystander winner discount value
        String bystanderWinnerDiscountStr = props.getProperty( _BYSTANDER_WINNER_DISCOUNT_KEY );
        if( null != bystanderWinnerDiscountStr )
        {
            _bystanderWinnerDiscount = Float.parseFloat( bystanderWinnerDiscountStr );
        }
        else
        {
            _bystanderWinnerDiscount = _discount;
        }
        _LOG.info( "Using _bystanderWinnerDiscount=[" + _bystanderWinnerDiscount + "]" );

        // Get the bystander loser discount value
        String bystanderLoserDiscountStr = props.getProperty( _BYSTANDER_LOSER_DISCOUNT_KEY );
        if( null != bystanderLoserDiscountStr )
        {
            _bystanderLoserDiscount = Float.parseFloat( bystanderLoserDiscountStr );
        }
        else
        {
            _bystanderLoserDiscount = _discount;
        }
        _LOG.info( "Using _bystanderLoserDiscount=[" + _bystanderLoserDiscount + "]" );

        // Get the winner reward
        String winnerRewardStr = props.getProperty( _WINNER_REWARD_KEY );
        Validate.notEmpty( winnerRewardStr,
                "Winner reward (key=["
                + _WINNER_REWARD_KEY
                + "]) may not be empty" );
        _winnerReward = Float.parseFloat( winnerRewardStr );
        _LOG.info( "Using _winnerReward=[" + _winnerReward + "]" );

        // Get the loser penalty
        String loserPenaltyStr = props.getProperty( _LOSER_PENALTY_KEY );
        Validate.notEmpty( loserPenaltyStr,
                "Loser penalty (key=["
                + _LOSER_PENALTY_KEY
                + "]) may not be empty" );
        _loserPenalty = Float.parseFloat( loserPenaltyStr );
        _LOG.info( "Using _loserPenalty=[" + _loserPenalty + "]" );

        // Get the true winner effect flag
        String trueWinnerEffectStr = props.getProperty( _TRUE_WINNER_EFFECTS_ACTIVE_KEY );
        Validate.notEmpty( trueWinnerEffectStr,
                "True winner effects active flag (key=["
                        + _TRUE_WINNER_EFFECTS_ACTIVE_KEY
                        + "]) may not be empty" );
        _trueWinnerEffectActive = Boolean.parseBoolean( trueWinnerEffectStr );
        _LOG.info( "Using _trueWinnerEffectActive=["
                + _trueWinnerEffectActive
                + "]" );

        // Get the true loser effect flag
        String trueLoserEffectStr = props.getProperty( _TRUE_LOSER_EFFECTS_ACTIVE_KEY );
        Validate.notEmpty( trueLoserEffectStr,
                "True loser effects active flag (key=["
                        + _TRUE_LOSER_EFFECTS_ACTIVE_KEY
                        + "]) may not be empty" );
        _trueLoserEffectActive = Boolean.parseBoolean( trueLoserEffectStr );
        _LOG.info( "Using _trueLoserEffectActive=["
                + _trueLoserEffectActive
                + "]" );

        // Get the bystander winner effect flag
        String bystanderWinnerEffectStr = props.getProperty( _BYSTANDER_WINNER_EFFECTS_ACTIVE_KEY );
        Validate.notEmpty( bystanderWinnerEffectStr,
                "Bystander winner effects active flag (key=["
                        + _BYSTANDER_WINNER_EFFECTS_ACTIVE_KEY
                        + "]) may not be empty" );
        _bystanderWinnerEffectActive = Boolean.parseBoolean( bystanderWinnerEffectStr );
        _LOG.info( "Using _bystanderWinnerEffectActive=["
                + _bystanderWinnerEffectActive
                + "]" );

        // Get the bystander loser effect flag
        String bystanderLoserEffectStr = props.getProperty( _BYSTANDER_LOSER_EFFECTS_ACTIVE_KEY );
        Validate.notEmpty( bystanderLoserEffectStr,
                "Bystander loser effects active flag (key=["
                        + _BYSTANDER_LOSER_EFFECTS_ACTIVE_KEY
                        + "]) may not be empty" );
        _bystanderLoserEffectActive = Boolean.parseBoolean( bystanderLoserEffectStr );
        _LOG.info( "Using _bystanderLoserEffectActive=["
                + _bystanderLoserEffectActive
                + "]" );

        // Get the min personality
        String minPersonalityStr = props.getProperty( _MIN_PERSONALITY_KEY );
        Validate.notEmpty( minPersonalityStr,
                "Minimum personality value (key="
                + _MIN_PERSONALITY_KEY
                + ") may not be empty" );
        _minPersonality = Float.parseFloat( minPersonalityStr );
        _LOG.info( "Using _minPersonality=[" + _minPersonality + "]" );

        // Get the max personality
        String maxPersonalityStr = props.getProperty( _MAX_PERSONALITY_KEY );
        Validate.notEmpty( maxPersonalityStr,
                "Maximum personality value (key="
                + _MAX_PERSONALITY_KEY
                + ") may not be empty" );
        _maxPersonality = Float.parseFloat( maxPersonalityStr );
        _LOG.info( "Using _maxPersonality=[" + _maxPersonality + "]" );

        // Validate the initial personality
        if( _minPersonality > _personality )
        {
            _LOG.warn( "Initial personality is less than the minimum min=["
                    + _minPersonality
                    + "] > initial=["
                    + _personality
                    + "]" );
            _personality = _minPersonality;
        }
        else if( _maxPersonality < _personality )
        {
            _LOG.warn( "Initial personality is greater than the maximum min=["
                    + _maxPersonality
                    + "] < initial=["
                    + _personality
                    + "]" );
            _personality = _maxPersonality;
        }

        _LOG.trace( "Leaving initialize( simState, agent )" );
    }

    /**
     * Returns the personality
     *
     * @return The personality
     * @see edu.snu.leader.spatial.PersonalityTrait#getPersonality()
     */
    @Override
    public float getPersonality()
    {
        return _personality;
    }

    /**
     * Updates this personality trait
     *
     * @see edu.snu.leader.spatial.PersonalityTrait#update()
     */
    @Override
    public void update()
    {
//        _LOG.trace( "Entering update()" );

        PersonalityUpdateType updateType = null;

        // Get the current timestep
        long currentSimStep = _simState.getCurrentSimulationStep();

        // Get the starting personality
        float startingPersonality = _personality;

        // Get the current decision of the agent and the leader
        DecisionEvent agentDecisionEvent = _agent.getCurrentDecisionEvent();
        DecisionEvent leaderDecisionEvent = null;
        Agent leader = _agent.getLeader();
        if( null != leader )
        {
            leaderDecisionEvent = leader.getCurrentDecisionEvent();
            _LOG.debug( "Leader ["
                    + leader.getID()
                    + "] decided to ["
                    + leaderDecisionEvent.getDecision().getType()
                    + "]" );
        }
        else
        {
            _LOG.debug( "Agent ["
                    + _agent.getID()
                    + "] has no leader and group ["
                    + _agent.getGroup().getID()
                    + "]" );
        }

        // Get the number of neighbors that are in the group
        List<Agent> neighbors = _agent.getCurrentNearestNeighbors();
        int neighborCount = neighbors.size();
        int neighborGroupCount = _agent.getGroup().getNeighborMemberCount(
                neighbors );

        // If we are initiating, did we reach our cancel threshold?
        if( _trueWinnerEffectActive
                && (DecisionType.INITIATE.equals( agentDecisionEvent.getDecision().getType() ))
                && (neighborGroupCount == Math.floor( neighborCount * _agent.getCancelThreshold() ) ) )
        {
            _LOG.debug( "Reached cancel threshold" );

            // Yup, but only update once
            if( (null ==_lastCancelThresholdReachedGroupID)
                    || !_lastCancelThresholdReachedGroupID.equals( _agent.getGroup().getID() ) )
            {
                updatePersonality( _winnerReward, _trueWinnerDiscount );
                _LOG.debug( "True winner effect applied" );
                _lastCancelThresholdReachedGroupID = _agent.getGroup().getID();
                updateType = PersonalityUpdateType.TRUE_WINNER;
            }
        }

        // Did we just cancel our initiation?
        else if( _trueLoserEffectActive
                && (currentSimStep == agentDecisionEvent.getTime())
                && (DecisionType.CANCEL.equals( agentDecisionEvent.getDecision().getType() )) )
        {
            // Yup
            updatePersonality( _loserPenalty, _trueLoserDiscount );
            _LOG.debug( "True loser effect applied" );
            updateType = PersonalityUpdateType.TRUE_LOSER;
        }

        /* Are we following someone that just reached a cancel threshold?
         * NOTE: This makes the assumption that all the members of the group
         * that are our neighbors are following the same leader. It also
         * works off the premise that an agent uses its own cancel threshold
         * and not the leaders (which could be different). */
        else if( _bystanderWinnerEffectActive
                && (null != leaderDecisionEvent)
                && (neighborGroupCount == Math.floor( neighborCount *_agent.getCancelThreshold() ) ) )
        {
            // Yup, but only do it the first time
            if( (null ==_lastCancelThresholdReachedGroupID)
                    || !_lastCancelThresholdReachedGroupID.equals( _agent.getGroup().getID() ) )
            {
                updatePersonality( _loserPenalty, _bystanderLoserDiscount );
                _LOG.debug( "Bystander loser effect applied (observed a winner)" );
                _lastCancelThresholdReachedGroupID = _agent.getGroup().getID();
                updateType = PersonalityUpdateType.BYSTANDER_WINNER;
            }
        }

        // Are we following someone that just canceled?
        else if( _bystanderLoserEffectActive
                && (null != leaderDecisionEvent)
                && (currentSimStep == leaderDecisionEvent.getTime())
                && (DecisionType.CANCEL.equals( leaderDecisionEvent.getDecision().getType() )) )
        {
            // Yup
            updatePersonality( _winnerReward, _bystanderWinnerDiscount );
            _LOG.debug( "Bystander winner effect applied (observed a loser)" );
            updateType = PersonalityUpdateType.BYSTANDER_LOSER;
        }

        // Was there an update?
        if( null != updateType )
        {
            // Yup
            _simState.getObserverManager().signalPersonalityUpdateEvent(
                    new PersonalityUpdateEvent( updateType,
                            startingPersonality,
                            _personality,
                            _agent,
                            _simState.getCurrentSimulationStep(),
                            _simState.getCurrentSimulationRun() ) );
        }

//        _LOG.trace( "Leaving update()" );
    }

    /**
     * Resets any state information in the personality trait.  This does NOT
     * affect any updates to the personality value itself.
     *
     * @see edu.snu.leader.spatial.PersonalityTrait#reset()
     */
    @Override
    public void reset()
    {
        _lastCancelThresholdReachedGroupID = null;
    }

    /**
     * Updates the personality using the specified reward and discount values
     *
     * @param reward
     * @param discount
     */
    private void updatePersonality( float reward, float discount )
    {
        float newPersonality = ( (1.0f - discount) * _personality )
                + (discount * reward);

        // Ensure it is within the bounds
        if( _minPersonality > newPersonality )
        {
            newPersonality = _minPersonality;
        }
        else if( _maxPersonality < newPersonality )
        {
            newPersonality = _maxPersonality;
        }

        // Log it
        if( _LOG.isDebugEnabled() )
        {
            _LOG.debug( "agentID=["
                    + _agent.getID()
                    + "] oldPersonality=["
                    + _personality
                    + "] newPersonality=["
                    + newPersonality
                    + "] reward=["
                    + reward
                    + "] discount=["
                    + discount
                    + "]" );
        }

        // Store it
        _personality = newPersonality;
    }
}
