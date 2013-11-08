/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.personality;

import edu.snu.leader.hidden.SpatialIndividual;

/**
 * ExponentialDecay
 * 
 * @author Jeremy Acre
 * @version $Revision$ ($Author$)
 */
public class ExponentialDecay extends AbstractPersonalityDecayCalculator
{
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

        // If the current personality does not equal the initial personality
        if( isDecaying( ind ) )
        {
            // Calculate the new personality
            newPersonality = (float) ( ( ind.getPersonalityAfterLastInitiation() - ind.getInitialPersonality() )
                    * Math.exp( ( ( _simState.getSimIndex() - ind.getLastInitiationAttempt() ) 
                                  * Math.log( _differenceThreshold * ind.getInitialPersonality() ) ) 
                                / _decayTime ) + ind.getInitialPersonality() );
        }

        // Return new personality
        return newPersonality;
    }

}
