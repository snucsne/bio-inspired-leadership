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

package edu.snu.leader.discrete.evolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Scanner;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import edu.snu.leader.discrete.evolution.EvolutionInputParameters.DestinationRunCounts;
import edu.snu.leader.discrete.simulator.Simulator;
import edu.snu.leader.util.MiscUtils;


public class SimulatorEvolution
{
    /** The number of simulations for each run of the simulator */
    public final static int SIMULATION_COUNT = 1;

    // Test of the methods provided here
    public static void main( String[] args )
    {
        DestinationRunCounts drc = new DestinationRunCounts(
                "cfg/sim/destinations/destinations-split-10-dis-150.0-ang-72.00-per-0.500-seed-1.dat",
                1 );
        DestinationRunCounts[] drcs = { drc };
        EvolutionInputParameters input = new EvolutionInputParameters( 0.006f,
                0.01f, 2, 2.3f, 0.009f, -0.009f, drcs );
        EvolutionOutputFitness output = null;

        output = runEvolutionFromInputParameters( input, null );
        System.out.println( "Time Towards: " + output.getPercentTime() );
        System.out.println( "Survivals: " + output.getPercentSurvive() );
        System.out.println( "Success: " + output.getPercentSuccess() );

        inputParametersToJson( input, "test.json" );

        // should be the same as above outputs
        output = runEvolutionFromJson( "test.json", null );
        System.out.println( "Time Towards: " + output.getPercentTime() );
        System.out.println( "Survivals: " + output.getPercentSurvive() );
        System.out.println( "Success: " + output.getPercentSuccess() );
    }

    /**
     * Used for running evolutionary computation on the Sueur model simulator
     * given an EvolutionInputParameters object
     *
     * @param p The input parameters for this evolution
     * @param propertiesFilename If null,
     *            "cfg/sim/discrete/sim-properties.parameters" will be used.
     * @return
     */
    public static EvolutionOutputFitness runEvolutionFromInputParameters(
            EvolutionInputParameters p,
            String propertiesFilename )
    {
        // make sure properties filename is not null
        if( propertiesFilename == null )
        {
            propertiesFilename = "cfg/sim/discrete/sim-properties.parameters";
        }
        // setup Properties object
        System.setProperty( "sim-properties", propertiesFilename );
        Properties _simulationProperties = MiscUtils.loadProperties( "sim-properties" );

        // set Properties with InputParameters' values
        _simulationProperties.setProperty( "simulation-count",
                String.valueOf( SIMULATION_COUNT ) );
        _simulationProperties.setProperty( "alpha",
                String.valueOf( p.getAlpha() ) );
        _simulationProperties.setProperty( "alpha-c",
                String.valueOf( p.getAlphaC() ) );
        _simulationProperties.setProperty( "beta", String.valueOf( p.getBeta() ) );
        _simulationProperties.setProperty( "beta-c",
                String.valueOf( p.getBetaC() ) );
        _simulationProperties.setProperty( "S", String.valueOf( p.getS() ) );
        _simulationProperties.setProperty( "q", String.valueOf( p.getQ() ) );

        int totalRuns = 0;// Total simulation runs
        float totalTime = 0f;// Total percents of the time fitness
        float totalSurvive = 0f;// Total percents of the survival fitness
        float totalSuccess = 0f;// Total percents of the success fitness

        // loop through each environment to test
        for( int i = 0; i < p._destinationRunCounts.length; i++ )
        {
            // set environment property
            _simulationProperties.setProperty( "destinations-file",
                    p._destinationRunCounts[i].destinationFilename );
            // this value should be set to 1
            _simulationProperties.setProperty( "current-run",
                    String.valueOf( 1 ) );
            // run simulations for an environment as many times as specified
            for( int j = 0; j < p._destinationRunCounts[i].runCount; j++ )
            {
                // create simulator and execute it
                Simulator simulator = new Simulator( 1 );
                simulator.initialize( _simulationProperties );
                simulator.execute();

                // add this simulator's fitness to the total fitness counts
                EvolutionOutputFitness temp = simulator.getSimulationOutputFitness();
                totalTime += temp.getPercentTime();
                totalSurvive += temp.getPercentSurvive();
                totalSuccess += temp.getPercentSuccess();
                totalRuns++;// increment run count
            }
        }

        // return the mean percentages of all the runs as the final
        // EvolutionOutputFitness
        return new EvolutionOutputFitness( totalTime / totalRuns, totalSurvive
                / totalRuns, totalSuccess / totalRuns );
    }

    /**
     * Used for running evolutionary computation on the Sueur model simulator
     * from a json file
     *
     * @param jsonFilename The json filename
     * @param propertiesFilename If null,
     *            "cfg/sim/discrete/sim-properties.parameters" will be used.
     * @return
     */
    public static EvolutionOutputFitness runEvolutionFromJson(
            String jsonFilename,
            String propertiesFilename )
    {
        // open the json file
        StringBuilder b = new StringBuilder();
        Scanner sc = null;
        try
        {
            sc = new Scanner( new File( jsonFilename ) );
        }
        catch( FileNotFoundException e1 )
        {
            e1.printStackTrace();
        }

        // read the lines from the json file
        while( sc.hasNextLine() )
        {
            b.append( sc.nextLine() );
        }
        sc.close();

        // Create a JsonReader to get an Object from json file
        Object jsonObj = null;
        try
        {
            jsonObj = JsonReader.jsonToJava( b.toString() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        // Our parameters object that was stored in json
        EvolutionInputParameters p = (EvolutionInputParameters) jsonObj;

        // run evolutionFromInputParameters with newly loaded object
        return runEvolutionFromInputParameters( p, propertiesFilename );
    }

    /**
     * TODO Method description
     *
     * @param P
     * @param filename
     */
    public static void inputParametersToJson( EvolutionInputParameters P,
            String filename )
    {
        // open a json writer
        String json = null;
        try
        {
            json = JsonWriter.objectToJson( P );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }

        // setup a print writer
        PrintWriter out = null;
        try
        {
            out = new PrintWriter( new BufferedWriter( new FileWriter(
                    filename, false ) ) );
        }
        catch( FileNotFoundException e )
        {
            throw new RuntimeException( "Could not open " + filename
                    + " output file." );
        }
        catch( IOException e )
        {
            throw new RuntimeException( "Could not write to " + filename
                    + " output file." );
        }

        // write the json string to the file
        out.print( json );
        out.close();
    }
}
