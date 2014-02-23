/*
 * COPYRIGHT
 */
package edu.snu.leader.util.plot;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import org.jibble.epsgraphics.EpsGraphics2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * PositionPlotGenerator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PositionPlotGenerator
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PositionPlotGenerator.class.getName() );

    /** The minimum number of command-line arguments */
    private static final int _MIN_ARG_COUNT = 3;


    /** The plot info */
    private PlotInfo _info = null;

    /** Our graphics object */
    private Graphics2D _g = null;

    /** A mapping between a team name and a color */
    private Map<String, Color> _teamColors = new HashMap<String, Color>();

    /** A mapping between a team name and a plot style */
    private Map<String, PlotStyle> _teamStyles = new HashMap<String, PlotStyle>();

    /** The properties for this plot */
    private Properties _props = null;

    /** The max timesteps */
    private int _maxTimesteps = Integer.MAX_VALUE;

    /**
     * Generates a position plot.
     *
     * @param outputFile The file to save the plot to
     * @param props The properties for this plot
     * @param filenames The filenames of the data dumps to use
     */
    public void generatePlot( String outputFile,
            Properties props,
            List<String> filenames )
        throws Exception
    {
        _LOG.trace( "Entering generatePlot( filenames )" );

        // Save the properties
        _props = props;

        // Load all the data dump files
        List<DataDump> dataDumps = loadDataDumpFiles( filenames );

        // Build all the plot info
        int height = 0;
        int width = 0;
        try
        {
            height = Integer.parseInt( props.getProperty( "height" ) );
            width = Integer.parseInt( props.getProperty( "width" ) );
        }
        catch( NumberFormatException nfe )
        {
            _LOG.error( "Unable to determine height and width" );
            throw new Exception( "Properites error" );
        }

        buildPlotInfo( height, width, dataDumps );

        // Determine the max timesteps
        String maxTimesteps = _props.getProperty( "max-timesteps" );
        if( null != maxTimesteps )
        {
            try
            {
                _maxTimesteps = Integer.parseInt( maxTimesteps );
            }
            catch( NumberFormatException nfe )
            {
                _LOG.error( "Invalid maxTimesteps=[" + maxTimesteps + "]" );
            }
        }

        // Use an eps graphics object
        _g = new EpsGraphics2D();
        _g.setRenderingHint( RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY );
        _g.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON );
        _g.setStroke( new BasicStroke( 0.5f ) );
