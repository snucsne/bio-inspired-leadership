/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

/**
 * EventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface EventTimeCalculator
{
    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     */
    public void initialize( SimulationState simState );

    /**
     * Calculates the time at the specified individual will initiate movement
     *
     * @param ind The individual
     * @return The initiation time
     */
    public float calculateInitiationTime( SpatialIndividual ind );

    /**
     * Calculates the time at the specified individual will follow an
     * initiator
     *
     * @param ind The individual
     * @param initiator The initiator
     * @param departed The number of individuals who have already departed
     * @param groupSize The size of the group
     * @return The follow time
     */
   public float calculateFollowTime( SpatialIndividual ind,
           SpatialIndividual initiator,
           int departed,
           int groupSize );

   /**
    * Calculates the time at the specified individual will cancel an initiation
    *
    * @param ind The individual
    * @param departed The number of individuals who have already departed
    * @return The cancellation time
    */
   public float calculateCancelTime( SpatialIndividual ind, int departed );

   /**
    * Returns a string description of the initiation time calculations
    *
    * @return A string description of the initiation time calculations
    */
   public String describeInitiation();

   /**
    * Returns a string description of the following time calculations
    *
    * @return A string description of the following time calculations
    */
   public String describeFollow();

   /**
    * Returns a string description of the cancellation time calculations
    *
    * @return A string description of the cancellation time calculations
    */
   public String describeCancellation();

}
