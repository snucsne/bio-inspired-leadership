/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import java.util.Iterator;


/**
 * PostSimMovementObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PostSimMovementObserver
        extends AbstractSimulationObserver
        implements SimulationObserver
{

    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        // Call the superclass implementation
        super.initialize( simState );
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.hidden.observer.SimulationObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        // Move all the individuals
        Iterator<SpatialIndividual> indIter = _simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            SpatialIndividual ind = indIter.next();

            // Did they follow a successful initiator?


            // Did they follow any failed initiators?
        }

        // Update all the nearest neighbors for all the individuals
        _simState.updateAllNearestNeighbors();
    }

}
