package edu.snu.leader.discrete.simulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.behavior.*;
import edu.snu.leader.discrete.behavior.Decision.DecisionType;
import edu.snu.leader.discrete.utils.Utils;


/**
 * Agent class
 * 
 * @author Tim Solum
 */
public class Agent
{
    /** The number of agents that have made the stop decision */
    public static int numReachedDestination = 0;

    /** The number of agents that have made the initiate decision */
    public static int numInitiating = 0;

    /** Used to make the unique id for every agent */
    private static int _uniqueIdCount = 0;

    private int _numberTimesInitiated = 0;

    private int _numberTimesSuccessful = 0;

    private List<InitiationHistoryEvent> _initiationHistory = null;

    private InitiationHistoryEvent _currentInitiationHistoryEvent = null;
    
    /**
     * This is used across agents to make sure only one initiates...only
     * slightly hacky
     */
    private static boolean _canInitiate = true;

    /** Boolean variable for whether or not multiple initiators are allowed */
    private static boolean _canMultipleInitiate = true;

    // ////////General Agent information\\\\\\\\\\
    /** The ID for this Agent */
    private Object _id = null;

    /** The simulation state */
    private SimulationState _simState = null;

    // ////////Behavior variables\\\\\\\\\\
    /** The modifier for personality each update */
    private float _lambda = 0.0f;

    /** The personality trait of the Agent */
    private PersonalityTrait _personalityTrait = null;

    /** The conflict trait of the Agent */
    private ConflictTrait _conflictTrait = null;

    /** The way this Agent moves */
    private MovementBehavior _movementBehavior = null;

    /** The current decision event of this Agent */
    private DecisionEvent _currentDecision = null;

    private List<DecisionEvent> _decisionHistory = null;
    
    private DecisionProbabilityCalculator _decisionCalc = null;

    /** The cancellation threshold */
    private double _cancellationThreshold = 1.0;

    // ////////2D movement variables\\\\\\\\\\
    /** Preferred destination of this agent */
    private Vector2D _preferredDestination = Vector2D.ZERO;
    
    /** The id of the preferred destination */
    private String _destinationId = null;

    /** Current destination of this agent */
    private Vector2D _currentDestination = Vector2D.ZERO;

    /** Initial location of this agent */
    private Vector2D _initialLocation = Vector2D.ZERO;

    /** Current location of this agent */
    private Vector2D _currentLocation = Vector2D.ZERO;

    /** Current velocity of this agent */
    private Vector2D _currentVelocity = Vector2D.ZERO;

    private double _speed = .1;

    private Color _destinationColor = null;

    // ////////Agent interaction variables\\\\\\\\\
    /** A list of all of the current Agents following this Agent */
    private Group _group = Group.NONE;

    /**
     * The history of all groups and their members that have been observed by
     * this Agent <agentId, groupId>
     */
    private Map<Object, ObservedGroupTime> _observedGroupHistory = null;

    private boolean _hasReachedDestination = false;
    
    /** The leader of this Agent if it has one */
    private Agent _leader = this;

    /** Radius for metric neighbor discovery */
    private double _maxLocationRadius = 0;

    /** Count for topological neighbor discovery */
    private int _nearestNeighborCount = 0;

    /** String for topological or metric */
    private String _communicationType = null;

    /** Whether a new decision was made or not */
    private boolean _hasNewDecision = false;

    /** Whether or not we should pre calculate the decision probabilities */
    private boolean _preCalcProbs = false;
    
    /**
     * Builds an Agent disregarding Personality and Conflict
     * 
     * @param dpc The DecisionProbabilityCalculator
     */
    Agent( DecisionProbabilityCalculator dpc )
    {
        this( new DefaultPersonalityTrait(), new DefaultConflictTrait(), dpc );
    }

    /**
     * Builds this Agent object with a default conflict trait
     * 
     * @param pt The Agent's PersonalityTrait
     * @param dpc The DecisionProbabilityCalculator for this agent
     */
    Agent( PersonalityTrait pt, DecisionProbabilityCalculator dpc )
    {
        this( pt, new DefaultConflictTrait(), dpc );
    }

