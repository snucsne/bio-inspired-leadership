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
import org.apache.log4j.Logger;

import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

import java.awt.geom.Point2D;
import java.util.Properties;


/**
 * ForcedPersonalityDistributionIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ForcedPersonalityDistributionIndividualBuilder
        extends AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ForcedPersonalityDistributionIndividualBuilder.class.getName() );

    /** Key for the minimum personality value */
    private static final String _MIN_PERSONALITY_KEY = "min-personality";

    /** Key for the maximum personality value */
    private static final String _MAX_PERSONALITY_KEY = "max-personality";

    /** Key for the max personality individual count value */
    private static final String _MAX_PERSONALITY_IND_COUNT_KEY = "max-personality-ind-count";


    /** The minimum allowable personality value */
    private float _minPersonality = 0.0f;

    /** The maximum allowable personality value */
    private float _maxPersonality = 1.0f;

    /** The max personality individual count */
    private int _maxPersonalityIndCount = 0;

    /** The number of individuals built with a max personality */
    private int _currentMaxPersonalityIndsBuild = 0;


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

        // Get the min personality
        String minPersonalityStr = props.getProperty( _MIN_PERSONALITY_KEY );
        Validate.notEmpty( minPersonalityStr,
                "Minimum personality value (key="
                + _MIN_PERSONALITY_KEY
                + ") may not be empty" );
        _minPersonality = Float.parseFloat( minPersonalityStr );
        _LOG.info( "Using _minPersonality=[" + _minPersonality + "]" );

        // Get the max personality
        String maxPersonalityStr = props.getProperty( _MAX_PERSONALITY_KEY );
        Validate.notEmpty( maxPersonalityStr,
                "Maximum personality value (key="
                + _MAX_PERSONALITY_KEY
                + ") may not be empty" );
        _maxPersonality = Float.parseFloat( maxPersonalityStr );
        _LOG.info( "Using _maxPersonality=[" + _maxPersonality + "]" );

        // Get the max personality individual count
        String maxPersonalityIndCountStr = props.getProperty(
                _MAX_PERSONALITY_IND_COUNT_KEY );
        Validate.notEmpty( maxPersonalityStr,
                "Maximum personality individual count value (key="
                + _MAX_PERSONALITY_IND_COUNT_KEY
                + ") may not be empty" );
        _maxPersonalityIndCount = Integer.parseInt( maxPersonalityIndCountStr );
        _LOG.info( "Using _maxPersonalityIndCount=[" + _maxPersonalityIndCount + "]" );


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
        // Create the personality
        float personality = _minPersonality;
        if( _maxPersonalityIndCount > _currentMaxPersonalityIndsBuild )
        {
            personality = _maxPersonality;
            _currentMaxPersonalityIndsBuild++;
        }

        // Create a valid location
        Point2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                personality,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_RAW_CONFLICT,
                true );

        return ind;
    }

}
