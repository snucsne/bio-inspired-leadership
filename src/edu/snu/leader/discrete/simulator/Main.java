package edu.snu.leader.discrete.simulator;

import java.util.Arrays;

import edu.snu.leader.discrete.behavior.Decision;
import me.solhub.simple.engine.DebugLocationsStructure;


// import org.apache.commons.lang.Validate;

public class Main
{
    /** The current run of the simulator */
    public static int run = 0;

    /** How many runs there will be */
    public static final int totalRuns = 50;

    public static void main( String[] args )
    {
        // System.out.println( "sim-properites file used: " + args[0] );
        // Validate.notEmpty( args[0],
        // "Must specify a sim-properties file to use at runtime" );
        System.setProperty( "sim-properties",
                "cfg/sim/discrete/sim-properties.parameters" );
//        Simulator simulator = new Simulator( 1 );
//        simulator.initialize();
//        simulator.execute();
        
        // System.setProperty( "sim-properties", args[0] );
        // run just text
        for( int i = 1; i <= totalRuns; i++ )
        {
            System.out.println( "Run " + i );
            System.out.println();
            run = i;
            Simulator simulator = new Simulator( run );
            simulator.initialize();
            simulator.execute();
        }

        // run graphical
        // DebugLocationsStructure db = new DebugLocationsStructure(
        // "Presentation Demo",
        // 800, 600, 30 );
        // db.run();
    }
}
