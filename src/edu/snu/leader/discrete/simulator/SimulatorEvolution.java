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

import edu.snu.leader.discrete.evolution.EvolutionOutputFitness;
import edu.snu.leader.util.MiscUtils;

public class SimulatorEvolution
{
    public final static int SIMULATION_COUNT = 1;

    public static void main(String[] args){
        InputParameters input = new InputParameters(0.006, 0.01, 2, 2.3, 0.009, -0.009);
        EvolutionOutputFitness output = null;

        output = runEvolutionFromInputParameters( input, null );
        System.out.println("Time Towards: " + output.getPercentTime());
        System.out.println("Survivals: " + output.getPercentSurvive());
        System.out.println("Success: " + output.getPercentSuccess());

        inputParametersToJson( input, "test.json" );

        output = runEvolutionFromJson( "test.json", null);
        System.out.println("Time Towards: " + output.getPercentTime());
        System.out.println("Survivals: " + output.getPercentSurvive());
        System.out.println("Success: " + output.getPercentSuccess());
    }

    /**
     * Used for running evolutionary computation on the Sueur model simulator
     *
     * @param p The input parameters for this evolution
     * @param prpertiesFilename If null, "cfg/sim/discrete/sim-properties.parameters" will be used.
     * @return
     */
    public static EvolutionOutputFitness runEvolutionFromInputParameters(InputParameters p, String propertiesFilename){
        if(propertiesFilename == null){
            propertiesFilename = "cfg/sim/discrete/sim-properties.parameters";
        }
        System.setProperty( "sim-properties", propertiesFilename );
        Properties _simulationProperties = MiscUtils.loadProperties("sim-properties");

        _simulationProperties.setProperty( "simulation-count", String.valueOf(SIMULATION_COUNT) );

        _simulationProperties.setProperty( "alpha", String.valueOf(p.getAlpha()) );
        _simulationProperties.setProperty( "alpha-c", String.valueOf(p.getAlphaC()) );
        _simulationProperties.setProperty( "beta", String.valueOf(p.getBeta()) );
        _simulationProperties.setProperty( "beta-c", String.valueOf(p.getBetaC()) );
        _simulationProperties.setProperty( "S", String.valueOf(p.getS()) );
        _simulationProperties.setProperty( "q", String.valueOf(p.getQ()) );

        Simulator simulator = new Simulator( 1 );
        _simulationProperties.setProperty( "current-run", String.valueOf(1) );
        simulator.initialize(_simulationProperties);
        simulator.execute();

        return simulator.getSimulationOutputFitness();
    }

    public static EvolutionOutputFitness runEvolutionFromJson(String jsonFilename, String propertiesFilename){
        if(propertiesFilename == null){
            propertiesFilename = "cfg/sim/discrete/sim-properties.parameters";
        }
        System.setProperty( "sim-properties", propertiesFilename );
        Properties _simulationProperties = MiscUtils.loadProperties("sim-properties");

        StringBuilder b = new StringBuilder();
        Scanner sc = null;
        try{
            sc = new Scanner(new File(jsonFilename));
        }
        catch( FileNotFoundException e1 ){
            e1.printStackTrace();
        }

        while(sc.hasNextLine()){
            b.append( sc.nextLine() );
        }
        sc.close();

        Object jsonObj = null;
        try{
            System.out.println(b.toString());
            jsonObj = JsonReader.jsonToJava(b.toString());
        }
        catch( IOException e ){
            e.printStackTrace();
        }
        InputParameters p = (InputParameters) jsonObj;

        _simulationProperties.setProperty( "simulation-count", String.valueOf(SIMULATION_COUNT) );

        _simulationProperties.setProperty( "alpha", String.valueOf(p.getAlpha()) );
        _simulationProperties.setProperty( "alpha-c", String.valueOf(p.getAlphaC()) );
        _simulationProperties.setProperty( "beta", String.valueOf(p.getBeta()) );
        _simulationProperties.setProperty( "beta-c", String.valueOf(p.getBetaC()) );
        _simulationProperties.setProperty( "S", String.valueOf(p.getS()) );
        _simulationProperties.setProperty( "q", String.valueOf(p.getQ()) );

        Simulator simulator = new Simulator( 1 );
        _simulationProperties.setProperty( "current-run", String.valueOf(1) );
        simulator.initialize(_simulationProperties);
        simulator.execute();

        return simulator.getSimulationOutputFitness();
    }

    public static void inputParametersToJson(InputParameters P, String filename){
        String json = null;
        try{
            json = JsonWriter.objectToJson(P);
        }
        catch( IOException e ){
            e.printStackTrace();
        }

        PrintWriter out = null;
        try{
            out = new PrintWriter( new BufferedWriter( new FileWriter( filename, false ) ) );
        }
        catch( FileNotFoundException e ){
            throw new RuntimeException( "Could not open " + filename + " output file." );
        }
        catch( IOException e ){
            throw new RuntimeException( "Could not write to " + filename + " output file." );
        }

        out.print( json );
        out.close();
    }



    public static class InputParameters{
        private double alpha = 0.0;
        private double beta = 0.0;
        private int S = 0;
        private double q = 0;
        private double alphaC = 0.0;
        private double betaC = 0.0;

        public InputParameters(double alpha, double beta, int S, double q, double alphaC, double betaC){
            this.alpha = alpha;
            this.beta = beta;
            this.S = S;
            this.q = q;
            this.alphaC = alphaC;
            this.betaC = betaC;
        }

        public double getAlpha(){
            return alpha;
        }

        public double getBeta(){
            return beta;
        }

        public int getS(){
            return S;
        }

        public double getQ(){
            return q;
        }

        public double getAlphaC(){
            return alphaC;
        }

        public double getBetaC(){
            return betaC;
        }
    }

    public static class OutputFitness{
        private double percentTime = 0.0;
        private double percentSurvive = 0.0;
        private double percentSuccess = 0.0;

        OutputFitness(double percentTime, double percentSurvive, double percentSuccess){
            this.percentTime = percentTime;
            this.percentSurvive = percentSurvive;
            this.percentSuccess = percentSuccess;
        }

        public double getPercentTime(){
            return percentTime;
        }

        public double getPercentSurvive(){
            return percentSurvive;
        }

        public double getPercentSuccess(){
            return percentSuccess;
        }
    }
}