//        _g.scale( 1.5, 1.5 );

        // Plot all the paths
        Iterator<DataDump> dumpIter = dataDumps.iterator();
        while( dumpIter.hasNext() )
        {
            // Draw the paths first
            plotPath( dumpIter.next(), false );
        }

        // Now draw the agents
        dumpIter = dataDumps.iterator();
        while( dumpIter.hasNext() )
        {
            // Draw the paths first
            plotPath( dumpIter.next(), true );
        }

        // Write the graphics object to a file
        BufferedWriter writer = new BufferedWriter(
                new FileWriter( outputFile ) );
        writer.write( _g.toString() );
        writer.close();
        _LOG.debug( "Wrote eps graphics to [" + outputFile + "]" );

        _LOG.trace( "Leaving loadDataDumpFiles( filenames )" );
    }

    /**
     * Loads all the specified data dump files
     *
     * @param filenames The files to load
     * @return The data dump files
     * @throws Exception
     */
    private List<DataDump> loadDataDumpFiles( List<String> filenames )
        throws Exception
    {
        _LOG.trace( "Entering loadDataDumpFiles( filenames )" );

        List<DataDump> dataDumps = new LinkedList<DataDump>();
        DataDumpLoader loader = new DataDumpLoader();

        try
        {
            Iterator<String> iter = filenames.iterator();
            while( iter.hasNext() )
            {
                dataDumps.add( loader.loadDataDumpFile( iter.next() ) );
                loader.reset();
            }
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to load data dumps: " + ioe.getMessage() );
            throw new Exception( "Unable to load data dumps" );
        }

        _LOG.trace( "Leaving loadDataDumpFiles( filenames )" );

        return dataDumps;
    }

    /**
     * TODO Method description
     *
     * @param args
     */
    public static void main( String[] args )
    {
        // Make sure we have the min number of arguments
        if( args.length < _MIN_ARG_COUNT )
        {
            _LOG.error( "Insufficient arguments ["
                    + args.length
                    + "]" );
            System.exit( 1 );
        }

        // Get the output file
        String outputFile = args[0];

        // Get the properties
        Properties props = new Properties();
        try
        {
            props.load( new FileInputStream( args[1] ) );
            _LOG.debug( "Loaded properties: " + props );
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to load properties file ["
                    + args[1]
                    + "]: "
                    + ioe.getMessage() );
            System.exit(1);
        }

        // Build a list of the filenames
        List<String> filenames = new LinkedList<String>();
        for( int i = 2; i < args.length; i++ )
        {
            filenames.add( args[i] );
        }

        // Create the plot generator
        PositionPlotGenerator generator = new PositionPlotGenerator();

        // Generate the plot
        try
        {
            generator.generatePlot( outputFile, props, filenames );
        }
        catch( Throwable t )
        {
            t.printStackTrace();
            _LOG.error( t );
        }
    }

    /**
     * Plots the path of a world object
     *
     * @param dump The dump containing the path
     */
    private void plotPath( DataDump dump, boolean agentOnly )
    {
        _LOG.trace( "Entering plotPath( dump )" );

        _LOG.debug( "Plotting path for ["
                + dump.getTeamName()
                + "."
                + dump.getName()
                + "]" );

        // Get the positions as points
        Vector3D currentPosition = null;
        Vector3D previousPosition = null;
        Vector3D newPosition = null;
        List<Vector3D> positions = dump.getAllPositions();
        int[] xPoints = new int[ positions.size() ];
        int[] yPoints = new int[ positions.size() ];
        int idx = 0;
        Iterator<Vector3D> positionIter = positions.iterator();
        int[] point = null;
        boolean drawDupePoints = getDupePointsFlag( dump.getTeamName() );
        while( positionIter.hasNext() )
        {
            /* Don't overwrite the previous position if the new
             * position is the same
             */
            newPosition = positionIter.next();
            if( (null != currentPosition)
                    && (newPosition.distance( currentPosition ) > 0.0001) )
            {
                previousPosition = currentPosition;
            }
//            else
//            {
//                _LOG.debug( "Not overwriting previous position" );
//            }
            currentPosition = newPosition;
            point = _info.getScreenLocation( currentPosition );

            if( (null == previousPosition)
                    || (currentPosition.distance( previousPosition ) > 0.0001)
                    || drawDupePoints )
            {
                xPoints[ idx ] = point[ 0 ];
                yPoints[ idx ] = point[ 1 ];
                idx++;
            }
            else
            {
                _LOG.debug( "Omitted dupe point that would go in [" + idx + "]" );
            }
        }

        // Get the line color for this team
        Color teamLineColor = buildColor( dump.getTeamName(), "line" );
        int redDiff = 255 - teamLineColor.getRed();
        int greenDiff = 255 - teamLineColor.getGreen();
        int blueDiff = 255 - teamLineColor.getBlue();

        // Get the plot style
        PlotStyle style = buildPlotStyle( dump.getTeamName() );

        // Draw each step as a line
        int positionCount = ( positions.size() < _maxTimesteps )
                ? positions.size() : _maxTimesteps;
        int partialPositionCount = positionCount / 4;
        int red, green, blue;
        if( !agentOnly )
        {
            for( int i = 0; i < ( positionCount - 1 ); i++ )
            {
                if( style.isDrawActive( i ) )
                {
                    red = 255 - ( ( redDiff * (i + 1 + partialPositionCount) ) / (partialPositionCount + positionCount) );
                    green = 255 - ( ( greenDiff * (i + 1 + partialPositionCount) ) / (partialPositionCount + positionCount) );
                    blue = 255 - ( ( blueDiff * (i + 1 + partialPositionCount) ) / (partialPositionCount + positionCount) );
                    _g.setColor( new Color( red, green, blue ) );
                    _g.drawLine( xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1] );
                }
            }
        }
        else
        {
            // Get the team fill color
            Color teamFillColor = buildColor( dump.getTeamName(), "fill" );

            // Draw a shape showing the world object
            int scaledBoundingRadius = (int) Math.ceil( dump.getCollisionBoundingRadius()
                    * _info.scale );
            int[] worldObjX = new int[4];
            int[] worldObjY = new int[4];
            worldObjX[0] = xPoints[ positionCount - 1 ];
            worldObjY[0] = yPoints[ positionCount - 1 ] - scaledBoundingRadius;
            worldObjX[1] = xPoints[ positionCount - 1 ] + ( scaledBoundingRadius / 2 );
            worldObjY[1] = yPoints[ positionCount - 1 ] + ( scaledBoundingRadius / 2 );
            worldObjX[2] = xPoints[ positionCount - 1 ];
            float tmp = scaledBoundingRadius / 1.2f;
            worldObjY[2] = yPoints[ positionCount - 1 ] + (int) tmp;
            worldObjX[3] = xPoints[ positionCount - 1 ] - ( scaledBoundingRadius / 2 );
            worldObjY[3] = yPoints[ positionCount - 1 ] + ( scaledBoundingRadius / 2 );

            // Calculate the angle to rotate
            Vector2D v1 = new Vector2D( 0.0f, 1.0f );
            Vector2D v2 = new Vector2D( currentPosition.getX() - previousPosition.getX(),
                    currentPosition.getY() - previousPosition.getY() );
            v2 = v2.normalize();
            float angle = (float) Math.acos( v1.dotProduct( v2 ) );
            if( v2.getX() < 0 )
            {
                angle *= -1;
            }
            angle = (float) Math.PI - angle;

            // Rotate and draw it
            AffineTransform transform = _g.getTransform();
            BasicStroke stroke = new BasicStroke( 2.0f );
            _g.setStroke( stroke );
            _g.rotate( angle, xPoints[ positionCount - 1 ], yPoints[ positionCount - 1 ] );
            _g.setColor( teamFillColor );
            _g.fillPolygon( worldObjX, worldObjY, worldObjX.length );
            _g.setColor( teamLineColor );
            _g.drawPolygon( worldObjX, worldObjY, worldObjX.length );
            _g.setTransform( transform );
            //        _g.rotate( -angle, xPoints[ positionCount - 1 ], yPoints[ positionCount - 1 ] );
        }


        _LOG.trace( "Leaving plotPath( dump )" );
    }

    /**
     * TODO Method description
     *
     * @param teamName
     * @return
     */
    private boolean getDupePointsFlag( String teamName )
    {
        String key = teamName + "-draw-dupe-points";
        String dupeStr = _props.getProperty( key );
        boolean flag = true;
        if( null != dupeStr )
        {
            flag = Boolean.parseBoolean( dupeStr );
        }

        _LOG.debug( "Drawing dupe points flag=[" + flag + "] for team [" + teamName + "]" );

        return flag;
    }

    /**
     * Builds the base color for a given team
     *
     * @param teamName The team name
     */
    private Color buildColor( String teamName, String type )
    {
        _LOG.trace( "Entering buildColor( teamName )" );

        _LOG.debug( "Building color for team [" + teamName + "]" );

        // See if we already have one
        Color color = _teamColors.get( teamName + "-" + type );
        if( null == color )
        {
            // See if we can find a system property for it
            String key = teamName + "-" + type + "-color";
            String colorStr = _props.getProperty( key );
            if( null != colorStr )
            {
                // Yup, try to parse it
                String[] splitColor = colorStr.split( "," );
                if( splitColor.length != 3 )
                {
                    _LOG.debug( "Inavlid color ["
                            + splitColor
                            + "]" );
                }
                else
                {
                    float r = 0.0f;
                    float g = 0.0f;
                    float b = 0.0f;
                    try
                    {
                        r = Float.parseFloat( splitColor[0] );
                        g = Float.parseFloat( splitColor[1] );
                        b = Float.parseFloat( splitColor[2] );
                        color = new Color( r, g, b );
                        _LOG.debug( "Loaded color [" + color + "]" );
                    }
                    catch( NumberFormatException nfe )
                    {
                        _LOG.error( "Unable to parse color ["
                                + colorStr
                                + "]" );
                    }
                }
            }
            else
            {
                _LOG.debug( "No color property found for key ["
                        + key
                        + "]" );
            }

            if( null == color )
            {
                // Use black as the default
                color = new Color( 1, 0, 0 );
                _LOG.debug( "Used default color" );
            }

            // Put it in the map
            _teamColors.put( teamName, color );
        }

        _LOG.trace( "Leaving buildColor( teamName )" );

        return color;
    }

    /**
     * Builds the plot style for the given team
     *
     * @param teamName
     * @return The plot style
     */
    private PlotStyle buildPlotStyle( String teamName )
    {
        _LOG.trace( "Entering buildPlotStyle( teamName )" );

        // Try to get it from the map
        PlotStyle style = _teamStyles.get( teamName );
        if( null == style )
        {
            String key = teamName + "-style";
            String value = _props.getProperty( key );
            if( null != value )
            {
                _LOG.debug( "Found style ["
                        + value
                        + "] for team ["
                        + teamName
                        + "]" );

                if( "solid".equals( value ) )
                {
                    style = new SolidPlotStyle();
                }
                else if( "dashed".equals( value ) )
                {
                    style = new SimpleDashedPlotStyle();
                }
            }

            // Use the default
            if( null == style )
            {
                style = new SolidPlotStyle();
                _LOG.debug( "Using default style" );
            }

            // Put it in the map
            _teamStyles.put( teamName, style );
        }

        _LOG.trace( "Leaving buildPlotStyle( teamName )" );

        return style;
    }

    /**
     * Determines the min and max x and y points do display
     *
     * @param height The height of the plot
     * @param width The width of the plot
     * @param dumps All the data dumps
     */
    private void buildPlotInfo( int height, int width, List<DataDump> dumps )
    {
        _LOG.trace( "Entering buildPlotInfo( height, width, dumps )" );

        float minX = 0.0f;
        float maxX = 0.0f;
        float minY = 0.0f;
        float maxY = 0.0f;

        // Iterate over all the world objects
        Iterator<DataDump> dumpIter = dumps.iterator();
        DataDump currentDump = null;
        while( dumpIter.hasNext() )
        {
            currentDump = dumpIter.next();

            // Get the collision bounding radius
            float collisionBoundingRadius = currentDump.getCollisionBoundingRadius();

            // Iterate over all the positions
            Vector3D currentPosition = null;
            Iterator<Vector3D> positionIter = currentDump.getAllPositions().iterator();
            while( positionIter.hasNext() )
            {
                currentPosition = positionIter.next();

                if( maxX < (currentPosition.getX() + collisionBoundingRadius) )
                {
                    maxX = (float) currentPosition.getX() + collisionBoundingRadius;
                }
                if( minX > (currentPosition.getX() - collisionBoundingRadius) )
                {
                    minX = (float) currentPosition.getX() - collisionBoundingRadius;
                }
                if( maxY < (currentPosition.getY() + collisionBoundingRadius) )
                {
                    maxY = (float) currentPosition.getY() + collisionBoundingRadius;
                }
                if( minY > (currentPosition.getY() - collisionBoundingRadius) )
                {
                    minY = (float) currentPosition.getY() - collisionBoundingRadius;
                }
            }
        }

        // Bump them up by 10 percent
        float xDelta = ( maxX - minX ) * 0.1f;
        float yDelta = ( maxY - minY ) * 0.1f;
        minX -= xDelta;
        maxX += xDelta;
        minY -= yDelta;
        maxY += yDelta;

        // Create the plot info
        _info = new PlotInfo( minX, maxX, minY, maxY, width, height );

        _LOG.trace( "Leaving buildPlotInfo( height, width, dumps )" );
    }

}
