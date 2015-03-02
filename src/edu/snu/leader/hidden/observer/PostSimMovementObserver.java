/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

// Imports
import edu.snu.leader.hidden.Neighbor;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.NotYetImplementedException;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.Properties;


/**
 * PostSimMovementObserver
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PostSimMovementObserver
        extends AbstractSimulationObserver
        implements SimulationObserver
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PostSimMovementObserver.class.getName() );

    /** Key for flag to enable moving towards successful leaders */
    private static final String _ENABLE_MOVE_TOWARDS_SUCCESSFUL_KEY =
            "enable-move-towards-successful";

    /** Key for flag to enable moving away from failed leaders */
    private static final String _ENABLE_MOVE_AWAY_FROM_FAILED_KEY =
            "enable-move-away-from-failed";

    /** Key for the max distance to move towards successful leaders */
    private static final String _MAX_TOWARDS_MOVE_DIST_KEY =
            "max-towards-move-distance";

    /** Key for the max distance to move away from failed leaders */
    private static final String _MAX_AWAY_FROM_MOVE_DIST_KEY =
            "max-away-from-move-distance";

    /** Key for the adjustment distance to move away from neighbors */
    private static final String _MAX_ADJUSTMENT_MOVE_DIST_KEY =
            "max-adjustment-move-distance";

    /** Exponent for magnitude calculation of movement */
    private static final double _MAGNITUDE_EXPONENT = 5.0;

    /** Threshold to determine if the individual moved */
    private static final double _MOVEMENT_THRESHOLD = 0.01;

    /** The minimum allowable distance to another individual */
    private static final double _MININMUM_DISTANCE = 1.1;


    /** Flag to enable moving towards successful leaders */
    private boolean _enableMoveTowardsSuccessful = false;

    /** Flag to enable moving away from failed leaders */
    private boolean _enableMoveAwayFromFailed = false;

    /** Max distance to move away from successful leaders */
    private float _maxTowardsMoveDistance = 0.0f;

    /** Max distance to move away from failed leaders */
    private float _maxAwayFromMoveDistance = 0.0f;

    /** Max distance to move away from failed leaders */
    private float _maxAdjustmentMoveDistance = 0.0f;


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

        // Get the flag to enable moving towards successful leaders
        String enableMoveTowardsSuccessfulStr = props.getProperty(
                _ENABLE_MOVE_TOWARDS_SUCCESSFUL_KEY );
        Validate.notEmpty( enableMoveTowardsSuccessfulStr,
                "Flag to enable moving towards successful leaders (key="
                + _ENABLE_MOVE_TOWARDS_SUCCESSFUL_KEY
                + ") may not be empty" );
        _enableMoveTowardsSuccessful = Boolean.parseBoolean(
                enableMoveTowardsSuccessfulStr );
        _LOG.info( "Using _enableMoveTowardsSuccessful=["
                + _enableMoveTowardsSuccessful
                + "]" );

        // Get the flag to enable moving away from failed leaders
        String enableMoveAwayFromFailedStr = props.getProperty(
                _ENABLE_MOVE_AWAY_FROM_FAILED_KEY );
        Validate.notEmpty( enableMoveAwayFromFailedStr,
                "Flag to enable moving away from failed leaders (key="
                + _ENABLE_MOVE_AWAY_FROM_FAILED_KEY
                + ") may not be empty" );
        _enableMoveAwayFromFailed = Boolean.parseBoolean(
                enableMoveAwayFromFailedStr );
        _LOG.info( "Using _enableMoveAwayFromFailed=["
                + _enableMoveAwayFromFailed
                + "]" );

        // Get the max distance to move towards successful leaders
        String maxTowardsMoveDistanceStr = props.getProperty(
                _MAX_TOWARDS_MOVE_DIST_KEY );
        Validate.notEmpty( maxTowardsMoveDistanceStr,
                "Max distance to move towards successful leaders (key="
                + _MAX_TOWARDS_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxTowardsMoveDistance = Float.parseFloat(
                maxTowardsMoveDistanceStr );
        _LOG.info( "Using _maxTowardsMoveDistance=["
                + _maxTowardsMoveDistance
                + "]" );

        // Get the max distance to move away from failed leaders
        String maxAwayFromMoveDistanceStr = props.getProperty(
                _MAX_AWAY_FROM_MOVE_DIST_KEY );
        Validate.notEmpty( maxAwayFromMoveDistanceStr,
                "Max distance to move away from failed leaders (key="
                + _MAX_AWAY_FROM_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxAwayFromMoveDistance = Float.parseFloat(
                maxAwayFromMoveDistanceStr );
        _LOG.info( "Using _maxAwayFromMoveDistance=["
                + _maxAwayFromMoveDistance
                + "]" );

        // Get the max distance to move for adjustments
        String maxAdjustmentMoveDistanceStr = props.getProperty(
                _MAX_ADJUSTMENT_MOVE_DIST_KEY );
        Validate.notEmpty( maxAdjustmentMoveDistanceStr,
                "Max distance to move for adjustment (key="
                + _MAX_ADJUSTMENT_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxAdjustmentMoveDistance = Float.parseFloat(
                maxAdjustmentMoveDistanceStr );
        _LOG.info( "Using _maxAdjustmentMoveDistance=["
                + _maxAdjustmentMoveDistance
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Performs any cleanup after a simulation run has finished execution
     *
     * @see edu.snu.leader.hidden.observer.SimulationObserver#simRunTearDown()
     */
    @Override
    public void simRunTearDown()
    {
        _LOG.trace( "Entering simRunTearDown()" );

        // Move all the individuals
        Iterator<SpatialIndividual> indIter = _simState.getAllIndividuals().iterator();
        while( indIter.hasNext() )
        {
            SpatialIndividual ind = indIter.next();

            _LOG.debug( "Moving ind=[" + ind.getID() + "]" );

            // Create some handy variables
            Vector2D movement = Vector2D.ZERO;
            Vector2D currentLocation = ind.getLocation();
            boolean moved = false;

            // Did they follow a successful initiator?
            Neighbor leader = ind.getLeader();
            if( _enableMoveTowardsSuccessful && (null != leader) )
            {
                // Yup
                Vector2D toLeader = leader.getIndividual().getLocation().subtract(
                        currentLocation );

                // Calculate the magnitude of the pull towards the leader
                double magnitude = _maxTowardsMoveDistance
                        * (1.0 - ( 1.0 / Math.max( 1.0, Math.pow( toLeader.getNorm(),
                                _MAGNITUDE_EXPONENT ) ) ) );

                // Move it
                movement = movement.add( toLeader.normalize().scalarMultiply(
                        magnitude ) );

                _LOG.debug( "Moving towards successful leader: current=["
                        + currentLocation
                        + "] leader["
                        + leader.getIndividual().getID()
                        + "]=["
                        + leader.getIndividual().getLocation()
                        + "] toLeader=["
                        + toLeader
                        + "] distance=["
                        + toLeader.getNorm()
                        + "] magnitude=["
                        + magnitude
                        + "] movement=["
                        + movement
                        + "]" );
            }

            // Did they follow any failed initiators?
            if( _enableMoveAwayFromFailed )
            {
                throw new NotYetImplementedException();
            }

            // Move the individual to the new position
            Vector2D newLocation = currentLocation.add( movement );

            // Did the individual move?
            if( movement.getNorm() >= _MOVEMENT_THRESHOLD )
            {
                // Ensure there are no collisions
                Vector2D adjustment = Vector2D.ZERO;
                Iterator<Neighbor> neighborIter = ind.getNearestNeighbors().iterator();
                while( neighborIter.hasNext() )
                {
                    // Calculate the push away from the neighbors
                    Neighbor current = neighborIter.next();
                    Vector2D toInd = newLocation.subtract(
                            current.getIndividual().getLocation() );

                    // Is it within the minimum?
//                    double magnitude = 0.0;
//                    if( toInd.getNorm() < _MININMUM_DISTANCE )
//                    {
//                        // Yup
//                        magnitude = ( _MININMUM_DISTANCE - toInd.getNorm() ) + 0.01;
//                    }
//                    else
//                    {
                        // Calculate the magnitude
                        double magnitude = _maxAdjustmentMoveDistance
                                * (1.0 / Math.max( 1.0, Math.pow( toInd.getNorm(),
                                        _MAGNITUDE_EXPONENT ) ) );
//                    }

                    // Move it
                    adjustment = adjustment.add( toInd.normalize().scalarMultiply(
                            magnitude ) );

                    _LOG.debug( "Adjusting: new=["
                            + newLocation
                            + "] neighbor=["
                            + current.getIndividual().getLocation()
                            + "] toInd=["
                            + toInd
                            + "] distance=["
                            + toInd.getNorm()
                            + "] magnitude=["
                            + magnitude
                            + "] adjustment=["
                            + adjustment
                            + "]" );
                }

                // Adjust the individual's position
                newLocation = newLocation.add( adjustment );
                ind.setLocation( newLocation );

                _LOG.debug( "Final location=["
                        + newLocation
                        + "]" );
            }
        }

        // Update all the nearest neighbors for all the individuals
        _simState.updateAllNearestNeighbors();

        _LOG.trace( "Leaving simRunTearDown()" );
    }
}
