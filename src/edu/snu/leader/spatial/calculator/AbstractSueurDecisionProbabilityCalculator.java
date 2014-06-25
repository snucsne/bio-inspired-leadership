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
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.DecisionProbabilityCalculator;
import edu.snu.leader.spatial.Group;
import edu.snu.leader.spatial.SimulationState;

/**
 * SueurDecisionProbabilityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractSueurDecisionProbabilityCalculator
        implements DecisionProbabilityCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            AbstractSueurDecisionProbabilityCalculator.class.getName() );

    
    /** Key for modifying the initiation rate flag */
    private static final String _MODIFY_INITIATION_RATE_KEY = "modify-initiation-rate";

    /** Key for modifying the following innate rate flag */
    private static final String _MODIFY_FOLLOWING_INNATE_RATE_KEY = "modify-following-innate-rate";

    /** Key for modifying the following mimetic rate flag */
    private static final String _MODIFY_FOLLOWING_MIMETIC_RATE_KEY = "modify-following-mimetic-rate";

    /** Key for modifying the cancellation rate flag */
    private static final String _MODIFY_CANCELLATION_RATE_KEY = "modify-cancellation-rate";

    
    
    /** The simulation state */
    protected SimulationState _simState = null;

    /** Flag for modifying the initiation rate */
    private boolean _modifyInitiationRate = false;

    /** Flag for modifying the following innate rate */
    private boolean _modifyFollowingInnateRate = false;

    /** Flag for modifying the following mimetic rate */
    private boolean _modifyFollowingMimeticRate = false;

    /** Flag for modifying the cancellation rate */
    private boolean _modifyCancellationRate = false;

    /** Movement alpha value */
    private float _alpha = 0.0f;
    
    /** Movement beta value */
    private float _beta = 0.0f;
    
    /** Movement S value */
    private float _s = 0.0f;
    
    /** Movement q value */
    private float _q = 0.0f;
    
    /** Cancel alpha value */
    private float _alphaC = 0.0f;
    
    /** Cancel beta value */
    private float _betaC = 0.0f;
    
    /** Cancel S value */
    private float _sC = 0.0f;
    
    /** Cancel q value */
    private float _qC = 0.0f;

    
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
        
        // Save the simulation state
        _simState = simState;
        
        // Get the properties
        Properties props = simState.getProperties();

        // Get the initiation rate modification flag
        String modifyInitiationRateStr = props.getProperty(
                _MODIFY_INITIATION_RATE_KEY );
        Validate.notEmpty( modifyInitiationRateStr,
                "Modify initation rate (key="
                + _MODIFY_INITIATION_RATE_KEY
                + ") may not be empty" );
        _modifyInitiationRate = Boolean.parseBoolean( modifyInitiationRateStr );
        _LOG.info( "Using _modifyInitiationRate=["
                + _modifyInitiationRate
                + "]" );

        // Get the following innate rate modification flag
        String modifyFollowingInnateRateStr = props.getProperty(
                _MODIFY_FOLLOWING_INNATE_RATE_KEY );
        Validate.notEmpty( modifyFollowingInnateRateStr,
                "Modify following innate rate (key="
                + _MODIFY_FOLLOWING_INNATE_RATE_KEY
                + ") may not be empty" );
        _modifyFollowingInnateRate = Boolean.parseBoolean( modifyFollowingInnateRateStr );
        _LOG.info( "Using _modifyFollowingInnateRate=["
                + _modifyFollowingInnateRate
                + "]" );

        // Get the following mimetic rate modification flag
        String modifyFollowingMimeticRateStr = props.getProperty(
                _MODIFY_FOLLOWING_MIMETIC_RATE_KEY );
        Validate.notEmpty( modifyFollowingMimeticRateStr,
                "Modify following mimetic rate (key="
                + _MODIFY_FOLLOWING_MIMETIC_RATE_KEY
                + ") may not be empty" );
        _modifyFollowingMimeticRate = Boolean.parseBoolean( modifyFollowingMimeticRateStr );
        _LOG.info( "Using _modifyFollowingMimeticRate=["
                + _modifyFollowingMimeticRate
                + "]" );

        // Get the cancellation rate modification flag
        String modifyCancellationRateStr = props.getProperty(
                _MODIFY_CANCELLATION_RATE_KEY );
        Validate.notEmpty( modifyCancellationRateStr,
                "Modify cancellation rate (key="
                + _MODIFY_CANCELLATION_RATE_KEY
                + ") may not be empty" );
        _modifyCancellationRate = Boolean.parseBoolean( modifyCancellationRateStr );
        _LOG.info( "Using _modifyCancellationRate=["
                + _modifyCancellationRate
                + "]" );
        
        // For now, hard code the equation values
        _alpha = 0.000775f;
        _LOG.info( "Using _alpha=[" + _alpha + "]" );
        _beta = 0.008f;
        _LOG.info( "Using _beta=[" + _beta + "]" );
        _s = simState.getAgentCount() * 0.6f;
        _LOG.info( "Using _s=[" + _s + "]" );
        _q = 1.4f;
        _LOG.info( "Using _q=[" + _q + "]" );
        _alphaC = 0.009f;
        _LOG.info( "Using _alphaC=[" + _alphaC + "]" );
        _betaC = -0.009f;
        _LOG.info( "Using _betaC=[" + _betaC + "]" );
        _sC = 2.0f;
        _LOG.info( "Using _sC=[" + _sC + "]" );
        _qC = 2.3f;
        _LOG.info( "Using _qC=[" + _qC + "]" );
        
        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculates the initiation probability for a given agent
     *
     * @param agent The agent
     * @return The initiation probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcInitiateProbability(edu.snu.leader.spatial.Agent)
     */
    @Override
    public float calcInitiateProbability( Agent agent )
    {
        // We only use the alpha component for initiation
        float alpha = _alpha;
        
        // Do we modify it?
        if( _modifyInitiationRate )
        {
            // Yup, get the individual's personality
            float personality = agent.getPersonalityTrait().getPersonality();

            // Calculate k and use it to change the probability
            float k = calculateK( personality );
            alpha *= k;
        }

        return alpha;
    }

    /**
     * Calculates the follow probability for a given agent
     *
     * @param agent The agent
     * @param group The potential group to join when following
     * @return The following probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcFollowProbability(edu.snu.leader.spatial.Agent, edu.snu.leader.spatial.Group)
     */
    @Override
    public float calcFollowProbability( Agent agent, Group group )
    {
        // Start off with the default alpha and beta values
        float alpha = _alpha;
        float beta = _beta;
        
        // Do we modify the innate (alpha) portion?
        float personality = agent.getPersonalityTrait().getPersonality();
        if( _modifyFollowingInnateRate )
        {
            // Yup
            float k = calculateK( personality );
            alpha *= k;
        }
        
        // Do we modify the mimetic (beta) portion?
        if( _modifyFollowingMimeticRate )
        {
            // Yup
            float k = calculateK( 1.0f - personality );
            beta *= k;
        }
        
        // Get the number of observed neighbors
        int neighborGroupCount = agent.getNeighborGroupCount( group );
        _LOG.debug( "Observed neighbor count ["
                + neighborGroupCount
                + "]" );

        // Calculate the probability
        float moversPart = (float) Math.pow( neighborGroupCount, _q );
        float probability = alpha + beta * moversPart
                / ((float) Math.pow( _s, _q) + moversPart);
        
        _LOG.debug( "Follow: prob=["
                + probability
                + "] alpha=["
                + alpha
                + "] beta=["
                + beta
                + "] X^q=["
                + moversPart
                + "] s^q=["
                + ((float) Math.pow( _s, _q))
                + "]" );
        
        return probability;
    }

    /**
     * Calculates the cancel probability for a given agent
     *
     * @param agent The agent
     * @return The cancel probability
     * @see edu.snu.leader.spatial.DecisionProbabilityCalculator#calcCancelProbability(edu.snu.leader.spatial.Agent)
     */
    @Override
    public float calcCancelProbability( Agent agent )
    {
        // Get the number of observed followers
        int observedFollowerCount = agent.getObservedFollowerCount();

        // Calculate the probability
        // Don't forget to include the leader in the "departed" count
        float moversPart = (float) Math.pow( observedFollowerCount + 1, _qC );
        float probability = _alphaC + _betaC * moversPart
                / ((float) Math.pow( _sC, _qC) + moversPart);

        // Do we modify it?
        if( _modifyCancellationRate )
        {
            // Yup, get the individual's personality
            float personality = agent.getPersonalityTrait().getPersonality();

            // Calculate k and use it to change the probability
            float k = calculateK( 1.0f - personality );
            probability *= k;
        }

        return probability;
    }
    
    /**
     * Calculates k coefficient for the collective movement equations
     *
     * @param value
     * @return The k coefficient
     */
    protected abstract float calculateK( float value );

}
