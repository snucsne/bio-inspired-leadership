package edu.snu.leader.discrete.behavior;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;
import edu.snu.leader.discrete.simulator.Group;
import edu.snu.leader.discrete.simulator.SimulationState;

public class SimpleAngularMovement implements MovementBehavior
{
    private Agent _agent;

    @Override
    public void move()
    {
        // set current velocity to that of the leader's
        _agent.setCurrentVelocity( _agent.getLeader().getCurrentVelocity() );
        // if agent is initiating and is with 3 units of their destination stop
        if( _agent.getCurrentLocation().distance1(
                        _agent.getPreferredDestination() ) < SimulationState.getDestinationRadius() )
        {
            _agent.reachedDestination();
        }
        else if(_agent.getGroup().getId().equals( Group.NONE.getId() )
                && _agent.getCurrentLocation().distance(
                        _agent.getInitialLocation() ) < SimulationState.getDestinationRadius() )
        {
            _agent.setCurrentVelocity( Vector2D.ZERO );
        }
        // otherwise move normally
        else
        {
            _agent.setCurrentLocation( _agent.getCurrentLocation().add(
                    _agent.getCurrentVelocity() ) );
            System.out.println(_agent.getId() + " " + _agent.getCurrentDecision().getDecision().getDecisionType() + " " + _agent.getLeader().getId() + ": " + _agent.getCurrentVelocity() + " pref: " + _agent.getPreferredDestination() + " time: " + _agent.getTime());
        }
    }

    @Override
    public void initialize( Agent agent )
    {
        _agent = agent;
    }

}
