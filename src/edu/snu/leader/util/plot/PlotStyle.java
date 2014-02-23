/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

/**
 * PlotStyle
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface PlotStyle
{
    /**
     * Determine if the current line segment should be dranw
     *
     * @param idx The current index
     */
    public boolean isDrawActive( int idx );

}
