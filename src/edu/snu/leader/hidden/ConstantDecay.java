/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

/**
 * ConstantDecay
 * 
 * @author Jeremy Acre
 * @version $Revision$ ($Author$)
 */
public class ConstantDecay extends AbstractPersonalityDecayCalculator
{
    // Calculate the constant decay rate using the decay time
    float decayRate = 1.0f / _decayTime;

    /**
     * Calculates the decayed personality of the specified individual
     *
     * @param ind The individual whose personality will decay
     * @return The decayed personality
     */
    @Override
    public float calculateDecayedPersonality( SpatialIndividual ind )
    {
        // The individual's new personality
        float newPersonality = ind.getPersonality();

        // If the current personality is greater than the initial personality
        if( ind.getPersonality() > ind.getInitialPersonality() )
        {
            // Calculate the new personality
            newPersonality = ind.getPersonalityAfterLastInitiation()
                    - ( ( _simState.getSimIndex() - ind.getLastInitiationAttempt() ) 
                      * decayRate );
        }

        // Otherwise if the current personality is less than the initial
        // personality.
        else if( ind.getPersonality() < ind.getInitialPersonality() )
        {
            // Calculate the new personality
            newPersonality = ind.getPersonalityAfterLastInitiation()
                    + ( ( _simState.getSimIndex() - ind.getLastInitiationAttempt() ) 
                      * decayRate );
        }

        // Return new personality
        return newPersonality;
    }

}
