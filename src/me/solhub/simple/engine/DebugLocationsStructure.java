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

package me.solhub.simple.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import edu.snu.leader.discrete.behavior.Decision.DecisionType;
import edu.snu.leader.discrete.simulator.Agent;
import edu.snu.leader.discrete.simulator.AgentBuilder;
import edu.snu.leader.discrete.simulator.Destination;
import edu.snu.leader.discrete.simulator.Group;
import edu.snu.leader.discrete.simulator.Predator;
import edu.snu.leader.discrete.simulator.SimulationState;
import edu.snu.leader.discrete.simulator.Simulator;
import edu.snu.leader.discrete.utils.Reporter;
import edu.snu.leader.discrete.utils.Utils;
import edu.snu.leader.util.MiscUtils;

//TODO anti-aliasing
public class DebugLocationsStructure extends AbstractGameStructure
{
    /** The delay for the live animations */
    private int LIVE_DELAY = 30;
    private int inputDelay = 10;
    private int inputDelayCount = 0;

    /** Whether pngs should be made for every time step or not */
    private final boolean SHOULD_VIDEO = false;

    /** Zoom factor */
    private final double _zoom = 2;
    /** the x offset for moving the agents and destinations on screen so they can be seen when zooming */
    private int _xOffset = _windowWidth / 3;
    /** the y offset for moving the agents and destinations on screen so they can be seen when zooming */
    private int _yOffset = _windowHeight / 3;
    /** The diameter of the agent circles */
    private final int _agentSize = Agent.AGENT_DIAMETER;
    /** The diameter of the destination circles */
    private final int _destinationSize = 3;
    /** The x offset for the font */
    private final int _fontXOffset = 30;
    /** The y offset for the font */
    private final int _fontYOffset = 12;
    /** The font size */
    private final int _fontSize = 10;
    /** The font type */
    private Font _infoFont = new Font("InfoFont", Font.BOLD, _fontSize);
    
    //for global and one initiator screen output
    /** The agent that started the initiation (For global and one initiator) */
    private Agent _initiatingAgent = null;
    /** The number of agents following the initiator (For global and one initiator) */
    private int _numberFollowing = 0;
    
    private Map<Vector2D, Color> _destinationColors = new HashMap<Vector2D, Color>();
    
    /** The number of occurrences of each group size at the end of a simulation */
//    private static int[] _groupSizeCounts = null;

    /**  */
    private static final long serialVersionUID = 1L;

    // private EpsGraphics2D _eps = new EpsGraphics2D("Test");
    private InputHandler _input = null;

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( Simulator.class.getName() );

    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the agent builder class name */
    private static final String _AGENT_BUILDER_CLASS = "agent-builder";

    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** The agent builder */
    private AgentBuilder _agentBuilder = null;

    /** For adhesion time limits */
    private static int _lastJoinedAgentTime = 0;

    /** Adhesion time limit */
    private int _adhesionTimeLimit = 0;

    /** The number of times an initiator reached his cancellation threshold */
//    private static int _successCount = 0;
    
    /**
     * Run the simulation after initialization
     */
    public void execute()
    {
        _LOG.trace( "Entering execute()" );

        while( _simState.getCurrentSimulationRun() < _simState.getSimulationRunCount() )
        {
            executeRun();
        }

        _LOG.trace( "Leaving execute()" );
    }

    private void executeRun()
    {
        _LOG.trace( "Entering executeRun()" );
        Iterator<Agent> agentIterator = null;
        while( isSimActive() )
        {
            handleColorSwitchingInput();
            
            // _LOG.trace("Making decisions");
            // make decisions
            agentIterator = _simState.getAgentIterator();
            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();
                if(temp.isAlive()){
                    temp.makeDecision();
                }
            }
            // _LOG.trace("Finished making decisions");

            // _LOG.trace("Executing decisions");
            // execute decisions
            agentIterator = _simState.getAgentIterator();
            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();
                if(temp.isAlive()){
                    temp.execute();
                }
            }
            // _LOG.trace("Finished executing decisions");

