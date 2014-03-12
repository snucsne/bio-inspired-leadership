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
package edu.snu.leader.hidden.event;

import edu.snu.leader.hidden.SpatialIndividual;
// Imports
import edu.snu.leader.util.MathUtils;


/**
 * DefaultEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DefaultEventTimeCalculator extends AbstractEventTimeCalculator
{
    /**
     * Calculates the time at the specified individual will initiate movement
     *
     * @param ind The individual
     * @return The initiation time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateInitiationTime(edu.snu.leader.hidden.SpatialIndividual)
     */
    @Override
    public float calculateInitiationTime( SpatialIndividual ind )
    {
        float tau = calculateInitiationRate(
                _simState.getIndividualCount() );
        return MathUtils.generateRandomExponential(
                1.0f / tau,
                _simState.getRandom() );
    }

    /**
     * Calculates the time at the specified individual will follow an
     * initiator
     *
     * @param ind The individual
     * @param initiator The initiator
     * @param departed The number of individuals who have already departed
     * @param groupSize The size of the group
     * @return The follow time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateFollowTime(edu.snu.leader.hidden.SpatialIndividual, edu.snu.leader.hidden.SpatialIndividual, int, int)
     */
    @Override
    public float calculateFollowTime( SpatialIndividual ind,
            SpatialIndividual initiator,
            int departed,
            int groupSize )
    {
        float tau = calculateFollowRate( groupSize, departed );
        return MathUtils.generateRandomExponential( 1.0f / tau,
                _simState.getRandom());
    }

    /**
    * Calculates the time at the specified individual will cancel an initiation
    *
    * @param ind The individual
    * @param departed The number of individuals who have already departed
    * @return The cancellation time
     * @see edu.snu.leader.hidden.event.EventTimeCalculator#calculateCancelTime(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    public float calculateCancelTime( SpatialIndividual ind, int departed )
    {
        float cRate = calculateCancelRate( departed );
        return MathUtils.generateRandomExponential( cRate,
                _simState.getRandom() );
    }
}
