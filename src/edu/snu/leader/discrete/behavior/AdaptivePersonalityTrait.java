package edu.snu.leader.discrete.behavior;

import java.util.List;

import edu.snu.leader.discrete.behavior.Decision.DecisionType;
import edu.snu.leader.discrete.simulator.Agent;


public class AdaptivePersonalityTrait implements PersonalityTrait
{
    private float _p = .5f;

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
