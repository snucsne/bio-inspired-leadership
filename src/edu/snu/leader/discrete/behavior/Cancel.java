package edu.snu.leader.discrete.behavior;


import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;
import edu.snu.leader.discrete.simulator.Group;


public class Cancel extends Decision
{

    public Cancel( Agent agent )
    {
        super( DecisionType.CANCELLATION, agent, agent );
    }

    @Override
    public void choose()
    {
        _agent.setLeader( _agent );
        _agent.getGroup().dissolve();
        _agent.setCurrentDestination( _agent.getInitialLocation() );
        if(! _agent.getCurrentDestination().subtract( _agent.getCurrentLocation() ).equals( Vector2D.ZERO )){
            _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
                    _agent.getSpeed() ) );
        }
        Group.NONE.addAgent( _agent, _agent.getTime() );
        // TODO add to NONE group set new currentVelocity
    }
}
