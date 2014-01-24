package edu.snu.leader.discrete.behavior;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;


public class DoNothing extends Decision
{

    public DoNothing( Agent agent, Agent leader )
    {
        super( DecisionType.DO_NOTHING, agent, leader );
    }

    @Override
    public void choose()
    {
        //set velocity to move towards leader's current position 
        if(!_agent.getCurrentDestination().subtract( _agent.getCurrentLocation()).equals( Vector2D.ZERO )){
            _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
                    _agent.getSpeed() ) );
        }
    }
}
