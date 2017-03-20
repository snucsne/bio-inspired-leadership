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
package edu.snu.leader.hidden;

// Imports
import edu.snu.leader.hidden.event.DepartureEvent;
import java.util.List;
import java.util.Set;

/**
 * ResultsReporter
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface ResultsReporter
{
    /**
     * Initialize this reporter
     *
     * @param simState
     */
    public void initialize( SimulationState simState );

    /**
     * Gather the results from the just-finished simulation
     *
     * @param successful Flag signaling whether or not the initiation was
     *                   successful
     * @param finalInitiatorCount The final count of initiators
     * @param maxInitiatorCount The max count of initiators
     * @param departureHistory
     */
    public void gatherSimulationResults( boolean successful,
            Set<SpatialIndividual> finalInitiators,
            int maxInitiatorCount,
            List<DepartureEvent> departureHistory );

    /**
     * Report the final results
     */
    public void reportFinalResults();
}
