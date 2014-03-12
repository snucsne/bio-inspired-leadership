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

import edu.snu.leader.hidden.SpatialIndividual;

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
