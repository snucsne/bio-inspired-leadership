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
package edu.snu.leader.spatial;

/**
 * Decision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface Decision
{
    /**
     * Returns the type of this decision
     *
     * @return The type of this decision
     */
    public DecisionType getType();

    /**
     * Calculates and returns the probability of this decision being made
     *
     * @return The probability that this decision is made
     */
    public float calcProbability();

    /**
     * Makes this decision
     */
    public void make();

    /**
     * Returns the time of the decision
     *
     * @return The time
     */
    public long getTime();
}
