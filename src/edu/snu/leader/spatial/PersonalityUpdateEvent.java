/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

// Imports
import org.apache.commons.lang.Validate;


/**
 * PersonalityUpdateEvent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityUpdateEvent
{
    /** The type of update */
    private PersonalityUpdateType _type = null;

    /** The previous personality value */
    private float _previousPersonality = 0.0f;

    /** The new personality value */
    private float _updatedPersonality = 0.0f;

    /** The associated agent */
    private Agent _agent = null;

    /** The simulation run step of the update */
    private long _simRunStep = 0l;

    /** The simulation run of the update */
    private long _simRun = 0l;


    /**
     * Builds this PersonalityUpdateEvent object
     *
     * @param type
     * @param previousPersonality
     * @param updatedPersonality
     * @param agent
     * @param simRunStep
     * @param simRun
     */
    public PersonalityUpdateEvent( PersonalityUpdateType type,
            float previousPersonality,
            float updatedPersonality,
            Agent agent,
            long simRunStep,
            long simRun )
    {
        // Validate the type
        Validate.notNull( type, "Type may not be null" );
        _type = type;

        // Validate and store the agent
        Validate.notNull( agent, "Agent may not be null" );
        _agent = agent;

        // Just store the primitives
        _previousPersonality = previousPersonality;
        _updatedPersonality = updatedPersonality;
        _simRunStep = simRunStep;
        _simRun = simRun;
    }

    /**
     * Returns the type for this object
     *
     * @return The type
     */
    public PersonalityUpdateType getType()
    {
        return _type;
    }

    /**
     * Returns the previousPersonality for this object
     *
     * @return The previousPersonality
     */
    public float getPreviousPersonality()
    {
        return _previousPersonality;
    }

    /**
     * Returns the updatedPersonality for this object
     *
     * @return The updatedPersonality
     */
    public float getUpdatedPersonality()
    {
        return _updatedPersonality;
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
     * Returns the simRunStep for this object
     *
     * @return The simRunStep
     */
    public long getSimRunStep()
    {
        return _simRunStep;
    }

    /**
     * Returns the simRun for this object
     *
     * @return The simRun
     */
    public long getSimRun()
    {
        return _simRun;
    }



}
