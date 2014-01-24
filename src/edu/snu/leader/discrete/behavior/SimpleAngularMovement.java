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
//            _agent.setCurrentDestination( _agent.getLeader().getCurrentLocation() );
//            if(!_agent.getCurrentDestination().subtract( _agent.getCurrentLocation()).equals( Vector2D.ZERO )){
//                _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
//                        _agent.getSpeed() ) );
//            }
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