            // update traits
            agentIterator = _simState.getAgentIterator();
            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();
                if(temp.isAlive()){
                    temp.update();
                }
            }
            
            if(_simState.isPredatorEnabled()){
                _simState.getPredator().hunt();
            }

            // _LOG.trace("Setting up next simulation run step");
            // setup next run step
            draw();
            _simState.setupNextSimulationRunStep();
            try
            {
                Thread.sleep( LIVE_DELAY );
            }
            catch( InterruptedException e )
            {
                e.printStackTrace();
            }
            // _LOG.trace("Finished setting up next simulation run step");
        }
        draw();
        waitForEnterKey();
        _LOG.trace( "Setting up next simulation run" );
        // setup next simulation run
        _simState.setupNextSimulationRun();
        _simState.numReachedDestination = 0;
        _LOG.trace( "Finished setting up next simulation run" );

        _lastJoinedAgentTime = 0;

        _LOG.trace( "Leaving executeRun()" );
    }

    private void buildAgents()
    {
        _LOG.trace( "Entering buildAgents()" );

        List<Agent> agents = _agentBuilder.build();
        for( int i = 0; i < agents.size(); i++ )
        {
            _simState.addAgent( agents.get( i ) );
            _destinationColors.put( agents.get( i ).getPreferredDestination().getVector(),
                    agents.get( i ).getDestinationColor() );
        }

        _LOG.trace( "Leaving buildAgents()" );
    }

    /**
     * This is used to reset lastJoinedAgentTime when using an adhesion time
     * limit
     * 
     * @param joined Whether an agent joined or not
     */
    public static void agentMoved()
    {
        _lastJoinedAgentTime = 0;
    }

