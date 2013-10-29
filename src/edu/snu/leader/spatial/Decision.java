/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

/**
 * Decision
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface Decision
{
    /**
     * Returns the type of this decision
     *
     * @return The type of this decision
     */
    public DecisionType getType();

    /**
     * Calculates and returns the probability of this decision being made
     *
     * @return The probability that this decision is made
     */
    public float calcProbability();

    /**
     * Makes this decision
     */
    public void make();

    /**
     * Returns the time of the decision
     *
     * @return The time
     */
    public long getTime();
}