    /**
     * Builds an Agent
     * 
     * @param pt The PersonalityTrait
     * @param ct The ConflcitTrait
     * @param dpc The DecisionProbabilityCalculator
     */
    Agent( PersonalityTrait pt,
            ConflictTrait ct,
            DecisionProbabilityCalculator dpc )
    {
        _id = "Agent" + _uniqueIdCount++;
        _personalityTrait = pt;
        _conflictTrait = ct;
        _decisionCalc = dpc;
        _observedGroupHistory = new HashMap<Object, ObservedGroupTime>();
        _decisionHistory = new LinkedList<DecisionEvent>();
        _initiationHistory = new LinkedList<InitiationHistoryEvent>();
    }

    /**
     * Initializes Agent
     * 
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState, Point2D initialLocation )
    {
        _simState = simState;

        _initialLocation = new Vector2D( initialLocation.getX(),
                initialLocation.getY() );

        String nearestNeighborCount = _simState.getProperties().getProperty(
                "nearest-neighbor-count" );
        Validate.notEmpty( nearestNeighborCount,
                "Nearest neighbor count may not be empty" );
        _nearestNeighborCount = Integer.parseInt( nearestNeighborCount );

        String maxLocationRadius = _simState.getProperties().getProperty(
                "max-location-radius" );
        Validate.notEmpty( maxLocationRadius,
                "Max location raidus may not be empty" );
        _maxLocationRadius = Double.parseDouble( maxLocationRadius );

        String canMultipleInitiate = _simState.getProperties().getProperty(
                "can-multiple-initiate" );
        Validate.notEmpty( canMultipleInitiate,
                "Can multiple initiate may not be empty" );
        _canMultipleInitiate = Boolean.parseBoolean( canMultipleInitiate );

        String cancellationThreshold = _simState.getProperties().getProperty(
                "cancellation-threshold" );
        Validate.notEmpty( cancellationThreshold,
                "Use cancellation threshold may not be empty" );
        _cancellationThreshold = Double.parseDouble( cancellationThreshold );

        String lambda = _simState.getProperties().getProperty( "lambda" );
        Validate.notEmpty( lambda, "Lambda may not be empty" );
        _lambda = Float.parseFloat( lambda );
        
        String preCalcProbs = _simState.getProperties().getProperty( "pre-calculate-probabilities" );
        Validate.notEmpty( preCalcProbs, "pre-calculate-probabilities may not be empty" );
        _preCalcProbs  = Boolean.parseBoolean( preCalcProbs );
        
        _communicationType = _simState.getCommunicationType();

        reset();

        _personalityTrait.initialize( this );
    }

    /** Resets the Agent for the next simulation run */
    public void reset()
    {
        // reset location stuff
        _currentLocation = _initialLocation;
        _currentVelocity = Vector2D.ZERO;

        // readd to group NONE
        _group = Group.NONE;
        Group.NONE.addAgent( this, 0 );

        // clear histories and reporters
        _observedGroupHistory.clear();
        _decisionHistory.clear();
        _currentDecision = new DecisionEvent( new DoNothing( this, this ), 0 );

        // reset leader and ability to initiate
        _leader = this;
        _canInitiate = true;
        _hasReachedDestination = false;

        _uniqueIdCount = 0;
    }

