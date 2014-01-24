package edu.snu.leader.discrete.behavior;


import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;


public class Follow extends Decision
{

    public Follow( Agent agent, Agent leader )
    {
        super( DecisionType.FOLLOW, agent, leader );
    }

    @Override
    public void choose()
    {
        // set leader to the new leader
        _agent.setLeader( _leader );
        // set group to the leader's group
        _leader.getGroup().addAgent( _agent, _agent.getTime() );
        // set destination to the leader's destination
        _agent.setCurrentDestination( _leader.getCurrentLocation() );
        //set velocity to move towards leader's current position 
        if(!_agent.getCurrentDestination().subtract( _agent.getCurrentLocation()).equals( Vector2D.ZERO )){
            _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
                    _agent.getSpeed() ) );
        }
    }
}