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
