/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

// Imports
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * MimicsDependantDirectionSimObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MimicsDependantDirectionSimObserver extends
        AbstractSimulationObserver
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            MimicsDependantDirectionSimObserver.class.getName() );

    /** Key for the low mimics direction offset */
    private static final String _LOW_MIMICS_DIR_OFFSET_KEY = "low-mimics-dir-offset";

    /** Key for the high mimics direction offset */
    private static final String _HIGH_MIMICS_DIR_OFFSET_KEY = "high-mimics-dir-offset";

    /** Key for the minimum preferred direction */
    private static final String _MIN_DIR_KEY = "min-direction";

    /** Key for the maximum preferred direction */
    private static final String _MAX_DIR_KEY = "max-direction";



    /** Offset applied to preferred direction of movement for individuals
     * with a low number of mimicking neighbors. */
    private float _lowMimicsDirOffset = 0.0f;

    /** Offset applied to preferred direction of movement for individuals
     * with a high number of mimicking neighbors. */
    private float _highMimicsDirOffset = 0.0f;

    /** Minimum preferred direction */
    private float _minDirection = 0.0f;

    /** Maximum preferred direction */
    private float _maxDirection = 0.0f;


    /**
     * Initializes this observer
     *
     * @param simState The simulation state
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the simulation properties
        Properties props = simState.getProps();

        // Get the low mimics dir offset
        String lowMimicsDirOffsetStr = props.getProperty(
                _LOW_MIMICS_DIR_OFFSET_KEY );
        Validate.notEmpty( lowMimicsDirOffsetStr,
                "Low mimiccs direction offset (key="
                + _LOW_MIMICS_DIR_OFFSET_KEY
                + ") may not be empty" );
        _lowMimicsDirOffset = Float.parseFloat( lowMimicsDirOffsetStr );
        _LOG.info( "Using _lowMimicsDirOffset=[" + _lowMimicsDirOffset + "]" );

        // Get the high mimics dir offset
        String highMimicsDirOffsetStr = props.getProperty(
                _HIGH_MIMICS_DIR_OFFSET_KEY );
        Validate.notEmpty( highMimicsDirOffsetStr,
                "High mimiccs direction offset (key="
                + _HIGH_MIMICS_DIR_OFFSET_KEY
                + ") may not be empty" );
        _highMimicsDirOffset = Float.parseFloat( highMimicsDirOffsetStr );
        _LOG.info( "Using _highMimicsDirOffset=[" + _highMimicsDirOffset + "]" );

        // Get the min direction
        String minDirectionStr = props.getProperty( _MIN_DIR_KEY );
        Validate.notEmpty( minDirectionStr,
                "Minimum Direction (key="
                + _MIN_DIR_KEY
                + ") may not be empty" );
        _minDirection = Float.parseFloat( minDirectionStr );
        _LOG.info( "Using _minDirection=[" + _minDirection + "]" );

        // Get the max direction
        String maxDirectionStr = props.getProperty( _MAX_DIR_KEY );
        Validate.notEmpty( maxDirectionStr,
                "Maximum direction (key="
                + _MAX_DIR_KEY
                + ") may not be empty" );
        _maxDirection = Float.parseFloat( maxDirectionStr );
        _LOG.info( "Using _maxDirection=[" + _maxDirection + "]" );


        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Prepares the simulation for execution
     *
     * @see edu.snu.leader.hidden.observer.AbstractSimulationObserver#simSetUp()
     */
    @Override
    public void simSetUp()
    {
        // Find the median number of mimicking neighbors
        SpatialIndividual current = null;
        List<SpatialIndividual> allInds = _simState.getAllIndividuals();
        int[] mimickingNeighborCounts = new int[ allInds.size() ];
        for( int i = 0; i < allInds.size(); i++ )
        {
            mimickingNeighborCounts[i] = allInds.get(i).getMimickingNeighborCount();
        }

        // Sort it
        Arrays.sort( mimickingNeighborCounts );

        // Get the median value
        int medianMimickingNeighbors =
                mimickingNeighborCounts[ mimickingNeighborCounts.length / 2 ];
        _LOG.info( "Median mimicking neighbors ["
                + medianMimickingNeighbors
                + "]" );

        // Iterate through the individuals again to set their preferred dir
        Iterator<SpatialIndividual> iter = allInds.iterator();
        while( iter.hasNext() )
        {
            // Get the current individual and their number of mimics
            current = iter.next();
            int currentMimics = current.getMimickingNeighborCount();

            /* If it is below the median add the low offset.  Otherwise,
             * add the high offset */
            float offset = _lowMimicsDirOffset;
            if( currentMimics >= medianMimickingNeighbors )
            {
                offset = _highMimicsDirOffset;
            }
            float direction = current.getPreferredDirection() + offset;

            // Ensure it is within the direction bounds
            if( _maxDirection < direction )
            {
                direction = _maxDirection;
            }
            else if (_minDirection > direction )
            {
                direction = _minDirection;
            }

            // Assign the direction
            current.setPreferredDirection( direction );

            // Log it
            _LOG.info( "Mimics=["
                    + currentMimics
                    + "] offset=["
                    + offset
                    + "] direction=["
                    + direction
                    + "]" );
        }
    }
}
