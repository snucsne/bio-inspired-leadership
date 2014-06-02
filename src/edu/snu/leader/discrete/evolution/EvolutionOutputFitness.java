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

public class EvolutionOutputFitness{
    private double percentTime = 0.0;
    private double percentSurvive = 0.0;
    private double percentSuccess = 0.0;
    
    public EvolutionOutputFitness(double percentTime, double percentSurvive, double percentSuccess){
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