    /**
     * The agent decides how to move
     */
    public void makeDecision()
    {
        if(!hasReachedDestination()){
            // whether or not there was a do nothing decision in list of
            // decisions
            boolean isAbleToDoNothing = false;
            // place holder of the do nothing decision of where it was in the
            // list
            // of decisions if there was one
            int doNothingIndex = 0;
    
            // get nearest neighbors
            List<Agent> neighbors = getNearestNeighbors();
    
            // update observed group history
            for( int i = 0; i < neighbors.size(); i++ )
            {
                addObservedGroupMember( neighbors.get( i ) );
            }
    
            // calculate probabilities and report them
            double sum = 0.0;
            List<Decision> possibleDecisions = generatePossibleDecisions();
    
            for( int i = 0; i < possibleDecisions.size(); i++ )
            {
                Decision decision = possibleDecisions.get( i );
                if(_preCalcProbs){
                    double[] followProbs = getDecisionCalculator().getPreCalculatedFollowProbabilities();
                    double[] cancelProbs = getDecisionCalculator().getPreCalculatedCancelProbabilities();
                    // calculate initiate decision and report probability
                    if( decision.getDecisionType() == DecisionType.INITIATION )
                    {
                        getDecisionCalculator().calcInitiateProb( decision );
                        // if only one can initiate remove this decision as a
                        // possibility
                        if( !_canInitiate )
                        {
                            possibleDecisions.remove( decision );
                        }
                    }
                    // calculate follow decision and report probability
                    else if( decision.getDecisionType() == DecisionType.FOLLOW )
                    {
                        int r = 0;
                        for( int j = 0; j < neighbors.size(); j++ )
                        {
                            if( getObservedGroupHistory().get( neighbors.get( j ).getId() ).groupId == ( decision.getLeader().getGroup().getId() ) )
                            {
                                r++;
                            }
                        }
                        decision.setProbability( followProbs[r] );
                    }
                    // calculate cancellation decision and report probability
                    else if( decision.getDecisionType() == DecisionType.CANCELLATION )
                    {
                        int r = 1;
                        for( int j = 0; j < neighbors.size(); j++ )
                        {
                            if( getObservedGroupHistory().get( neighbors.get( j ).getId() ).groupId == ( decision.getLeader().getGroup().getId() ) )
                            {
                                r++;
                            }
                        }
                        decision.setProbability( cancelProbs[r] );
                    }
                    // allow for the possibility of doing nothing
                    else if( decision.getDecisionType() == DecisionType.DO_NOTHING )
                    {
                        isAbleToDoNothing = true;
                        doNothingIndex = i;
                    }
                }
                else{
                    // calculate initiate decision and report probability
                    if( decision.getDecisionType() == DecisionType.INITIATION )
                    {
                        getDecisionCalculator().calcInitiateProb( decision );
                        // if only one can initiate remove this decision as a
                        // possibility
                        if( !_canInitiate )
                        {
                            possibleDecisions.remove( decision );
                        }
                    }
                    // calculate follow decision and report probability
                    else if( decision.getDecisionType() == DecisionType.FOLLOW )
                    {
                        getDecisionCalculator().calcFollowProb( decision );
                    }
                    // calculate cancellation decision and report probability
                    else if( decision.getDecisionType() == DecisionType.CANCELLATION )
                    {
                        getDecisionCalculator().calcCancelProb( decision );
                    }
                    // allow for the possibility of doing nothing
                    else if( decision.getDecisionType() == DecisionType.DO_NOTHING )
                    {
                        isAbleToDoNothing = true;
                        doNothingIndex = i;
                    }
                }
                // add probabilities to the sum
                sum += decision.getProbability();
            }
    
            // do the math!
            // if we can do nothing
            if( isAbleToDoNothing )
            {
                double rand = Utils.getRandomNumber( _simState.getRandomGenerator(), 0, 1 );

                // if the sum is less than the random then we do nothing,
                // decision
                // does not change
                if( sum < rand )
                {
                    // _currentDecision = new
                    // DecisionEvent(possibleDecisions.get(doNothingIndex),
                    // _simState.getSimulationTime());
                    _hasNewDecision = false;
                }
                // we did not do nothing, find out what decision we did make
                else
                {
                    // removing do nothing decision is not necessary, but I did
                    // just
                    // in case
//                    possibleDecisions.remove( doNothingIndex );//TODO be careful here
                    // set the new decision
                    boolean wasInitiating = false;
                    if( _currentDecision.getDecision().getDecisionType().equals(
                            DecisionType.INITIATION ) )
                    {
                        wasInitiating = true;
                    }
                    _currentDecision = new DecisionEvent( Utils.getDecision(
                            possibleDecisions, rand ),
                            _simState.getSimulationTime() );
                    if( wasInitiating
                            && !_currentDecision.getDecision().getDecisionType().equals(
                                    DecisionType.INITIATION ) )
                    {
                        numInitiating--;
                    }
                    _hasNewDecision = true;
                }
            }
            // if we cannot do nothing
            else
            {
                double rand = Utils.getRandomNumber(
                        _simState.getRandomGenerator(), 0, sum );
                // set the new decision
                boolean wasInitiating = false;
                if( _currentDecision.getDecision().getDecisionType().equals(
                        DecisionType.INITIATION ) )
                {
                    wasInitiating = true;
                }
                _currentDecision = new DecisionEvent( Utils.getDecision(
                        possibleDecisions, rand ),
                        _simState.getSimulationTime() );
                if( wasInitiating
                        && !_currentDecision.getDecision().getDecisionType().equals(
                                DecisionType.INITIATION ) )
                {
                    numInitiating--;
                }
                _hasNewDecision = true;
            }
    
            if( _hasNewDecision )
            {
                // add the new decision to the history
                _decisionHistory.add( _currentDecision );
    
                // once an agent decides to initiate restrict the possibility of
                // others to initiate
                if( _currentDecision.getDecision().getDecisionType() == DecisionType.INITIATION
                        && _communicationType.equals( "global" )
                        && !_canMultipleInitiate )
                {
                    _canInitiate = false;
                }
            }
            
            //if it was a new decision add it to the conflict history list
            if(_hasNewDecision){
                _simState.conflictEvents.add( new ConflictHistoryEvent(_simState.getCurrentSimulationRun(), getTime(), getId().toString(), getPreferredDestinationId(), getCurrentDecision().getDecision(), possibleDecisions) );
            }
        }
    }

