/*
 * The Bio-inspired Leadership Toolkit is a set of tools used to simulate the
 * emergence of leaders in multi-agent systems. Copyright (C) 2014 Southern
 * Nazarene University This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package edu.snu.leader.discrete.behavior;

import java.util.List;

import edu.snu.leader.discrete.behavior.Decision.DecisionType;
import edu.snu.leader.discrete.simulator.Agent;


/**
 * AdaptivePersonalityTrait Personality trait that changes over time.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class AdaptivePersonalityTrait implements PersonalityTrait
{
    /** Personality value */
    private float _p = .5f;

    /** The agent that has this trait */
    private Agent _agent = null;

    @Override
    public float getPersonality()
    {
        return _p;
    }

    @Override
    public void update()
    {
        float _lambda = _agent.getLambda();
        if( _agent.getCurrentDecision().getDecision().getDecisionType().equals(
                DecisionType.INITIATION )
                || _agent.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.CANCELLATION ) )
        {
            int r = 0;
            // calculate how many are currently in this agents group
            int inGroup = 0;
            List<Agent> neighbors = _agent.getNearestNeighbors();
            for( int i = 0; i < neighbors.size(); i++ )
            {
                if( _agent.getGroup().getId().equals(
                        neighbors.get( i ).getGroup().getId() ) )
                {
                    inGroup++;
                }
            }
            // update personality only if succeeded or cancelled
            if( inGroup / neighbors.size() >= _agent.getCancelThreshold()
                    || _agent.getCurrentDecision().getDecision().getDecisionType().equals(
                            DecisionType.CANCELLATION ) )
            {
                // if succeeded then r = 1
                if( inGroup / neighbors.size() >= _agent.getCancelThreshold() )
                {
                    r = 1;
                }
                _p = ( _p * ( 1 - _lambda ) ) + ( _lambda * r );
            }
        }
    }

    @Override
    public void initialize( Agent agent )
    {
        _agent = agent;
    }

}
