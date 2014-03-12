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

//Imports
import edu.snu.leader.spatial.decision.CancelDecision;
import edu.snu.leader.spatial.decision.NoChangeDecision;
import edu.snu.leader.spatial.decision.FollowDecision;
import edu.snu.leader.spatial.decision.InitiateDecision;
import edu.snu.leader.spatial.movement.VoidMovementBehavior;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Agent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class Agent
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            Agent.class.getName() );

    private class NeighborDistance
    {
        private Agent _neighbor = null;
        private float _distanceSqrd = 0.0f;
        public NeighborDistance( Agent neighbor, float distanceSqrd )
        {
            _neighbor = neighbor;
            _distanceSqrd = distanceSqrd;
        }
        public Agent getNeighbor()
        {
            return _neighbor;
        }
        public float getDistanceSquared()
        {
            return _distanceSqrd;
        }
        public float getDistance()
        {
            return (float) Math.sqrt( _distanceSqrd );
        }
    }

    private class NeighborDistanceComparator implements Comparator<NeighborDistance>
    {
        public NeighborDistanceComparator()
        {
            // Do nothing
        }

        @Override
        public int compare( NeighborDistance dist1, NeighborDistance dist2 )
        {
            int result = 0;
            if( dist1.getDistanceSquared() < dist2.getDistanceSquared() )
            {
                result = -1;
            }
            else if( dist1.getDistanceSquared() > dist2.getDistanceSquared() )
            {
                result = 1;
            }
            return result;
        }
    }


    /** Unique ID of this agent */
    private Object _id = null;

    /** The simulation's state */
    private SimulationState _simState = null;

    /** The agent's current group */
    private Group _group = null;

    /** The agent's current leader (if any) */
    private Agent _leader = null;

    /** The agent's personality trait */
    private PersonalityTrait _personalityTrait = null;

    /** The agent's conflict trait */
    private ConflictTrait _conflictTrait = null;

    /** The decision the agent has just chosen */
    private Decision _chosenDecision = null;

    /** The agent's current decision */
    private DecisionEvent _currentDecisionEvent = null;

    /** The spatial state of the agent */
    private AgentSpatialState _spatialState = null;

    /** A history of this agent's decisions */
    private List<DecisionEvent> _decisionHistory =
            new LinkedList<DecisionEvent>();

    /** The behavior that currently determines the agent's movements */
    private MovementBehavior _movementBehavior = new VoidMovementBehavior();

    /** The type of communication used by agents */
    private AgentCommunicationType _communicationType =
            AgentCommunicationType.GLOBAL;

    /** The number of neighbors to use for topological local communication */
    private int _topoNearestNeighborCount = 0;

    /** The distance to neighbors to use for metric local communication */
    private float _metricNearestNeighborDistance = 0.0f;

    /** The current nearest neighbors of the agent */
    private List<Agent> _currentNearestNeighbors = new LinkedList<Agent>();

    /** Map of observed agents to their observed group membership */
    private Map<Agent, Group> _observedAgentMemberships =
            new HashMap<Agent, Group>();

    /** Map of observed groups to their observed membership histories */
    private Map<Group, List<Agent>> _observedGroupHistories =
            new HashMap<Group, List<Agent>>();

    /** Decision probability calculator for all decisions */
    private DecisionProbabilityCalculator _calculator = null;

    /** Movement behavior for initiation decisions */
    private MovementBehavior _initiationMovementBehavior = null;

    /** Movement behavior for follow decisions */
    private MovementBehavior _followMovementBehavior = null;

    /** Movement behavior for cancel decisions */
    private MovementBehavior _cancelMovementBehavior = null;

    /** The threshold percentage of neighbors that must follow for
     *  canceling to not be possible */
    private float _cancelThreshold = 0;


    /**
     * Builds this Agent object
     *
     * @param id
     * @param group
     * @param personalityTrait
     * @param conflictTrait
     * @param spatialState
     * @param communicationType
     * @param initiationMovementBehavior
     * @param followMovementBehavior
     * @param cancelMovementBehavior
     * @param calculator
     * @param cancelThreshold
     */
    public Agent( Object id,
            Group group,
            PersonalityTrait personalityTrait,
            ConflictTrait conflictTrait,
            AgentSpatialState spatialState,
            AgentCommunicationType communicationType,
            MovementBehavior initiationMovementBehavior,
            MovementBehavior followMovementBehavior,
            MovementBehavior cancelMovementBehavior,
            DecisionProbabilityCalculator calculator,
            float cancelThreshold )
    {
        // Store the id
        Validate.notNull( id, "ID may not be null" );
        _id = id;

        // Validate and store the group
        Validate.notNull( group, "Group may not be null" );
        _group = group;

        // Validate and store the personality trait
        Validate.notNull( personalityTrait, "Personality trait may not be null" );
        _personalityTrait = personalityTrait;

        // Validate and store the conflict trait
        Validate.notNull( conflictTrait, "Conflict trait may not be null" );
        _conflictTrait = conflictTrait;

        // Validate and store the spatial state
        Validate.notNull( spatialState, "Spatial state may not be null" );
        _spatialState = spatialState;

        // Validate and store the communication type
        Validate.notNull( communicationType, "Communication type may not be null" );
        _communicationType = communicationType;

        // Validate and store the initiate movement behavior
        Validate.notNull( initiationMovementBehavior,
                "Initiation movement behavior may not be null" );
        _initiationMovementBehavior = initiationMovementBehavior;

        // Validate and store the follow movement behavior
        Validate.notNull( followMovementBehavior,
                "Follow movement behavior may not be null" );
        _followMovementBehavior = followMovementBehavior;

        // Validate and store the cancel movement behavior
        Validate.notNull( cancelMovementBehavior,
                "Cancel movement behavior may not be null" );
        _cancelMovementBehavior = cancelMovementBehavior;

        // Validate and store the decision probability calculator
        Validate.notNull( calculator,
                "Decision probability calculator may not be null" );
        _calculator = calculator;

        // Validate and store the cancel threshold
        Validate.isTrue( ((0.0f <= cancelThreshold)
                && (1.0f >= cancelThreshold)),
                "Cancel threshold must be a number between in [0,1]" );
        _cancelThreshold = cancelThreshold;
    }


    /**
     * Initializes this agent using the simulator state
     *
     * @param simState The simulator state
     */
    public void initialize( SimulationState simState )
    {
        // Save the simulation state
        _simState = simState;

        // Initialize the traits
        _personalityTrait.initialize( simState, this );
        _conflictTrait.initialize( simState, this );
    }


    /**
     * Allows an agent to make a decision regarding its next action
     */
    public void makeDecision()
    {
        boolean debugIsEnabled = _LOG.isDebugEnabled();

        // Clear out the "made" decision
        _chosenDecision = null;

        // Get the current time step
        long currentSimRunStep = _simState.getCurrentSimulationStep();

        // Get all our nearest neighbors and their groups
        _currentNearestNeighbors = buildNearestNeighbors();
        Set<Group> neighborGroups = findNeighborGroups( _currentNearestNeighbors );

        // Log all the group membership events
        logObservedGroupMembershipEvents();

        // Get all the possible decisions
        List<Decision> possibleDecisions = new LinkedList<Decision>();
        float probabilitySum = 0.0f;

        // Is initiation possible?
        if( isInitiationPossible() )
        {
            // Add initiation
            Decision initiation = new InitiateDecision( this,
                    _initiationMovementBehavior,
                    _calculator,
                    _simState );
            possibleDecisions.add( initiation );
            probabilitySum += initiation.calcProbability();

            // Log it
            if( debugIsEnabled )
            {
                _LOG.debug( "Agent ["
                        + getID()
                        + "] initiation probability=["
                        + initiation.calcProbability()
                        + "] personality=["
                        + getPersonalityTrait().getPersonality()
                        + "]" );
            }
        }

        // Is following possible?
        if( isFollowingPossible( neighborGroups ) )
        {
            // Add a follow decision for each group observed
            Set<Group> observedGroups = _observedGroupHistories.keySet();
//            _LOG.debug( "Agent ["
//                    + getID()
//                    + "] is processing ["
//                    + observedGroups.size()
//                    + "] observed groups" );
            for( Group group : observedGroups )
            {
                /* Only continue if this is a different group, the default
                 * group or our current leader switched groups */
                if( group.getID().equals( _group.getID() )
                        || Group.NONE.getID().equals( group.getID() )
                        || ((null != _leader)
                                && !(_leader.getGroup().getID().equals( _group.getID() ) )) )
                {
                    continue;
                }

//                _LOG.debug( "Agent ["
//                        + getID()
//                        + "] is processing valid group ["
//                        + group.getID()
//                        + "]" );

                // Get the potential leader
                Agent leader = findOldestObservedMemberOfGroup( group );
                if( null == leader )
                {
                    _LOG.error( "Observed group ["
                            + group.getID()
                            + "] doesn't have an oldest observed member" );
                    throw new RuntimeException( "Observed group ["
                            + group.getID()
                            + "] doesn't have an oldest observed member" );
                }

                // Is it us?
                if( this.equals( leader ) )
                {
                    // OOPS!
                    _LOG.error( "How can we lead ourselves?" );
                }

                // Create a follow decision
                Decision follow = new FollowDecision( this,
                        leader,
                        group,
                        _followMovementBehavior,
                        _calculator,
                        currentSimRunStep );
                possibleDecisions.add( follow );
                probabilitySum += follow.calcProbability();

                // Log it
                if( debugIsEnabled )
                {
                    _LOG.debug( "Agent ["
                            + getID()
                            + "] follow probability=["
                            + follow.calcProbability()
                            + "]: leader=["
                            + leader.getID()
                            + "] group=["
                            + group.getID()
                            + "] personality=["
                            + getPersonalityTrait().getPersonality()
                            + "]" );
                }
            }
        }

        // Is canceling possible?
        if( isCancellationPossible() )
        {
            Decision cancel = new CancelDecision( this,
                    _cancelMovementBehavior,
                    _calculator,
                    currentSimRunStep );
            possibleDecisions.add( cancel );
            probabilitySum += cancel.calcProbability();

            // Log it
            if( debugIsEnabled )
            {
                _LOG.debug( "Agent ["
                        + getID()
                        + "] cancellation probability=["
                        + cancel.calcProbability()
                        + "] personality=["
                        + getPersonalityTrait().getPersonality()
                        + "]" );
            }
        }

        // Perform a sanity check on the probabilities
        if( 1.0f < probabilitySum )
        {
            _LOG.error( "Decision probability sum should not exceed 1.0 ["
                    + probabilitySum
                    + "]" );
        }

        // Generate a decision value
        float decisionValue = _simState.getRandom().nextFloat();

        // Is doing nothing possible?
        if( !isDoNothingPossible() )
        {
            // Nope, cap the decision value to the probability sum
            decisionValue *= probabilitySum;

            _LOG.debug( "Doing nothing is not possible!" );
        }

        // Find out which decision we make
        float probabilityTotal = 0.0f;
        for( Decision decision : possibleDecisions )
        {
            // Is this the one?
            probabilityTotal += decision.calcProbability();
            if( decisionValue < probabilityTotal )
            {
//                /* Yup, but if it is an initiation event, make sure we can
//                 * still initiate */
//                if( DecisionType.INITIATE.equals( decision )
//                        && !isInitiationPossible() )
//                {
//                    // Can't do it.  Go on to the next one
//                    continue;
//                }

                // We are a go!
                _chosenDecision = decision;
                _LOG.debug( "Agent ["
                        + getID()
                        + "] chose decision ["
                        + decision.getType()
                        + "]" );
            }
        }
    }

    /**
     * Executes an agent's actions
     */
    public void execute()
    {
        // Did we choose a decision?
        if( null != _chosenDecision )
        {
            // If it is initiation, make sure it is still valid
            if( !DecisionType.INITIATE.equals( _chosenDecision.getType() )
                    || isInitiationPossible() )
            {
                // Yup.  Make the decision and log it.
                _chosenDecision.make();
                _currentDecisionEvent = new DecisionEvent( _chosenDecision,
                        _group,
                        _simState.getCurrentSimulationStep() );
                _decisionHistory.add( _currentDecisionEvent );

                // Signal it!
                _simState.getObserverManager().signalAgentDecisionEvent( this,
                        _currentDecisionEvent );
            }
        }

        // Execute the current movement behavior
        _movementBehavior.execute();
    }

    /**
     * Resets this agent for the next simulation run
     */
    public void reset()
    {
        // Reset our spatial state
        _spatialState.reset();

        // Reset our personality
        _personalityTrait.reset();

        // Reset our group and leader
        _group = Group.NONE;
        Group.NONE.join( this, -1l );
        _leader = null;

        // Reset our decision event history
        _currentDecisionEvent = new DecisionEvent( new NoChangeDecision( this, -1l ),
                _group,
                -1l );
        _decisionHistory.clear();

        // Reset our movement behavior
        _movementBehavior = new VoidMovementBehavior();

        // Reset our observations
        _observedAgentMemberships.clear();
        _observedGroupHistories.clear();
    }

    /**
     * Returns the unique ID of this agent
     *
     * @return The unique ID of this agent
     */
    public Object getID()
    {
        return _id;
    }

    /**
     * Returns the group of which this agent is a member
     *
     * @return The group of which this agent is a member
     */
    public Group getGroup()
    {
        return _group;
    }

    /**
     * Sets the group for this object.
     *
     * @param group The specified group
     */
    public void setGroup( Group group )
    {
        _group = group;
    }

    /**
     * Returns this agent's leader (if any)
     *
     * @return The leader, or <code>null</code> if no leader
     */
    public Agent getLeader()
    {
        return _leader;
    }

    /**
     * Sets the leader for this object.
     *
     * @param leader The specified leader
     */
    public void setLeader( Agent leader )
    {
        _leader = leader;
    }

    /**
     * Returns this agent's personality trait
     *
     * @return The personality Trait
     */
    public PersonalityTrait getPersonalityTrait()
    {
        return _personalityTrait;
    }

    /**
     * Returns this agent's conflict trait
     *
     * @return The conflict trait
     */
    public ConflictTrait getConflictTrait()
    {
        return _conflictTrait;
    }

    /**
     * Returns the spatialState for this object
     *
     * @return The spatialState
     */
    public AgentSpatialState getSpatialState()
    {
        return _spatialState;
    }

    /**
     * Returns the currentDecisionEvent for this object
     *
     * @return The currentDecisionEvent.
     */
    public DecisionEvent getCurrentDecisionEvent()
    {
        return _currentDecisionEvent;
    }

    /**
     * Returns the history of decisions made by this agent
     *
     * @return A list of decision events made by this agent
     */
    public List<DecisionEvent> getDecisionHistory()
    {
        // Return a new list so our history can't be modified
        return new LinkedList<DecisionEvent>( _decisionHistory );
    }

    /**
     * Returns this agent's current nearest neighbors
     *
     * @return A list of agents that are the nearest neighbors of this agent
     */
    public List<Agent> getCurrentNearestNeighbors()
    {
        return new LinkedList<Agent>( _currentNearestNeighbors );
    }

    /**
     * Returns the number of observed neighbors for this agent
     *
     * @return The number of observed neighbors for this agent
     */
    public int getCurrentNearestNeighborCount()
    {
        return _currentNearestNeighbors.size();
    }

    /**
     * Returns this agent's cancel threshold
     *
     * @return This agent's cancel threshold
     */
    public float getCancelThreshold()
    {
        return _cancelThreshold;
    }

    /**
     * Sets the movementBehavior for this object.
     *
     * @param movementBehavior The specified movementBehavior
     */
    public void setMovementBehavior( MovementBehavior movementBehavior )
    {
        _movementBehavior = movementBehavior;
    }

    /**
     * If this agent is an initiator, return the number of observed followers
     *
     * @param group
     * @return The number of observed followers if this agent is an initiator,
     * otherwise, 0
     */
    public int getObservedFollowerCount()
    {
        int observedFollowerCount = 0;

        // Are we an initiator?
        if( DecisionType.INITIATE.equals( _currentDecisionEvent.getDecision().getType() ) )
        {
            observedFollowerCount = getNeighborGroupCount();
        }

        return observedFollowerCount;
    }

    /**
     * Returns the number of observed neighbors in the same group as the agent
     *
     * @return The number of observed neighbors in the same group as the agent
     */
    public int getNeighborGroupCount()
    {
        return getNeighborGroupCount( _group );
    }

    /**
     * Returns the number of observed neighbors in the specified group
     *
     * @param group
     * @return The number of observed neighbors in the specified group
     */
    public int getNeighborGroupCount( Group group )
    {
        // Get the number of neighbors in the group
        int neighborGroupCount = 0;
        if( null != group )
        {
            List<Agent> observedGroupMembers = _observedGroupHistories.get( group );
            if( null != observedGroupMembers )
            {
                neighborGroupCount = observedGroupMembers.size();
            }
        }
        return neighborGroupCount;
    }


    /**
     * Returns this agent's nearest neighbors
     *
     * @return A list of agents that are the nearest neighbors of this agent
     */
    private List<Agent> buildNearestNeighbors()
    {
        List<Agent> nearestNeighbors = null;

        switch( _communicationType )
        {
            case GLOBAL:
                // Grab all the neighbors
                nearestNeighbors = findAllNeighbors();
                break;

            case TOPOLOGICAL:
                // Get the neighbors based on topological distance
                nearestNeighbors = findNearestNeighborsUsingTopologicalDistance();
                break;

            case METRIC:
                // Get the neighbors based on metric distance
                nearestNeighbors = findNearestNeighborsUsingMetricDistance();
                break;

            default:
                _LOG.error( "Unknown communication type ["
                        + _communicationType
                        + "]" );
                throw new RuntimeException( "Unknown communication type ["
                        + _communicationType
                        + "]" );
        }

        return nearestNeighbors;
    }

    /**
     * Returns all the agents in the simulation
     *
     * @return All the agents
     */
    private List<Agent> findAllNeighbors()
    {
        // Just grab all the agents
        List<Agent> allNeighbors = new LinkedList<Agent>();
        Iterator<Agent> iter = _simState.getAgentIterator();
        while( iter.hasNext() )
        {
            // Ensure that this agent isn't included
            Agent current = iter.next();
            if( !this.equals( current ) )
            {
                allNeighbors.add( current );
            }
        }

        return allNeighbors;
    }

    /**
     * Finds all the neighbors nearest to the agent using topological distance
     *
     * @return The agent's nearest neighbors
     */
    private List<Agent> findNearestNeighborsUsingTopologicalDistance()
    {
        NeighborDistance[] nearestNeighbors = buildSortedNearestNeighbors();

        // Calculate the number of neighbors to find
        int neighborCount = _topoNearestNeighborCount;
        if( neighborCount > nearestNeighbors.length )
        {
            neighborCount = nearestNeighbors.length;
        }

        // Find the N nearest neighbors
        List<Agent> topoNeighbors = new LinkedList<Agent>();
        for( int i = 0; i < neighborCount; i++ )
        {
            topoNeighbors.add( nearestNeighbors[i].getNeighbor() );
        }

        return topoNeighbors;
    }

    /**
     * Finds all the neighbors nearest to the agent using metric distance
     *
     * @return The agent's nearest neighbors
     */
    private List<Agent> findNearestNeighborsUsingMetricDistance()
    {
        NeighborDistance[] nearestNeighbors = buildSortedNearestNeighbors();

        // Calculate the metric distance squared to save time
        float metricDistanceSqrd = _metricNearestNeighborDistance
                * _metricNearestNeighborDistance;

        // Find all the neighbors within the distance
        List<Agent> metricNeighbors = new LinkedList<Agent>();
        for( int i = 0; i < nearestNeighbors.length; i++ )
        {
            // Is the agent beyond the distance?
            if( nearestNeighbors[i].getDistanceSquared() > metricDistanceSqrd )
            {
                // Yup, bail since they are sorted
                break;
            }

            // Add it to the list
            metricNeighbors.add( nearestNeighbors[i].getNeighbor() );
        }

        return metricNeighbors;
    }

    /**
     * Builds an array of the agent's nearest neighbors and sorts them from
     * closest to farthest
     *
     * @return THe agent's nearest neighbors sorted by distance
     */
    private NeighborDistance[] buildSortedNearestNeighbors()
    {
        // Build a list of all the agents and their distances
        List<NeighborDistance> neighborList = new LinkedList<Agent.NeighborDistance>();
        Iterator<Agent> agentIter = _simState.getAgentIterator();
        while( agentIter.hasNext() )
        {
            // Get the agent
            Agent agent = agentIter.next();

            // Ensure that it isn't this agent
            if( this.equals( agent ) )
            {
                continue;
            }

            // Calculate the distance squared
            float distanceSqrd = (float) _spatialState.getPosition().distanceSq(
                    agent._spatialState.getPosition() );

            // Add it to the list
            neighborList.add( new NeighborDistance( agent, distanceSqrd ) );
        }

        // Get the list as an array so we can sort it
        NeighborDistance[] neighbors = neighborList.toArray(
                new NeighborDistance[ neighborList.size() ] );

        // Sort it
        Arrays.sort( neighbors,
                new NeighborDistanceComparator() );

        // Return it
        return neighbors;
    }


    /**
     * Determines if initiation is a possible decision this timestep
     *
     * @return <code>true</code> if initiation is possible, otherwise,
     * <code>false</code>
     */
    private boolean isInitiationPossible()
    {
        // Default to situations where initiation is possible
        boolean possible = true;

        /* If we are using global communication and we only allow one
         * initiator, then check to see if anyone else is initiating. */
        if( AgentCommunicationType.GLOBAL.equals( _communicationType ) )
        {
            possible = _simState.isInitiationPossible();
        }

        return possible;
    }

    /**
     * Determines if following is a possible decision this timestep
     *
     * @param neighborGroups
     * @return <code>true</code> if following is possible, otherwise,
     * <code>false</code>
     */
    private boolean isFollowingPossible( Set<Group> neighborGroups )
    {
        // Are any of our neighbors members of a different group?
        return neighborGroups.size() > 0;
    }

    /**
     * Determines if cancellation is a possible decision this timestep
     *
     * @return <code>true</code> if cancellation is possible, otherwise,
     * <code>false</code>
     */
    private boolean isCancellationPossible()
    {
        // Default to false
        boolean possible = false;

        // Are we an initiator?
        if( (null != _currentDecisionEvent)
                && DecisionType.INITIATE.equals( _currentDecisionEvent.getDecision().getType() ) )
        {
            // Yup.  Have we passed the threshold number of followers?
            int followerCount = getObservedFollowerCount();
            int neighborCount = _currentNearestNeighbors.size();
            if( followerCount < Math.floor(_cancelThreshold * neighborCount) )
            {
                // Nope.  Canceling is possible
                possible = true;
            }

//            _LOG.debug( "Agent ["
//                    + getID()
//                    + "] is an initiator with: followerCount=["
//                    + followerCount
//                    + "] neighborCount=["
//                    + neighborCount
//                    + "] possible=["
//                    + possible
//                    + "]" );
        }

        // Otherwise, are there no more observed members of our group?
        else
        {
            int neighborGroupCount = getNeighborGroupCount();
            if( !Group.NONE.getID().equals( _group.getID() )
                    && (0 == neighborGroupCount) )
            {
                // Yup
                possible = true;
            }
        }

        return possible;
    }

    /**
     * Determines if doing nothing is a possible decision this timestep
     *
     * @return <code>true</code> if doing nothing is possible, otherwise,
     * <code>false</code>
     */
    private boolean isDoNothingPossible()
    {
        // Default to true
        boolean possible = true;

        /* Doing nothing is not possible if our leader has made a new
         * decision.  We can tell if they aren't a member of our group */
        if( (null != _leader) && !(_group.getID().equals( _leader.getGroup().getID() )) )
        {
            possible = false;
        }

        return possible;
    }

    /**
     * Finds all the neighboring groups (i.e., not our own) from our neighbors
     *
     * @param neighbors Our neighbors
     * @return All the neighboring groups
     */
    private Set<Group> findNeighborGroups( List<Agent> neighbors )
    {
        Set<Group> neighborGroups = new HashSet<Group>();

        // Iterate through the neighbors
        for( Agent neighbor : neighbors )
        {
            // Are they a member of a different group?
            Group neighborsGroup = neighbor.getGroup();
            if( !neighborsGroup.getID().equals( _group.getID() ) )
            {
                // Yup
                neighborGroups.add( neighborsGroup );
            }
        }

        return neighborGroups;
    }

    /**
     * Logs all the observed group membership events
     */
    private void logObservedGroupMembershipEvents()
    {
        // Find all the agents we aren't observing any more
        if( !AgentCommunicationType.GLOBAL.equals(_communicationType) )
        {
            // Get the previously observed agents
            Set<Agent> previouslyObserved = _observedAgentMemberships.keySet();

            // Remove all the ones we currently observe
            previouslyObserved.removeAll( _currentNearestNeighbors );

            // Remove all the remaining ones from the maps
            for( Agent agent : previouslyObserved )
            {
                // Get their last observed group
                Group lastObservedGroup = _observedAgentMemberships.get( agent );

                // Remove it from the observed group history
                List<Agent> groupHistory = _observedGroupHistories.get( lastObservedGroup );
                if( null != groupHistory )
                {
                    groupHistory.remove( agent );
                }
                else
                {
                    // Something is foobar
                    _LOG.error( "Group history for id=["
                            + lastObservedGroup.getID()
                            + "] is missing, but we previously observed an agent in that group" );
                }

                // Remove it from the agent membership
                _observedAgentMemberships.remove( agent );
            }
        }

        // Iterate through all our neighbors to process their group membership
        for( Agent agent : _currentNearestNeighbors )
        {
            // Have we observed them before?
            if( AgentCommunicationType.GLOBAL.equals(_communicationType)
                    || _observedAgentMemberships.containsKey( agent ) )
            {
                // Yup.  Check to see if their group membership changed
                Group lastObservedGroup = _observedAgentMemberships.get( agent );
                Group currentObservedGroup = agent.getGroup();
                if( (null == lastObservedGroup)
                        || !lastObservedGroup.equals( currentObservedGroup ) )
                {
                    // Yup.
                    // Remove them from the old group history
                    if( null != lastObservedGroup )
                    {
                        List<Agent> oldGroupHistory = _observedGroupHistories.get(
                                lastObservedGroup );
                        if( null != oldGroupHistory )
                        {
                            oldGroupHistory.remove( agent );
                        }
                    }

                    // Add them to the new one
                    List<Agent> newGroupHistory = _observedGroupHistories.get(
                            currentObservedGroup );
                    if( null == newGroupHistory )
                    {
                        // Create it
                        newGroupHistory = new LinkedList<Agent>();
                        _observedGroupHistories.put( currentObservedGroup,
                                newGroupHistory );
                    }
                    newGroupHistory.add( agent );

                    // Change their group
                    _observedAgentMemberships.put( agent, currentObservedGroup );

//                    if( _LOG.isDebugEnabled() )
//                    {
//                        Object lastGroupID = "????";
//                        if( null != lastObservedGroup )
//                        {
//                            lastGroupID = lastObservedGroup.getID();
//                        }
//                        _LOG.debug( "Agent ["
//                                + getID()
//                                + "] observed agent ["
//                                + agent.getID()
//                                + "] change groups from ["
//                                + lastGroupID
//                                + "] to ["
//                                + currentObservedGroup.getID()
//                                + "] for a total of ["
//                                + newGroupHistory.size()
//                                + "] agents" );
//                    }
                }
            }
            else
            {
                // Nope, they are new.  Add them to the maps.
                Group observedGroup = agent.getGroup();
                _observedAgentMemberships.put( agent, observedGroup );
                List<Agent> groupHistory = _observedGroupHistories.get(
                        observedGroup );
                if( null == groupHistory )
                {
                    groupHistory = new LinkedList<Agent>();
                    _observedGroupHistories.put( observedGroup,
                            groupHistory );
                }
                groupHistory.add( agent );

//                _LOG.debug( "Agent ["
//                        + getID()
//                        + "] observed new agent ["
//                        + agent.getID()
//                        + "] in group ["
//                        + agent.getGroup().getID()
//                        + "]" );
            }
        }

    }

    /**
     * Finds the oldest observed member of the specified group
     *
     * @param group
     * @return The oldest observed member of the group
     */
    private Agent findOldestObservedMemberOfGroup( Group group )
    {
        // Ensure that we have observed members of the group
        List<Agent> observedMembers = _observedGroupHistories.get( group );
        if( (null == observedMembers) || (0 == observedMembers.size()) )
        {
            _LOG.error( "Unable to find observed members of group ["
                    + group.getID()
                    + "] for agent=["
                    + getID()
                    + "]" );
            throw new RuntimeException( "Unable to find observed members of group ["
                    + group.getID()
                    + "] for agent=["
                    + getID()
                    + "]" );
        }

        // Return the first agent in the list
        return observedMembers.get(0);
    }

}
