package edu.snu.leader.hidden.builder;

import java.util.EnumMap;
// Imports
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import edu.snu.leader.hidden.PersonalityTrait;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MiscUtils;

/**
 * HomogeneousPersonalityTraitsIndBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class HomogeneousPersonalityTraitsIndBuilder
        extends AbstractIndividualBuilder
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            HomogeneousPersonalityTraitsIndBuilder.class.getName() );

    /** Key for bold default trait value */
    private static final String _BOLD_TRAIT_VALUE_KEY = "bold-trait-value";
    
    /** Key for sociability default trait value */
    private static final String _SOCIAL_TRAIT_VALUE_KEY = "social-trait-value";
    
    /** Key for activity default trait value */
    private static final String _ACTIVITY_TRAIT_VALUE_KEY = "activity-trait-value";
    
    /** Key for fearful default trait value */
    private static final String _FEARFUL_TRAIT_VALUE_KEY = "fearful-trait-value";
    
    
    /** Bold default trait value */
    protected float _boldTraitValue = 0.0f;
    
    /** Social default trait value */
    protected float _socialTraitValue = 0.0f;
    
    /** Activity default trait value */
    protected float _activityTraitValue = 0.0f;
    
    /** Fearful default trait value */
    protected float _fearfulTraitValue = 0.0f;
    
    
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

        // Get the default values
        _boldTraitValue = MiscUtils.loadNonEmptyFloatProperty( props,
                _BOLD_TRAIT_VALUE_KEY,
                "Bold default trait value" );
        _LOG.info( "Using _boldTraitValue=[" + _boldTraitValue + "]" );

        _socialTraitValue = MiscUtils.loadNonEmptyFloatProperty( props,
                _SOCIAL_TRAIT_VALUE_KEY,
                "Social default trait value" );
        _LOG.info( "Using _socialTraitValue=[" + _socialTraitValue + "]" );

        _activityTraitValue = MiscUtils.loadNonEmptyFloatProperty( props,
                _ACTIVITY_TRAIT_VALUE_KEY,
                "Activity default trait value" );
        _LOG.info( "Using _activityTraitValue=[" + _activityTraitValue + "]" );

        _fearfulTraitValue = MiscUtils.loadNonEmptyFloatProperty( props,
                _FEARFUL_TRAIT_VALUE_KEY,
                "Fearful default trait value" );
        _LOG.info( "Using _fearfulTraitValue=[" + _fearfulTraitValue + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The individual
     * @see edu.snu.leader.hidden.builder.IndividualBuilder#build(int)
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
     * Create a map of personality traits for an individual
     *
     * @param index The index of the individual
     * @return The personality traits
     */
    protected Map<PersonalityTrait,Float> createPersonalityTraits( int index )
    {
        Map<PersonalityTrait,Float> traits =
                new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );
        
        // Store the values
        traits.put( PersonalityTrait.BOLD_SHY, new Float( _boldTraitValue ) );
        traits.put( PersonalityTrait.SOCIAL_SOLITARY, new Float( _socialTraitValue ) );
        traits.put( PersonalityTrait.ACTIVE_LAZY, new Float( _activityTraitValue ) );
        traits.put( PersonalityTrait.FEARFUL_ASSERTIVE, new Float( _fearfulTraitValue ) );

        return traits;
    }

}
