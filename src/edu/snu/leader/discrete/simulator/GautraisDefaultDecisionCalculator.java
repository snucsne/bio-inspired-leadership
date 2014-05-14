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

import java.util.List;

import org.apache.commons.lang.Validate;

import edu.snu.leader.discrete.behavior.Decision;
import edu.snu.leader.discrete.utils.Reporter;


public class GautraisDefaultDecisionCalculator implements
        DecisionProbabilityCalculator
{

    /** The simulation state */
    private SimulationState _simState = null;

    private double _tauO = 0;

    private double _alphaC = 0;

    private double _gammaC = 0;

    private double _epsilonC = 0;

    private double _alphaF = 0;

    private double _betaF = 0;
    
    private boolean _preCalcProbs = false;
    
    private double[] _followProbabilities = null;
    private double[] _cancelProbabilities = null;

    @Override
    public void initialize( SimulationState simState )
    {
        _simState = simState;

        String tauO = _simState.getProperties().getProperty( "tau-o" );
        Validate.notEmpty( tauO, "tau-o may not be empty" );
        _tauO = Double.parseDouble( tauO );

        String alphaF = _simState.getProperties().getProperty( "alpha-f" );
        Validate.notEmpty( alphaF, "alpha-f may not be empty" );
        _alphaF = Double.parseDouble( alphaF );

        String gammaC = _simState.getProperties().getProperty( "gamma-c" );
        Validate.notEmpty( gammaC, "gamma-c may not be empty" );
        _gammaC = Double.parseDouble( gammaC );

        String epsilonC = _simState.getProperties().getProperty( "epsilon-c" );
        Validate.notEmpty( epsilonC, "epsilon-c may not be empty" );
        _epsilonC = Double.parseDouble( epsilonC );

        String alphaC = _simState.getProperties().getProperty( "alpha-c" );
        Validate.notEmpty( alphaC, "alpha-c may not be empty" );
        _alphaC = Double.parseDouble( alphaC );

        String betaF = _simState.getProperties().getProperty( "beta-f" );
        Validate.notEmpty( betaF, "beta-f may not be empty" );
        _betaF = Double.parseDouble( betaF );
        
        String preCalcProbs = _simState.getProperties().getProperty( "pre-calculate-probabilities" );
        Validate.notEmpty( preCalcProbs, "pre-calculate-probabilities may not be empty" );
        _preCalcProbs = Boolean.parseBoolean( preCalcProbs );
        
        String stringAgentCount = _simState.getProperties().getProperty( "individual-count" );
        Validate.notEmpty( stringAgentCount, "individual-count may not be empty" );
        int agentCount = Integer.parseInt( stringAgentCount );

        // add gautrais info to root directory path
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY + "GautraisValues" );
        
        if(_preCalcProbs){
            _followProbabilities = new double[agentCount];
            _cancelProbabilities = new double[agentCount ];
            
            for(int r = 1; r < agentCount; r++){
                _followProbabilities[r] = 1 / (_alphaF + ( ( _betaF * ( agentCount - r ) ) / r ));
                _cancelProbabilities[r] = _alphaC / ( 1 + ( Math.pow( r / _gammaC, _epsilonC ) ) );
            }
        }

    }
    
    /**
     * Returns an array of all the possible follow probabilities. Will be null if pre-generation was not specified in properties file. 
     *
     * @return
     */
    public double[] getPreCalculatedFollowProbabilities(){
        return _followProbabilities;
    }
    
    /**
     * Returns an array of all the possible cancel probabilities. Will be null if pre-generation was not specified in properties file. 
     *
     * @return
     */
    public double[] getPreCalculatedCancelProbabilities(){
        return _cancelProbabilities;
    }

    @Override
    public void calcInitiateProb( Decision decision )
    {
        //tauO is the base initiation rate (should be multiplied by agent count but makes the simulations go a lot slower
        double tauI = _tauO;// * _simState.getAgentCount();
        decision.setProbability( 1 / tauI );
    }

    @Override
    public void calcFollowProb( Decision decision )
    {
        double tauR = 0;
        // total number neighbors
        int N = 0;
        // followers
        int r = 0;

        Agent agent = decision.getAgent();

        // calculate r followers and N total
        List<Agent> neighbors = agent.getNearestNeighbors();
        N = neighbors.size();
        for( int i = 0; i < neighbors.size(); i++ )
        {
            // N++;
            if( agent.getObservedGroupHistory().get( neighbors.get( i ).getId() ).groupId == ( decision.getLeader().getGroup().getId() ) )
            {
                r++;
            }
        }

        // calculate tauR
        tauR = _alphaF + ( ( _betaF * ( N - r ) ) / r );
        decision.setProbability( 1 / tauR );
    }

    @Override
    public void calcCancelProb( Decision decision )
    {
        double Cr = 0;
        // followers
        int r = 1;

        Agent agent = decision.getAgent();

        // calculate r followers
        List<Agent> neighbors = agent.getNearestNeighbors();
        for( int i = 0; i < neighbors.size(); i++ )
        {
            if( agent.getObservedGroupHistory().get( neighbors.get( i ).getId() ).groupId.equals( agent.getGroup().getId() ) )
            {
                r++;
            }
        }

        // calculate Cr
        Cr = _alphaC / ( 1 + ( Math.pow( r / _gammaC, _epsilonC ) ) );
        decision.setProbability( Cr );

    }
}
