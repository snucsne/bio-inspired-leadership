/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

// imports
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;

import java.util.Properties;


/**
 * ForcedPersonalityDistributionSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ForcedPersonalityDistributionSimObserver
    extends AbstractSimulationObserver
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ForcedPersonalityDistributionSimObserver.class.getName() );


    protected enum PersonalityAssignmentMethod {
        RANDOM,
        OBSERVERS,
        INVERSE_OBSERVERS
    };

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

        // Get the simulation properties
        Properties props = simState.getProps();


    }

    /**
     * Prepares a simulation run for execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simRunSetUp()
     */
    @Override
    public void simRunSetUp()
    {
        /* Is it the beginning?  Note that we can't do it when we build the
         * individuals since we need them all built to begin with.
         */
        if( 0 == _simState.getSimulationCount() )
        {
            // Yup
        }
    }

}
