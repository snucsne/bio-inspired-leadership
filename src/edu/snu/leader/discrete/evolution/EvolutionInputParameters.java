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

public class EvolutionInputParameters{
    private float alpha = 0.0f;
    private float beta = 0.0f;
    private int S = 0;
    private float q = 0;
    private float alphaC = 0.0f;
    private float betaC = 0.0f;
    public DestinationRunCounts[] destinationRunCounts = null;
    
    public EvolutionInputParameters(float alpha, float beta, int S, float q, float alphaC, float betaC, DestinationRunCounts[] destinationRunCounts){
        this.alpha = alpha;
        this.beta = beta;
        this.S = S;
        this.q = q;
        this.alphaC = alphaC;
        this.betaC = betaC;
        this.destinationRunCounts = destinationRunCounts;
    }
    
    public float getAlpha(){
        return alpha;
    }

    public float getBeta(){
        return beta;
    }

    public int getS(){
        return S;
    }

    public float getQ(){
        return q;
    }

    public float getAlphaC(){
        return alphaC;
    }

    public float getBetaC(){
        return betaC;
    }
    
    public static class DestinationRunCounts{
        public String destinationFilename = null;
        public int runCount = 0;
        
        public DestinationRunCounts(String destinationFilename, int runCount){
            this.destinationFilename = destinationFilename;
            this.runCount = runCount;
        }
    }
}
