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

// Imports
import org.apache.commons.lang.Validate;


/**
 * DecisionEvent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DecisionEvent
{
    /** A specific decision */
    private Decision _decision = null;

    /** The associated group */
    private Group _group = null;

    /** The time the decision was made */
    private long _time = 0;


    /**
     * Builds this DecisionEvent object
     *
     * @param decision
     * @param group
     * @param time
     */
    public DecisionEvent( Decision decision, Group group, long time )
    {
        // Validate the decision
        Validate.notNull( decision, "Decision may not be null" );
        _decision = decision;

        // Validate the group
        Validate.notNull( group, "Group may not be null" );

        // Just store the time
        _time = time;
    }

    /**
     * Returns the decision for this object
     *
     * @return The decision
     */
    public Decision getDecision()
    {
        return _decision;
    }

    /**
     * Returns the group associated with the decision
     *
     * @return The group
     */
    public Group getGroup()
    {
        return _group;
    }

    /**
     * Returns the time for this object
     *
     * @return The time
     */
    public long getTime()
    {
        return _time;
    }

}
