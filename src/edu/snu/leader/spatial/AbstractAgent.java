/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

//Imports
import edu.snu.leader.util.NotYetImplementedException;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.LinkedList;
import java.util.List;


/**
 * AbstractAgent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractAgent //implements Agent
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            AbstractAgent.class.getName() );

    /** Minimum personality value */
    private static final float _MIN_PERSONALITY = 0.0f;

    /** Maximum personality value */
    private static final float _MAX_PERSONALITY = 1.0f;

    /** Minimum assertiveness value */
    private static final float _MIN_ASSERTIVENESS = 0.0f;

    /** Maximum assertiveness value */
    private static final float _MAX_ASSERTIVENESS = 1.0f;


    /** Unique ID of this agent */
    private Object _id = null;

    /** The agent's current group */
    private Group _group = null;

    /** The agent's current leader (if any) */
    private Agent _leader = null;

    /** The agent's personality trait */
    private PersonalityTrait _personalityTrait = null;

    /** The agent's conflict trait */
    private ConflictTrait _conflictTrait = null;

    private DecisionEvent _currentDecisionEvent = null;

    private List<DecisionEvent> _decisionHistory =
            new LinkedList<DecisionEvent>();

    private MovementBehavior _movementBehavior = null;


    /**
     * Builds this AbstractAgent object
     *
     * @param id
     * @param personalityTrait
     * @param conflictTrait
     */
    public AbstractAgent( Object id,
            PersonalityTrait personalityTrait,
            ConflictTrait conflictTrait )
    {
        // Store the id
        Validate.notNull( id, "ID may not be null" );
        _id = id;

        // Store the personality trait
        _personalityTrait = personalityTrait;

        // Store the conflict trait
        _conflictTrait = conflictTrait;
    }

    /**
     * Initializes this agent using the simulator state
     *
     * @param simState The simulator state
     * @see edu.snu.leader.spatial.Agent#initialize(edu.snu.leader.spatial.SimulationState)
     */
//    @Override
    public void initialize( SimulationState simState )
    {
        // Initialize the traits


        throw new NotYetImplementedException();
    }

    /**
     * Executes an agent's actions
     *
     * @see edu.snu.leader.spatial.Agent#execute()
     */
//    @Override
    public void execute()
    {
        /* If the current decision event is different from the last in the
         * history, add it to the list */
        int decisionHistorySize = _decisionHistory.size();
        if( (0 == decisionHistorySize)
                || (_currentDecisionEvent.equals( _decisionHistory.get( decisionHistorySize - 1 ) ) ) )
        {
            _decisionHistory.add( _currentDecisionEvent );
        }

        // Execute the current movement behavior
        _movementBehavior.execute();
    }

    /**
     * Resets this agent for the next simulation run
     *
     * @see edu.snu.leader.spatial.Agent#reset()
     */
//    @Override
    public void reset()
    {
        // Reset our decision event history
        // *** NEED A GOOD DEFAULT DECISION EVENT ***
        _currentDecisionEvent = null;
        _decisionHistory.clear();
        throw new NotYetImplementedException();
    }

    /**
     * Returns the unique ID of this agent
     *
     * @return The unique ID of this agent
     * @see edu.snu.leader.spatial.Agent#getID()
     */
//    @Override
    public Object getID()
    {
        return _id;
    }

    /**
     * Returns the group for this object
     *
     * @return The group
     */
//    @Override
    public Group getGroup()
    {
        return _group;
    }

    /**
     * Returns the leader for this object
     *
     * @return The leader
     */
//    @Override
    public Agent getLeader()
    {
        return _leader;
    }

    /**
     * Returns the personalityTrait for this object
     *
     * @return The personalityTrait
     */
//    @Override
    public PersonalityTrait getPersonalityTrait()
    {
        return _personalityTrait;
    }

    /**
     * Returns the conflictTrait for this object
     *
     * @return The conflictTrait
     */
//    @Override
    public ConflictTrait getConflictTrait()
    {
        return _conflictTrait;
    }

    /**
     * Returns the history of decisions made by this agent
     *
     * @return A list of decision events made by this agent
     * @see edu.snu.leader.spatial.Agent#getDecisionHistory()
     */
//    @Override
    public List<DecisionEvent> getDecisionHistory()
    {
        // Return a new list so our history can't be modified
        return new LinkedList<DecisionEvent>( _decisionHistory );
    }

    /**
     * Returns this agent's nearest neighbors
     *
     * @return A list of agents that are the nearest neighbors of this agent
     * @see edu.snu.leader.spatial.Agent#getNearestNeighbors()
     */
//    @Override
    public List<Agent> getNearestNeighbors()
    {
        return null;
    }


}
