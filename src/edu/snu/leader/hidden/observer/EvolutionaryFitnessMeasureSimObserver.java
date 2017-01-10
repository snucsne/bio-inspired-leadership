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
package edu.snu.leader.hidden.observer;

import java.util.LinkedList;
import java.util.List;

// Imports
import org.apache.log4j.Logger;
import edu.snu.leader.hidden.evolution.FitnessMeasures;


/**
 * EvolutionaryFitnessMeasureSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class EvolutionaryFitnessMeasureSimObserver
        extends AbstractSimulationObserver
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            EvolutionaryFitnessMeasureSimObserver.class.getName() );

    /** All the fitness measures from the simulations */
    private List<FitnessMeasures> _allFitnessMeasures =
            new LinkedList<FitnessMeasures>();


    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        // Get the fitness measures from the simulation state
        _allFitnessMeasures.add( _simState.getFitnessMeasures() );
    }
    
    /**
     * Returns all the fitness measures from the different simulations
     *
     * @return All the fitness measures
     */
    public List<FitnessMeasures> getAllFitnessMeasures()
    {
        return new LinkedList<FitnessMeasures>( _allFitnessMeasures );
    }
}