//    /**
//     * Gets the number of agents that were able to reach their cancellation
//     * threshold after initiating
//     * 
//     * @return The success count
//     */
//    public static int getSuccessCount()
//    {
//        return _successCount;
//    }

    private Agent initiationAgent = null;
    /**
     * Returns a flag denoting whether or not the simulation run is still active
     * 
     * @return A flag denoting whether or not the simulation run is still active
     */
    private boolean isSimActive()
    {
        boolean isActive = false;

        // if global and only one can initiate then use adhesion time limit
        if( _simState.getCommunicationType().equals( "global" )
                && !Agent.canMultipleInitiate() )
        {
            isActive = true;
            int groupCount = 0;
            Iterator<Agent> agentIterator = _simState.getAgentIterator();

            while( agentIterator.hasNext() )
            {
                Agent temp = agentIterator.next();

                if( temp.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.FOLLOW ) )
                {
                    groupCount++;
                }
                if( temp.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.INITIATION ) )
                {
                    groupCount++;
                    initiationAgent = temp;
                }
                // if an agent is canceling then this simulation is finished
                if( temp.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.CANCELLATION ) )
                {
                    // temp.endOfInitiation( false, groupCount );
                    // isActive = false;
                    // break;
                    groupCount++;
                }
            }
            // if we have an initiator
            if( initiationAgent != null )
            {
                // if last joined time is greater than the adhesion time limit
                // then
                // this run is done
                if( _lastJoinedAgentTime > _adhesionTimeLimit )
                {
                    System.out.println("Adhesion Time Exit");
                    initiationAgent.endOfInitiation( false, groupCount );
                    isActive = false;
                }
                if( initiationAgent.getCurrentDecision().getDecision().getDecisionType().equals(
                        DecisionType.CANCELLATION ) )
                {
                    initiationAgent.endOfInitiation( false, groupCount );
                    isActive = false;
                }
                if( groupCount / (float) _simState.getAgentCount() >= initiationAgent.getCancelThreshold() )
                {
                    _simState.successCount++;
                    initiationAgent.endOfInitiation( true, groupCount );
                    isActive = false;
                }
                _lastJoinedAgentTime++;
            }
            if( groupCount >= _simState.getAgentCount() )
            {
                isActive = false;
            }

            if( !isActive )
            {
                _simState.groupSizeCounts[groupCount]++;
//                _groupSizeCounts[groupCount]++;
            }
        }
        // do the simulation for as many time steps as there are
        else if( _simState.getSimulationTime() < _simState.getMaxSimulationTimeSteps() )
        {
            isActive = true;
            if(_simState.numReachedDestination >= _simState.getAgentCount() - _simState.getPredator().getTotalAgentsEaten()){
                _simState.successCount++;
                isActive = false;
            }
        }

        return isActive;
    }

    public DebugLocationsStructure( String title,
            int windowWidth,
            int windowHeight,
            int fps )
    {
        super( title, windowWidth, windowHeight, fps );
    }

    public void initialize(Properties properties, long randomSeedOverride){
        _props = properties;
        
        _props.put( "random-seed-override", String.valueOf( randomSeedOverride ) );
    }
    
    @Override
    protected void initialize()
    {
        _input = new InputHandler( this );

        _LOG.trace( "Entering initialize()" );

        // Load the properties
//        _props = MiscUtils.loadProperties( _PROPS_FILE_KEY );
        
        // Initialize the simulation state
        _simState.initialize( _props );
        
        String liveDelay = _simState.getProperties().getProperty(
                "live-delay" );
        Validate.notEmpty( liveDelay,
                "Live delay may not be empty" );
        LIVE_DELAY = Integer.parseInt( liveDelay );

        String adhesionTimeLimit = _simState.getProperties().getProperty(
                "adhesion-time-limit" );
        Validate.notEmpty( adhesionTimeLimit,
                "Adhesion time limit may not be empty" );
        _adhesionTimeLimit = Integer.parseInt( adhesionTimeLimit );

        // add the adhesion time limit info to root directory
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY
                + "adhesion-time-limit=" + _adhesionTimeLimit + "_" );

        // Load and instantiate the agent builder
        String agentBuilderClassName = _props.getProperty( _AGENT_BUILDER_CLASS );
        Validate.notEmpty( agentBuilderClassName,
                "Agent builder class name (key=" + _AGENT_BUILDER_CLASS
                        + ") may not be empty" );
        _agentBuilder = (AgentBuilder) MiscUtils.loadAndInstantiate(
                agentBuilderClassName, "Agent builder class name" );

        _agentBuilder.initialize( _simState );

        // Build the agents
        buildAgents();
        
        // create the predator
        Predator predator = new Predator("PredDebug");
        predator.initialize( _simState );
        _simState.setPredator( predator );
        
