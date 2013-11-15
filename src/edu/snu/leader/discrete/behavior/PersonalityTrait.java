package edu.snu.leader.discrete.behavior;

import edu.snu.leader.discrete.simulator.Agent;


public interface PersonalityTrait
{
    /**
     * Returns the personality
     * 
     * @return The personality
     */
    public float getPersonality();

    /**
     * Updates the personality of the agent
     * 
     * @param agent Agent associated with this personality behavior
     */
    public void update();

    /**
     * Initializes this personality trait and links it to the Agent
     * 
     * @param agent Agent to be linked to
     */
    public void initialize( Agent agent );
}
