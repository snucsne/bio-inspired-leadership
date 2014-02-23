/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.personality;

// Imports
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.log4j.Logger;


/**
 * MomentumDecay
 *
 * @author Jeremy Acre
 * @version $Revision$ ($Author$)
 */
public class MomentumDecay extends AbstractPersonalityDecayCalculator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( MomentumDecay.class.getName() );

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
            newPersonality = (float) ( ( ( ind.getPersonalityAfterLastInitiation()
                                       - ind.getInitialPersonality() )
                                     * ( 1 - Math.exp( ( ( _simState.getSimIndex()
                                                         - ind.getLastInitiationAttempt() )
                                                       - _decayTime ) / 5.0f ) ) )
                                     + ind.getInitialPersonality() );

        }

//        _LOG.warn( "MomentumDecay: ind=["
//                + ind.getID()
//                + "] old=["
//                + ind.getPersonality()
//                + "] new=["
//                + newPersonality
//                + "] lastAttempt=["
//                + ind.getLastInitiationAttempt()
//                + "] afterLast=["
//                + ind.getPersonalityAfterLastInitiation()
//                + "] initial=["
//                + ind.getInitialPersonality()
//                + "] isDecaying=["
//                + isDecaying( ind )
//                + "] diff=["
//                + ( ind.getPersonalityAfterLastInitiation()
//                        - ind.getInitialPersonality() )
//                + "] exp=["
//                + Math.exp( ( (_simState.getSimIndex() - ind.getLastInitiationAttempt())
//                      - _decayTime ) / 5.0f )
//                + "] simIndex=["
//                + _simState.getSimIndex()
//                + "] decayTime=["
//                + _decayTime
//                + "]" );

        // Return new personality
        return newPersonality;
    }

}
