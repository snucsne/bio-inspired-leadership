package edu.snu.leader.discrete.behavior;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;
import edu.snu.leader.discrete.simulator.Group;


public class Initiate extends Decision
{

    public Initiate( Agent agent )
    {
        super( DecisionType.INITIATION, agent, agent );
    }

    @Override
    public void choose()
    {
        // set leader to self
        _agent.setLeader( _agent );
        // set group to a new group
        _agent.setGroup( new Group( _agent, _agent.getTime() ) );
        // set destination to preferred destination
        _agent.setCurrentDestination( _agent.getPreferredDestination() );
        // set current velocity to that of going towards the preferred
        // destination
        if(!_agent.getCurrentDestination().subtract( _agent.getCurrentLocation()).equals( Vector2D.ZERO )){
            _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
                    _agent.getSpeed() ) );
        }
    }
}
