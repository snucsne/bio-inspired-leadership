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


    /** Parameter key for the codon size */
    private static final String _P_CODON_SIZE = "codon-size";

    /** Parameter key for the alpha scaling factor */
    private static final String _P_ALPHA_SCALING_FACTOR = "alpha-scaling-factor";

    /** Parameter key for the beta scaling factor */
    private static final String _P_BETA_SCALING_FACTOR = "beta-scaling-factor";

    /** Parameter key for the S scaling factor */
    private static final String _P_S_SCALING_FACTOR = "s-scaling-factor";

    /** Parameter key for the q scaling factor */
    private static final String _P_Q_SCALING_FACTOR = "q-scaling-factor";

    /** Parameter key for the alpha-C scaling factor */
    private static final String _P_ALPHA_C_SCALING_FACTOR = "alpha-c-scaling-factor";

    /** Parameter key for the beta-C scaling factor */
    private static final String _P_BETA_C_SCALING_FACTOR = "beta-c-scaling-factor";

    /** Parameter key for the simulator properties file */
    private static final String _P_SIM_PROPERTIES_FILE = "sim-properties-file";



    /** The codon size */
    private int _codonSize = 0;

    /** The alpha scaling factor */
    private float _alphaScalingFactor = 0.0f;

    /** The beta scaling factor */
    private float _betaScalingFactor = 0.0f;

    /** The S scaling factor */
    private float _sScalingFactor = 0.0f;

    /** The q scaling factor */
    private float _qScalingFactor = 0.0f;

    /** The alpha-C scaling factor */
    private float _alphaCScalingFactor = 0.0f;

    /** The beta-C scaling factor */
    private float _betaCScalingFactor = 0.0f;

    /** The simulator properties file */
    private String _simulatorPropertiesFile = null;


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

        // Get the codon size
        Validate.isTrue( state.parameters.exists(
                base.push( _P_CODON_SIZE ), null ),
                "Codon size is required " );
        _codonSize = state.parameters.getInt( base.push( _P_CODON_SIZE ),
                null );
        _LOG.info( "Using _codonSize=[" + _codonSize + "]" );

        // Get the alpha scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_ALPHA_SCALING_FACTOR ), null ),
                "Alpha scaling factor is required " );
        _alphaScalingFactor = state.parameters.getFloat(
                base.push( _P_ALPHA_SCALING_FACTOR ),
                null );
        _LOG.info( "Using _alphaScalingFactor=[" + _alphaScalingFactor + "]" );

        // Get the beta scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_BETA_SCALING_FACTOR ),
                    null ),
                "Beta scaling factor is required " );
        _betaScalingFactor = state.parameters.getFloat(
                 base.push( _P_BETA_SCALING_FACTOR ),
                null );
        _LOG.info( "Using _betaScalingFactor=[" + _betaScalingFactor + "]" );

        // Get the S scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_S_SCALING_FACTOR ), null ),
                "S scaling factor is required " );
        _sScalingFactor = state.parameters.getFloat(
                 base.push( _P_S_SCALING_FACTOR ),
                null );
        _LOG.info( "Using _sScalingFactor=[" + _sScalingFactor + "]" );

        // Get the q scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_Q_SCALING_FACTOR ), null ),
                "q scaling factor is required " );
        _qScalingFactor = state.parameters.getFloat(
                 base.push( _P_Q_SCALING_FACTOR ),
                null );
        _LOG.info( "Using _qScalingFactor=[" + _qScalingFactor + "]" );

        // Get the alpha-C scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_ALPHA_C_SCALING_FACTOR ), null ),
                "Alpha-C scaling factor is required " );
        _alphaCScalingFactor = state.parameters.getFloat(
                 base.push( _P_ALPHA_C_SCALING_FACTOR ),
                null );
        _LOG.info( "Using _alphaCScalingFactor=[" + _alphaCScalingFactor + "]" );

        // Get the beta-C scaling factor
        Validate.isTrue( state.parameters.exists(
                base.push( _P_BETA_C_SCALING_FACTOR ), null ),
                "Beta-C scaling factor is required " );
        _betaCScalingFactor = state.parameters.getFloat(
                 base.push( _P_BETA_C_SCALING_FACTOR ),
                null );
        _LOG.info( "Using _betaCScalingFactor=[" + _betaCScalingFactor + "]" );

        // Get the simulator properties file
        Validate.isTrue( state.parameters.exists(
                base.push( _P_SIM_PROPERTIES_FILE ), null ),
                "Simulator properties file is required " );
        _simulatorPropertiesFile = state.parameters.getString(
                 base.push( _P_SIM_PROPERTIES_FILE ),
                null );
        _LOG.info( "Using _simulatorPropertiesFile=[" + _simulatorPropertiesFile + "]" );

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
        EvolutionInputParameters inputParameters = decodeGenome( bitInd.genome );

        // Run the simulation
        EvolutionOutputFitness outputFitness = SimulatorEvolution.runEvolutionFromInputParameters(
                inputParameters,
                _simulatorPropertiesFile );

        // Store the fitness (or objective) values
        float[] objectiveValues = new float[2];
        objectiveValues[0] = outputFitness.getPercentTime();
        objectiveValues[1] = outputFitness.getPercentSurvive();
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
        EvolutionInputParameters inputParameters = decodeGenome( bitInd.genome );

        StringBuilder builder = new StringBuilder();
        builder.append( prefix );
        builder.append( "decoded-parameters = " );

        // Describe the parameters
        builder.append( "alpha=[" );
        builder.append( inputParameters.getAlpha() );
        builder.append( "] beta=[" );
        builder.append( inputParameters.getBeta() );
        builder.append( "] S=[" );
        builder.append( inputParameters.getS() );
        builder.append( "] q=[" );
        builder.append( inputParameters.getQ() );
        builder.append( "] alphaC=[" );
        builder.append( inputParameters.getAlphaC() );
        builder.append( "] betaC=[" );
        builder.append( inputParameters.getBetaC() );
        builder.append( "]" );

        return builder.toString();
    }

    /**
     * Decode the genome
     *
     * @param genome
     */
    protected EvolutionInputParameters decodeGenome( boolean[] genome )
    {
        // Pre-calculate the max codon value
        float maxValue = (float) Math.pow( 2.0, _codonSize );

        // Decode each codon in the genome, starting with alpha
        int rawAlpha = decodeAndConvert( genome, 0 );
        float normalizedAlpha = rawAlpha / maxValue;
        float alpha = normalizedAlpha * _alphaScalingFactor;

        int rawBeta = decodeAndConvert( genome, _codonSize );
        float normalizedBeta = rawBeta / maxValue;
        float beta = normalizedBeta * _betaScalingFactor;

        int rawS = decodeAndConvert( genome, _codonSize * 2 );
        float normalizedS = rawS / maxValue;
        int s = Math.round( normalizedS * _sScalingFactor );

        int rawQ = decodeAndConvert( genome, _codonSize * 3 );
        float normalizedQ = rawQ / maxValue;
        float q = normalizedQ * _qScalingFactor;

        int rawAlphaC = decodeAndConvert( genome, _codonSize * 4 );
        float normalizedAlphaC = rawAlphaC / maxValue;
        float alphaC = normalizedAlphaC * _alphaCScalingFactor;

        int rawBetaC = decodeAndConvert( genome, _codonSize * 5 );
        float normalizedBetaC = rawBetaC / maxValue;
        float betaC = normalizedBetaC * _betaCScalingFactor;

        // THIS IS BAD
        // Hardcode the destinations
        DestinationRunCounts[] destinations = new DestinationRunCounts[3];
        destinations[0] = new DestinationRunCounts(
                "cfg/sim/destinations/destinations-diffdis-10-per-0.5-seed-1.dat",
                3 );
        destinations[1] = new DestinationRunCounts(
                "cfg/sim/destinations/destinations-poles-10-per-0.5-seed-1.dat",
                3 );
        destinations[2] = new DestinationRunCounts(
                "cfg/sim/destinations/destinations-split-10-per-0.5-seed-1.dat",
                3 );

        // Store the values
        EvolutionInputParameters inputParameters = new EvolutionInputParameters(
                alpha,
                beta,
                s,
                q,
                alphaC,
                betaC,
                destinations );

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
     * @return The decoded value
     */
    protected int decodeAndConvert( boolean[] genome, int startIdx )
    {
        // Decode it into the gray code
        int grayCode = MiscUtils.decodeBitArray( genome, startIdx, _codonSize );

        // Convert it to binary
        int binary = MiscUtils.convertGrayCodeToBinary( grayCode );

        return binary;
    }
}
