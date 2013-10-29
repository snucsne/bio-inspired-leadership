/*
 * COPYRIGHT
 */
package edu.snu.leader.util;

import org.apache.log4j.Logger;
import ec.EvolutionState;
import ec.Individual;


/**
 * CrossValidationStatistics
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class CrossValidationStatistics extends ParseableStatistics
{
    /** Default serial version UID */
    private static final long serialVersionUID = 1L;

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            CrossValidationStatistics.class.getName() );

    /**
     * Builds the description of an individual
     *
     * @param ind The individual to describe
     * @param state The current state of evolution
     * @param useGen Flag indicating whether or not the generation is included
     * @param indPrefix String to add to the prefix describing the individual
     */
    @Override
    protected String buildIndDescription( Individual ind,
            EvolutionState state,
            boolean useGen,
            String indPrefix )
    {
        _LOG.trace( "Entering buildIndDescription( ind, state, useGen, indPrefix )" );

        String description = super.buildIndDescription( ind,
                state,
                useGen,
                indPrefix );

        if( ind.fitness instanceof CrossValidationFitness )
        {
            // Cast it
            CrossValidationFitness fitness = (CrossValidationFitness)
                    ind.fitness;

            // Build the line prefix
            String linePrefix = buildStdPrefix( state, useGen ) + indPrefix;

            StringBuilder builder = new StringBuilder( description );

            // Describe the training fitness
            describeFitnessStatistics( builder,
                    linePrefix,
                    "training-fitness",
                    fitness.getTrainingResults(),
                    fitness.getTrainingFitnessSum(),
                    fitness.getTrainingFitnessMean() );

            // Describe the validation fitness
            describeFitnessStatistics( builder,
                    linePrefix,
                    "validation-fitness",
                    fitness.getValidationResults(),
                    fitness.getValidationFitnessSum(),
                    fitness.getValidationFitnessMean() );

            // Describe the testing fitness
            describeFitnessStatistics( builder,
                    linePrefix,
                    "testing-fitness",
                    fitness.getTestingResults(),
                    fitness.getTestingFitnessSum(),
                    fitness.getTestingFitnessMean() );

            description = builder.toString();
        }

        _LOG.trace( "Leaving buildIndDescription( ind, state, useGen, indPrefix )" );

        return description;
    }

    /**
     * Describes the given fitness results
     */
    protected void describeFitnessStatistics( StringBuilder builder,
            String linePrefix,
            String name,
            float[] results,
            float sum,
            float mean )
    {
        if( null == results )
        {
            return;
        }

        // Describe the raw fitness
        builder.append( linePrefix );
        builder.append( name );
        builder.append( " = [" );
        for( int i = 0; i < results.length; i++ )
        {
            if( i > 0 )
            {
                builder.append( ", " );
            }
            builder.append( results[i] );
        }
        builder.append( "]" );
        builder.append( NEWLINE );

        // Describe the total fitness
        builder.append( linePrefix );
        builder.append( name );
        builder.append( "-sum = " );
        builder.append( sum );
        builder.append( NEWLINE );

        // Describe the normalized fitness
        builder.append( linePrefix );
        builder.append( name );
        builder.append( "-mean = " );
        builder.append( mean );
        builder.append( NEWLINE );
    }

}