    /**
     * The agent moves on his decision
     */
    public void execute()
    {
        if(!hasReachedDestination()){
            if( _currentDecision.getDecision().getDecisionType() != DecisionType.INITIATION ){
                _currentDecision.getDecision().choose();
            }
            // execute the new decision if we have one
            if( _hasNewDecision )
            {
//                _currentDecision.getDecision().choose();
                if( _currentDecision.getDecision().getDecisionType() == DecisionType.INITIATION )// &&
                                                                                                 // _canInitiate
                                                                                                 // )
                {
                    _currentDecision.getDecision().choose();
                    _numberTimesInitiated++;
                    _currentInitiationHistoryEvent = new InitiationHistoryEvent();
                    _currentInitiationHistoryEvent.simRun = _simState.getCurrentSimulationRun();
                    _currentInitiationHistoryEvent.beforePersonality = getPersonalityTrait().getPersonality();
                    _simState.addGroup( _group );
                    Simulator.agentMoved();
                    numInitiating++;
                }
                else if( _currentDecision.getDecision().getDecisionType() == DecisionType.FOLLOW )
                {
                    Simulator.agentMoved();
                }
            }
            _movementBehavior.move();
        }
        _hasNewDecision = false;
    }
    
    /**
     * Updates the Agent and its traits
     */
    public void update()
    {
        _personalityTrait.update();
        _conflictTrait.update();
    }

    public Object getId()
    {
        return _id;
    }

    public Group getGroup()
    {
        return _group;
    }

    public void setGroup( Group group )
    {
        _group = group;
    }

    public Agent getLeader()
    {
        return _leader;
    }

    /**
     * Sets the leader of this Agent
     * 
     * @param agent The new leader of this Agent
     */
    public void setLeader( Agent agent )
    {
        _leader = agent;
    }

    public float getLambda()
    {
        return _lambda;
    }

    public PersonalityTrait getPersonalityTrait()
    {
        return _personalityTrait;
    }

    public ConflictTrait getConflictTrait()
    {
        return _conflictTrait;
    }

    /**
     * Gets this agent's cancellation threshold
     * 
     * @return The cancellation threshold for this agent
     */
    public double getCancelThreshold()
    {
        return _cancellationThreshold;
    }

    public DecisionEvent getCurrentDecision()
    {
        return _currentDecision;
    }

    /**
     * Returns the decision probability calculator for this agent
     * 
     * @return The decision probability calculator
     */
    public DecisionProbabilityCalculator getDecisionCalculator()
    {
        return _decisionCalc;
    }

    /**
     * Returns a list of the nearest neighbors. Can be used topologically or
     * metrically. If radius is set to 0 then its topological. If quantity is
     * set to 0 then its metric.
     * 
     * @param radius This value is used for metric
     * @param quantity This value is used for topological
     * @return List of nearest neighbors
     */
    public List<Agent> getNearestNeighbors()
    {
        List<Agent> nearest = new ArrayList<Agent>();
        Iterator<Agent> iter = _simState.getAgentIterator();
        if( _communicationType.equals( "topological" ) )
        {
            while( iter.hasNext() )
            {
                Agent temp = iter.next();
                // we don't want this agent to show up as his own neighbor
                if( temp == this )
                {
                    // temp = iter.next();
                }
                // if we do not have any just add the agent in there
                else if( nearest.size() == 0 )
                {
                    nearest.add( temp );
                }
                else
                {
                    // this boolean will let us know if it the current agent
                    // distance is the biggest out of the ones we have seen
                    boolean biggest = true;
                    // the new distance we are testing
                    double newDistance = _currentLocation.distanceSq( temp.getCurrentLocation() );
                    for( int i = 0; i < nearest.size(); i++ )
                    {
                        // old distance we have already seen
                        double oldDistance = _currentLocation.distanceSq( nearest.get(
                                i ).getCurrentLocation() );

                        // if this agents distance is closer than put it before
                        // the last one in the list
                        if( newDistance < oldDistance )
                        {
                            biggest = false;
                            nearest.add( i, temp );
                            // break out because I am lazy
                            break;
                        }
                    }
                    // if it is the biggest add it last
                    if( biggest )
                    {
                        nearest.add( temp );
                    }
                }
                // if we have too many
                if( nearest.size() > _nearestNeighborCount )
                {
                    // remove the last one off
                    nearest.remove( nearest.size() - 1 );
                }
            }

        }
        else if( _communicationType.equals( "metric" ) )
        {
            while( iter.hasNext() )
            {
                Agent temp = iter.next();
                // we don't want this agent to show up as his own neighbor
                if( temp == this )
                {
                    // temp = iter.next();
                }
                else if( _currentLocation.distance( temp._currentLocation ) < _maxLocationRadius )
                {
                    nearest.add( temp );
                }
            }
        }
        else if( _communicationType.equals( "global" ) )
        {
            while( iter.hasNext() )
            {
                Agent temp = iter.next();
                nearest.add( temp );
            }
            nearest.remove( this );
            
        }
        return nearest;
    }
    
