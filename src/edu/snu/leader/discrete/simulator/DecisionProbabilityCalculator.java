package edu.snu.leader.discrete.simulator;

import edu.snu.leader.discrete.behavior.Decision;


public interface DecisionProbabilityCalculator
{
    /**
     * Initializes the calculator
     * 
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState );

    /**
     * Calculates the probability for an initiation decision
     * 
     * @param decision The initiation decision
     */
    public void calcInitiateProb( Decision decision );

    /**
     * Calculates the probability for a follow decision
     * 
     * @param decision The follow decision
     */
    public void calcFollowProb( Decision decision );

    /**
     * Calculates the probability for a cancellation decision
     * 
     * @param decision The cancellation decision
     */
    public void calcCancelProb( Decision decision );
    
    /**
     * Returns an array of all the possible follow probabilities. Will be null if pre-generation was not specified in properties file. 
     *
     * @return
     */
    public double[] getPreCalculatedFollowProbabilities();
    
    /**
     * Returns an array of all the possible cancel probabilities. Will be null if pre-generation was not specified in properties file. 
     *
     * @return
     */
    public double[] getPreCalculatedCancelProbabilities();
}
