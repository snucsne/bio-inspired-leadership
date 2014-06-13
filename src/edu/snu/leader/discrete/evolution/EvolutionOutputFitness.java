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

package edu.snu.leader.discrete.evolution;

/**
 * EvolutionOutputFitness The output fitness from a simulation used for
 * evolution
 *
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class EvolutionOutputFitness
{
    /** Percentage of time alive that agents move toward their preferred
     * destination */
    private float percentTime = 0.0f;

    /** Percentage of agents that survive */
    private float percentSurvive = 0.0f;

    /** Percent of success as defined by (inits - cancels)/inits */
    private float percentSuccess = 0.0f;

    /** Percentage of time alive that agents move away from their preferred
     * destination
     */
    private float percentTimeAway = 0.0f;
    
    /**
     * The time taken to make it to the preferred destination relative
     * to the maximum amount of time steps
     */
    private float percentTimeToDestination = 0.0f;
    
    /**
     * The percentage of distance away from the agent's preferred
     * destination
     */
    private float percentDistanceToDestination = 0.0f;
    
    /**
     * The percentage of time steps alive
     */
    private float percentTimeAlive = 0.0f;
    
    /** The percent of agents that made it to a good destination */
    private float percentGoodDestination = 0.0f;
    
    /** The percent of agents that made it to their preferred destination
     * if they had one.
     */
    private float percentToPreferredDestination = 0.0f;
    
    /**
     * Builds this EvolutionOutputFitness object
     *
     * @param percentTime
     * @param percentSurvive
     * @param percentSuccess
     */
    public EvolutionOutputFitness( float percentTime,
            float percentSurvive,
            float percentSuccess,
            float percentTimeAway,
            float percentTimeToDestination,
            float percentDistanceToDestination,
            float percentTimeAlive,
            float percentGoodDestination,
            float percentToPreferredDestination)
    {
        this.percentTime = percentTime;
        this.percentSurvive = percentSurvive;
        this.percentSuccess = percentSuccess;
        this.percentTimeAway = percentTimeAway;
        this.percentTimeToDestination = percentTimeToDestination;
        this.percentDistanceToDestination = percentDistanceToDestination;
        this.percentTimeAlive = percentTimeAlive;
        this.percentGoodDestination = percentGoodDestination;
        this.percentToPreferredDestination = percentToPreferredDestination;
    }

    /**
     * Returns percentage of time moving towards
     * preferred destination
     *
     * @return
     */
    public float getPercentTime()
    {
        return percentTime;
    }

    /**
     * Returns percentage of survival
     *
     * @return
     */
    public float getPercentSurvive()
    {
        return percentSurvive;
    }

    /**
     * Returns percentage of success
     *
     * @return
     */
    public float getPercentSuccess()
    {
        return percentSuccess;
    }
    
    /**
     * Returns the percentage of time alive that agents move away from 
     * their preferred destination
     *
     * @return
     */
    public float getPercentTimeAway()
    {
        return percentTimeAway;
    }
    
    /**
     * The time taken to make it to the preferred destination relative
     * to the maximum amount of time steps
     * @return
     */
    public float getPercentTimeToDestination()
    {
        return percentTimeToDestination;
    }
    
    /**
     * Returns the percentage of distance away from the agent's preferred
     * destination
     *
     * @return
     */
    public float getPercentDistanceToDestination()
    {
        return percentDistanceToDestination;
    }
    
    /**
     * Returns the percentage of time steps alive
     *
     * @return
     */
    public float getPercentTimeAlive()
    {
        return percentTimeAlive;
    }
    
    public float getPercentGoodDestination()
    {
        return percentGoodDestination;
    }
    
    public float getPercentToPreferredDestination()
    {
        return percentToPreferredDestination;
    }
}
