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
package edu.snu.leader.discrete.evolution;

//Imports
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.vector.BitVectorIndividual;

import edu.snu.leader.discrete.evolution.EvolutionInputParameters.DestinationRunCounts;
import edu.snu.leader.util.IndividualDescriber;
import edu.snu.leader.util.MiscUtils;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;


/**
 * MultiObjectiveCoordinationProblem
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MultiObjectiveCoordinationProblem extends Problem implements
        SimpleProblemForm, IndividualDescriber
{
    /** Default serial version UID */
    private static final long serialVersionUID = 1L;

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            MultiObjectiveCoordinationProblem.class.getName() );


    /** Parameter key for the alpha scaling factor */
    private static final String _P_ALPHA_PREFIX = "alpha.";

    /** Parameter key for the beta scaling factor */
    private static final String _P_BETA_PREFIX = "beta.";

    /** Parameter key for the S modulus */
    private static final String _P_S_PREFIX = "s.";

    /** Parameter key for the q scaling factor */
    private static final String _P_Q_PREFIX = "q.";

    /** Parameter key for the alpha-C scaling factor */
    private static final String _P_ALPHA_C_PREFIX = "alpha-c.";

    /** Parameter key for the beta-C scaling factor */
    private static final String _P_BETA_C_PREFIX = "beta-c.";

    /** Parameter key postfix for scaling factors */
    private static final String _SCALING_FACTOR_POSTFIX = "scaling-factor";

    /** Parameter key postfix for modulii */
    private static final String _MODULUS_POSTFIX = "modulus";

    /** Parameter key postfix for codon size */
    private static final String _CODON_SIZE_POSTFIX = "codon-size";

    /** Parameter key for the simulator properties file */
    private static final String _P_SIM_PROPERTIES_FILE = "sim-properties-file";

    /** Parameter key prefix for the simulator destination information */
    private static final String _P_SIM_DESTINATIONS_PREFIX = "sim-destinations";

    /** Parameter key for flag to force re-evaluation of individuals */
    private static final String _P_FORCE_REEVALUATION = "force-reevaluation";


    /** The alpha scaling factor */
    private float _alphaScalingFactor = 0.0f;

    /** The alpha codon size */
    private int _alphaCodonSize = 0;

    /** The beta scaling factor */
    private float _betaScalingFactor = 0.0f;

    /** The beta codon size */
    private int _betaCodonSize = 0;

    /** The S modulus */
    private int _sModulus = 0;

    /** The S codon size */
    private int _sCodonSize = 0;

    /** The q scaling factor */
    private float _qScalingFactor = 0.0f;

    /** The q codon size */
    private int _qCodonSize = 0;

    /** The alpha-C scaling factor */
    private float _alphaCScalingFactor = 0.0f;

    /** The alpha-C codon size */
    private int _alphaCCodonSize = 0;

    /** The beta-C scaling factor */
    private float _betaCScalingFactor = 0.0f;

    /** The beta-C codon size */
    private int _betaCCodonSize = 0;

    /** The simulator properties file */
    private String _simulatorPropertiesFile = null;

    /** The array of destination files */
    private String[] _destinationFiles = new String[0];

    /** The array of destination simulation counts */
    private int[] _destinationSimCounts = new int[0];

    /** Flag indicating that individuals should be re-evaluated every generation */
    private boolean _forceReevaluation = false;


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

        // Get the alpha scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_ALPHA_PREFIX + _SCALING_FACTOR_POSTFIX ), null ),
                "Alpha scaling factor is required " );
        _alphaScalingFactor = state.parameters.getFloat(
                base.push( _P_ALPHA_PREFIX + _SCALING_FACTOR_POSTFIX ),
                null );
        _LOG.info( "Using _alphaScalingFactor=[" + _alphaScalingFactor + "]" );

        // Get the alpha codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_ALPHA_PREFIX + _CODON_SIZE_POSTFIX ), null ),
                "Alpha codon size is required " );
        _alphaCodonSize = state.parameters.getInt( base.push(
                _P_ALPHA_PREFIX + _CODON_SIZE_POSTFIX ),
                null );
        _LOG.info( "Using _alphaCodonSize=[" + _alphaCodonSize + "]" );

        // Get the beta scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_BETA_PREFIX + _SCALING_FACTOR_POSTFIX ),
                    null ),
                "Beta scaling factor is required " );
        _betaScalingFactor = state.parameters.getFloat(
                 base.push( _P_BETA_PREFIX + _SCALING_FACTOR_POSTFIX ),
                null );
        _LOG.info( "Using _betaScalingFactor=[" + _betaScalingFactor + "]" );

        // Get the beta codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_BETA_PREFIX + _CODON_SIZE_POSTFIX ), null ),
                "Beta codon size is required " );
        _betaCodonSize = state.parameters.getInt( base.push(
                _P_BETA_PREFIX + _CODON_SIZE_POSTFIX ),
                null );
        _LOG.info( "Using _betaCodonSize=[" + _betaCodonSize + "]" );

        // Get the S modulus
        Validate.isTrue( state.parameters.exists(
                base.push( _P_S_PREFIX + _MODULUS_POSTFIX ), null ),
                "S modulus is required " );
        _sModulus = state.parameters.getInt(
                 base.push( _P_S_PREFIX + _MODULUS_POSTFIX ),
                null );
        _LOG.info( "Using _sModulus=[" + _sModulus + "]" );

        // Get the S codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_S_PREFIX + _CODON_SIZE_POSTFIX ), null ),
                "S codon size is required " );
        _sCodonSize = state.parameters.getInt( base.push(
                _P_S_PREFIX + _CODON_SIZE_POSTFIX ),
                null );
        _LOG.info( "Using _sCodonSize=[" + _sCodonSize + "]" );

        // Get the q scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_Q_PREFIX + _SCALING_FACTOR_POSTFIX ), null ),
                "q scaling factor is required " );
        _qScalingFactor = state.parameters.getFloat(
                 base.push( _P_Q_PREFIX + _SCALING_FACTOR_POSTFIX ),
                null );
        _LOG.info( "Using _qScalingFactor=[" + _qScalingFactor + "]" );

        // Get the q codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_Q_PREFIX + _CODON_SIZE_POSTFIX ), null ),
                "q Codon size is required " );
        _qCodonSize = state.parameters.getInt( base.push(
                _P_Q_PREFIX + _CODON_SIZE_POSTFIX ),
                null );
        _LOG.info( "Using _qCodonSize=[" + _qCodonSize + "]" );

        // Get the alpha-C scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_ALPHA_C_PREFIX + _SCALING_FACTOR_POSTFIX ), null ),
                "Alpha-C scaling factor is required " );
        _alphaCScalingFactor = state.parameters.getFloat(
                 base.push( _P_ALPHA_C_PREFIX + _SCALING_FACTOR_POSTFIX ),
                null );
        _LOG.info( "Using _alphaCScalingFactor=[" + _alphaCScalingFactor + "]" );

        // Get the alpha-C codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_ALPHA_C_PREFIX + _CODON_SIZE_POSTFIX ), null ),
                "Alpha-C codon size is required " );
        _alphaCCodonSize = state.parameters.getInt( base.push(
                _P_ALPHA_C_PREFIX + _CODON_SIZE_POSTFIX ),
                null );
        _LOG.info( "Using _alphaCCodonSize=[" + _alphaCCodonSize + "]" );

        // Get the beta-C scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_BETA_C_PREFIX + _SCALING_FACTOR_POSTFIX ), null ),
                "Beta-C scaling factor is required " );
        _betaCScalingFactor = state.parameters.getFloat(
                 base.push( _P_BETA_C_PREFIX + _SCALING_FACTOR_POSTFIX ),
                null );
        _LOG.info( "Using _betaCScalingFactor=[" + _betaCScalingFactor + "]" );

        // Get the beta-c codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_BETA_C_PREFIX + _CODON_SIZE_POSTFIX ), null ),
                "Beta-C codon size is required " );
        _betaCCodonSize = state.parameters.getInt( base.push(
                _P_BETA_C_PREFIX + _CODON_SIZE_POSTFIX ),
                null );
        _LOG.info( "Using _betaCCodonSize=[" + _betaCCodonSize + "]" );

        // Get the simulator properties file
        Validate.isTrue( state.parameters.exists(
                base.push( _P_SIM_PROPERTIES_FILE ), null ),
                "Simulator properties file is required " );
        _simulatorPropertiesFile = state.parameters.getString(
                 base.push( _P_SIM_PROPERTIES_FILE ),
                null );
        _LOG.info( "Using _simulatorPropertiesFile=[" + _simulatorPropertiesFile + "]" );

        // Save it for later
        System.setProperty( _P_SIM_PROPERTIES_FILE, _simulatorPropertiesFile );

        // Get the number of destination files
        String destCountKey = _P_SIM_DESTINATIONS_PREFIX + "-count";
        Validate.isTrue( state.parameters.exists(
                base.push( destCountKey ), null ),
                "Simulator destination count is required " );
        int destCount = state.parameters.getInt(
                base.push( destCountKey ),
                null );
        _destinationFiles = new String[destCount];
        _destinationSimCounts = new int[destCount];

        // Load each destination file
        for( int i = 0; i < destCount; i++ )
        {
            // Get the file
            String destFileKey = _P_SIM_DESTINATIONS_PREFIX
                    + "."
                    + String.format( "%02d", i )
                    + ".file";
            Validate.isTrue( state.parameters.exists(
                    base.push( destFileKey ), null ),
                    "Simulator destination file ["
                    + String.format( "%02d", i )
                    + "] is required" );
            _destinationFiles[i] = state.parameters.getString(
                    base.push( destFileKey ),
                    null );

            // Get the number of simulations to run
            String destSimCountKey = _P_SIM_DESTINATIONS_PREFIX
                    + "."
                    + String.format( "%02d", i )
                    + ".sim-count";
            Validate.isTrue( state.parameters.exists(
                    base.push( destSimCountKey ), null ),
                    "Simulator destination simulation count ["
                    + String.format( "%02d", i )
                    + "] is required" );
            _destinationSimCounts[i] = state.parameters.getInt(
                    base.push( destSimCountKey ),
                    null );
        }

        // Get the simulator properties file
        Validate.isTrue( state.parameters.exists(
                base.push( _P_FORCE_REEVALUATION ), null ),
                "Force reevaluation flag is required " );
        _forceReevaluation = state.parameters.getBoolean(
                 base.push( _P_FORCE_REEVALUATION ),
                 null,
                 false );
        _LOG.info( "Using _forceReevaluation=[" + _forceReevaluation + "]" );


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
        if( ind.evaluated && !_forceReevaluation )
        {
            // Yup, bail out early
            return;
        }

        // Is it the correct type of individual
        if( !(ind instanceof BitVectorIndividual) )
        {
            // Nope, complain
            _LOG.error( "Individual is not of correct type ["
                    + ind.getClass().getCanonicalName()
                    + "]" );
            state.output.fatal( "Individual is not the correct type" );
        }

        // Cast it to the proper type
        BitVectorIndividual bitInd = (BitVectorIndividual) ind;

        // Decode the genome
        EvolutionInputParameters inputParameters = decodeGenome( bitInd.genome,
                state.random[threadnum] );

        // Run the simulation
        EvolutionOutputFitness outputFitness = SimulatorEvolution.runEvolutionFromInputParameters(
                inputParameters,
                _simulatorPropertiesFile );

        // Store the fitness (or objective) values
        float[] objectiveValues = new float[3];
