/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.observer;

// Imports
import edu.snu.leader.hidden.Neighbor;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.List;
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
            "enable-move-towards-successful-leader";

    /** Key for flag to enable moving away from failed leaders */
    private static final String _ENABLE_MOVE_AWAY_FROM_FAILED_KEY =
            "enable-move-away-from-failed-leader";

    /** Key for the max distance to move towards successful leaders */
    private static final String _MAX_TOWARDS_MOVE_DIST_KEY =
            "max-towards-leader-move-distance";

    /** Key for the max distance to move away from failed leaders */
    private static final String _MAX_AWAY_FROM_MOVE_DIST_KEY =
            "max-away-from-leader-move-distance";

    /** Key for the adjustment distance to move away from neighbors */
    private static final String _MAX_TOWARDS_NEIGHBOR_MOVE_DIST_KEY =
            "max-towards-neighbor-move-distance";

    /** Key for the adjustment distance to move away from neighbors */
    private static final String _MAX_AWAY_FROM_NEIGHBOR_MOVE_DIST_KEY =
            "max-away-from-neighbor-move-distance";

    /** Key for the preferred initiating individual's ID */
    protected static final String _INITIATOR_ID_KEY = "initiator-id";

    /** Exponent for magnitude calculation of movement */
    private static final double _MAGNITUDE_EXPONENT = 3.0;

    /** Threshold to determine if the individual moved */
    private static final double _MOVEMENT_THRESHOLD = 0.01;


    /** Flag to enable moving towards successful leaders */
    private boolean _enableMoveTowardsSuccessfulLeader = false;

    /** Flag to enable moving away from failed leaders */
    private boolean _enableMoveAwayFromFailedLeader = false;

    /** Max distance to move away from successful leaders */
    private float _maxTowardsLeaderMoveDistance = 0.0f;

    /** Max distance to move away from failed leaders */
    private float _maxAwayFromLeaderMoveDistance = 0.0f;

    /** Max distance to move away from neighbors */
    private float _maxTowardsNeighborMoveDistance = 0.0f;

    /** Max distance to move away from neighbors */
    private float _maxAwayFromNeighborMoveDistance = 0.0f;

    /** The ID of the preferred initiating individual */
    protected Object _initiatorID = null;




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
        _enableMoveTowardsSuccessfulLeader = Boolean.parseBoolean(
                enableMoveTowardsSuccessfulStr );
        _LOG.info( "Using _enableMoveTowardsSuccessful=["
                + _enableMoveTowardsSuccessfulLeader
                + "]" );

        // Get the flag to enable moving away from failed leaders
        String enableMoveAwayFromFailedStr = props.getProperty(
                _ENABLE_MOVE_AWAY_FROM_FAILED_KEY );
        Validate.notEmpty( enableMoveAwayFromFailedStr,
                "Flag to enable moving away from failed leaders (key="
                + _ENABLE_MOVE_AWAY_FROM_FAILED_KEY
                + ") may not be empty" );
        _enableMoveAwayFromFailedLeader = Boolean.parseBoolean(
                enableMoveAwayFromFailedStr );
        _LOG.info( "Using _enableMoveAwayFromFailed=["
                + _enableMoveAwayFromFailedLeader
                + "]" );

        // Get the max distance to move towards successful leaders
        String maxTowardsMoveDistanceStr = props.getProperty(
                _MAX_TOWARDS_MOVE_DIST_KEY );
        Validate.notEmpty( maxTowardsMoveDistanceStr,
                "Max distance to move towards successful leaders (key="
                + _MAX_TOWARDS_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxTowardsLeaderMoveDistance = Float.parseFloat(
                maxTowardsMoveDistanceStr );
        _LOG.info( "Using _maxTowardsMoveDistance=["
                + _maxTowardsLeaderMoveDistance
                + "]" );

        // Get the max distance to move away from failed leaders
        String maxAwayFromMoveDistanceStr = props.getProperty(
                _MAX_AWAY_FROM_MOVE_DIST_KEY );
        Validate.notEmpty( maxAwayFromMoveDistanceStr,
                "Max distance to move away from failed leaders (key="
                + _MAX_AWAY_FROM_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxAwayFromLeaderMoveDistance = Float.parseFloat(
                maxAwayFromMoveDistanceStr );
        _LOG.info( "Using _maxAwayFromMoveDistance=["
                + _maxAwayFromLeaderMoveDistance
                + "]" );

        // Get the max distance to move towards neighbors
        String maxTowardsNeighborMoveDistanceStr = props.getProperty(
                _MAX_TOWARDS_NEIGHBOR_MOVE_DIST_KEY );
        Validate.notEmpty( maxTowardsNeighborMoveDistanceStr,
                "Max distance to move towards neighbors (key="
                + _MAX_TOWARDS_NEIGHBOR_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxTowardsNeighborMoveDistance = Float.parseFloat(
                maxTowardsNeighborMoveDistanceStr );
        _LOG.info( "Using _maxTowardsNeighborMoveDistance=["
                + _maxTowardsNeighborMoveDistance
                + "]" );

        // Get the max distance to move away from neighbors
        String maxAwayFromNeighborMoveDistanceStr = props.getProperty(
                _MAX_AWAY_FROM_NEIGHBOR_MOVE_DIST_KEY );
        Validate.notEmpty( maxAwayFromNeighborMoveDistanceStr,
                "Max distance to move away from neighbors (key="
                + _MAX_AWAY_FROM_NEIGHBOR_MOVE_DIST_KEY
                + ") may not be empty" );
        _maxAwayFromNeighborMoveDistance = Float.parseFloat(
                maxAwayFromNeighborMoveDistanceStr );
        _LOG.info( "Using _maxAwayFromNeighborMoveDistance=["
                + _maxAwayFromNeighborMoveDistance
                + "]" );

        // Get the ID of the preferred initiating individual
        String initiatorIDStr = props.getProperty( _INITIATOR_ID_KEY );
        Validate.notEmpty( initiatorIDStr,
                "Initiating ID (key="
                + _INITIATOR_ID_KEY
                + ") may not be empty" );
        _initiatorID = initiatorIDStr;
        _LOG.info( "Using _initiatorID=[" + _initiatorID + "]" );


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

            // Get the successful and failed leaders
            Neighbor leader = ind.getLeader();
            List<Neighbor> failedLeaders = ind.getFailedLeaders();

            /* If the successful leader wasn't the preferred one, treat
             * it as if it failed (e.g., it led the group to the wrong
             * destination). */
            if( ( null != leader ) && !_initiatorID.equals( leader.getIndividual().getID() ) )
            {
                _LOG.debug( "Successful leader ["
                        + leader.getIndividual().getID()
                        + "] wasn't preferred initiator ["
                        + _initiatorID
                        + "]" );
                failedLeaders.add( leader );
                leader = null;
            }

            // Did they follow a successful initiator?
            if( _enableMoveTowardsSuccessfulLeader && (null != leader) )
            {
                // Yup
                Vector2D toLeader = leader.getIndividual().getLocation().subtract(
                        currentLocation );

                // Calculate the magnitude of the pull towards the leader
                double magnitude = _maxTowardsLeaderMoveDistance
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
            if( _enableMoveAwayFromFailedLeader )
            {
                Iterator<Neighbor> failedLeaderIter = failedLeaders.iterator();
                while( failedLeaderIter.hasNext() )
                {
                    leader = failedLeaderIter.next();

                    // Yup
                    Vector2D fromLeader = currentLocation.subtract(
                            leader.getIndividual().getLocation() );

                    // Calculate the magnitude of the push from the failed leader
                    double magnitude = _maxAwayFromLeaderMoveDistance
                            * ( 1.0 / Math.max( 1.0, Math.pow( fromLeader.getNorm(),
                                    _MAGNITUDE_EXPONENT ) ) );

                    // Move it
                    movement = movement.add( fromLeader.normalize().scalarMultiply(
                            magnitude ) );

                    _LOG.debug( "Moving away from failed leader: current=["
                            + currentLocation
                            + "] leader["
                            + leader.getIndividual().getID()
                            + "]=["
                            + leader.getIndividual().getLocation()
                            + "] fromLeader=["
                            + fromLeader
                            + "] distance=["
                            + fromLeader.getNorm()
                            + "] magnitude=["
                            + magnitude
                            + "] movement=["
                            + movement
                            + "]" );
                }
            }

            // Does the individual move towards its neighbors?
            if( 0.0f < _maxTowardsNeighborMoveDistance )
            {
                // Calculate the mean position of all the neighbors
                Vector2D meanPosition = Vector2D.ZERO;
                List<Neighbor> neighbors = ind.getNearestNeighbors();
                Iterator<Neighbor> neighborIter = neighbors.iterator();
                while( neighborIter.hasNext() )
                {
                    Neighbor neighbor = neighborIter.next();
                    Vector2D neighborLocation = neighbor.getIndividual().getLocation();
                    meanPosition = meanPosition.add( neighborLocation );
                }
                meanPosition = meanPosition.scalarMultiply(
                        1.0 / neighbors.size() );

                // Move towards the mean position
                Vector2D toMean = meanPosition.subtract(
                        currentLocation );

                // Calculate the magnitude of the pull towards the leader
                double magnitude = _maxTowardsNeighborMoveDistance
                        * (1.0 - ( 1.0 / Math.max( 1.0, Math.pow( toMean.getNorm(),
                                _MAGNITUDE_EXPONENT ) ) ) );

                // Move it
                movement = movement.add( toMean.normalize().scalarMultiply(
                        magnitude ) );

                _LOG.debug( "Moving towards neighbor mean location: current=["
                        + currentLocation
                        + "] meanPosition["
                        + meanPosition
                        + "] toMean=["
                        + toMean
                        + "] distance=["
                        + toMean.getNorm()
                        + "] magnitude=["
                        + magnitude
                        + "] movement=["
                        + movement
                        + "]" );

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

                    // Calculate the magnitude
                    double magnitude = _maxAwayFromNeighborMoveDistance
                            * (1.0 / Math.max( 1.0, Math.pow( toInd.getNorm(),
                                    _MAGNITUDE_EXPONENT ) ) );

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
