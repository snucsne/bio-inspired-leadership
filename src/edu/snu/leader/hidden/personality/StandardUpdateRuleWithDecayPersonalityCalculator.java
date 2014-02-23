/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.personality;

// Imports
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MiscUtils;

/**
 * StandardUpdateRuleWithDecayPersonalityCalculator
 *
 * @author Jeremy Acre
 * @version $Revision$ ($Author$)
 */
public class StandardUpdateRuleWithDecayPersonalityCalculator
    extends StandardUpdateRulePersonalityCalculator
        implements PersonalityCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            StandardUpdateRuleWithDecayPersonalityCalculator.class.getName() );

    /** Key for the decay calculator */
    private static final String _PERSONALITY_DECAY_CALCULATOR_KEY = "personality-decay-calculator";


    /** The simulation state */
    private SimulationState _simState = null;

    /** The decay calculator */
    private PersonalityDecayCalculator _decayCalc;

    /**
     * Initializes the updater
     *
     * @param simState The simulation's state
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        _simState = simState;
        Properties props = simState.getProps();

        // Get the decay calculator
        String decayCalcStr = props.getProperty( _PERSONALITY_DECAY_CALCULATOR_KEY );
        Validate.notEmpty( decayCalcStr,
                "Decay calculator value (key="
                + _PERSONALITY_DECAY_CALCULATOR_KEY
                + ") may not be empty" );

        // Load and instantiate the decay calculator
        _decayCalc = (PersonalityDecayCalculator) MiscUtils.loadAndInstantiate( decayCalcStr, "Calculator" );

        // Initialize decay calculator
        _decayCalc.initialize( simState );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculate the new personality value
     *
     * @param individual The individual who's personality is being calculated
     * @param updateType The type of update being applied
     * @param followers The number of followers in the initiation
     * @return The updated personality value
     */
    @Override
    public float calculatePersonality( SpatialIndividual individual,
            PersonalityUpdateType updateType, int followers )
    {
        // Get the superclass implementation's calculation for new personality
        float newPersonality = super.calculatePersonality( individual,
                updateType,
                followers );

        // Check to see if the personality should decay
        if( !PersonalityUpdateType.TRUE_WINNER.equals( updateType )
                && !PersonalityUpdateType.TRUE_LOSER.equals( updateType ) )
        {
            // It isn't a winner or loser, so it might be decaying
            // Check and see
            if( _decayCalc.isDecaying( individual ) )
            {
                // Yup, calculate the new personality from decaying
                newPersonality = _decayCalc.calculateDecayedPersonality(
                        individual );
            }
        }

        // Validate the new personality
        newPersonality = ensureValidPersonality( newPersonality );

        return newPersonality;
    }
}
