package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


public interface MovementBehavior
{
    /**
     * Link this movement behavior to an Agent
     * 
     * @param agent
     */
    public void initialize( Agent agent );

    /**
     * The way that an Agent moves
     */
    public void move();
}
