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

package edu.snu.leader.discrete.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredationResultsAnalyzer
{
    public static void main( String[] args )
    {
        StringBuilder rfile = new StringBuilder();
        StringBuilder xrange = new StringBuilder();
        StringBuilder yrange = new StringBuilder();
        
        xrange.append( "constants <- c(" );
        yrange.append( "meanEaten <- c(" );
        
        File folder = new File("results_global_agent-count=10_SueurValues");
        File[] listOfFiles = folder.listFiles();
        
        Map<Double, PredationContainer> predationEvents = new HashMap<Double, PredationContainer>();
        
        for(int i = 0; i < listOfFiles.length; i++){
            if(listOfFiles[i].getName().contains( "pred_const" )){
                String runRegex = "run-count=([0-9]+)";
                String constantRegex = "predation-constant=([0-9].[0-9]+E-[0-9]+|[0-9]+.[0-9]+)";
                String meanEatenRegex = "([0-9]{3})[\\s]*([0-9]{6})[\\s]*(P-D)[\\s]*(Ind[0-9]{5})[\\s]*([0-9]{3})[\\s]*(Group[0-9]{1,3})";
                Scanner scanner = null;
                Pattern pattern = null;
                Matcher matcher = null;
                StringBuilder b = new StringBuilder();
                PredationContainer temp = new PredationContainer();
                try{
                    scanner = new Scanner( new File(listOfFiles[i].getAbsolutePath()));
                }
                catch( FileNotFoundException e ){
                    e.printStackTrace();
                }
                //get data from file
                while(scanner.hasNextLine()){
                    b.append( scanner.nextLine() + "\n");
                }
                
                String rawData = b.toString();
                
                System.out.println(listOfFiles[i].getName());
                
                //run regex
                pattern = Pattern.compile( runRegex );
                matcher = pattern.matcher( rawData );
                if(matcher.find()){
                    int runCount = Integer.parseInt(matcher.group( 1 ));
                    temp.totalRuns = runCount;
                    System.out.println(runCount);//debug
                }
                
                //constant regex
                pattern = Pattern.compile( constantRegex );
                matcher = pattern.matcher( rawData );
                if(matcher.find()){
                    double predationConstant = Double.parseDouble(matcher.group( 1 ));
                    temp.predationConstant = predationConstant;
                    System.out.println(predationConstant);//debug
                }
                
                //mean eaten regex
                pattern = Pattern.compile( meanEatenRegex );
                matcher = pattern.matcher( rawData );
                while(matcher.find()){
                    int run = Integer.parseInt( matcher.group(1) );//run information
                    int n = Integer.parseInt( matcher.group(5) );//group size information
                    temp.totalEaten += 1;
                    System.out.println("Run: " + run + "  n: " + n);//debug
                }
                System.out.println("Mean eaten: " + temp.getMeanEaten() + "\n");//debug
                
                if(predationEvents.containsKey( temp.predationConstant )){
                    predationEvents.get( temp.predationConstant ).totalEaten += temp.totalEaten;
                    predationEvents.get( temp.predationConstant ).totalRuns += temp.totalRuns;
                }
                else{
                    predationEvents.put( temp.predationConstant, temp );
                }
            }
        }
        
        List<Double> constants = new ArrayList<Double>();
        constants.addAll( predationEvents.keySet() );
        Collections.sort( constants );
        for(int i = 0; i < constants.size(); i++){
            
            xrange.append( constants.get( i ) + ", " );
            yrange.append( predationEvents.get( constants.get( i ) ).getMeanEaten() + ", " );
        }
        
        xrange.deleteCharAt( xrange.length() - 1 );
        xrange.deleteCharAt( xrange.length() - 1 );
        xrange.append( ")\n" );
        
        yrange.deleteCharAt( yrange.length() - 1 );
        yrange.deleteCharAt( yrange.length() - 1 );
        yrange.append( ")\n" );
        
        rfile.append( xrange.toString() );
        rfile.append( yrange.toString() );
        rfile.append( "plot(meanEaten, type=\"o\", col=\"blue\", axes=FALSE, ann=FALSE)\n" );
        rfile.append( "axis(1, at=1:" + constants.size() + ", constants, las=2)\n" );
        rfile.append( "axis(2, las=1.0, at=0:10)\n" );
        rfile.append( "title(main=\"Predation Constants\", col.main=\"red\", fon.main=4)\n" );
        rfile.append( "title(xlab=\"Predation Constant\", col.lab=rgb(0,0.5,0))\n" );
        rfile.append( "title(ylab=\"Mean Number Eaten\", col.lab=rgb(0,0.5,0))\n" );
        System.out.println(rfile.toString());
    }
    
    public static class PredationContainer{
        public double predationConstant = 0;
        public double totalEaten = 0;
        public int totalRuns = 0;
        
        public PredationContainer(){
            predationConstant = 0;
            totalEaten = 0;
            totalRuns = 0;
        }
        
        public double getMeanEaten(){
            return totalEaten / totalRuns;
        }
    }
}
