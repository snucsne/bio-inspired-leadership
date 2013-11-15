package edu.snu.leader.discrete.simulator;

import java.util.List;


public interface AgentBuilder
{
    /**
     * Initializes builder
     * 
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState );

    /**
     * Creates a List of Agents
     * 
     * @return The List of Agents
     */
    public List<Agent> build();
}
