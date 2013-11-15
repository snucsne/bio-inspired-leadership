package edu.snu.leader.discrete.behavior;

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
        // _agent.setCurrentVelocity( _leader.getCurrentVelocity() );
    }
}
