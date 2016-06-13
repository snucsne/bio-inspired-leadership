/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.builder;

// Imports
import edu.snu.leader.hidden.PersonalityTrait;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * SpecificMultiplerPersonalityTraitsIndividualBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SpecificMultiplePersonalityTraitsIndividualBuilder extends
        AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            SpecificMultiplePersonalityTraitsIndividualBuilder.class.getName() );

    /** Key for the personality traits file */
    private static final String _PERSONALITY_TRAITS_FILE = "personality-traits-file";


    /** The predefined personality traits for all individuals */
    protected List<Map<PersonalityTrait,Float>> _allPersonalityTraits =
            new ArrayList<Map<PersonalityTrait,Float>>();


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Was a personality traits file supplied?
        String personalityTraitsFileStr = props.getProperty(
                _PERSONALITY_TRAITS_FILE );
        Validate.notEmpty( personalityTraitsFileStr,
                "Personality traits file (key=["
                + _PERSONALITY_TRAITS_FILE
                + "]) ");

        // Load it
        loadPersonalityTraits( personalityTraitsFileStr );
        _LOG.info( "Using personality traits file ["
                + personalityTraitsFileStr
                + "]" );

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

        // Get the personality traits
        Map<PersonalityTrait,Float> personalityTraits = createPersonalityTraits(
                index );

        // Create the individual
        SpatialIndividual ind = new SpatialIndividual(
                generateUniqueIndividualID( index ),
                location,
                personalityTraits,
                DEFAULT_ASSERTIVENESS,
                DEFAULT_PREFERRED_DIR,
                DEFAULT_RAW_CONFLICT,
                false );

        return ind;
    }

    /**
     * Load the personality traits from the specified file
     *
     * @param filename The personality traits file
     */
    protected void loadPersonalityTraits( String filename )
    {
        _LOG.trace( "Entering loadPersonalityTraits( filename )" );

        // Try to process the file
        try
        {
            // Create a reader
            BufferedReader reader = new BufferedReader(
                    new FileReader( filename ) );

            // Process each line
            String line;
            while( null != (line = reader.readLine()) )
            {
                // Is it a comment or empty?
                if( (0 == line.length()) || line.startsWith( "#" ) )
                {
                    // Yup
                    continue;
                }

                // Split it
                String[] parts = line.split( "\\s+" );

                // Parse it
                Float meanShortestPath = new Float( parts[0] );
                Float bold = new Float( parts[1] );
                Float sociability = new Float( parts[2] );
                Float exploration = new Float( parts[3] );
                Float escape = new Float( parts[4] );

                // Add it to the list
                Map<PersonalityTrait,Float> personalityTraits =
                        new EnumMap<PersonalityTrait,Float>( PersonalityTrait.class );
                personalityTraits.put( PersonalityTrait.BOLDNESS_SHYNESS, bold );
                personalityTraits.put( PersonalityTrait.SOCIABILITY, sociability );
                personalityTraits.put( PersonalityTrait.EXPLORATION, exploration );
                personalityTraits.put( PersonalityTrait.ESCAPE, escape );
                _allPersonalityTraits.add( personalityTraits );
            }
        }
        catch( Exception e )
        {
            _LOG.error( "Unable to read personality traits file ["
                    + filename
                    + "]", e );
            throw new RuntimeException( "Unable to read personality traits file ["
                    + filename
                    + "]" );
        }

        _LOG.trace( "Leaving loadPersonalityTraits( filename )" );
    }

    /**
     *
     * Create a map of personality traits for an individual
     *
     * @param index The index of the individual
     * @return The personality traits
     */
    protected Map<PersonalityTrait,Float> createPersonalityTraits( int index )
    {
        // Just load the ones from the file for now
        Map<PersonalityTrait,Float> personalityTraits =
                _allPersonalityTraits.get( index );

        return personalityTraits;
    }
}
