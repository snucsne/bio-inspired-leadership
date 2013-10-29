/*
 * COPYRIGHT
 */
package edu.snu.leader.hierarchy.simple;

// Imports
import edu.snu.leader.util.MiscUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.util.Properties;



/**
 * HierarchyBuildingExperiment
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class HierarchyBuildingSimulation
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            HierarchyBuildingSimulation.class.getName() );

    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    /** Key for the reporter class */
    private static final String _REPORTER_CLASS_KEY = "reporter-class";



    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The reporter */
    private Reporter _reporter = null;



    /**
     * Initialize the simulation
     */
    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Load the properties
        _props = MiscUtils.loadProperties( _PROPS_FILE_KEY );

        // Initialize the simulation state
        _simState.initialize( _props );

        // Load the reporter
        String reporter = _props.getProperty( _REPORTER_CLASS_KEY );
        Validate.notEmpty( reporter,
                "Reporter class (key="
                + _REPORTER_CLASS_KEY
                + ") may not be empty" );
        _reporter = (Reporter) MiscUtils.loadAndInstantiate(
                reporter,
                "Reporter class" );
        _reporter.initialize( _simState );


        _LOG.trace( "Leaving initialize()" );
    }

    /**
     * Run the simulation
     */
    public void run()
    {
        // Run the simulation
        while( !_simState.isFinished() )
        {
            _simState.update();
        }

        // Report the final results
        _reporter.reportFinalResults();
    }


    /**
     * Main entry into the simulation
     *
     * @param args
     */
    public static void main( String[] args )
    {
        HierarchyBuildingSimulation sim = new HierarchyBuildingSimulation();
        sim.initialize();
        sim.run();
    }
}
