/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
