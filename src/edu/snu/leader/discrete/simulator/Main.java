/*
 * The Bio-inspired Leadership Toolkit is a set of tools used to simulate the
 * emergence of leaders in multi-agent systems. Copyright (C) 2014 Southern
 * Nazarene University This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or at your option) any later version. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package edu.snu.leader.discrete.simulator;

import java.util.Properties;

import org.apache.commons.lang.Validate;

import edu.snu.leader.util.MiscUtils;

import me.solhub.simple.engine.DebugLocationsStructure;


/**
 * Main Main entry point for starting the simulator. Can choose between
 * graphical or textual in the properties file
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class Main
{
    static boolean shouldRunGraphical = false;

    /** How many runs there will be */
    public static int totalRuns = 50;

    private static Properties _simulationProperties = null;

    public static void main( String[] args )
    {
        System.setProperty( "sim-properties",
                "cfg/sim/discrete/sim-properties.parameters" );

        _simulationProperties = MiscUtils.loadProperties( "sim-properties" );

        String stringShouldRunGraphical = _simulationProperties.getProperty( "run-graphical" );
        Validate.notEmpty( stringShouldRunGraphical,
                "Run graphical option required" );
        shouldRunGraphical = Boolean.parseBoolean( stringShouldRunGraphical );

        String stringTotalRuns = _simulationProperties.getProperty( "run-count" );
        Validate.notEmpty( stringTotalRuns, "Run count required" );
        totalRuns = Integer.parseInt( stringTotalRuns );

        if( !shouldRunGraphical )
        {
            // run just text
            for( int run = 1; run <= totalRuns; run++ )
            {
                System.out.println( "Run " + run );
                System.out.println();

                // create and initialize simulator
                Simulator simulator = new Simulator( run );
                _simulationProperties.put( "current-run", String.valueOf( run ) );
                simulator.initialize( _simulationProperties );
                // run it
                simulator.execute();
            }
        }
        else
        {
            // run graphical
            DebugLocationsStructure db = new DebugLocationsStructure(
                    "Conflict Simulation", 800, 600, 60 );
            _simulationProperties.put( "current-run", String.valueOf( 1 ) );
            db.initialize( _simulationProperties, 1 );
            db.run();
        }
    }
}
