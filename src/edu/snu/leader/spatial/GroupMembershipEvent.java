/*
 * COPYRIGHT
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
