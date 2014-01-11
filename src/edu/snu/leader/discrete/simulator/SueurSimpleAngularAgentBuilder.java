package edu.snu.leader.discrete.simulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.behavior.MovementBehavior;
import edu.snu.leader.discrete.behavior.SimpleAngularMovement;
import edu.snu.leader.discrete.utils.Reporter;
import edu.snu.leader.discrete.utils.Utils;
import edu.snu.leader.util.MiscUtils;


public class SueurSimpleAngularAgentBuilder implements AgentBuilder
{
    private Point2D[] _locations = null;

    private Point2D[] _destinations = null;

    private SimulationState _simState = null;

    private int _numAgents = 0;

    private String _locationsFile = null;

    private String _destinationsFile = null;

    @Override
    public void initialize( SimulationState simState )
    {
        _simState = simState;

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

        _locations = Utils.readPoints( _locationsFile, _numAgents );
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

        // supports up to 10 destinations currently
        /** Array of 70 unique colors to use for destinations */
        Color[] colors = { new Color( 0x000000 ),
                new Color( 0x9ACD32 ), new Color( 0xFFFF00 ),
                new Color( 0xF5DEB3 ), new Color( 0xEE82EE ),
                new Color( 0x40E0D0 ), new Color( 0xFF6347 ),
                new Color( 0xD8BFD8 ), new Color( 0x008080 ),
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
        
        
        Map<Vector2D, Color> destinationColors = new HashMap<Vector2D, Color>();
        Map<Color, Integer> destinationIds = new HashMap<Color, Integer>();
        int colorCount = 0;
        // initialize them
        for( int i = 0; i < _numAgents; i++ )
        {
            Agent tempAgent = agents.get( i );
            MovementBehavior mb = new SimpleAngularMovement();
            tempAgent.initialize( _simState, _locations[i] );
            // set their destination
            tempAgent.setPreferredDestination(
                    new Vector2D( _destinations[i].getX(),
                            _destinations[i].getY() ) );
            Color destinationColor = null;
            // set their color for their destination
            // if new destination then give it new color
            if( destinationColors.containsKey( tempAgent.getPreferredDestination() ) )
            {
                destinationColor = destinationColors.get( tempAgent.getPreferredDestination() );
                tempAgent.setDestinationColor( destinationColor );
            }
            // not a new destination, give it the color other's have been
            // assigned
            else
            {
                destinationColor = colors[colorCount];
                destinationColors.put( tempAgent.getPreferredDestination(), destinationColor );
                destinationIds.put( destinationColor, colorCount );
                tempAgent.setDestinationColor( destinationColor );
                colorCount++;
            }
            tempAgent.setPreferredDestinationId( "D-" + destinationIds.get( destinationColor ) );
            // set and initialize movement behavior
            tempAgent.setMovementBehavior( mb );
            mb.initialize( tempAgent );
        }
        return agents;
    }

}
