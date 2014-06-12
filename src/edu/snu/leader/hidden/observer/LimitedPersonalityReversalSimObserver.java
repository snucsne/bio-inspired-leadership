/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;


/**
 * LimitedPersonalityReversalSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class LimitedPersonalityReversalSimObserver
        extends AbstractSimulationObserver
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            LimitedPersonalityReversalSimObserver.class.getName() );

    /** The minimum personality value for a bold personality */
    private static final float _MIN_BOLD_PERSONALITY = 0.8f;

    /** Key for the reversal simulation count value */
    private static final String _REVERSAL_SIM_COUNT_KEY = "reversal-sim-count";

    /** Key for the limit bold count flag */
    private static final String _LIMIT_BOLD_COUNT_KEY = "limit-bold-count";

    private static class IndividualMimicComparator implements Comparator<SpatialIndividual>
    {
        /**
         * Compares to individuals w.r.t. their number of mimicking neighbors
         *
         * @param ind1 The first individual to be compared
         * @param ind2 The second individual to be compared
         * @return A negative integer, zero, or a positive integer as the first
         *         individual has less than, equal to, or greater than the second
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare( SpatialIndividual ind1, SpatialIndividual ind2 )
        {
            // Get the number of mimics for each
            int ind1Mimics = ind1.getMimicingNeighborCount();
            int ind2Mimics = ind2.getMimicingNeighborCount();

            // Compare them
            int result = ind1Mimics - ind2Mimics;

            return result;
        }
    }


    /** The simulation count at which personalities are reversed */
    private int _reversalSimCount = 0;

    /** Flag to limit the bold count to the original count */
    private boolean _limitBoldCount = false;

    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the simulation properties
        Properties props = simState.getProps();

        // Get the simulation count at which personalities are reversed
        String reversalSimCountStr = props.getProperty( _REVERSAL_SIM_COUNT_KEY );
        Validate.notEmpty( reversalSimCountStr,
                "Reversal sim count (key="
                + _REVERSAL_SIM_COUNT_KEY
                + ") may not be empty" );
        _reversalSimCount = Integer.parseInt( reversalSimCountStr );
        _LOG.info( "Using _reversalSimCount=[" + _reversalSimCount + "]" );

        // Get the flag to limit the bold count
        String limitBoldCountStr = props.getProperty( _LIMIT_BOLD_COUNT_KEY );
        Validate.notEmpty( limitBoldCountStr,
                "Limit bold count flag (key="
                + _LIMIT_BOLD_COUNT_KEY
                + ") may not be empty" );
        _limitBoldCount = Boolean.parseBoolean( limitBoldCountStr );
        _LOG.info( "Using _limitBoldCount=[" + _limitBoldCount + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Prepares a simulation run for execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simRunSetUp()
     */
    @Override
    public void simRunSetUp()
    {
        // Is this the desired simulation?
        if( _reversalSimCount == _simState.getSimIndex() )
        {
            _LOG.warn( "Reversing personalities at ["
                    + _simState.getSimIndex()
                    + "]" );

            // Are we interested in the bold count?
            float originalBoldCount = 0;
            if( _limitBoldCount )
            {
                SpatialIndividual current = null;
                Iterator<SpatialIndividual> iter = _simState.getAllIndividuals().iterator();
                while( iter.hasNext() )
                {
                    // Get the current individual's personality
                    current = iter.next();
                    if( _MIN_BOLD_PERSONALITY <= current.getPersonality() )
                    {
                        originalBoldCount++;
                    }
                }
            }

            // Sort the individuals by the number of mimics
            SpatialIndividual[] sortedInds =
                    _simState.getAllIndividuals().toArray( new SpatialIndividual[0] );
            Arrays.sort( sortedInds, new IndividualMimicComparator() );

            // Reverse personalities
            int currentBoldCount = 0;
            SpatialIndividual current = null;
            for( int i = 0; i < sortedInds.length; i++ )
            {
                // Get the current personality
                current = sortedInds[i];

                _LOG.debug( "Reversing: mimics=["
                        + current.getMimicingNeighborCount()
                        + "] personality=["
                        + current.getPersonality()
                        + "] id=["
                        + current.getID()
                        + "]" );

                // Calculate the reversed personality
                float reversedPersonality = 1.0f - current.getPersonality();

                // Only do it if we won't go over the limit
                if( (0.5f > reversedPersonality)
                        || !_limitBoldCount
                        || (originalBoldCount >= ( currentBoldCount + 1 )) )
                {
                    // Reverse the personality
                    current.setPersonality( reversedPersonality );

                    // Update our bold count if appropriate
                    if( _MIN_BOLD_PERSONALITY <= reversedPersonality )
                    {
                        currentBoldCount++;
                    }

                    _LOG.debug( "Actually reversed" );
                }
            }
        }
    }

}
