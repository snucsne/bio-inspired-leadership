package edu.snu.leader.discrete.behavior;

public class DecisionEvent
{
    /** The Decision of this event */
    private Decision _decision = null;

    /** The time this Decision was made */
    private int _time = 0;

    /**
     * Create a DecisionEvent
     * 
     * @param decision The Decision of this event
     * @param time The time this Decision was made
     */
    public DecisionEvent( Decision decision, int time )
    {
        _decision = decision;
        _time = time;
    }

    public Decision getDecision()
    {
        return _decision;
    }

    public int getTime()
    {
        return _time;
    }
}
