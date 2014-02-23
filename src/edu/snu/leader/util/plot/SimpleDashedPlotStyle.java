/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

/**
 * SimpleDashedPlotStyle
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class SimpleDashedPlotStyle implements PlotStyle
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
        return ((idx % 2) == 0);
    }

}
