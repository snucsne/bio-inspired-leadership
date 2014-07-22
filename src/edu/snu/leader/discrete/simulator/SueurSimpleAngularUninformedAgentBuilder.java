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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.behavior.MovementBehavior;
import edu.snu.leader.discrete.behavior.SimpleAngularMovement;
import edu.snu.leader.discrete.utils.Reporter;
import edu.snu.leader.discrete.utils.Utils;
import edu.snu.leader.util.MiscUtils;


/**
 * SueurSimpleAngularUninformedAgentBuilder Builds agents that have simple
 * angular movement. Some agents are not given a preferred destination and are
 * considered uninformed.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class SueurSimpleAngularUninformedAgentBuilder implements AgentBuilder
{
    /** Starting locations */
    private List<Point2D> _locations = null;

    /** Destinations */
    private Point2D[] _destinations = null;

    /** The simstate */
    private SimulationState _simState = null;

    /** Number of agents to create */
    private int _numAgents = 0;

    /** The locations file used */
    private String _locationsFile = null;

    /** The destinations file used */
    private String _destinationsFile = null;

    /** The radius of destinations */
    private int _destinationRadius = 10;

    @Override
    public void initialize( SimulationState simState )
    {
        _simState = simState;

        // get values from properties file
        String numAgents = _simState.getProperties().getProperty(
                "individual-count" );
        Validate.notEmpty( numAgents, "Individual count may not be empty" );
        _numAgents = Integer.parseInt( numAgents );

        _locationsFile = _simState.getProperties().getProperty(
                "locations-file" );
        Validate.notEmpty( _locationsFile, "Locations file may not be empty" );

        _destinationsFile = _simState.getProperties().getProperty(
                "destinations-file" );
        Validate.notEmpty( _destinationsFile,
                "Destinations file may not be empty" );

        String stringDestinationRadius = _simState.getProperties().getProperty(
                "destination-size-radius" );
        Validate.notEmpty( stringDestinationRadius,
                "Desination-size-radius must have a value" );
        _destinationRadius = Integer.parseInt( stringDestinationRadius );

        _locations = Utils.readPoints( _locationsFile );
        _destinations = Utils.readPoints( _destinationsFile, _numAgents );

        // add the agent count info to root directory
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY + "agent-count="
                + _numAgents + "_" );
    }

    @Override
    public List<Agent> build()
    {
        List<Agent> agents = new ArrayList<Agent>();
        // build them
        DecisionProbabilityCalculator temp = (DecisionProbabilityCalculator) MiscUtils.loadAndInstantiate(
                _simState.getProperties().getProperty( "decision-calculator" ),
                "Decision Probability Calculator Class" );
        temp.initialize( _simState );
        for( int i = 0; i < _numAgents; i++ )
        {
            agents.add( new Agent( temp ) );
        }

        /** Array of 70 unique colors to use for destinations */
        Color[] colors = { new Color( 0xFF0000 ), new Color( 0x9ACD32 ),
                new Color( 0xFFFF00 ), new Color( 0xF5DEB3 ),
                new Color( 0xEE82EE ), new Color( 0x40E0D0 ),
                new Color( 0xFF6347 ), new Color( 0xD8BFD8 ),
                new Color( 0x008080 ), new Color( 0x4682B4 ),
                new Color( 0x00FF7F ), new Color( 0x708090 ),
                new Color( 0x6A5ACD ), new Color( 0x87CEEB ),
                new Color( 0xC0C0C0 ), new Color( 0xA0522D ),
                new Color( 0x2E8B57 ), new Color( 0xF4A460 ),
                new Color( 0xFA8072 ), new Color( 0x8B4513 ),
                new Color( 0x4169E1 ), new Color( 0xBC8F8F ),
                new Color( 0x000000 ), new Color( 0x800080 ),
                new Color( 0xB0E0E6 ), new Color( 0xDDA0DD ),
                new Color( 0xFFC0CB ), new Color( 0xCD853F ),
                new Color( 0xFFDAB9 ), new Color( 0xFFEFD5 ),
                new Color( 0xDB7093 ), new Color( 0x98FB98 ),
                new Color( 0xEEE8AA ), new Color( 0xDA70D6 ),
                new Color( 0xFF4500 ), new Color( 0xFFA500 ),
                new Color( 0x6B8E23 ), new Color( 0x000080 ),
                new Color( 0xFFDEAD ), new Color( 0xF0A0AA ),
                new Color( 0x191970 ), new Color( 0xC71585 ),
                new Color( 0x48D1CC ), new Color( 0x00FA9A ),
                new Color( 0x7B68EE ), new Color( 0x3CB371 ),
                new Color( 0x0000CD ), new Color( 0x66CDAA ),
                new Color( 0x800000 ), new Color( 0x32CD32 ),
                new Color( 0x00FF00 ), new Color( 0xFFFFE0 ),
                new Color( 0xB0C4DE ), new Color( 0x778899 ),
                new Color( 0x87CEFA ), new Color( 0x20B2AA ),
                new Color( 0xFFA07A ), new Color( 0xFFB6C1 ),
                new Color( 0x90EE90 ), new Color( 0xD3D3D3 ),
                new Color( 0xFAFAD2 ), new Color( 0xE0FFFF ),
                new Color( 0xF08080 ), new Color( 0xADD8E6 ),
                new Color( 0x7CFC00 ), new Color( 0x4B0082 ),
                new Color( 0xFF69B4 ), new Color( 0xFFD700 ),
                new Color( 0x1E90FF ), new Color( 0x8FBC8F ) };

        // holds the count of agents that prefer each destination
        Map<Vector2D, Integer> destinationCounts = new HashMap<Vector2D, Integer>();
        // holds the colors assigned to each destination
        Map<Vector2D, Color> destinationColors = new HashMap<Vector2D, Color>();
        // holds the index for colors used to assign destination colors
        Map<Color, Integer> destinationIds = new HashMap<Color, Integer>();
        // the current index for colors
        int colorCount = 0;

        // initialize them
        for( int i = 0; i < _numAgents; i++ )
        {
            Agent tempAgent = agents.get( i );
            // create new movement behavior instance
            MovementBehavior mb = new SimpleAngularMovement();
            // Initialize the agent
            tempAgent.initialize( _simState, _locations.get( i ) );

            // get the number of informed individuals as defined by file name
            int informedCount = 0;
            // create the pattern and matcher
            Pattern pattern = Pattern.compile( "(split-poles-)([0-9]+)" );
            Matcher matcher = pattern.matcher( _destinationsFile );
            // if we have a match
            if( matcher.find() )
            {
                // get the informed individual per pole count in group 2
                informedCount = Integer.parseInt( matcher.group( 2 ) );
                // multiply it by 2 to get our total number of informed agents
                informedCount *= 2;
            }

            // set their destination if they have one
            if( i < informedCount )
            {
                Vector2D agentDestination = new Vector2D(
                        _destinations[i].getX(), _destinations[i].getY() );
                Color destinationColor = null;
                // set their color for their destination
                // if new destination then give it new color
                if( destinationColors.containsKey( agentDestination ) )
                {
                    // assign its color from the map
                    destinationColor = destinationColors.get( agentDestination );
                    // increment agent count going to this destination
                    destinationCounts.put( agentDestination,
                            destinationCounts.get( agentDestination ) + 1 );
                }
                // not a new destination, give it the color other's have been
                // assigned
                else
                {
                    // add new destination
                    _simState.addDestination( agentDestination );
                    // assign color and set counts to 0
                    destinationColor = colors[colorCount];
                    destinationCounts.put( agentDestination, 0 );
                    destinationColors.put( agentDestination, destinationColor );
                    destinationIds.put( destinationColor, colorCount );
                    // increment color count
                    colorCount++;
                }
                String destinationID = "D-"
                        + destinationIds.get( destinationColor );

                // create a new preferred destination and give it to agent
                Destination agentPreferredDestination = new Destination(
                        destinationID, true, agentDestination,
                        destinationColor, _destinationRadius );
                tempAgent.setPreferredDestination( agentPreferredDestination );
            }

            // the agent name formatted for the position reporter
            String agentName = tempAgent.getId().toString();
            agentName = agentName.replaceAll( "Agent", "" );
            agentName = "Ind"
                    + String.format( "%05d", Integer.parseInt( agentName ) );

            // the header for the position reporter
            tempAgent.setPositionReportHeader( "world-object-name=" + agentName
                    + "\n" + "team-name="
                    + tempAgent.getPreferredDestinationId() + "\n"
                    + "collision-bounding-radius="
                    + ( Agent.AGENT_DIAMETER / 2 ) + "\n\n" + "position="
                    + tempAgent.getInitialLocation().getX() + ","
                    + tempAgent.getInitialLocation().getY() + ",0\n" );

            // set and initialize movement behavior
            tempAgent.setMovementBehavior( mb );
            mb.initialize( tempAgent );
        }

        // set the good and bad destinations
        for( int i = 0; i < agents.size(); i++ )
        {
            // if there is more than one agent going to a destination it is good
            if( agents.get( i ).getPreferredDestination() != null
                    && destinationCounts.get( agents.get( i ).getPreferredDestination().getVector() ) > 1 )
            {
                agents.get( i ).getPreferredDestination().setIsGood( true );
            }
            else if( agents.get( i ).getPreferredDestination() == null )
            {
                agents.get( i ).setPreferredDestination(
                        _simState.noneDestination );
            }
        }

        return agents;
    }
}
