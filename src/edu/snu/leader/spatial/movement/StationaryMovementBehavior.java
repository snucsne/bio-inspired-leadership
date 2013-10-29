/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.movement;

// Imports
import edu.snu.leader.spatial.MovementBehavior;


/**
 * StationaryMovementBehavior
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class StationaryMovementBehavior extends AbstractMovementBehavior
{
    /**
     * Returns a copy of this movement behavior
     *
     * @return A copy of this movement behavior
     * @see edu.snu.leader.spatial.MovementBehavior#copy()
     */
    @Override
    public MovementBehavior copy()
    {
        return new StationaryMovementBehavior();
    }
}
