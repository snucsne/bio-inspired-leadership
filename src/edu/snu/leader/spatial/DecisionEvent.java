/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

// Imports
import org.apache.commons.lang.Validate;


/**
 * DecisionEvent
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DecisionEvent
{
    /** A specific decision */
    private Decision _decision = null;

    /** The associated group */
    private Group _group = null;

    /** The time the decision was made */
    private long _time = 0;


    /**
     * Builds this DecisionEvent object
     *
     * @param decision
     * @param group
     * @param time
     */
    public DecisionEvent( Decision decision, Group group, long time )
    {
        // Validate the decision
        Validate.notNull( decision, "Decision may not be null" );
        _decision = decision;

        // Validate the group
        Validate.notNull( group, "Group may not be null" );

        // Just store the time
        _time = time;
    }

    /**
     * Returns the decision for this object
     *
     * @return The decision
     */
    public Decision getDecision()
    {
        return _decision;
    }

    /**
     * Returns the group associated with the decision
     *
     * @return The group
     */
    public Group getGroup()
    {
        return _group;
    }

    /**
     * Returns the time for this object
     *
     * @return The time
     */
    public long getTime()
    {
        return _time;
    }

}
