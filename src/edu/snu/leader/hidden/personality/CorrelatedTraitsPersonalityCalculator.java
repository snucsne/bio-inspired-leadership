package edu.snu.leader.hidden.personality;


//Imports
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.log4j.Logger;
import edu.snu.leader.hidden.PersonalityTrait;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.hidden.Task;
import edu.snu.leader.util.MiscUtils;


/**
 * CorrelatedTraitsPersonalityCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class CorrelatedTraitsPersonalityCalculator
        implements PersonalityCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            CorrelatedTraitsPersonalityCalculator.class.getName() );
    
    
    /** Key for the discount */
    private static final String _DISCOUNT_KEY = "trait-update-discount";

    /** Key for the correlated discount */
    private static final String _CORRELATED_DISCOUNT_KEY = "correlated-trait-update-discount";

    /** Key for the minimum trait value */
    private static final String _MIN_TRAIT_VALUE_KEY = "min-trait-value";

    /** Key for the maximum trait value */
    private static final String _MAX_TRAIT_VALUE_KEY = "max-trait-value";

    
    /** Positive correlation coefficient */
    private static final int _POSITIVE_CORRELATION = 1;
    
    /** Negative correlation coefficient */
    private static final int _NEGATIVE_CORRELATION = -1;

    
    
    /** The update's discount value */
    protected float _discount = 0.0f;
    
    /** The update's discount value for correlated traits */
    protected float _correlatedDiscount = 0.0f;
    
    /** The current simulation state */
    protected SimulationState _simState = null;
    
    /** The update rule's true winner reward */
    protected float _winnerReward = 1.0f;

    /** The update rule's true loser reward */
    protected float _loserPenalty = 0.0f;

    /** The minimum allowable trait value */
    protected float _minTraitValue = 0.0f;
    
    /** The maximum allowable trait value */
    protected float _maxTraitValue = 0.0f;

    /** The current sigma */
    private float _sigma = 0.1f;

    /** The maximum gaussian value */
    private float _maxGaussianValue = 0.0f;

    /** The current Gaussian used to calculate winner reward */
    private Gaussian _gaussian = null;

    
    /**
     * Initializes the updater
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Save the simulation state
        _simState = simState;
        
        // Get the properties
        Properties props = simState.getProps();

        // Get the discount value
        _discount = MiscUtils.loadNonEmptyFloatProperty( props,
                _DISCOUNT_KEY,
                "Trait value discount" );
        _LOG.info( "Using _discount=[" + _discount + "]" );

        // Get the discount value
        _correlatedDiscount = MiscUtils.loadNonEmptyFloatProperty( props,
                _CORRELATED_DISCOUNT_KEY,
                "Correlated trait value discount" );
        _LOG.info( "Using _correlatedDiscount=[" + _correlatedDiscount + "]" );

        // Get the min trait value
        _minTraitValue = MiscUtils.loadNonEmptyFloatProperty( props,
                _MIN_TRAIT_VALUE_KEY,
                "Minimum trait value" );
        _LOG.info( "Using _minTraitValue=[" + _minTraitValue + "]" );

        // Get the min trait value
        _maxTraitValue = MiscUtils.loadNonEmptyFloatProperty( props,
                _MAX_TRAIT_VALUE_KEY,
                "Maximum trait value" );
        _LOG.info( "Using _maxTraitValue=[" + _maxTraitValue + "]" );

        // Create a gaussian for computing changes to discount
        _gaussian = new Gaussian( 0.0f, _sigma );
        _maxGaussianValue = 1.0f / (_sigma * (float) Math.sqrt( 2.0f * Math.PI ) );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculate the new personality value
     *
     * @param currentPersonality The individual's current personality
     * @param updateType The type of update being applied
     * @param followers The number of followers in the initiation
     * @return The updated personality value
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#calculatePersonality(edu.snu.leader.hidden.SpatialIndividual, edu.snu.leader.hidden.personality.PersonalityUpdateType, int)
     */
    @Override
    public float calculatePersonality( SpatialIndividual individual,
            PersonalityUpdateType updateType,
            int followers )
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Calculate the personality trait value(s)
     *
     * @param individual The individual
     * @param updateType The type of update being applied
     * @param task The current task
     * @see edu.snu.leader.hidden.personality.PersonalityCalculator#updateTraits(edu.snu.leader.hidden.SpatialIndividual, edu.snu.leader.hidden.personality.PersonalityUpdateType, edu.snu.leader.hidden.Task)
     */
    @Override
    public void updateTraits( SpatialIndividual individual,
            PersonalityUpdateType updateType,
            Task currentTask )
    {
        // Update depends on the trait type
        Map<PersonalityTrait, Float> traits = null;
        switch( currentTask )
        {
            case NAVIGATE:
                traits = updateTraitsForNavigate( individual, updateType );
                break;
            
            case EXPLORE:
                traits = updateTraitsForExplore( individual, updateType );
                break;
                
            case ESCAPE:
                traits = updateTraitsForEscape( individual, updateType );
                break;
                
            default:
                throw new RuntimeException( "Inavlid task ["
                        + currentTask
                        + "]" );
        }
        
        // Log the new personality traits
        StringBuilder builder = new StringBuilder();
        Iterator<PersonalityTrait> iter = traits.keySet().iterator();
        while( iter.hasNext() )
        {
            PersonalityTrait current = iter.next();
            builder.append( current.name() );
            builder.append( "=[" );
            builder.append( traits.get( current ) );
            builder.append( "] " );
        }
//        _LOG.warn( "Updated traits for ind=["
//                + individual.getID()
//                + "] task=["
//                + currentTask
//                + "] type=["
//                + updateType
//                + "]: "
//                + builder.toString() );
        
        // Store them
        for( PersonalityTrait trait : traits.keySet() )
        {
            individual.setPersonalityTrait( trait, traits.get( trait ) );
        }
    }

    /**
     * Updates the temperament traits for the navigation task
     *
     * @param individual The individual
     * @param updateType The type of update being applied
     * @return The updated traits
     */
    protected Map<PersonalityTrait, Float> updateTraitsForNavigate(
            SpatialIndividual individual,
            PersonalityUpdateType updateType )
    {
        // Default to no discount of the traits
        float discount = _discount;
        float correlatedDiscount = _correlatedDiscount;
        float result = 0.0f;
        
        // Was it successful?
        if( updateType.equals( PersonalityUpdateType.TRUE_WINNER ) )
        {
//            discount = _discount;
//            correlatedDiscount = _correlatedDiscount;
            result = _winnerReward;
        }
        else if( updateType.equals( PersonalityUpdateType.TRUE_LOSER ) )
        {
//            discount = -1 * _discount;
//            correlatedDiscount = -1 * _correlatedDiscount;
            result = _loserPenalty;
        }

        // Also, the individual's position affects its update
        float normalizedMeanTopoDistance = _simState.computeNormalizedMeanTopologicalDistance(
                individual );
        float distanceModifier = (float) _gaussian.value(
                normalizedMeanTopoDistance ) / _maxGaussianValue;
        result *= distanceModifier;
//        correlatedDiscount *= distanceModifier;

//        _LOG.warn( "NAVIGATE [" + updateType + "]: distanceModifier=["
//                + distanceModifier
//                + "] followers=["
//                + individual.getTotalFollowerCount()
//                + "]" );

        // Calculate the updated traits
        Map<PersonalityTrait,Float> traits =
                new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );
        
        // Bold is the predominant trait in this task
        float boldValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.BOLD_SHY ),
                discount,
                result );
        traits.put( PersonalityTrait.BOLD_SHY, new Float( boldValue) );
        
        // Sociability is positively correlated with bold
        float socialValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.SOCIAL_SOLITARY ),
                correlatedDiscount,
                1 - result );
        traits.put( PersonalityTrait.SOCIAL_SOLITARY, new Float( socialValue) );
        
        // Activity is negatively correlated with bold
        float activeValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.ACTIVE_LAZY ),
                correlatedDiscount,
                1 -  result );
        traits.put( PersonalityTrait.ACTIVE_LAZY, new Float( activeValue) );

        // Fearfulness is negatively correlated with bold
        float fearfulValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.FEARFUL_ASSERTIVE ),
                correlatedDiscount,
                1 - result );
        traits.put( PersonalityTrait.FEARFUL_ASSERTIVE, new Float( fearfulValue) );

        return traits;
    }

    /**
     * Updates the temperament traits for the explore task
     *
     * @param individual The individual
     * @param updateType The type of update being applied
     * @return The updated traits
     */
    protected Map<PersonalityTrait, Float> updateTraitsForExplore(
            SpatialIndividual individual,
            PersonalityUpdateType updateType )
    {
        // Default to no discount of the traits
        float discount = _discount;
        float correlatedDiscount = _correlatedDiscount;
        float result = 0.0f;
        

        // Was it successful?
        if( updateType.equals( PersonalityUpdateType.TRUE_WINNER ) )
        {
            result = _winnerReward;
        }
        else if( updateType.equals( PersonalityUpdateType.TRUE_LOSER ) )
        {
            result = _loserPenalty;
        }
        
        // Also, the individual's position affects its update
        float normalizedMeanTopoDistance = _simState.computeNormalizedMeanTopologicalDistance(
                individual );
        float distanceModifier = (float) _gaussian.value(
                1.0f - normalizedMeanTopoDistance ) / _maxGaussianValue;
//        result *= distanceModifier;
//        correlatedDiscount *= distanceModifier;

//        _LOG.warn( "EXPLORE [" + updateType + "]: distanceModifier=["
//                + distanceModifier
//                + "] followers=["
//                + individual.getTotalFollowerCount()
//                + "]" );
        
        // Calculate the updated traits
        Map<PersonalityTrait,Float> traits =
                new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );
        
        // Activity is the predominant trait in this task
        float activeValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.ACTIVE_LAZY ),
                discount,
                result );
        traits.put( PersonalityTrait.ACTIVE_LAZY, new Float( activeValue) );
        
        // Fearfulness is positively correlated with activity
        float fearfulValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.FEARFUL_ASSERTIVE ),
                correlatedDiscount,
                result );
        traits.put( PersonalityTrait.FEARFUL_ASSERTIVE, new Float( fearfulValue) );
        
        // Bold is negatively correlated with activity
        float boldValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.BOLD_SHY ),
                correlatedDiscount,
                1 - result );
        traits.put( PersonalityTrait.BOLD_SHY, new Float( boldValue) );
        
        // Sociability is negatively correlated with activity
        float socialValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.SOCIAL_SOLITARY ),
                correlatedDiscount,
                result );
        traits.put( PersonalityTrait.SOCIAL_SOLITARY, new Float( socialValue) );
        
        return traits;
    }

    /**
     * Updates the temperament traits for the escape task
     *
     * @param individual The individual
     * @param updateType The type of update being applied
     * @return The updated traits
     */
    protected Map<PersonalityTrait, Float> updateTraitsForEscape(
            SpatialIndividual individual,
            PersonalityUpdateType updateType )
    {
        // Default to no result
        float discount = _discount;
        float correlatedDiscount = _correlatedDiscount;
        float result = 0.0f;
        
        // Was it successful?
        // We may have other options at some point so be excplicit
        if( updateType.equals( PersonalityUpdateType.TRUE_WINNER ) )
        {
            result = _winnerReward;
        }
        else if( updateType.equals( PersonalityUpdateType.TRUE_LOSER ) )
        {
            result = _loserPenalty;
        }
        
        // Also, the individual's position affects its update
        float normalizedMeanTopoDistance = _simState.computeNormalizedMeanTopologicalDistance(
                individual );
        float distanceModifier = (float) _gaussian.value(
                1.0f - normalizedMeanTopoDistance ) / _maxGaussianValue;
//        result *= distanceModifier;
//        correlatedDiscount *= distanceModifier;

//        _LOG.warn( "ESCAPE [" + updateType + "]: distanceModifier=["
//                + distanceModifier
//                + "] followers=["
//                + individual.getTotalFollowerCount()
//                + "]" );
        
        // Calculate the updated traits
        Map<PersonalityTrait,Float> traits =
                new EnumMap<PersonalityTrait, Float>( PersonalityTrait.class );
        
        // Fearfulness is the predominant trait in this task
        float fearfulValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.FEARFUL_ASSERTIVE ),
                discount,
                result );
        traits.put( PersonalityTrait.FEARFUL_ASSERTIVE, new Float( fearfulValue) );
        
        // Activity is positively correlated with fearfulness
        float activeValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.ACTIVE_LAZY ),
                correlatedDiscount,
                result );
        traits.put( PersonalityTrait.ACTIVE_LAZY, new Float( activeValue) );
        
        // Bold is negatively correlated with fearfulness
        float boldValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.BOLD_SHY ),
                correlatedDiscount,
                1 - result );
        traits.put( PersonalityTrait.BOLD_SHY, new Float( boldValue) );
        
        // Sociability is positively correlated with fearfulness
        float socialValue = updatePersonalityTrait(
                individual.getPersonalityTrait( PersonalityTrait.SOCIAL_SOLITARY ),
                correlatedDiscount,
                result );
        traits.put( PersonalityTrait.SOCIAL_SOLITARY, new Float( socialValue) );
        
        return traits;
    }

    /**
     * Calculate the new trait value using the standard update rule
     *
     * @param initialTraitValue
     * @param discount
     * @param result
     * @return The updated trait value
     */
    protected float updatePersonalityTrait( float initialTraitValue,
            float discount,
            float result )
    {
        // Calculate the new value
        float traitValue = ( (1.0f - discount) * initialTraitValue )
                + (discount * result);
        
        // Ensure it is valid
        traitValue = ensureValidTraitValue( traitValue );
        
//        _LOG.warn( "oldTraitValue=["
//                + initialTraitValue
//                + "] newTraitValue=["
//                + traitValue
//                + "]" );
        
        return traitValue;
    }
    
    /**
     * Ensure that the trait value is within bounds
     *
     * @param traitValue A calculated trait value
     * @return The new trait value within bounds
     */
    protected float ensureValidTraitValue( float traitValue )
    {
        // Is it under the minimum?
        if( _minTraitValue > traitValue )
        {
            traitValue = _minTraitValue;
        }
        // Is it over the maximum?
        else if( _maxTraitValue < traitValue )
        {
            traitValue = _maxTraitValue;
        }

        return traitValue;
    }

}