    public Map<Object, ObservedGroupTime> getObservedGroupHistory()
    {
        return _observedGroupHistory;
    }

    /**
     * Add an observed group member to the history
     * 
     * @param agent Agent to add to observedGroupHistory
     */
    public void addObservedGroupMember( Agent agent )
    {
        _observedGroupHistory.put( agent.getId(), new ObservedGroupTime(
                agent.getGroup().getId(), getTime() ) );
    }

    public void setMovementBehavior( MovementBehavior mb )
    {
        _movementBehavior = mb;
    }

    public Vector2D getPreferredDestination()
    {
        return _preferredDestination;
    }
    
    public String getPreferredDestinationId(){
        return _destinationId;
    }

    public void setPreferredDestination( Vector2D newDestination )
    {
        _preferredDestination = newDestination;
    }
    
    public void setPreferredDestinationId( String destinationId ){
        _destinationId = destinationId;
    }

    public Vector2D getCurrentDestination()
    {
        return _currentDestination;
    }

    public void setCurrentDestination( Vector2D currentDestination )
    {
        _currentDestination = currentDestination;
    }

    public void setCurrentLocation( Vector2D currentLocation )
    {
        _currentLocation = currentLocation;
    }

    public Vector2D getCurrentLocation()
    {
        return _currentLocation;
    }

    public Vector2D getInitialLocation()
    {
        return _initialLocation;
    }

    public double getPreferredDirection()
    {
        Vector2D heading = getPreferredDestination().subtract(
                getCurrentLocation() ).normalize();
        return Math.cos( heading.getX() );
    }

    public void setCurrentVelocity( Vector2D currentVelocity )
    {
        _currentVelocity = currentVelocity;
    }

    public Vector2D getCurrentVelocity()
    {
        return _currentVelocity;
    }

    public double getSpeed()
    {
        return _speed;
    }

    public void setDestinationColor( Color color )
    {
        _destinationColor = color;
    }

    public Color getDestinationColor()
    {
        return _destinationColor;
    }

    public int getTime()
    {
        return _simState.getSimulationTime();
    }

    public int getNumberTimesInitiated()
    {
        return _numberTimesInitiated;
    }

    public List<InitiationHistoryEvent> getInitiationHistory()
    {
        return _initiationHistory;
    }
    
    public void endOfInitiation( boolean wasSuccessful, int followers )
    {
        if( wasSuccessful )
        {
            _numberTimesSuccessful++;
        }
        _currentInitiationHistoryEvent.wasSuccess = wasSuccessful;
        _currentInitiationHistoryEvent.afterPersonality = getPersonalityTrait().getPersonality();
        _currentInitiationHistoryEvent.participants = followers;
        _initiationHistory.add( _currentInitiationHistoryEvent );
    }

    public int getNumberTimesSuccessful()
    {
        return _numberTimesSuccessful;
    }
    
    public void reachedDestination(){
        if(!_hasReachedDestination){
            numReachedDestination++;
            _currentVelocity = Vector2D.ZERO;
            _simState.conflictEvents.add( new ConflictHistoryEvent(_simState.getCurrentSimulationRun(), getTime(), getId().toString(), getPreferredDestinationId(), new Reached(this), null) );
        }
        _hasReachedDestination = true;
        _hasNewDecision = false;
    }
    
    public boolean hasReachedDestination(){
        return _hasReachedDestination;
    }

