package edu.snu.leader.discrete.behavior;

public interface ConflictTrait
{
    /**
     * Returns the conflict of a Decision
     * 
     * @param decision The Decision
     * @return The conflict
     */
    public float getConflict( Decision decision );

    /**
     * Updates the conflict of the agent
     */
    public void update();
}
