/*
 * The Bio-inspired Leadership Toolkit is a set of tools used to simulate the
 * emergence of leaders in multi-agent systems. Copyright (C) 2014 Southern
 * Nazarene University This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package edu.snu.leader.hidden.evolution;

/**
 * Class that encapsulates many evolutionary fitness measures
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class FitnessMeasures
{
    /** Percentage of agents that arrive at their preferred Direction */
    private float _percentageAtPreferredDirection = 0.0f;
    
    /** Percentage of agents that arrive at the "correct" Direction */
    private float _percentageAtCorrectDirection = 0.0f;
    
    /** Time until the group has a consensus decision */
    private float _timeUntilConsensus = Float.POSITIVE_INFINITY;
    
    /** Flag indicating whether or not the simulation was successful */
    private boolean _successful = false;
    
    
    /**
     * Builds this FitnessMeasures object
     *
     * @param successful
     * @param percentageAtPreferredDirection
     * @param percentageAtCorrectDirection
     * @param timeUntilConsensus
     */
    public FitnessMeasures( boolean successful,
            float percentageAtPreferredDirection,
            float percentageAtCorrectDirection,
            float timeUntilConsensus )
    {
        this._successful = successful;
        this._percentageAtPreferredDirection = percentageAtPreferredDirection;
        this._percentageAtCorrectDirection = percentageAtCorrectDirection;
        this._timeUntilConsensus = timeUntilConsensus;
    }

    
    /**
     * Returns the success of the simulation
     *
     * @return The success of the simulation
     */
    public boolean wasSuccessful()
    {
        return _successful;
    }

    /**
     * Returns the percentageAtPreferredDirection for this object
     *
     * @return The percentageAtPreferredDirection.
     */
    public float getPercentageAtPreferredDirection()
    {
        return _percentageAtPreferredDirection;
    }

    /**
     * Returns the percentageAtCorrectDirection for this object
     *
     * @return The percentageAtCorrectDirection.
     */
    public float getPercentageAtCorrectDirection()
    {
        return _percentageAtCorrectDirection;
    }

    /**
     * Returns the timeUntilConsensus for this object
     *
     * @return The timeUntilConsensus.
     */
    public float getTimeUntilConsensus()
    {
        return _timeUntilConsensus;
    }
    
    
}
