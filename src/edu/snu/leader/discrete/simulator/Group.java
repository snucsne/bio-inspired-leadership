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

package edu.snu.leader.discrete.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


/**
 * Group Represents a group of agents. Created by initiations from agents.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class Group
{
    /** The ID of the Agent group */
    private Object _id = null;

    /** List of current members */
    private List<Agent> _members = null;

    /** A log of all of the membership events */
    private List<MembershipEvent> _membershipEvents = null;

    /** The max group size the group achieved */
    private int _maxGroupSize = 0;

    /** The color of this group */
    private Color _groupColor = null;

    /** The index of this groups color in the _colors array */
    private int _colorIndex = 0;

    private SimulationState _simState = null;

    /**
     * This constructor is for the NONE group
     */
    Group( SimulationState simState )
    {
        // setup variables
        _simState = simState;
        _id = "Group" + _simState.uniqueGroupIdCount++;
        _members = new LinkedList<Agent>();
        _membershipEvents = new ArrayList<MembershipEvent>();
        // set the color of this group and increment group size
        for( int i = 0; i < _simState.colors.length; i++ )
        {
            // find an unused color
            if( !_simState.colorsInUse[i] )
            {
                // reserve and assign it a color
                _groupColor = _simState.colors[i];
                _colorIndex = i;
                _simState.colorsInUse[i] = true;
                // increment total number of groups
                simState.totalNumGroups++;
                // break out of this loop since we are done
                break;
            }
        }
    }

    /**
     * Creates the Group
     * 
     * @param agent The Agent that started the group
     * @param time The time this group was created
     */
    public Group( Agent agent, int time )
    {
        this( agent.getSimState() );
        addAgent( agent, time );

    }

    /**
     * Adds an Agent to the group. Creates a new MembershipEvent
     * 
     * @param agent Agent that joined
     * @param time Time it joined
     */
    public void addAgent( Agent agent, int time )
    {
        if( !_members.contains( agent ) )
        {

            // increment group size only if not in membership events log
            boolean shouldIncrementGroupSize = true;
            for( int i = 0; i < _membershipEvents.size(); i++ )
            {
                if( _membershipEvents.get( i ).getAgent().equals( agent )
                        && _membershipEvents.get( i ).getType().equals(
                                MembershipEventType.JOIN ) )
                {
                    shouldIncrementGroupSize = false;
                }
            }
            if( shouldIncrementGroupSize )
            {
                _maxGroupSize++;
            }

            // remove agent from old group
            agent.getGroup().removeAgent( agent, time );
            // set new group of agent to this group
            agent.setGroup( this );
            // add a membership event and add agent to members
            _membershipEvents.add( new MembershipEvent( agent, time,
                    MembershipEventType.JOIN ) );
            _members.add( agent );
        }
    }

    /**
     * Removes an Agent to the group. Creates a new MembershipEvent
     * 
     * @param agent Agent that left
     * @param time Time it left
     */
    public void removeAgent( Agent agent, int time )
    {
        // if the group contains the agent to remove then add the remove event
        // and remove it
        if( _members.contains( agent ) )
        {
            _membershipEvents.add( new MembershipEvent( agent, time,
                    MembershipEventType.LEAVE ) );
            _members.remove( agent );
        }
        // if group has 0 members and is not the none group then get rid of the
        // group
        if( _members.size() == 0 && !this.equals( _simState.noneGroup ) )
        {
            _simState.colorsInUse[_colorIndex] = false;
            _simState.totalNumGroups--;
        }
    }

    public void dissolve()
    {
        int temp = _members.size();
        // remove all the agents
        while( temp > 0 )
        {
            Agent tempAgent = _members.get( 0 );
            tempAgent.setCurrentVelocity( Vector2D.ZERO );
            _simState.noneGroup.addAgent( tempAgent, tempAgent.getTime() );
            temp--;
        }
        // free up the color for use later if its not the noneGroup's color
        if( !this.equals( _simState.noneGroup ) )
        {
            _simState.colorsInUse[_colorIndex] = false;
        }
    }

    /**
     * This is for Group.NONE so it can reset itself after every simulation run
     */
    public void reset()
    {
        // reset all of the colors in use to false
        Arrays.fill( _simState.colorsInUse, false );
        // set unique group id count equal to 0
        _simState.uniqueGroupIdCount = 0;
        _simState.noneGroup = new Group( _simState );

        _simState.totalNumGroups = 0;
    }

    /**
     * Returns the max size that the group achieved
     * 
     * @return The max group size achieved
     */
    public int getSize()
    {
        return _maxGroupSize;
    }

    public Object getId()
    {
        return _id;
    }

    public int getNumberGroups()
    {
        int temp = -1;
        for( int i = 0; i < _simState.colorsInUse.length; i++ )
        {
            if( _simState.colorsInUse[i] )
            {
                temp++;
            }
        }
        return temp;
    }

    public int getLastTimeJoined( Agent agent )
    {
        // start last time joined to be really large
        int timeJoined = Integer.MAX_VALUE;
        // membership event count
        int count = _membershipEvents.size() - 1;
        boolean isFound = false;
        // while we have events to look through
        while( count >= 0 && !isFound )
        {
            MembershipEvent temp = _membershipEvents.get( count );
            // if this one is the agent, then get its time and exit loop
            if( agent.getId().equals( temp.getAgent().getId() )
                    && temp.getType().equals( MembershipEventType.JOIN ) )
            {
                timeJoined = temp.getTime();
                isFound = true;
            }
            // decrement count
            count--;
        }
        return timeJoined;
    }

    public Color getGroupColor()
    {
        return _groupColor;
    }

    // //////nested classes\\\\\\\\\

    /**
     * enum for MembershipEvent types. Can either be JOIN or LEAVE.
     * 
     * @author Tim
     */
    public enum MembershipEventType {
        JOIN, LEAVE
    }

    /**
     * A MebershipEvent will be generated whenever a group adds or removes a
     * member
     * 
     * @author Tim
     */
    public class MembershipEvent
    {
        private Agent _agent = null;

        private int _time = 0;

        private MembershipEventType _type = null;

        /**
         * Creates a MembershipEvent
         * 
         * @param agent The Agent joining or leaving
         * @param time The time the Agent joined or left
         * @param type The time of event, either JOIN or LEAVE
         */
        public MembershipEvent( Agent agent, int time, MembershipEventType type )
        {
            _agent = agent;
            _time = time;
            _type = type;
        }

        /**
         * Returns the Agent that caused this event
         * 
         * @return The agent
         */
        public Agent getAgent()
        {
            return _agent;
        }

        /**
         * Returns the time this event happened
         * 
         * @return The time
         */
        public int getTime()
        {
            return _time;
        }

        /**
         * Returns the type of the event
         * 
         * @return The type of the event
         */
        public MembershipEventType getType()
        {
            return _type;
        }
    }
}
