package edu.snu.leader.discrete.behavior;

public class DefaultConflictTrait implements ConflictTrait
{

    @Override
    public float getConflict( Decision decision )
    {
        return 0;
    }

    @Override
    public void update()
    {
        // do nothing
    }
}
