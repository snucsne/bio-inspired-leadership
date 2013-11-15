package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


public class NoMove implements MovementBehavior
{

    @Override
    public void move()
    {
        // No movement
    }

    @Override
    public void initialize( Agent agent )
    {
        // Do nothing
    }
}
