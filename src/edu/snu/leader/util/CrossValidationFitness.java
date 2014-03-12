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

// Imports
import ec.simple.SimpleFitness;


/**
 * CrossValidationFitness
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class CrossValidationFitness extends SimpleFitness
{
    /** Default serial version UID */
    private static final long serialVersionUID = 1L;


    /** Training evaluation results */
    protected float[] _trainingResults = null;

    /** Sum of training fitness */
    protected float _trainingFitnessSum = Float.NEGATIVE_INFINITY;

    /** Mean training fitness */
    protected float _trainingFitnessMean = Float.NEGATIVE_INFINITY;

    /** Testing evaluation results */
    protected float[] _testingResults = null;

    /** Sum of testing fitness */
    protected float _testingFitnessSum = Float.NEGATIVE_INFINITY;

    /** Mean testing fitness */
    protected float _testingFitnessMean = Float.NEGATIVE_INFINITY;

    /** Validation evaluation results */
    protected float[] _validationResults = null;

    /** Sum of validation fitness */
    protected float _validationFitnessSum = Float.NEGATIVE_INFINITY;

    /** Mean validation fitness */
    protected float _validationFitnessMean = Float.NEGATIVE_INFINITY;


    /**
     * Sets the training results for this fitness
     *
     * @param trainingResults The training results
     */
    public void setTrainingResults( float[] trainingResults )
    {
        // Validate it
        if( (null == trainingResults) || (0 == trainingResults.length) )
        {
            throw new IllegalArgumentException( "TrainingResults may not be null or empty" );
        }
        _trainingResults = trainingResults;

        // Compute the sum and mean
        _trainingFitnessSum = 0.0f;
        for( int i = 0; i < _trainingResults.length; i++ )
        {
            _trainingFitnessSum += _trainingResults[i];
        }
        _trainingFitnessMean = _trainingFitnessSum / _trainingResults.length;
    }

    /**
     * Sets the testing results for this fitness
     *
     * @param testingResults The testing results
     */
    public void setTestingResults( float[] testingResults )
    {
        // Validate it
        if( (null == testingResults) || (0 == testingResults.length) )
        {
            throw new IllegalArgumentException( "TestingResults may not be null or empty" );
        }
        _testingResults = testingResults;

        // Compute the sum and mean
        _testingFitnessSum = 0.0f;
        for( int i = 0; i < _testingResults.length; i++ )
        {
            _testingFitnessSum += testingResults[i];
        }
        _testingFitnessMean = _testingFitnessSum / _testingResults.length;
    }

    /**
     * Sets the validation results for this fitness
     *
     * @param validationResults The validation results
     */
    public void setValidationResults( float[] validationResults )
    {
        // Validate it
        if( (null == validationResults) || (0 == validationResults.length) )
        {
            throw new IllegalArgumentException( "ValidationResults may not be null or empty" );
        }
        _validationResults = validationResults;

        // Compute the sum and mean
        _validationFitnessSum = 0.0f;
        for( int i = 0; i < _validationResults.length; i++ )
        {
            _validationFitnessSum += _validationResults[i];
        }
        _validationFitnessMean = _validationFitnessSum / _validationResults.length;
    }

    /*
     * Returns the training fitness for this individual
     *
     * @return The training fitness
     */
    public float[] getTrainingResults()
    {
        return _trainingResults;
    }

    /**
     * Returns the trainingFitnessSum for this individual
     *
     * @return The trainingFitnessSum
     */
    public float getTrainingFitnessSum()
    {
        return _trainingFitnessSum;
    }

    /**
     * Returns the mean of the training fitness
     *
     * @return The mean of the training fitness
     */
    public float getTrainingFitnessMean()
    {
        return _trainingFitnessMean;
    }

    /**
     * Returns the testing fitness for this individual
     *
     * @return The testing fitness
     */
    public float[] getTestingResults()
    {
        return _testingResults;
    }

    /**
     * Returns the testingFitnessSum for this individual
     *
     * @return The testingFitnessSum
     */
    public float getTestingFitnessSum()
    {
        return _testingFitnessSum;
    }

    /**
     * Returns the mean of the testing fitness
     *
     * @return The mean of the testing fitness
     */
    public float getTestingFitnessMean()
    {
        return _testingFitnessMean;
    }

    /**
     * Returns the validation fitness for this individual
     *
     * @return The validation fitness
     */
    public float[] getValidationResults()
    {
        return _validationResults;
    }

    /**
     * Returns the validationFitnessSum for this individual
     *
     * @return The validationFitnessSum
     */
    public float getValidationFitnessSum()
    {
        return _validationFitnessSum;
    }

    /**
     * Returns the mean of the validation fitness
     *
     * @return The mean of the validation fitness
     */
    public float getValidationFitnessMean()
    {
        return _validationFitnessMean;
    }

    /**
     * Determines if this testing fitness is better than the other
     *
     * @param other The other fitness
     * @return <code>true</code> if this testing fitness is better, otherwise,
     *     <code>false</code>
     */
    public boolean testingBetterThan( CrossValidationFitness other )
    {
        return (_testingFitnessSum > other._testingFitnessSum);
    }

    /**
     * Determines if this validation fitness is better than the other
     *
     * @param other The other fitness
     * @return <code>true</code> if this validation fitness is better,
     *     otherwise, <code>false</code>
     */
    public boolean validationBetterThan( CrossValidationFitness other )
    {
        return (_validationFitnessMean > other._validationFitnessMean);
    }

}
