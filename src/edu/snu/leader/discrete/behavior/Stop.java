package edu.snu.leader.discrete.behavior;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;


public class Stop extends Decision
{
    public Stop( Agent agent )
    {
        super( DecisionType.STOP, agent, agent );
    }

    @Override
    public void choose()
    {
        _agent.setCurrentVelocity( Vector2D.ZERO );
    }
}
