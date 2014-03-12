/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
