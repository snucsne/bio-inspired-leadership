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

    /** Default serial version uid */
    private static final long serialVersionUID = 1L;

    /** Number of fitness objectives parameter */
    public static final String P_NUM_OBJECTIVES = "num-objectives";


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

        _LOG.trace( "Leaving setup( state, base )" );
    }




    /**
     * Called immediately after evaluation occurs.
     *
     * @param state The current state of evolution
     * @see edu.snu.leader.util.ParseableStatistics#postEvaluationStatistics(ec.EvolutionState)
     */
    @Override
    public void postEvaluationStatistics( EvolutionState state )
    {
        // Before we do anything, get the time
        long evalTime = ( System.currentTimeMillis() - _evalStartTime );
        println( "eval-time = "
                + evalTime,
                state );
        println( "eval-time-human = "
                + TimeUnit.MILLISECONDS.toMinutes( evalTime )
                + "m "
                + TimeUnit.MILLISECONDS.toSeconds( evalTime )
                + "s",
                state );
        _evalTotalTime += evalTime;

        // Call the superclass impl
        super.postEvaluationStatistics( state );

        // Define the variables to prevent a lot of gc
        Individual bestOfGenInd = null;
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
            bestOfGenInd = null;
//            _bestFound[i] = bestOfGenInd;
//            _bestFoundGen[i] = state.generation;
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

                // Is this individual the best found for this subpopulation
                // for this generation?
                if( (null == bestOfGenInd)
                        || (currentInd.fitness.betterThan( bestOfGenInd.fitness )) )
                {
                    bestOfGenInd = currentInd;

                    // Is it the best of the run?
                    if( (_bestFound[i] == null) ||
                            (currentInd.fitness.betterThan(_bestFound[i].fitness)) )
                    {
                        // Yup
                        _bestFound[i] = currentInd;
                        _bestFoundGen[i] = state.generation;
                    }
                }
            }

            // Compute and log the mean values and variance of the fitness stats
            for( int j = 0; j < fitnessStats.length; j++ )
            {
                String objectiveID = String.format( "%02d", j );
                println( prefix
                        + "fitness-"
                        + objectiveID
                        + "-mean = "
                        + fitnessStats[j].getMean(),
                        state );
                println( prefix
                        + "fitness-"
                        + objectiveID
                        + "-variance = "
                        + fitnessStats[j].getVariance(),
                        state );
                println( prefix
                        + "fitness-"
                        + objectiveID
                        + "-std-dev = "
                        + fitnessStats[j].getStandardDeviation(),
                        state );
            }

            // Display the best individual's stats
            print( buildIndDescription( bestOfGenInd,
                    state,
                    true,
                    prefix + "best-individual."),
                    state );

            indFitness = (MultiObjectiveFitness) _bestFound[i].fitness;
            float[] bestFoundObjectiveFitnessValues = indFitness.getObjectives();
            for( int j = 0; j < fitnessStats.length; j++ )
            {
                println( prefix
                        + "best-individual-found-so-far.fitness-"
                        + String.format( "%02d", j )
                        + bestFoundObjectiveFitnessValues[j],
                        state );
            }
            println( prefix + "best-individual-found-so-far.generation = "
                    + _bestFoundGen[i],
                    state );
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
        if( !useGen && ( problem instanceof IndividualDescriber ) )
        {
            builder.append( ((IndividualDescriber) problem).describe(
                    ind,
                    linePrefix,
                    _statDir ) );
        }

        return builder.toString();
    }

}