//        objectiveValues[0] = outputFitness.getPercentTime();
        objectiveValues[0] = 1.0f - outputFitness.getPercentDistanceToDestination();
        objectiveValues[1] = 1.0f - outputFitness.getPercentTimeToDestination();
        objectiveValues[2] = outputFitness.getPercentSurvive();
        MultiObjectiveFitness fitness = (MultiObjectiveFitness) ind.fitness;
        fitness.setObjectives( state, objectiveValues );

        // Mark the individual as evaluated
        ind.evaluated = true;
    }

    /**
     * TODO Method description
     *
     * @param ind
     * @param prefix
     * @param statDir
     * @return
     * @see edu.snu.leader.util.IndividualDescriber#describe(ec.Individual, java.lang.String, java.lang.String)
     */
    @Override
    public String describe( Individual ind, String prefix, String statDir )
    {
        // Cast it to the proper type
        BitVectorIndividual bitInd = (BitVectorIndividual) ind;

        // Decode the genome
        EvolutionInputParameters inputParameters = decodeGenome( bitInd.genome, null );

        StringBuilder builder = new StringBuilder();
        builder.append( prefix );
        builder.append( "decoded-parameters = " );

        // Describe the parameters
        builder.append( "alpha=[" );
        builder.append( String.format( "%08.6f", inputParameters.getAlpha() ) );
        builder.append( "] beta=[" );
        builder.append( String.format( "%08.6f", inputParameters.getBeta() ) );
        builder.append( "] S=[" );
        builder.append( String.format( "%2d", inputParameters.getS() ) );
        builder.append( "] q=[" );
        builder.append( String.format( "%08.6f", inputParameters.getQ() ) );
        builder.append( "] alphaC=[" );
        builder.append( String.format( "%08.6f", inputParameters.getAlphaC() ) );
        builder.append( "] betaC=[" );
        builder.append( String.format( "%+09.6f", inputParameters.getBetaC() ) );
        builder.append( "]" );
        builder.append( System.getProperty( "line.separator" ) );

        return builder.toString();
    }

    /**
     * Decode the genome
     *
     * @param genome
     */
    protected EvolutionInputParameters decodeGenome( boolean[] genome,
            MersenneTwisterFast random )
    {
        int codonIdx = 0;

        // Decode each codon in the genome, starting with alpha
        float maxValue = (float) Math.pow( 2.0, _alphaCodonSize );
        int rawAlpha = decodeAndConvert( genome, 0, _alphaCodonSize );
        float normalizedAlpha = ( rawAlpha / maxValue );
        float alpha = normalizedAlpha * _alphaScalingFactor;
        codonIdx += _alphaCodonSize;

        maxValue = (float) Math.pow( 2.0, _betaCodonSize );
        int rawBeta = decodeAndConvert( genome, codonIdx, _betaCodonSize );
        float normalizedBeta = rawBeta / maxValue;
        float beta = normalizedBeta * _betaScalingFactor;
        codonIdx += _betaCodonSize;

        int rawS = decodeAndConvert( genome, codonIdx, _sCodonSize );
        int s = rawS % _sModulus;
        codonIdx += _sCodonSize;

        maxValue = (float) Math.pow( 2.0, _qCodonSize );
        int rawQ = decodeAndConvert( genome, codonIdx, _qCodonSize );
        float normalizedQ = rawQ / maxValue;
        float q = normalizedQ * _qScalingFactor;
        codonIdx += _qCodonSize;

        maxValue = (float) Math.pow( 2.0, _alphaCCodonSize );
        int rawAlphaC = decodeAndConvert( genome, codonIdx, _alphaCCodonSize );
        float normalizedAlphaC = rawAlphaC / maxValue;
        float alphaC = normalizedAlphaC * _alphaCScalingFactor;
        codonIdx += _alphaCCodonSize;

        maxValue = (float) Math.pow( 2.0, _betaCCodonSize );
        int rawBetaC = decodeAndConvert( genome, codonIdx, _betaCCodonSize );
        float normalizedBetaC = rawBetaC / maxValue;
        float betaC = normalizedBetaC * _betaCScalingFactor;

        // Build the destinations
        DestinationRunCounts[] destinationInfo =
                new DestinationRunCounts[ _destinationFiles.length ];
        for(int i = 0; i < destinationInfo.length; i++ )
        {
            long seed = 0;
            if( null != random )
            {
                seed = random.nextInt();
            }
            destinationInfo[i] = new DestinationRunCounts(
                    _destinationFiles[i],
                    _destinationSimCounts[i],
                    seed );

        }

        // Store the values
        EvolutionInputParameters inputParameters = new EvolutionInputParameters(
                alpha,
                beta,
                s,
                q,
                alphaC,
                betaC,
                destinationInfo );

        // Log it
        _LOG.debug( inputParameters.toString() );

        // Return them
        return inputParameters;
    }

    /**
     * Decode a codon to a gray code and convert it to binary
     *
     * @param genome The genome containing the codon
     * @param startIdx The starting index of the codon
     * @param codongSize The size of the codon
     * @return The decoded value
     */
    protected int decodeAndConvert( boolean[] genome, int startIdx, int codonSize )
    {
        // Decode it into the gray code
        int grayCode = MiscUtils.decodeBitArray( genome, startIdx, codonSize );

        // Convert it to binary
        int binary = MiscUtils.convertGrayCodeToBinary( grayCode );

        return binary;
    }
}
