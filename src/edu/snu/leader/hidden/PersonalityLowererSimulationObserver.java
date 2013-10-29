/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

import java.util.Properties;

/**
 * PersonalityLowererSimulationObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityLowererSimulationObserver extends
        AbstractSimulationObserver
{
    /** The number of simulations after which the personalities will be lowered */
    private int _loweringSimulationCount = 0;

    /** The number of individuals whose personality will be lowered */
    private int _indCount = 0;



    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hidden.AbstractSimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
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
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.hidden.AbstractSimulationObserver#simSetUp()
     */
    @Override
    public void simSetUp()
    {
        // Have the desired number of simulations taken place?
        if( _loweringSimulationCount == _simState.getSimulationCount() )
        {
            // Yup
        }
    }
}
