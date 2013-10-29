/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.awt.geom.Point2D;
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
     * @see edu.snu.leader.hidden.AbstractIndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
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
     * @see edu.snu.leader.hidden.IndividualBuilder#build(int)
     */
    @Override
    public SpatialIndividual build( int index )
    {
        // Create a valid location
        Point2D location = createValidLocation( index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                _personality,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_CONFLICT_DIR,
                false );

        return ind;
    }

}
