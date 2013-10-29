/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial.calculator.test;

import ec.util.MersenneTwisterFast;

import edu.snu.leader.util.MathUtils;

/**
 * GautraisProbabilityTest
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class GautraisProbabilityTest
{
    protected static final int _EVAL_COUNT = 20000;

    /** The base initiation rate */
    protected float _initRateBase = 1290.0f;

    /** Follow alpha constant (see Eq. 1) */
    protected float _followAlpha = 162.3f;

    /** Follow beta constant (see Eq. 1) */
    protected float _followBeta = 75.4f;

    /** Cancel alpha constant (see Eq. 2) */
    protected float _cancelAlpha = 0.009f;

    /** Cancel gamma constant (see Eq. 2) */
    protected float _cancelGamma = 2.0f;

    /** Cancel epsilon constant (see Eq. 2) */
    protected float _cancelEpsilon = 2.3f;


    public static void main( String[] args )
    {
        GautraisProbabilityTest test = new GautraisProbabilityTest();
        test.execute();
    }

    public void execute()
    {
        for( int i = 0; i <= 10; i++ )
        {
            float personality = 0.1f * i;
            float probability = calculateInitiateProbability( true, personality );
            System.out.println( "personality=["
                    + String.format( "%03.1f", personality )
                    + "]\tprob=["
                    + probability
                    + "]\ttau=["
                    + (1/probability)
                    + "]" );
        }
        System.out.println();

        for( int i = 1; i <= 9; i++ )
        {
            for( int j = 0; j <= 10; j++ )
            {
                float personality = 0.1f * j;
                float probability = calculateFollowProbability( 10, i, true, personality );
                System.out.println( "personality=["
                        + String.format( "%03.1f", personality )
                        + "]\tdeparted=["
                        + i
                        + "]\tprob=["
                        + String.format( "%010.8f", probability )
                        + "]\ttau=["
                        + (1/probability)
                        + "]" );
            }
            System.out.println();
        }

        for( int i = 1; i <= 9; i++ )
        {
            float probability = calculateFollowProbability( 10,
                    i,
                    false,
                    0.5f );
            long contStart = System.currentTimeMillis();
            float meanContinuousTime = gatherMeanContinuousTime( 1/probability );
            long contEnd = System.currentTimeMillis();
            long discStart = System.currentTimeMillis();
            float meanDiscreteTime = gatherMeanDiscreteTime( probability );
            long discEnd = System.currentTimeMillis();
            System.out.println( "departed=["
                    + i
                    + "]\t  prob=["
                    + String.format( "%010.8f", probability )
                    + "]\t  tau=["
                    + (1/probability)
                    + "]\t  meanContinuousTime=["
                    + String.format( "%010.8f", meanContinuousTime )
                    + "]\t  meanDiscreteTime=["
                    + String.format( "%010.8f", meanDiscreteTime )
                    + "]\t  contTime=["
                    + (contEnd - contStart)
                    + "]\t  discTime=["
                    + (discEnd - discStart)
                    + "]" );
        }
    }


    private float calculateInitiateProbability( boolean modify,
            float personality )
    {
        float tau = _initRateBase;

        if( modify )
        {
            float k = calculateK( personality );
            tau /= k;
        }

        return 1.0f / tau;
    }

    private float calculateFollowProbability( int neighborCount,
            int departedCount,
            boolean modify,
            float personality )
    {
        float tau = _followAlpha
                + ( ( _followBeta * (neighborCount - departedCount))
                        / departedCount );

        if( modify )
        {
            float k = calculateK( 1.0f - personality );
            tau /= k;
        }

        return 1.0f / tau;
    }

    private float calculateCancelProbability( int departedCount,
            boolean modify,
            float personality )
    {
        float probability = _cancelAlpha / (1.0f + (float) Math.pow(
                (departedCount / _cancelGamma ), _cancelEpsilon ));

        if( modify )
        {
            float k = calculateK( 1.0f - personality );
            probability *= k;
        }

        return probability;
    }

    private float calculateK( float value )
    {
        return 2.0f * ( 1.0f / (1.0f + (float) Math.exp( (0.5f-value) * 10.0f) ) );
    }


    private float gatherMeanContinuousTime( float tau )
    {
        MersenneTwisterFast random = new MersenneTwisterFast();
        float sumContinuousTime = 0.0f;
        for( int i = 0; i < _EVAL_COUNT; i++ )
        {
            sumContinuousTime +=
                    MathUtils.generateRandomExponential( 1.0f/tau, random );
        }

        return sumContinuousTime / _EVAL_COUNT;
    }

    private float gatherMeanDiscreteTime( float probability )
    {
        MersenneTwisterFast random = new MersenneTwisterFast();
        long sumDiscreteTime = 0;
        for( int i = 0; i < _EVAL_COUNT; i++ )
        {
            int count = 0;
            boolean success = false;
            while( !success )
            {
                success = random.nextFloat() <= probability;
                ++count;
            }
            sumDiscreteTime += count;
        }

        return sumDiscreteTime / ((float) _EVAL_COUNT);
    }
}
