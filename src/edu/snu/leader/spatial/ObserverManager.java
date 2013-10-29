/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

// Imports
import edu.snu.leader.util.MiscUtils;

// Imports
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * ObserverManager
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class ObserverManager
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            ObserverManager.class.getName() );

    /** Key for the number of observers */
    private static final String _SIM_OBSERVER_COUNT = "sim-observer-count";

    /** Key prefix a sim observer's properties */
    private static final String _SIM_OBSERVER_PREFIX = "sim-observer";

    /** Key for a sim observer's class name */
    private static final String _SIM_OBSERVER_CLASS_NAME = "class-name";


    /** The simulation state */
    private SimulationState _simState = null;

    /** The simulation observers */
    private List<SimObserver> _observers =
            new LinkedList<SimObserver>();


    /**
     * Initializes this Observer manager
     *
     * @param simState The simulation state
     */
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Save the simulation state
        _simState = simState;

        // Get the system properties
        Properties props = simState.getProperties();

        // Get the number of observers to load
        String observerCountStr = props.getProperty( _SIM_OBSERVER_COUNT );
        Validate.notNull( observerCountStr,
                "Observer count property ["
                + _SIM_OBSERVER_COUNT
                + "] is required" );
        int observerCount = Integer.parseInt( observerCountStr );

        // Log it
        _LOG.debug( "Loading ["
                + observerCount
                + "] simulation observers" );

        // Load each observer
        for( int i = 0; i < observerCount; i++ )
        {
            // Build the key prefix
            String keyPrefix = _SIM_OBSERVER_PREFIX
                    + "."
                    + i;

            // load the observer
            SimObserver observer = loadObserver( simState, keyPrefix );
            if( null != observer )
            {
                _observers.add( observer );
            }
        }

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Signals all the observers that the simulation is setup to be executed
     */
    public void signalSimSetup()
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simSetup();
        }
    }

    /**
     * Signals all the observers that a simulation run is setup to be executed
     */
    public void signalSimRunSetup()
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simRunSetup();
        }
    }

    /**
     * Signals all the observers that a step within a simulation run is
     * setup to be executed
     */
    public void signalSimRunStepSetup()
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simRunStepSetup();
        }
    }

    /**
     * Signals all the observers that a step within a simulation run has finished
     */
    public void signalSimRunStepTearDown()
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simRunStepTearDown();
        }
    }

    /**
     * Signals all the observers that a simulation run has finished
     */
    public void signalSimRunTearDown()
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simRunTearDown();
        }
    }

    /**
     * Signals all the observers that the simulation has finished
     */
    public void signalSimTearDown()
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simTearDown();
        }
    }

    /**
     * Signals all the observers that an agent has made a decision
     *
     * @param agent The agent making the decision
     * @param event The decision event
     */
    public void signalAgentDecisionEvent( Agent agent, DecisionEvent event )
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().agentDecided( agent, event );
        }
    }

    /**
     * Signals all the observers that a personality has been updated
     *
     * @param event The personality update event
     */
    public void signalPersonalityUpdateEvent( PersonalityUpdateEvent event )
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().personalityUpdated( event );
        }
    }

    /**
     * Signals all the observes that the simulation run will be halted
     *
     * @param reason The reason for the simulation run halt
     */
    public void signalHaltSimulationRun( SimulationRunHaltReason reason )
    {
        Iterator<SimObserver> iter = _observers.iterator();
        while( iter.hasNext() )
        {
            iter.next().simulationRunHalted( reason );
        }

        // Signal the simulation state
        _simState.haltSimulationRun();
    }

    /**
     * Loads a single simulation observer with the given key prefix
     *
     * @param simState The simulation state including configuration
     * @param keyPrefix Prefix for configuration property keys
     * @return The simulation observer
     */
    private SimObserver loadObserver( SimulationState simState, String keyPrefix )
    {
        _LOG.trace( "Entering loadObserver( simState, keyPrefix )" );

        // Create some handy variables
        Properties props = simState.getProperties();

        // Get the class name
        String classNameKey = keyPrefix
                + "."
                + _SIM_OBSERVER_CLASS_NAME;
        String observerClassName = props.getProperty( classNameKey );
        Validate.notNull( observerClassName,
                "Observer class name ["
                + classNameKey
                + "] property is required" );

        // Log a debug statement
        _LOG.debug( "Instantiated observer with key prefix ["
                + keyPrefix
                + "] and class name ["
                + observerClassName
                + "]" );

        // Instantiate the observer
        SimObserver observer = (SimObserver) MiscUtils.loadAndInstantiate(
                observerClassName,
                "Observer classname" );

        // Initialize it
        observer.initialize( simState, keyPrefix );

        _LOG.trace( "Leaving loadObserver( simState, keyPrefix )" );

        return observer;
    }

}
