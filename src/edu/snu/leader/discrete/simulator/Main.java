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

package edu.snu.leader.discrete.simulator;

import me.solhub.simple.engine.DebugLocationsStructure;


// import org.apache.commons.lang.Validate;

public class Main
{
    static boolean debug = false;
    static boolean shouldRunGraphical = false;
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
        if(debug){
            debug();
        }
        else{
            if(!shouldRunGraphical){
             // run just text
              for( run = 1; run <= totalRuns; run++ )
              {
                  System.out.println( "Run " + run );
                  System.out.println();
                  Simulator simulator = new Simulator( run );
                  simulator.initialize();
                  simulator.execute();
              }
            }
            // System.setProperty( "sim-properties", args[0] );
            else{
            // run graphical
                 DebugLocationsStructure db = new DebugLocationsStructure(
                 "Conflict Simulation",
                 800, 600, 30 );
                 db.run();
            }
        }
    }
    
    private static void debug(){
        int agentCount = 10;
        double tauO = 1290;
        double gammaC = 2.0;
        double epsilonC = 2.3;
        double alphaF = 162.3;
        double betaF = 75.4;
        double alphaC = 0.009;
        
        int r;
        double tauI = tauO * agentCount;
        System.out.println("Initiation");
        System.out.println("Prob: " + 1/tauI + "   Rate: " + tauI);
        
        System.out.println("Following");
        for(r = 1; r < agentCount; r++){
            double tauR = alphaF + ( ( betaF * ( agentCount - r ) ) / r );
            System.out.println("Prob: " + (1 / tauR) + "   Rate: " + tauR);
        }
        
        System.out.println("Cancelling");
        for(r = 1; r < agentCount; r++){
            double Cr = alphaC / ( 1 + ( Math.pow( r / gammaC, epsilonC ) ) );
            System.out.println("Prob: " + Cr + "    Rate: " + (1/Cr));
        }
        
    }
}
