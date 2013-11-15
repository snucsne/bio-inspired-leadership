package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


public class DefaultPersonalityTrait implements PersonalityTrait
{

    @Override
    public float getPersonality()
    {
        return .5f;
    }

    @Override
    public void update()
    {
        // do nothing
    }

    @Override
    public void initialize( Agent agent )
    {
        // TODO Auto-generated method stub

    }
}
