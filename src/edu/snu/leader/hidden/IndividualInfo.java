/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden;

/**
 * IndividualInfo
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class IndividualInfo
{
    /** The base initiation rate */
    private float _initiationRate = 0.0f;

    /** The alpha parameter for the cancellation rate */
    private float _cancelAlpha = 0.0f;

    /** The gamma parameter for the cancellation rate */
    private float _cancelGamma = 0.0f;

    /** The epsilon parameter for the cancellation rate */
    private float _cancelEpsilon = 0.0f;

    /** The alpha parameter for the following rate */
    private float _followAlpha = 0.0f;

    /** The beta parameter for the following rate */
    private float _followBeta = 0.0f;

    /** The number of times the individual attempted initiation */
    private int _initiationAttempts = 0;

    /** The number of times the individual successfully initiated */
    private int _initiationSuccesses = 0;


    /**
     * Builds this IndividualInfo object
     *
     * @param initiationRate
     * @param cancelAlpha
     * @param cancelGamma
     * @param cancelEpsilon
     * @param followAlpha
     * @param followBeta
     */
    public IndividualInfo( float initiationRate,
            float cancelAlpha,
            float cancelGamma,
            float cancelEpsilon,
            float followAlpha,
            float followBeta )
    {
        _initiationRate = initiationRate;
        _cancelAlpha = cancelAlpha;
        _cancelGamma = cancelGamma;
        _cancelEpsilon = cancelEpsilon;
        _followAlpha = followAlpha;
        _followBeta = followBeta;
    }

    /**
     *  Notes that the individual attempted an initiation
     */
    public void signalInitiationAttempt()
    {
        _initiationAttempts++;
    }

    /**
     * Notes that the individual was successful in initiating movement
     */
    public void signalInitiationSuccess()
    {
        _initiationSuccesses++;
    }

    /**
     * Returns the initiationRate for this object
     *
     * @return The initiationRate
     */
    public float getInitiationRate()
    {
        return _initiationRate;
    }

    /**
     * Returns the cancelAlpha for this object
     *
     * @return The cancelAlpha
     */
    public float getCancelAlpha()
    {
        return _cancelAlpha;
    }

    /**
     * Returns the cancelGamma for this object
     *
     * @return The cancelGamma
     */
    public float getCancelGamma()
    {
        return _cancelGamma;
    }

    /**
     * Returns the cancelEpsilon for this object
     *
     * @return The cancelEpsilon
     */
    public float getCancelEpsilon()
    {
        return _cancelEpsilon;
    }

    /**
     * Returns the followAlpha for this object
     *
     * @return The followAlpha
     */
    public float getFollowAlpha()
    {
        return _followAlpha;
    }

    /**
     * Returns the followBeta for this object
     *
     * @return The followBeta
     */
    public float getFollowBeta()
    {
        return _followBeta;
    }

    /**
     * Returns the initiationAttempts for this object
     *
     * @return The initiationAttempts
     */
    public int getInitiationAttempts()
    {
        return _initiationAttempts;
    }

    /**
     * Returns the initiationSuccesses for this object
     *
     * @return The initiationSuccesses
     */
    public int getInitiationSuccesses()
    {
        return _initiationSuccesses;
    }


}
