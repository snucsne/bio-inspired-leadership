/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