    /**
     * Generates a list of possible decisions
     * 
     * @return The list of possible decisions
     */
    private List<Decision> generatePossibleDecisions()
    {
        List<Decision> possibleDecisions = new ArrayList<Decision>();
        List<Agent> neighbors = getNearestNeighbors();

        // TODO potential bug here (I think it is finally fixed ^.^ (8-24-13))
        // if our current leader is no longer initiating then look for oldest
        // group member near us, also make sure we are not off on our own
//        if(this._id.toString().equals( "Agent1") && getTime() > 6708 && getTime() < 6717){
//            System.out.println(getTime() + "   "  + getId() + " " + getGroup().getId() + "==" + _observedGroupHistory.get( _leader.getId() ).groupId + " " + _leader.getId()) ;
//        }//TODO delete
        
        if( _leader == this
                || _group.getId().equals(
                        _observedGroupHistory.get( _leader.getId() ).groupId ) )
        {
            possibleDecisions.add( new DoNothing( this, _leader ) );
        }

        // if we are initiating we can cancel, if we are not we can initiate
        if( _currentDecision.getDecision().getDecisionType() == DecisionType.INITIATION )
        {
            possibleDecisions.add( new Cancel( this ) );
        }
        else
        {
            // only add initiate decision if it is possible
            if( isInitiationPossible() )
            {
                possibleDecisions.add( new Initiate( this ) );
            }
        }

        // requires that observedGroupHistory is a Map<groupId,
        // ObservedGroupTime>
        Map<Object, Integer> groupJoinTime = new HashMap<Object, Integer>();
        Map<Object, Agent> oldestObservedMembersOfGroups = new HashMap<Object, Agent>();
        for( int i = 0; i < neighbors.size(); i++ )
        {
            Agent temp = neighbors.get( i );
            Object groupId = _observedGroupHistory.get( temp.getId() ).groupId;
            if( !groupJoinTime.containsKey( groupId ) )
            {
                groupJoinTime.put( groupId,
                        _observedGroupHistory.get( temp.getId() ).time );
                oldestObservedMembersOfGroups.put( groupId, temp );
            }
            else if( _observedGroupHistory.get( temp.getId() ).time < groupJoinTime.get( groupId ) )
            {
                groupJoinTime.put( groupId,
                        _observedGroupHistory.get( temp.getId() ).time );
                oldestObservedMembersOfGroups.put( groupId, temp );
            }
        }

        // iterate through the list and create a follow decision for each oldest
        // group member
        Iterator<Entry<Object, Agent>> iter = oldestObservedMembersOfGroups.entrySet().iterator();
        while( iter.hasNext() )
        {
            Agent temp = iter.next().getValue();
            if( temp.getGroup().getId() != Group.NONE.getId()
                    && temp.getGroup().getId() != _group.getId()
//                    && !temp.getCurrentVelocity().equals( Vector2D.ZERO) //temporary to prevent following of non-moving agents
                    )
            {
                possibleDecisions.add( new Follow( this, temp ) );
            }
        }

        return possibleDecisions;
    }

    public static boolean canMultipleInitiate()
    {
        return _canMultipleInitiate;
    }

    /**
     * Returns true if this individual can initiate
     * 
     * @return
     */
    private boolean isInitiationPossible()
    {
        return _canMultipleInitiate || _canInitiate;
    }

    public class ObservedGroupTime
    {
        public Object groupId;

        public int time;

        public ObservedGroupTime( Object groupId, int time )
        {
            this.groupId = groupId;
            this.time = time;
        }
    }

    public class InitiationHistoryEvent
    {
        public int simRun = 0;;

        public float beforePersonality = 0.0f;

        public boolean wasSuccess = false;

        public float afterPersonality = 0.0f;

        public int participants = 0;
    }
    
    public class ConflictHistoryEvent
    {
        public int currentRun = 0;
        
        public int timeStep = 0;
        
        public String agentId = null;
        
        public String destinationId = null;
        
        public Decision decisionMade = null;
        
        public List<Decision> possibleDecisions = null;
        
        public ConflictHistoryEvent(int currentRun, int timeStep, String agentId, String destinationId, Decision decisionMade, List<Decision> possibleDecisions){
            this.currentRun = currentRun;
            this.timeStep = timeStep;
            this.agentId = agentId;
            this.destinationId = destinationId;
            this.decisionMade = decisionMade;
            this.possibleDecisions = possibleDecisions;
        }
    }
}
