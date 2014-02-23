/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

/**
 * SolidPlotStyle
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SolidPlotStyle implements PlotStyle
{

    /**
     * TODO Method description
     *
     * @param idx
     * @return
     * @see edu.snu.leader.util.plot.PlotStyle#isDrawActive(int)
     */
    @Override
    public boolean isDrawActive( int idx )
    {
        return true;
    }

}