//        _groupSizeCounts = new int[_simState.getAgentCount() + 1];
//        _simState.groupSizeCounts = n

        _LOG.trace( "Exiting initialize()" );
    }

    @Override
    protected void update()
    {
        while( _simState.getCurrentSimulationRun() < _simState.getSimulationRunCount() )
        {
            executeRun();
            _initiatingAgent = null;
            _numberFollowing = 0;
        }
    }

    boolean isDestinationColors = false;

    int pulseWhite = 0;

    @Override
    protected void draw()
    {
        Graphics2D g = (Graphics2D) getGraphics();
        Graphics2D bbg = (Graphics2D) _backBuffer.getGraphics();
          
        //anti-aliasing code
//        bbg.setRenderingHint(
//                RenderingHints.KEY_TEXT_ANTIALIASING,
//                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        bbg.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        bbg.setColor( Color.WHITE );
        bbg.fillRect( 0, 0, _windowWidth, _windowHeight );

        bbg.translate( _xOffset, _yOffset );
        
        // draw destinations
        bbg.setColor( Destination.startingDestination.getColor() );
        bbg.drawOval( -(int)Destination.startingDestination.getRadius(), -(int)Destination.startingDestination.getRadius(), (int)Destination.startingDestination.getRadius() * 2, (int)Destination.startingDestination.getRadius() * 2 );
        Iterator<Entry<Vector2D, Color>> blah = _destinationColors.entrySet().iterator();
        double destinationRadius = _simState.getDestinationRadius();
        while( blah.hasNext() )
        {
            Entry<Vector2D, Color> temp = blah.next();
            bbg.setColor( temp.getValue() );
            //calculate center coordinate
            int x = (int) ( temp.getKey().getX() - (destinationRadius) );
            int y = (int) ( temp.getKey().getY() - (destinationRadius) );
            //drawOval draws a circle inside a rectangle
            bbg.drawOval( x, y, _simState.getDestinationRadius() * 2, _simState.getDestinationRadius() * 2);
        }

        // draw each of the agents
        Iterator<Agent> agentIter = _simState.getAgentIterator();
        while( agentIter.hasNext() )
        {
            Agent temp = agentIter.next();
            if(temp.isAlive()){
                // decide whether to color for destination or group
//                if( isDestinationColors )
//                {
//                    // if stopped then blink white and destination color
//                    if( temp.hasReachedDestination() )
//                    {
//                        if( pulseWhite % 20 == 0 )
//                        {
//                            bbg.setColor( Color.WHITE );
//                        }
//                        else
//                        {
//                            bbg.setColor( temp.getDestinationColor() );
//                        }
//                    }
//                    else
//                    {
//                        bbg.setColor( temp.getDestinationColor() );
//                    }
//                }
//                else
//                {
//                    // if stopped then blink black and white
//                    if( temp.hasReachedDestination() )
//                    {
//                        if( pulseWhite % 20 == 0 )
//                        {
//                            bbg.setColor( Color.WHITE );
//                        }
//                        else
//                        {
//                            bbg.setColor( temp.getGroup().getGroupColor() );
//                        }
//                    }
//                    //set color to red if cancelled and global and not multiple initiators
//                    else if(temp.getCurrentDecision().getDecision().getDecisionType().equals(
//                            DecisionType.CANCELLATION ) 
//                            && _simState.getCommunicationType().equals( "global" )
//                            && !Agent.canMultipleInitiate()
//                            )
//                    {
//                        bbg.setColor( Color.RED );
//                    }
//                    else
//                    {
//                        bbg.setColor( temp.getGroup().getGroupColor() );
//                    }
//                }
                
                double dx = temp.getCurrentDestination().getX() - temp.getCurrentLocation().getX();
                double dy = temp.getCurrentDestination().getY() - temp.getCurrentLocation().getY();
                double heading = Math.atan2( dy,  dx );
                Utils.drawDirectionalTriangle( bbg, heading - Math.PI /2, temp.getCurrentLocation().getX(), temp.getCurrentLocation().getY(), 7, temp.getPreferredDestination().getColor(), temp.getGroup().getGroupColor() );
                
//                bbg.fillOval( (int) temp.getCurrentLocation().getX() - _agentSize,
//                        (int) temp.getCurrentLocation().getY() - _agentSize , _agentSize * 2, _agentSize * 2 );
            }
        }
        pulseWhite++;
        bbg.setColor( Color.BLACK );
        // the total number of groups
        bbg.setFont( _infoFont );
        
        bbg.drawString( "Run: " + (_simState.getCurrentSimulationRun() + 1), _fontXOffset, _fontYOffset );
        bbg.drawString( "Time: " + _simState.getSimulationTime(), _fontXOffset, _fontYOffset + _fontSize );
        bbg.drawString( "Delay: " + LIVE_DELAY, _fontXOffset, _fontYOffset + _fontSize * 2 );
        
        if( _simState.getCommunicationType().equals( "global" )
                && !Agent.canMultipleInitiate() )
        {
            String initiatorName = "None";
            if(_initiatingAgent != null){
                initiatorName = _initiatingAgent.getId().toString();
            }
            bbg.drawString( "Init: " + initiatorName, _fontXOffset, _fontYOffset + _fontSize * 3 );
            bbg.drawString( "Followers: " + _numberFollowing, _fontXOffset, _fontYOffset + _fontSize * 4 );
        }
        else{
            bbg.drawString( "Groups: " + _simState.getNumberGroups(), _fontXOffset, _fontYOffset + _fontSize * 3);
            bbg.drawString( "Reached: " + _simState.numReachedDestination, _fontXOffset, _fontYOffset + _fontSize * 4 );
            bbg.drawString( "Inits: " + _simState.numInitiating, _fontXOffset, _fontYOffset + _fontSize * 5 );
            bbg.drawString( "Eaten: " + _simState.getPredator().getTotalAgentsEaten(), _fontXOffset, _fontYOffset + _fontSize * 6 );
        }
        
        
        g.scale( _zoom, _zoom );
        g.drawImage( _backBuffer, 0, 0, this );
        if( SHOULD_VIDEO )
        {

            // setup to save to a png
            BufferedImage buff = new BufferedImage( _windowWidth,
                    _windowHeight, BufferedImage.TYPE_INT_RGB );
            Graphics2D temp = (Graphics2D) buff.getGraphics();
            temp.scale( 8, 4 );
            temp.drawImage( _backBuffer, 0, 0, this );
            // sub-directory
            File dir = new File( "video" );
            dir.mkdir();
            // format string for filename
            String filename = String.format( "video/run-%03d-time-%05d.png",
                    _simState.getCurrentSimulationRun(),
                    _simState.getSimulationTime() );
            File outputfile = new File( filename );
            // save it
            try
            {
                ImageIO.write( buff, "png", outputfile );
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    
    @Override
    protected boolean isRunning()
    {
        return true;
    }
    
    

    /**
     * Switches the color of the agent from destination to group or vice versa
     * whenever the space bar is pressed
     */
    private void handleColorSwitchingInput()
    {
        if( _input.isKeyDown( KeyEvent.VK_SPACE ) && inputDelayCount > inputDelay)
        {
            isDestinationColors = !isDestinationColors;
            inputDelayCount = 0;
        }
        if( _input.isKeyDown( KeyEvent.VK_PERIOD )){
            LIVE_DELAY += 1;
            if(LIVE_DELAY < 0){
                LIVE_DELAY = 0;
            }
        }
        if( _input.isKeyDown( KeyEvent.VK_COMMA )){
            LIVE_DELAY -= 1;
            if(LIVE_DELAY < 0){
                LIVE_DELAY = 0;
            }
        }
        if(_input.isKeyDown( KeyEvent.VK_UP )){
            _yOffset += 1;
        }
        if(_input.isKeyDown( KeyEvent.VK_DOWN )){
            _yOffset -= 1;
        }
        if(_input.isKeyDown( KeyEvent.VK_RIGHT )){
            _xOffset -= 1;
        }
        if(_input.isKeyDown( KeyEvent.VK_LEFT )){
            _xOffset += 1;
        }
        if( _input.isKeyDown( KeyEvent.VK_ENTER )){
            while( !_input.isKeyDown( KeyEvent.VK_SHIFT )){
                //this makes it work, without this line it will not work as desired
//                System.out.println(_input.isKeyDown( KeyEvent.VK_ENTER ));
                draw();
                handleColorSwitchingInput();
            }
        }
        inputDelayCount++;
    }
    
    private void waitForEnterKey()
    {
        //TODO figure out how this works o.O
        while( !_input.isKeyDown( KeyEvent.VK_ENTER )){
            //this makes it work, without this line it will not work as desired
//            System.out.println(_input.isKeyDown( KeyEvent.VK_ENTER ));
            draw();
        }
    }

}
