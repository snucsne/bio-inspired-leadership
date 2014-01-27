package edu.snu.leader.discrete.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;


public class Group
{
    private static int uniqueIdCount = 0;

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

    /** Array that keeps track of what colors are in use so they can be recycled */
    private static boolean[] _colorsInUse = new boolean[70];

    /*
     * new Color( 0x114477 ), new Color( 0x777711 ),
            new Color( 0x771155 ), new Color( 0x117744 ),
            new Color( 0x771122 ), new Color( 0x117777 ),
            new Color( 0x774411 ),
     */
    
    /** Array of 70 unique colors to use for groups */
    private static Color[] _colors = { new Color( 0x000000 ),
            new Color( 0x9ACD32 ), new Color( 0x008080 ),
            new Color( 0xF5DEB3 ), new Color( 0xEE82EE ),
            new Color( 0x40E0D0 ), new Color( 0xFF6347 ),
            new Color( 0xD8BFD8 ), new Color( 0xFFFF00 ),
            new Color( 0x4682B4 ), new Color( 0x00FF7F ),
            new Color( 0x708090 ), new Color( 0x6A5ACD ),
            new Color( 0x87CEEB ), new Color( 0xC0C0C0 ),
            new Color( 0xA0522D ), new Color( 0x2E8B57 ),
            new Color( 0xF4A460 ), new Color( 0xFA8072 ),
            new Color( 0x8B4513 ), new Color( 0x4169E1 ),
            new Color( 0xBC8F8F ), new Color( 0xFF0000 ),
            new Color( 0x800080 ), new Color( 0xB0E0E6 ),
            new Color( 0xDDA0DD ), new Color( 0xFFC0CB ),
            new Color( 0xCD853F ), new Color( 0xFFDAB9 ),
            new Color( 0xFFEFD5 ), new Color( 0xDB7093 ),
            new Color( 0x98FB98 ), new Color( 0xEEE8AA ),
            new Color( 0xDA70D6 ), new Color( 0xFF4500 ),
            new Color( 0xFFA500 ), new Color( 0x6B8E23 ),
            new Color( 0x000080 ), new Color( 0xFFDEAD ),
            new Color( 0xF0A0AA ), new Color( 0x191970 ),
            new Color( 0xC71585 ), new Color( 0x48D1CC ),
            new Color( 0x00FA9A ), new Color( 0x7B68EE ),
            new Color( 0x3CB371 ), new Color( 0x0000CD ),
            new Color( 0x66CDAA ), new Color( 0x800000 ),
            new Color( 0x32CD32 ), new Color( 0x00FF00 ),
            new Color( 0xFFFFE0 ), new Color( 0xB0C4DE ),
            new Color( 0x778899 ), new Color( 0x87CEFA ),
            new Color( 0x20B2AA ), new Color( 0xFFA07A ),
            new Color( 0xFFB6C1 ), new Color( 0x90EE90 ),
            new Color( 0xD3D3D3 ), new Color( 0xFAFAD2 ),
            new Color( 0xE0FFFF ), new Color( 0xF08080 ),
            new Color( 0xADD8E6 ), new Color( 0x7CFC00 ),
            new Color( 0x4B0082 ), new Color( 0xFF69B4 ),
            new Color( 0xFFD700 ), new Color( 0x1E90FF ), new Color( 0x8FBC8F ) };

    /** Total number of groups. Set to -1 so that Group0 will not be counted */
    public static int totalNumGroups = 1;

    /** This is a default group */
    public static Group NONE = new Group();

    /**
     * This constructor is for the NONE group
     */
    private Group()
    {
        _id = "Group" + uniqueIdCount++;
        _members = new LinkedList<Agent>();
        _membershipEvents = new ArrayList<MembershipEvent>();
        for( int i = 0; i < Group._colors.length; i++ )
        {
            if( !_colorsInUse[i] )
            {
                _groupColor = Group._colors[i];
                _colorIndex = i;
                Group._colorsInUse[i] = true;
                totalNumGroups++;
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
        this();
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

            // only if not in membership events log
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

            agent.getGroup().removeAgent( agent, time );
            agent.setGroup( this );
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
        if( _members.contains( agent ) )
        {
            _membershipEvents.add( new MembershipEvent( agent, time,
                    MembershipEventType.LEAVE ) );
            _members.remove( agent );
        }
        // if group has 0 members and is not the none group then get rid of the
        // group
        if( _members.size() == 0 && !this.equals( Group.NONE ) )
        {
            Group._colorsInUse[_colorIndex] = false;
            totalNumGroups--;
        }
    }

    public void dissolve()
    {
        int temp = _members.size();
        while( temp > 0 )
        {
            Agent tempAgent = _members.get( 0 );
            tempAgent.setCurrentVelocity( Vector2D.ZERO );
//            tempAgent.setCurrentDestination( tempAgent.getInitialLocation() );
//            if( !( tempAgent.getCurrentDestination().subtract( tempAgent.getCurrentLocation() ) ).equals( Vector2D.ZERO ) )
//            {
//                tempAgent.setCurrentVelocity( ( tempAgent.getCurrentDestination().subtract( 
//                        tempAgent.getCurrentLocation() ) ).normalize().scalarMultiply(
//                        tempAgent.getSpeed() ) );
//            }
            Group.NONE.addAgent( tempAgent, tempAgent.getTime() );
            temp--;
        }
        if( !this.equals( Group.NONE ) )
        {
            Group._colorsInUse[_colorIndex] = false;
        }
    }

    /**
     * This is for Group.NONE so it can reset itself after every simulation run
     */
    public void reset()
    {
        Arrays.fill( _colorsInUse, false );
        uniqueIdCount = 0;
        NONE = new Group();

        totalNumGroups = 0;
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

    public static int getNumberGroups()
    {
        int temp = -1;
        for( int i = 0; i < _colorsInUse.length; i++ )
        {
            if( _colorsInUse[i] )
            {
                temp++;
            }
        }
        return temp;
    }
    
    public int getTimeJoined( Agent agent){
        int timeJoined = Integer.MAX_VALUE;
        int count = _membershipEvents.size() - 1;
        boolean isFound = false;
        while(count >= 0 && !isFound){
            MembershipEvent temp = _membershipEvents.get( count );
            if(agent.getId().equals( temp.getAgent().getId() )){
                timeJoined = temp.getTime();
            }
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
