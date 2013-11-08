/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

/**
 * LinearDecay
 * 
 * @author Jeremy Acre
 * @version $Revision$ ($Author$)
 */
public class LinearDecay extends AbstractPersonalityDecayCalculator
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
            newPersonality = ind.getPersonalityAfterLastInitiation()
                    + ( ( ( ind.getInitialPersonality() - ind.getPersonalityAfterLastInitiation() ) 
                          / _decayTime ) 
                      * ( _simState.getSimIndex() - ind.getLastInitiationAttempt() ) );
        }

        // Return new personality
        return newPersonality;
    }

}
