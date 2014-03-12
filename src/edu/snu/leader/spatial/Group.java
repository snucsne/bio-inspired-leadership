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
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Group
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class Group
{
    /** Default/initial group */
    public static final Group NONE = new Group( "NONE" );

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( Group.class.getName() );


    /** Unique identifier for the group */
    private Object _id = null;

    /** Members of this group */
    private List<Agent> _members = new LinkedList<Agent>();

    /** Membership events of this group */
    private List<GroupMembershipEvent> _membershipEvents =
            new ArrayList<GroupMembershipEvent>();

    /** The maximum size of the group throughout its existence */
    private int _maxSize = 0;


    /**
     * Builds this Group object
     *
     * @param id
     */
    public Group( Object id )
    {
        _id = id;
    }

    /**
     * Signals an agent joining the group
     *
     * @param agent
     * @param time
     */
    public void join( Agent agent, long time )
    {
        // Tell the agent their new group
        agent.setGroup( this );

        // Add the agent to the list of members
        _members.add( agent );

        // Log the event
        _membershipEvents.add(
                new GroupMembershipEvent( agent,
                        time,
                        GroupMembershipEvent.Type.JOIN ) );

        // Is it the biggest the group has been?
        if( _members.size() > _maxSize )
        {
            // Yup
            _maxSize = _members.size();
        }

        _LOG.debug( "Agent ["
                + agent.getID()
                + "] joined group ["
                + getID()
                + "] at time ["
                + time
                + "]" );
    }

    /**
     * Signals an agent leaving the group
     *
     * @param agent
     * @param time
     */
    public void leave( Agent agent, long time )
    {
        // Remove the agent from the list of members
        _members.remove( agent );

        // Log the event
        _membershipEvents.add(
                new GroupMembershipEvent( agent,
                        time,
                        GroupMembershipEvent.Type.LEAVE ) );

        _LOG.debug( "Agent ["
                + agent.getID()
                + "] left group ["
                + getID()
                + "] at time ["
                + time
                + "]" );
    }

    /**
     * Returns the id for this object
     *
     * @return The ID
     */
    public Object getID()
    {
        return _id;
    }

    /**
     * Returns the members for this object
     *
     * @return The members
     */
    public List<Agent> getMembers()
    {
        return _members;
    }

    /**
     * Returns the current number of members in the group
     *
     * @return The current number of members
     */
    public int getSize()
    {
        return _members.size();
    }

    /**
     * Returns the maximum number of members that the group has had
     *
     * @return The maximum group size reached
     */
    public int getMaxSize()
    {
        return _maxSize;
    }

    /**
     * Returns the membershipEvents for this object
     *
     * @return The membershipEvents
     */
    public List<GroupMembershipEvent> getMembershipEvents()
    {
        return new ArrayList<GroupMembershipEvent>( _membershipEvents );
    }

    /**
     * Returns the number of neighbors that are a member of this group
     *
     * @param neighbors An agent's neighbors
     * @return The number of neighbors that are a member of this group
     */
    public int getNeighborMemberCount( List<Agent> neighbors )
    {
        int count = 0;

        // Check each neighbor to see if they are in this group
        for( Agent neighbor : neighbors )
        {
            // Do they have the same group id?
            if( _id.equals( neighbor.getGroup().getID() ) )
            {
                // Yup
                ++count;
            }
        }

        return count;
    }

    /**
     * Finds and returns the last neighbor join event time for the list of
     * neighbors
     *
     * @param neighbors
     * @return The last neighbor join event time
     */
    public long findLastNeighborJoinEventTime( List<Agent> neighbors )
    {
        long time = 0l;

        /* Look through the events from most recent to oldest looking for
         * joining neighbors */
        for( int i = _membershipEvents.size() - 1; i >= 0; i-- )
        {
            // Is it for one of the neighbors?
            GroupMembershipEvent currentEvent = _membershipEvents.get( i );
            if( neighbors.contains( currentEvent.getAgent() ) )
            {
                // Yup
                time = currentEvent.getTime();
            }
        }

        return time;
    }

    /**
     * Resets this group
     */
    public void reset()
    {
        _members = new LinkedList<Agent>();
        _membershipEvents = new ArrayList<GroupMembershipEvent>();
        _maxSize = 0;
    }

    /**
     * Builds a new group
     *
     * @param simState
     * @return A new group
     */
    public static Group buildNewGroup( SimulationState simState )
    {
        return new Group( simState.buildNextNewGroupID() );
    }
}
