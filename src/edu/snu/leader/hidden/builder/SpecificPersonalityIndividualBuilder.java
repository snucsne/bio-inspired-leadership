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
package edu.snu.leader.hidden.builder;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import java.util.Properties;


/**
 * SpecificPersonalityIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SpecificPersonalityIndividualBuilder
        extends AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SpecificPersonalityIndividualBuilder.class.getName() );

    /** Key for the specified personality value */
    private static final String _SPECIFIC_PERSONALITY = "specific-personality";

    /** The specific personality for all individuals */
    private float _personality = 0.0f;

    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the specific personality
        String specificPersonalityStr = props.getProperty( _SPECIFIC_PERSONALITY );
        Validate.notEmpty( specificPersonalityStr,
                "Specific personality value (key="
                + _SPECIFIC_PERSONALITY
                + ") may not be empty" );
        _personality = Float.parseFloat( specificPersonalityStr );
        _LOG.info( "Using _personality=[" + _personality + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }


    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The individual
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create a valid location
        Vector2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                _personality,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_RAW_CONFLICT,
                false );

        return ind;
    }

}
