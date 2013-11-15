package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.behavior.Decision.DecisionType;
import edu.snu.leader.discrete.simulator.Agent;

public class SimpleAngularMovement implements MovementBehavior
{
    private Agent _agent;

    @Override
    public void move()
    {
        // set current velocity to that of the leader's
        _agent.setCurrentVelocity( _agent.getLeader().getCurrentVelocity() );
        // if agent is initiating and is with 3 units of their destination stop
        if( _agent.getCurrentDecision().getDecision().getDecisionType().equals(
                DecisionType.INITIATION )
                && _agent.getCurrentLocation().distance(
                        _agent.getPreferredDestination() ) < 3 )
        {
            _agent.stop();
        }
        else if(_agent.getCurrentDecision().getDecision().getDecisionType().equals(
                DecisionType.FOLLOW) && _agent.getCurrentLocation().distance(
                        _agent.getPreferredDestination() ) < 3)
        {
            _agent.stop();
        }
        // otherwise move normally
        else
        {
            _agent.setCurrentLocation( _agent.getCurrentLocation().add(
                    _agent.getCurrentVelocity() ) );
        }
    }

    @Override
    public void initialize( Agent agent )
    {
        _agent = agent;
    }

}
