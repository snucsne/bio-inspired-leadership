package edu.snu.leader.discrete.behavior;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;

public class Reached extends Decision
{
    public Reached(Agent agent)
    {
        this(DecisionType.REACHED, agent, agent);
    }
    
    Reached( DecisionType type, Agent agent, Agent leader )
    {
        super( type, agent, leader );
    }

    @Override
    public void choose()
    {
        _agent.setCurrentVelocity( Vector2D.ZERO );
    }

}
