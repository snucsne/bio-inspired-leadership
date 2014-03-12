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
package edu.snu.leader.simple;

// Imports
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleProblemForm;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import edu.snu.jyperneat.core.NeatIndividual;
import edu.snu.jyperneat.core.Network;
import edu.snu.leader.util.CrossValidationFitness;
import edu.snu.leader.util.IndividualDescriber;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * DecisionDevelopmentProblem
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class DecisionDevelopmentProblem extends Problem
        implements SimpleProblemForm, IndividualDescriber
{
    /** Default serial version UID */
    private static final long serialVersionUID = 1L;

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            DecisionDevelopmentProblem.class.getName() );


    private enum PredationInputType {
        ACCURATE {
            @Override
            double calculateInput( float predationLevel,
                    float noiseMultiplier,
                    MersenneTwisterFast random )
            {
                return predationLevel;
            }
        },
        NOISY {
            @Override
            double calculateInput( float predationLevel,
                    float noiseMultiplier,
                    MersenneTwisterFast random )
            {
                double noisyPredationLevel = predationLevel
                        + noiseMultiplier * random.nextGaussian();
                return Math.min( 1.0,
                        Math.max( 0.0, noisyPredationLevel ) );
            }
        },
        RANDOM {
            @Override
            double calculateInput( float predationLevel,
                    float noiseMultiplier,
                    MersenneTwisterFast random )
            {
                return random.nextDouble();
            }
        },
        ZERO {
            @Override
            double calculateInput( float predationLevel,
                    float noiseMultiplier,
                    MersenneTwisterFast random )
            {
                return 0.0;
            }
        },
        OFF {
            @Override
            double calculateInput( float predationLevel,
                    float noiseMultiplier,
                    MersenneTwisterFast random )
            {
                return -1.0;
            }
        };

        abstract double calculateInput( float predationLevel,
                float noiseMultiplier,
                MersenneTwisterFast random );
    };

    /** The bias node's name */
    private static final String _BIAS_NODE_NAME = "Bias";

    /** The predation level node's name */
    private static final String _PREDATION_NODE_NAME = "Predation";

    /** The energy node name */
    private static final String _ENERGY_NODE_NAME = "Energy";

    /** The maturation node name */
    private static final String _MATURATION_NODE_NAME = "Maturation";

    /** The other individual's activity node name */
    private static final String _OTHER_ACTIVITY_NODE_NAME = "Other-Activity";

    /** The sensor failure node name */
    private static final String _SENSOR_FAILURE_NODE_NAME = "Sensor-Failure";

    /** The output node's name */
    private static final String _OUTPUT_NODE_NAME = "Output";

    /** The follow output node name */
    private static final String _FOLLOW_OUTPUT_NODE_NAME = "Follow-Output";

    /** The no-follow output node name */
    private static final String _NO_FOLLOW_OUTPUT_NODE_NAME = "No-Follow-Output";

    /** The bias input value */
    private static final double _BIAS_INPUT_VALUE = 0.03;

    /** The minimum amount of energy for survival of a timestep */
    private static final float _MIN_ENERGY_THRESHOLD = 0.001f;

    /** The description format */
    private static final String _DESCRIPTION_FORMAT = "%1$4.2f   %2$4.2f   %3$4.2f   %4$4.2f   %5$4.2f";


    /** Parameter key for the maximum number of timesteps */
    private static final String _MAX_TIMESTEPS_KEY = "max-timesteps";

    /** Parameter key for the minimum energy consumed per timestep */
    private static final String _MIN_ENERGY_CONSUMED_PER_TIMESTEP_KEY =
            "min-energy-consumed-per-timestep";

    /** Parameter key for the multiplier multiplied by the activity level to
     * compute energy loss */
    private static final String _ACTIVITY_ENERGY_LOSS_MULTIPLIER_KEY =
            "activity-energy-loss-multiplier";

    /** Parameter key for the maturation energy threshold */
    private static final String _MATURATION_ENERGY_THRESHOLD_KEY =
            "maturation-energy-threshold";

    /** Parameter key for the amount of energy consumed per timestep by
     * maturation */
    private static final String _MATURATION_ENERGY_CONSUMED_PER_TIMESTEP_KEY =
            "maturation-energy-consumed-per-timestep";

    /** Parameter key for the multiplier multiplied by the activity level to
     * compute energy gain */
    private static final String _ENERGY_GAIN_MULTIPLIER_KEY =
            "energy-gain-multiplier";

    /** Parameter key for the initial energy level */
    private static final String _INITIAL_ENERGY_LEVEL_KEY =
            "initial-energy-level";

    /** Parameter key for the number of evaluations to perform per network */
    private static final String _EVALUATION_COUNT_KEY = "evaluation-count";

    /** Parameter key for the number of evaluations to perform using the
     * alternate predation input type */
    private static final String _ALT_EVALUATION_COUNT_KEY = "alt-evaluation-count";

    /** Parameter key for the number of steps to use for describing a network */
    private static final String _DESCRIPTION_STEP_COUNT_KEY =
            "description-step-count";

    /** Parameter key for the type of predation input */
    private static final String _PREDATION_INPUT_TYPE_KEY =
            "predation-input-type";

    /** Parameter key for the alternate type of predation input */
    private static final String _ALT_PREDATION_INPUT_TYPE_KEY =
            "alt-predation-input-type";

    /** Parameter key for predation level noise multiplier */
    private static final String _PREDATION_NOISE_MULTIPLIER_KEY =
            "predation-noise-multiplier";

    /** Parameter key for the other's network file */
    private static final String _OTHER_NETWORK_FILE = "other-network-file";

    /** Parameter key for the type of predation input for the other ind */
    private static final String _OTHER_PREDATION_INPUT_TYPE_KEY =
            "other-predation-input-type";

    /** Parameter key for the alternate type of predation input for the other ind */
    private static final String _ALT_OTHER_PREDATION_INPUT_TYPE_KEY =
            "alt-other-predation-input-type";

    /** Parameter key for the other's predation level noise multiplier */
    private static final String _OTHER_PREDATION_NOISE_MULTIPLIER_KEY =
            "other-predation-noise-multiplier";

    /** Parameter key for the follow network file */
    private static final String _FOLLOW_NETWORK_FILE = "follow-network-file";

    /** Parameter key for the no-follow network file */
    private static final String _NO_FOLLOW_NETWORK_FILE = "no-follow-network-file";




    /** The maximum number of timesteps */
    private int _maxTimesteps = 0;

    /** The energy consumed per timestep */
    private float _minEnergyConsumedPerTimestep = 0.0f;

    /** The multiplier multiplied by the activity level to compute energy loss */
    private float _activityEnergyLossMultiplier = 0.0f;

    /** The energy threshold for maturation to occur */
    private float _maturationEnergyThreshold = 0.0f;

    /** The amount of energy consumed per timestep by maturation */
    private float _maturationEnergyConsumedPerTimestep = 0.0f;

    /** The multiplier multiplied by the activity level to compute energy gain */
    private float _energyGainMultiplier = 0.0f;

    /** The initial energy level of the individual */
    private float _initialEnergyLevel = 0.0f;

    /** The number of evaluations to perform per network */
    private int _evaluationCount = 0;

    /** The number of evaluations to perform using the alternation predation
     * input type */
    private int _altEvaluationCount = 0;

    /** The number of steps to use for describing a network */
    private int _descriptionStepCount = 0;

    /** The type of predation input */
    private PredationInputType _predationInputType = PredationInputType.ACCURATE;

    /** The alternate type of predation input */
    private PredationInputType _altPredationInputType = PredationInputType.ACCURATE;

    /** The predation level noise multiplier */
    private float _predationNoiseMultiplier = 0.0f;

    /** The other individual's network */
    private Network _otherNetwork = null;

    /** The type of predation input for the other individual */
    private PredationInputType _otherPredationInputType = PredationInputType.ACCURATE;

    /** The alternate type of predation input for the other individual */
    private PredationInputType _altOtherPredationInputType = PredationInputType.ACCURATE;

    /** The other individual's predation level noise multiplier */
    private float _otherPredationNoiseMultiplier = 0.0f;

    private Network _followNetwork = null;
    private Network _noFollowNetwork = null;


    /**
     * Sets up the object by reading it from the parameters stored in
     * state, built off of the parameter base base.
     *
     * @param state
     * @param base
     * @see ec.Problem#setup(ec.EvolutionState, ec.util.Parameter)
     */
    @Override
    public void setup( EvolutionState state, Parameter base )
    {
        _LOG.trace( "Entering setup( state, base )" );

        // Call the superclass implementation
        super.setup( state, base );

        // Get the maximum number of timesteps
        Validate.isTrue( state.parameters.exists( base.push( _MAX_TIMESTEPS_KEY ) ),
                "Max timesteps parameter not found" );
        _maxTimesteps = state.parameters.getInt(
                base.push( _MAX_TIMESTEPS_KEY ),
                null );
        _LOG.info( "Using maxTimesteps=[" + _maxTimesteps + "]" );

        // Get the energy consumed per timestep
        Validate.isTrue( state.parameters.exists( base.push(
                _MIN_ENERGY_CONSUMED_PER_TIMESTEP_KEY  ) ),
                "Energy consumed per timestep not found" );
        _minEnergyConsumedPerTimestep = state.parameters.getFloat(
                base.push( _MIN_ENERGY_CONSUMED_PER_TIMESTEP_KEY ),
                null );
        _LOG.info( "Using minEnergyConsumedPerTimestep=["
                + _minEnergyConsumedPerTimestep
                + "]" );

        // Get the activity energy loss multiplier
        Validate.isTrue( state.parameters.exists( base.push(
                _ACTIVITY_ENERGY_LOSS_MULTIPLIER_KEY  ) ),
                "Energy consumed per timestep not found" );
        _activityEnergyLossMultiplier = state.parameters.getFloat(
                base.push( _ACTIVITY_ENERGY_LOSS_MULTIPLIER_KEY ),
                null );
        _LOG.info( "Using activityEnergyLossMultiplier=["
                + _activityEnergyLossMultiplier
                + "]" );

        // Get the maturation energy threshold
        Validate.isTrue( state.parameters.exists( base.push(
                _MATURATION_ENERGY_THRESHOLD_KEY ) ),
                "Maturation energy threshold not found" );
        _maturationEnergyThreshold = state.parameters.getFloat(
                base.push( _MATURATION_ENERGY_THRESHOLD_KEY ),
                null );
        _LOG.info( "Using maturationEnergyThreshold=["
                + _maturationEnergyThreshold
                + "]" );

        // Get the maturation energy consumed per timestep
        Validate.isTrue( state.parameters.exists( base.push(
                _MATURATION_ENERGY_CONSUMED_PER_TIMESTEP_KEY ) ),
                "Maturation energy consumed per timestep not found" );
        _maturationEnergyConsumedPerTimestep = state.parameters.getFloat(
                base.push( _MATURATION_ENERGY_CONSUMED_PER_TIMESTEP_KEY ),
                null );
        _LOG.info( "Using maturationEnergyConsumedPerTimestep=["
                + _maturationEnergyConsumedPerTimestep
                + "]" );

        // Get the maturation energy threshold
        Validate.isTrue( state.parameters.exists( base.push(
                _ENERGY_GAIN_MULTIPLIER_KEY ) ),
                "Energy gain multiplier not found" );
        _energyGainMultiplier = state.parameters.getFloat(
                base.push( _ENERGY_GAIN_MULTIPLIER_KEY ),
                null );
        _LOG.info( "Using energyGainMultiplier=["
                + _energyGainMultiplier
                + "]" );

        // Get the initial energy level
        Validate.isTrue( state.parameters.exists( base.push(
                _INITIAL_ENERGY_LEVEL_KEY ) ),
                "Energy gain multiplier not found" );
        _initialEnergyLevel = state.parameters.getFloat(
                base.push( _INITIAL_ENERGY_LEVEL_KEY ),
                null );
        _LOG.info( "Using initialEnergyLevel=["
                + _initialEnergyLevel
                + "]" );

        // Get the evaluation count
        Validate.isTrue( state.parameters.exists( base.push(
                _EVALUATION_COUNT_KEY ) ),
                "Evaluation count not found" );
        _evaluationCount = state.parameters.getInt(
                base.push( _EVALUATION_COUNT_KEY ),
                null );
        _LOG.info( "Using evaluationCount=["
                + _evaluationCount
                + "]" );

        /* Get the number of evaluations to perform using the alternation
         * predation input type */
        _altEvaluationCount = state.parameters.getInt(
                base.push( _ALT_EVALUATION_COUNT_KEY ),
                null,
                -1 );
        _LOG.info( "Using altEvaluationCount=["
                + _altEvaluationCount
                + "]" );

        // Get the initial energy level
        Validate.isTrue( state.parameters.exists( base.push(
                _DESCRIPTION_STEP_COUNT_KEY ) ),
                "Description step count not found" );
        _descriptionStepCount = state.parameters.getInt(
                base.push( _DESCRIPTION_STEP_COUNT_KEY ),
                null );
        _LOG.info( "Using descriptionStepCount=["
                + _descriptionStepCount
                + "]" );

        // Get the predation input type
        Validate.isTrue( state.parameters.exists( base.push(
                _PREDATION_INPUT_TYPE_KEY ) ),
                "Predation input type not found" );
        String predationInputType = state.parameters.getString(
                base.push( _PREDATION_INPUT_TYPE_KEY ),
                null );
        _predationInputType = PredationInputType.valueOf(
                predationInputType.toUpperCase().trim() );
        Validate.notNull( _predationInputType,
                "Unknown predation input type ["
                + predationInputType
                + "]" );
        _LOG.info( "Using predationInputType=["
                + _predationInputType
                + "]" );

        // Get the alternate predation input type
        String altPredationInputType = state.parameters.getString(
                base.push( _ALT_PREDATION_INPUT_TYPE_KEY ),
                null );
        if( null != altPredationInputType )
        {
            _altPredationInputType = PredationInputType.valueOf(
                    altPredationInputType.toUpperCase().trim() );
            Validate.notNull( _altPredationInputType,
                    "Unknown alternate predation input type ["
                    + _altPredationInputType
                    + "]" );
            _LOG.info( "Using altPredationInputType=["
                    + _altPredationInputType
                    + "]" );
        }


        // Get the predation noise level multiplier
        _predationNoiseMultiplier = state.parameters.getFloat(
                base.push( _PREDATION_NOISE_MULTIPLIER_KEY ),
                null,
                0.0f );
        _LOG.info( "Using predationNoiseMultiplier=["
                + _predationNoiseMultiplier
                + "]" );

        // Get the other's serialized network file (if specified)
        if( state.parameters.exists( base.push( _OTHER_NETWORK_FILE ) ) )
        {
            String otherNetworkFile = state.parameters.getString(
                    base.push( _OTHER_NETWORK_FILE ),
                    null );

            // Deserialize the network file
            try
            {
                ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream( otherNetworkFile ) );
                Object obj = in.readObject();
                in.close();
                _otherNetwork = (Network) obj;
            }
            catch( Exception e )
            {
                _LOG.error( "Unable to load other's serialized network from ["
                        + otherNetworkFile
                        + "]",
                        e );
                System.err.println( "Unable to load other's serialized network from ["
                        + otherNetworkFile
                        + "]" );
                e.printStackTrace();
                System.exit( 1 );
            }

            // Get the follow network file
            String followNetworkFile = state.parameters.getString(
                    base.push( _FOLLOW_NETWORK_FILE ),
                    null );

            // Deserialize the network file
            try
            {
                ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream( followNetworkFile ) );
                Object obj = in.readObject();
                in.close();
                _followNetwork = (Network) obj;
            }
            catch( Exception e )
            {
                _LOG.error( "Unable to load follow serialized network from ["
                        + followNetworkFile
                        + "]",
                        e );
                System.err.println( "Unable to load follow serialized network from ["
                        + followNetworkFile
                        + "]" );
                e.printStackTrace();
                System.exit( 1 );
            }

            // Get the no-follow network file
            String noFollowNetworkFile = state.parameters.getString(
                    base.push( _NO_FOLLOW_NETWORK_FILE ),
                    null );

            // Deserialize the network file
            try
            {
                ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream( noFollowNetworkFile ) );
                Object obj = in.readObject();
                in.close();
                _noFollowNetwork = (Network) obj;
            }
            catch( Exception e )
            {
                _LOG.error( "Unable to load no-follow serialized network from ["
                        + noFollowNetworkFile
                        + "]",
                        e );
                System.err.println( "Unable to load no-follow serialized network from ["
                        + noFollowNetworkFile
                        + "]" );
                e.printStackTrace();
                System.exit( 1 );
            }



            // Get the predation input type
            Validate.isTrue( state.parameters.exists( base.push(
                    _OTHER_PREDATION_INPUT_TYPE_KEY ) ),
                    "Other predation input type not found" );
            String otherPredationInputType = state.parameters.getString(
                    base.push( _OTHER_PREDATION_INPUT_TYPE_KEY ),
                    null );
            _otherPredationInputType = PredationInputType.valueOf(
                    otherPredationInputType.toUpperCase().trim() );
            Validate.notNull( _otherPredationInputType,
                    "Unknown other predation input type ["
                    + otherPredationInputType
                    + "]" );
            _LOG.info( "Using otherPredationInputType=["
                    + _otherPredationInputType
                    + "]" );

            // Get the alternate predation input type
            String otherAltPredationInputType = state.parameters.getString(
                    base.push( _ALT_OTHER_PREDATION_INPUT_TYPE_KEY ),
                    null );
            if( null != otherAltPredationInputType )
            {
                _altOtherPredationInputType = PredationInputType.valueOf(
                        otherAltPredationInputType.toUpperCase().trim() );
                Validate.notNull( otherAltPredationInputType,
                        "Unknown alternate other predation input type ["
                        + otherAltPredationInputType
                        + "]" );
                _LOG.info( "Using altOtherPredationInputType=["
                        + _altOtherPredationInputType
                        + "]" );
            }

            // Get the noise multiplier
            _otherPredationNoiseMultiplier = state.parameters.getFloat(
                    base.push( _OTHER_PREDATION_NOISE_MULTIPLIER_KEY ),
                    null,
                    0.0f );
            _LOG.info( "Using otherPredationNoiseMultiplier=["
                    + _otherPredationNoiseMultiplier
                    + "]" );
        }

        _LOG.trace( "Leaving setup( state, base )" );
    }


    /**
     * Evaluates the individual in ind, if necessary (perhaps not
     * evaluating them if their evaluated flags are true), and sets
     * their fitness appropriately.
     *
     * @param state
     * @param ind
     * @param subpopulation
     * @param threadnum
     * @see ec.simple.SimpleProblemForm#evaluate(ec.EvolutionState, ec.Individual, int, int)
     */
    @Override
    public void evaluate( EvolutionState state,
            Individual ind,
            int subpopulation,
            int threadnum )
    {
        // Has the individual already been evaluated?
        if( ind.evaluated )
        {
            // Yup, bail out early
            return;
        }

        // Is it the correct type of individual?
        if( !(ind instanceof NeatIndividual) )
        {
            // Nope, complain
            _LOG.error( "Individual is not of correct type ["
                    + ind.getClass().getCanonicalName()
                    + "]" );
            state.output.fatal( "Individual is not the correct type" );
        }

        // Cast it to the correct type
        NeatIndividual neatInd = (NeatIndividual) ind;

        // Build the network
        Network network = neatInd.createPhenotype();

        // Evaluate the network
        MersenneTwisterFast random = state.random[threadnum];
        float[] trainFitness = new float[ _evaluationCount ];
        for( int i = 0; i < _evaluationCount; i++ )
        {
            trainFitness[i] = evaluateNetwork( network,
                    random,
                    (i < _altEvaluationCount) );
            network.reinitialize( random );
        }
        ((CrossValidationFitness) ind.fitness).setTrainingResults(
                trainFitness );

        // Average them to get the total evolutionary fitness
        float evoFitness = 0.0f;
        for( int i = 0; i < trainFitness.length; i++ )
        {
            evoFitness += trainFitness[i];
        }
        evoFitness /= trainFitness.length;

        if( evoFitness > 0.9999f )
        {
            _LOG.debug( "PERFECT!!" );
        }

        // Set the individual's fitness
        ((CrossValidationFitness) ind.fitness).setFitness( state,
                evoFitness,
                false );

        // Mark the individual as evaluated
        neatInd.evaluated = true;
    }

    /**
     * Returns a description of the specified individual using the specified
     * line prefix.
     *
     * @param ind The individual to describe
     * @param prefix The prefix for every line in the description
     * @param statDir The statistics directory
     * @return A description of the individual
     * @see edu.snu.leader.util.IndividualDescriber#describe(ec.Individual, java.lang.String)
     */
    @Override
    public String describe( Individual ind, String prefix, String statDir )
    {
        _LOG.trace( "Entering describe( ind, prefix )" );

        // Is it the correct type of individual?
        if( !(ind instanceof NeatIndividual) )
        {
            // Nope, complain
            _LOG.error( "Individual is not of correct type ["
                    + ind.getClass().getCanonicalName()
                    + "]" );
            throw new IllegalArgumentException(
                    "Individual is not of correct type" );
        }

        // Cast it to the correct type
        NeatIndividual neatInd = (NeatIndividual) ind;

        // Build the network
        Network network = neatInd.createPhenotype();

        // Serialize the network
        String serializeFilename = "network-"
                + buildDescriptionFilenameBase( prefix )
                + "-"
                + System.getProperty( "hostname" )
                + "-runid-"
                + System.getProperty( "run-id", "default" )
                + ".ser";
        try
        {
            ObjectOutputStream objOut = new ObjectOutputStream(
                    new FileOutputStream( new File( statDir, serializeFilename ) ) );
            objOut.writeObject( network );
            objOut.close();
        }
        catch( FileNotFoundException fnfe )
        {
            _LOG.error( "Unable to open network serialization file ["
                    + serializeFilename
                    + "]",
                    fnfe );
            return "ERROR";
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to open network serialization file ["
                    + serializeFilename
                    + "]",
                    ioe );
            return "ERROR";
        }

        _LOG.trace( "Leaving describe( ind, prefix )" );

        return prefix + "file = " + serializeFilename + NEWLINE;
    }

    /**
     * TODO Method description
     *
     * @param ind
     * @param state
     * @param subpopulation
     * @param threadnum
     * @param log
     * @param verbosity
     * @see ec.simple.SimpleProblemForm#describe(ec.Individual, ec.EvolutionState, int, int, int, int)
     */
    @Override
    public void describe( Individual ind,
            EvolutionState state,
            int subpopulation,
            int threadnum,
            int log,
            int verbosity )
    {
        throw new RuntimeException( "NOT YET IMPLEMENTED" );
    }

    /**
     * Evaluates the network in the maturation simulation
     *
     * @param network
     * @param random
     * @param useAlt
     * @return
     */
    private float evaluateNetwork( Network network,
            MersenneTwisterFast random,
            boolean useAlt )
    {
        // Which predation types do we use?
        PredationInputType thisPredationType = _predationInputType;
        PredationInputType otherPredationType = _otherPredationInputType;
        if( useAlt )
        {
            thisPredationType = _altPredationInputType;
            otherPredationType = _altOtherPredationInputType;
        }
//_LOG.warn( "  OtherPredationType is: " + otherPredationType.name() );

        _LOG.debug( "thisPredationType=["
                + thisPredationType
                + "] otherPredationType=["
                + otherPredationType
                + "]" );

        // Compute a random offset for the predation level
        float predationLevelOffset = random.nextFloat();

        // Create some variables needed for the simulation
        float maturationLevel = 0.0f;
        float energyLevel = _initialEnergyLevel;
        float predationLevel = 0.0f;

        // Run the simulation
        for( int i = 0; i < _maxTimesteps; ++i )
        {
            // Calculate the predation level
            predationLevel = calculatePredationLevel( i, predationLevelOffset );

            // Send the inputs to the network
            network.reinitialize( random );
            network.setValue( _BIAS_NODE_NAME, _BIAS_INPUT_VALUE );
            network.setValue( _ENERGY_NODE_NAME, energyLevel );
            network.setValue( _MATURATION_NODE_NAME, maturationLevel );

            // Send the appropriate predation input
            network.setValue( _PREDATION_NODE_NAME,
                    thisPredationType.calculateInput( predationLevel,
                            _predationNoiseMultiplier,
                            random ) );

            // Send the status of our predation sensor
            float sensorFailure = 0.0f;
            if( PredationInputType.RANDOM.equals( thisPredationType ) )
            {
                sensorFailure = 1.0f;
            }
            network.setValue( _SENSOR_FAILURE_NODE_NAME,
                    sensorFailure );

            // Send the appropriate input from the other's actions
            double otherActivity = 0.0;
            if( null == _otherNetwork )
            {
                otherActivity = 0.0f;
            }
            else if( PredationInputType.OFF.equals( otherPredationType ) )
            {
                otherActivity = -1.0f;
            }
            else
            {
                // Find out what the other individual will do
                _otherNetwork.reinitialize( random );
                _otherNetwork.setValue( _BIAS_NODE_NAME, _BIAS_INPUT_VALUE );
                _otherNetwork.setValue( _ENERGY_NODE_NAME, energyLevel );
                _otherNetwork.setValue( _MATURATION_NODE_NAME, maturationLevel );
                _otherNetwork.setValue( _PREDATION_NODE_NAME,
                        otherPredationType.calculateInput(
                                predationLevel,
                                _otherPredationNoiseMultiplier,
                                random ) );
                _otherNetwork.update();
                otherActivity = _otherNetwork.getValue( _OUTPUT_NODE_NAME );
            }

            // Send it to the current individual's network
            network.setValue( _OTHER_ACTIVITY_NODE_NAME, otherActivity );

            // Update the network
            network.update();

            // Get the values for the follow/no-follow outputs
            float followStrength = (float) network.getValue( _FOLLOW_OUTPUT_NODE_NAME );
            float noFollowStrength = (float) network.getValue( _NO_FOLLOW_OUTPUT_NODE_NAME );

//            _LOG.info( "FollowStrength=[" + followStrength + "]" );
//            _LOG.info( "NoFollowStrength=[" + noFollowStrength + "]" );

            // Do we follow?
            float activity = 0.0f;
            if( followStrength > noFollowStrength )
            {
                // Yup
//                _LOG.info( "Following" );
                _followNetwork.reinitialize( random );
                _followNetwork.setValue( _BIAS_NODE_NAME, _BIAS_INPUT_VALUE );
                _followNetwork.setValue( _ENERGY_NODE_NAME, energyLevel );
                _followNetwork.setValue( _MATURATION_NODE_NAME, maturationLevel );
                _followNetwork.setValue( _PREDATION_NODE_NAME,
                        thisPredationType.calculateInput(
                                predationLevel,
                                _otherPredationNoiseMultiplier,
                                random ) );
                _followNetwork.setValue( _OTHER_ACTIVITY_NODE_NAME, otherActivity );
                _followNetwork.update();

                activity = (float) _followNetwork.getValue( _OUTPUT_NODE_NAME );
            }
            else
            {
                // Nope
//                _LOG.info( "NOT Following" );
                _noFollowNetwork.reinitialize( random );
                _noFollowNetwork.setValue( _BIAS_NODE_NAME, _BIAS_INPUT_VALUE );
                _noFollowNetwork.setValue( _ENERGY_NODE_NAME, energyLevel );
                _noFollowNetwork.setValue( _MATURATION_NODE_NAME, maturationLevel );
                _noFollowNetwork.setValue( _PREDATION_NODE_NAME,
                        thisPredationType.calculateInput(
                                predationLevel,
                                _otherPredationNoiseMultiplier,
                                random ) );
                _noFollowNetwork.setValue( _OTHER_ACTIVITY_NODE_NAME, otherActivity );
                _noFollowNetwork.update();

                activity = (float) _noFollowNetwork.getValue( _OUTPUT_NODE_NAME );
            }

            // Did the activity result in capture?
            if( isCaptured( activity, predationLevel, random ) )
            {
                // Yup, bail here
                if( _LOG.isDebugEnabled() )
                {
                    _LOG.debug( "CAPTURED: time["
                            + i
                            + "] predationLevel=["
                            + predationLevel
//                            + "*****"
                            + "] predationLevelOffset=["
                            + predationLevelOffset
                            + "] energyLevel=["
                            + energyLevel
                            + "] maturationLevel=["
                            + maturationLevel
                            + "] activity=["
                            + activity
                            + "] sensorFailure=["
                            + sensorFailure
                            + "]" );
                }
                break;
            }

            // Nope, how much energy was gained?
            energyLevel += _energyGainMultiplier * activity;

            // Compute how much energy was used by the activity
            energyLevel -= (_minEnergyConsumedPerTimestep
                    + activity * _activityEnergyLossMultiplier );

            // Is there any left over for maturation??
            if( energyLevel > _maturationEnergyThreshold )
            {
                // Yes, calculate how much energy was used
                float maturationEnergy = Math.min(
                        _maturationEnergyConsumedPerTimestep,
                        (energyLevel - _maturationEnergyThreshold) );
                energyLevel -= maturationEnergy;
                maturationLevel += maturationEnergy;

                _LOG.debug( "Matured [" + maturationEnergy + "] amount" );
            }

            // Ensure the maximum energy isn't exceeded
            energyLevel = Math.min( 1.0f, energyLevel );

            // Does the individual have enough energy to survive?
            if( energyLevel < _MIN_ENERGY_THRESHOLD )
            {
                // Nope
                if( _LOG.isDebugEnabled() )
                {
                    _LOG.debug( "STARVED: time["
                            + i
                            + "] predationLevel=["
                          + predationLevel
//                            + "*****"
                            + "] predationLevelOffset=["
                            + predationLevelOffset
                            + "] energyLevel=["
                            + energyLevel
                            + "] maturationLevel=["
                            + maturationLevel
                            + "] activity=["
                            + activity
                            + "] sensorFailure=["
                            + sensorFailure
                            + "]" );
                }
                break;
            }

            // Did the individual reach full maturation?
            if( maturationLevel >= 1.0f )
            {
                // Yup
                maturationLevel = 1.0f;
                if( _LOG.isDebugEnabled() )
                {
                    _LOG.debug( "FULL MATURATION: time["
                            + i
                            + "] predationLevel=["
//                          + predationLevel
                            + "*****"
                            + "] predationLevelOffset=["
                            + predationLevelOffset
                            + "] energyLevel=["
                            + energyLevel
                            + "] maturationLevel=["
                            + maturationLevel
                            + "] activity=["
                            + activity
                            + "] sensorFailure=["
                            + sensorFailure
                            + "]" );
                }
                break;
            }

            // Log the state and activity if needed
            if( _LOG.isDebugEnabled() )
            {
                _LOG.debug( "EVADED: time["
                        + i
                        + "] predationLevel=["
                        + predationLevel
//                        + "*****"
                        + "] predationLevelOffset=["
                        + predationLevelOffset
                        + "] energyLevel=["
                        + energyLevel
                        + "] maturationLevel=["
                        + maturationLevel
                        + "] activity=["
                        + activity
                        + "] sensorFailure=["
                        + sensorFailure
                        + "]" );
            }

        }

        float fitness = maturationLevel;
        if( Math.abs( fitness - 1.0f ) > 0.001 )
        {
            fitness *= energyLevel;
        }

        return maturationLevel;
    }

    /**
     * Calculates the current predation level
     *
     * @param timestep
     * @param offset
     * @return
     */
    private float calculatePredationLevel( int timestep, float offset )
    {
        double period = 80.0;
        double sineParameter = Math.PI * (offset + (timestep * 2.0f) / period);
        return (float) Math.pow( ((Math.sin( sineParameter ) + 1.0) / 2.0 ), 2 );
    }

    /**
     * Determines if the individual was captures
     *
     * @param activityLevel
     * @param predationLevel
     * @param random
     * @return
     */
    private boolean isCaptured( float activityLevel,
            float predationLevel,
            MersenneTwisterFast random )
    {
        return (random.nextFloat() < (activityLevel * predationLevel) );
    }

    private String buildDescriptionFilenameBase( String prefix )
    {
        String filenameBase = prefix.replaceAll( "\\[", "-" );
        filenameBase = filenameBase.replaceAll( "\\.", "-" );
        filenameBase = filenameBase.replaceAll( "\\]", "" );
        if( filenameBase.endsWith( "-" ) )
        {
            filenameBase = filenameBase.substring( 0,
                    filenameBase.length() - 1 );
        }

        return filenameBase;
    }
}
