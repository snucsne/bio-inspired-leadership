/*
 * COPYRIGHT
 */
package edu.snu.leader.util;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * MooParseableStatistics
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MooParseableStatistics extends ParseableStatistics
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( ParseableStatistics.class.getName() );

    private static class MooIndComparator implements Comparator<Individual>
    {
        /**
         * Compares to individuals w.r.t. their objective fitness values
         *
         * @param ind1 The first individual to be compared
         * @param ind2 The second individual to be compared
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare( Individual ind1, Individual ind2 )
        {
            int result = 0;

            // Get the fitness values
            MultiObjectiveFitness ind1Fitness = (MultiObjectiveFitness) ind1.fitness;
            float[] ind1Objectives = ind1Fitness.getObjectives();
            MultiObjectiveFitness ind2Fitness = (MultiObjectiveFitness) ind2.fitness;
            float[] ind2Objectives = ind2Fitness.getObjectives();

            // Compare them
            for( int i = 0; (i < ind1Objectives.length) && (0 == result); i++ )
            {
                if( ind1Objectives[i] < ind2Objectives[i] )
                {
                    result = -1;
                }
                else if ( ind1Objectives[i] > ind2Objectives[i] )
                {
                    result = 1;
                }
            }

            return result;
        }

    }


    /** Default serial version uid */
    private static final long serialVersionUID = 1L;

    /** Number of fitness objectives parameter key */
    public static final String P_NUM_OBJECTIVES = "num-objectives";

    /** Objective information parameter key prefix */
    public static final String P_OBJECTIVE_PREFIX = "objective";

    /** Parameter key for the simulator properties file */
    private static final String _SIM_PROPERTIES_FILE = "sim-properties-file";



    /** The number of fitness objectives */
    protected int _numObjectives = 0;


    /**
     * TODO Method description
     *
     * @param state
     * @param base
     * @see edu.snu.leader.util.ParseableStatistics#setup(ec.EvolutionState, ec.util.Parameter)
     */
    @Override
    public void setup( EvolutionState state, Parameter base )
    {
        _LOG.trace( "Entering setup( state, base )" );

        // Call the superclass implementation
        super.setup( state, base );

        // Get the number of objectives
        Validate.isTrue( state.parameters.exists(
                base.push( P_NUM_OBJECTIVES ), null ),
                "Number of fitness objectives is required " );
        _numObjectives = state.parameters.getInt(
                 base.push( P_NUM_OBJECTIVES ),
                null );
        _LOG.info( "Using _numObjectives=[" + _numObjectives + "]" );

        // We don't need a "best found" since we have a pareto front
        _bestFound = new Individual[0];

        _LOG.trace( "Leaving setup( state, base )" );
    }


    /**
     * Called immediately after initialization.
     *
     * @param state The current state of evolution
     * @see edu.snu.leader.util.ParseableStatistics#postInitializationStatistics(ec.EvolutionState)
     */
    @Override
    public void postInitializationStatistics( EvolutionState state )
    {
        // Call the superclass implementation
        super.postInitializationStatistics( state );

        // Display the parameters from the simulator properties file
        String simulatorPropertiesFile = System.getProperty( _SIM_PROPERTIES_FILE );
        Properties simProps = new Properties();
        try
        {
            // Load the properties file
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream input = loader.getResourceAsStream(
                    simulatorPropertiesFile );
            if( null == input )
            {
                _LOG.error( "Null input stream for simulator properties file ["
                        + simulatorPropertiesFile
                        + "] key" );
                throw new RuntimeException( "Null input stream for simulator properties file ["
                        + simulatorPropertiesFile
                        + "]" );
            }

            // Load the properties
            simProps.load( input );

            // Display the parameters
            String newline = System.getProperty("line.separator");
            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append( newline );
            paramBuilder.append( "# =========================================================" );
            paramBuilder.append( newline );
            paramBuilder.append( "# Simulator parameters" );
            paramBuilder.append( newline );
            paramBuilder.append( newline );
            String[] names = simProps.stringPropertyNames().toArray( new String[0] );
            Arrays.sort( names );
            for( int i = 0; i < names.length; i++ )
            {
                String currentName = names[i];
                String currentValue = simProps.getProperty( currentName );
                paramBuilder.append( "# " );
                paramBuilder.append( currentName );
                paramBuilder.append( " = " );
                paramBuilder.append( currentValue );
                paramBuilder.append( newline );
            }
            paramBuilder.append( "# =========================================================" );
            paramBuilder.append( newline );
            state.output.println( paramBuilder.toString(),
                    _statLog );
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to read simulator properties in file ["
                    + simulatorPropertiesFile
                    + "]",
                    ioe );
            throw new RuntimeException( "Unable to read simulator properties in file ["
                    + simulatorPropertiesFile
                    + "]", ioe );
        }
    }

    /**
     * Called immediately after evaluation occurs.
     *
     * @param state The current state of evolution
     * @see edu.snu.leader.util.ParseableStatistics#postEvaluationStatistics(ec.EvolutionState)
     */
    @SuppressWarnings( { "synthetic-access", "rawtypes" } )
    @Override
    public void postEvaluationStatistics( EvolutionState state )
    {
        state.output.println( "",
                _statLog );

        // Before we do anything, get the time
        long evalTime = ( System.currentTimeMillis() - _evalStartTime );
        println( "eval-time = "
                + evalTime,
                state );
        long totalSeconds = TimeUnit.SECONDS.convert( evalTime, TimeUnit.MILLISECONDS);
        long minutes = TimeUnit.MINUTES.convert( totalSeconds, TimeUnit.SECONDS );
        long seconds = totalSeconds - TimeUnit.SECONDS.convert( minutes, TimeUnit.MINUTES );
        println( "eval-time-human = "
                + minutes
                + "m "
                + seconds
                + "s",
                state );
        _evalTotalTime += evalTime;

        // Call the superclass impl
//        super.postEvaluationStatistics( state );

        // Define the variables to prevent a lot of gc
        Individual currentInd = null;
        Subpopulation subPop = null;
        int subPopSize = 0;
        String prefix = null;
        MultiObjectiveFitness indFitness = null;

        // Get the statistics objects
        DescriptiveStatistics[] fitnessStats = new DescriptiveStatistics[_numObjectives];
        for( int i = 0; i < fitnessStats.length; i++ )
        {
            fitnessStats[i] = new DescriptiveStatistics();
        }

        // Iterate over the sub-populations
        for( int i = 0; i < state.population.subpops.length; i++ )
        {
            // Save some commonly accessed variables here
            subPop = state.population.subpops[i];
            subPopSize = subPop.individuals.length;
            prefix = "subpop["
                + _2_DIGIT_FORMATTER.format( i )
                + "].";

            // Iterate over all the individuals in the sub-population
            for( int j = 0; j < subPopSize; j++ )
            {
                // Get the current individual
                currentInd = subPop.individuals[ j ];

                // Get the fitness statistic
                indFitness = (MultiObjectiveFitness) currentInd.fitness;
                float[] objectiveFitnessValues = indFitness.getObjectives();
                for( int k = 0; k < fitnessStats.length; k++ )
                {
                    fitnessStats[k].addValue( objectiveFitnessValues[k] );
                }
            }

            // Compute and log the mean values and variance of the fitness stats
            for( int j = 0; j < fitnessStats.length; j++ )
            {
                String objectiveID = String.format( "%02d", j );
                println( prefix
                        + "objective-fitness."
                        + objectiveID
                        + ".mean = "
                        + fitnessStats[j].getMean(),
                        state );
                println( prefix
                        + "objective-fitness."
                        + objectiveID
                        + ".variance = "
                        + fitnessStats[j].getVariance(),
                        state );
                println( prefix
                        + "objective-fitness."
                        + objectiveID
                        + ".std-dev = "
                        + fitnessStats[j].getStandardDeviation(),
                        state );
            }

            // Get the individuals on the pareto front
            List paretoInds = MultiObjectiveFitness.partitionIntoParetoFront(
                    subPop.individuals,
                    null,
                    null );
            println( prefix
                    + "pareto-front-individual-count = "
                    + paretoInds.size(),
                    state );

            // Prune the pareto front to only display the unique individuals
            Map<String,Individual> uniqueParetoInds = new HashMap<String,Individual>();
            for( int j = 0; j < paretoInds.size(); j++ )
            {
                Individual current = (Individual) paretoInds.get( j );

                // Check to see if the same genome is in the list
                String genome = current.genotypeToStringForHumans();
                if( uniqueParetoInds.containsKey( genome ) )
                {
                    // Yup, continue on to the next one
                    continue;
                }
                else
                {
                    // Nope, add it
                    uniqueParetoInds.put( genome, current );
                }
            }
            println( prefix
                    + "pareto-front-unique-individual-count = "
                    + uniqueParetoInds.size(),
                    state );

            // Sort the unique individuals
            Individual[] sortedUnique = uniqueParetoInds.values().toArray(
                    new Individual[uniqueParetoInds.size() ] );
            Arrays.sort( sortedUnique, new MooIndComparator() );

            // Display the stats of the unique individuals
            for( int j = 0; j < sortedUnique.length; j++ )
            {
                Individual current = sortedUnique[j];

                print( buildIndDescription( current,
                        state,
                        true,
                        prefix
                            + "pareto-individual."
                            + String.format( "%03d", j )
                            + "." ),
                        state );
            }
        }

        state.output.flush();
    }


    /**
     * Builds the description of an individual
     *
     * @param ind The individual to describe
     * @param state The current state of evolution
     * @param useGen Flag indicating whether or not the generation is included
     * @param indPrefix String to add to the prefix describing the individual
     * @return The string representation of the individual
     *
     * @see edu.snu.leader.util.ParseableStatistics#buildIndDescription(ec.Individual, ec.EvolutionState, boolean, java.lang.String)
     */
    @Override
    protected String buildIndDescription( Individual ind,
            EvolutionState state,
            boolean useGen,
            String indPrefix )
    {
        String linePrefix = buildStdPrefix( state, useGen ) + indPrefix;

        StringBuilder builder = new StringBuilder();

        // Describe the fitness
        MultiObjectiveFitness fitness = (MultiObjectiveFitness) ind.fitness;
        float[] objectives = fitness.getObjectives();
        builder.append( linePrefix );
        builder.append( "fitness =" );
        for( int i = 0; i < objectives.length; i++ )
        {
            builder.append( " " );
            builder.append( objectives[i] );
        }
        builder.append( NEWLINE );

        // Describe the genotype
        builder.append(  linePrefix );
        builder.append( "genotype = " );
        builder.append( ind.genotypeToStringForHumans() );
        builder.append( NEWLINE );

        Problem problem = state.evaluator.p_problem;
        if( problem instanceof IndividualDescriber )
        {
            builder.append( ((IndividualDescriber) problem).describe(
                    ind,
                    linePrefix,
                    _statDir ) );
        }

        return builder.toString();
    }

}
