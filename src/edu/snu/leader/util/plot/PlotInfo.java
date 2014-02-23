/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Logger;

/**
 * PlotInfo
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PlotInfo
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PlotInfo.class.getName() );

    /** Min x value to display in world */
    public final float minX;

    /** Max x value to display in world */
    public final float maxX;

    /** Min y value to display in world */
    public final float minY;

    /** Max y value to display in world */
    public final float maxY;

    /** Display width */
    public final int width;

    /** Display height */
    public final int height;

    /** Our scaling factor */
    public final float scale;

    /**
     * Constructor
     */
    public PlotInfo( float minX, float maxX,
            float minY, float maxY,
            int width, int height )
    {
        // Determine the scale
        float xScale = width / (maxX - minX);
        float yScale = height / (maxY - minY);

        if( xScale < yScale )
        {
            this.scale = xScale;
            this.minX = minX;
            this.maxX = maxX;

            float yDelta = height / ( xScale * 2.0f );
            float yAvg = (minY + maxY) / 2.0f;
            this.minY = yAvg - yDelta;
            this.maxY = yAvg + yDelta;

            if( _LOG.isDebugEnabled() )
            {
                _LOG.debug( "Using xScale["
                        + xScale
                        + "]: minY=["
                        + minY
                        + "=>"
                        + this.minY
                        + "] maxY=["
                        + maxY
                        + "=>"
                        + this.maxY
                        + "]" );
                _LOG.debug( "yScale=[" + yScale + "]" );
            }
        }
        else
        {
            this.scale = yScale;
            this.minY = minY;
            this.maxY = maxY;

            float xDelta = width / ( yScale * 2.0f );
            float xAvg = (minX + maxX) / 2.0f;
            this.minX = xAvg - xDelta;
            this.maxX = xAvg + xDelta;


            if( _LOG.isDebugEnabled() )
            {
                _LOG.debug( "Using yScale["
                        + yScale
                        + "]: minX=["
                        + minX
                        + "=>"
                        + this.minX
                        + "] maxX=["
                        + maxX
                        + "=>"
                        + this.maxX
                        + "]" );
                _LOG.debug( "xScale=[" + xScale + "]" );
            }
        }

        this.width = width;
        this.height = height;
    }

    /** Determines the screen location of a point */
    public int[] getScreenLocation( Vector3D point )
    {
        // Reverse every coordinate
        return new int[] {
                (int) ((( point.getX() ) - minX) * scale),
                (int) (this.height - ( (( -1 * point.getY() ) - minY) * scale )) };
    }

}
