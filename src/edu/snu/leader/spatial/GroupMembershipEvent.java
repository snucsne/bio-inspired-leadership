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
 * MembershipEvent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class GroupMembershipEvent
{
    /** The types of membership events */
    public static enum Type
    {
        JOIN,
        LEAVE;
    }

    /** The agent initiating the event */
    private Agent _agent = null;

    /** The time of the event */
    private long _time = 0;

    /** The type of event */
    private Type _type = null;

    /**
     * Builds this MembershipEvent object
     *
     * @param agent
     * @param time
     * @param type
     */
    public GroupMembershipEvent( Agent agent, long time, Type type )
    {
        // Store the attributes
        _agent = agent;
        _time = time;
        _type = type;
    }

    /**
     * Returns the agent for this object
     *
     * @return The agent
     */
    public Agent getAgent()
    {
        return _agent;
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

    /**
     * Returns the type for this object
     *
     * @return The type
     */
    public Type getType()
    {
        return _type;
    }
}
