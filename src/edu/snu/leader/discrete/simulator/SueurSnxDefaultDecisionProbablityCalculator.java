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

import java.util.List;

import org.apache.commons.lang.Validate;

import edu.snu.leader.discrete.behavior.Decision;
import edu.snu.leader.discrete.utils.Reporter;


/**
 * SueurSnxDefaultDecisionProbablityCalculator This calculator is the same as
 * the default sueur calculator except that it uses S = N-X for S instead of a
 * set value for S.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class SueurSnxDefaultDecisionProbablityCalculator implements
        DecisionProbabilityCalculator
{

    /** The simulation state */
    private SimulationState _simState = null;

    /** The intrinsic probability to initiate */
    private double _alpha = 0;

    /** The intrinsic probability to cancel */
    private double _alphaC = 0;

    /** Mimetic coefficient */
    private double _beta = 0;

    /** Inverse mimetic coefficient */
    private double _betaC = 0;

    /** Agents sensitivity to the system */
    private double _q = 0;

    /** A threshold */
    private int _S = 0;

    @Override
    public void initialize( SimulationState simState )
    {
        _simState = simState;

        String alpha = _simState.getProperties().getProperty( "alpha" );
        Validate.notEmpty( alpha, "Alpha may not be empty" );
        _alpha = Double.parseDouble( alpha );

        String alphaC = _simState.getProperties().getProperty( "alpha-c" );
        Validate.notEmpty( alphaC, "Alpha-c may not be empty" );
        _alphaC = Double.parseDouble( alphaC );

        String beta = _simState.getProperties().getProperty( "beta" );
        Validate.notEmpty( beta, "Beta may not be empty" );
        _beta = Double.parseDouble( beta );

        String betaC = _simState.getProperties().getProperty( "beta-c" );
        Validate.notEmpty( betaC, "Beta-c may not be empty" );
        _betaC = Double.parseDouble( betaC );

        String q = _simState.getProperties().getProperty( "q" );
        Validate.notEmpty( q, "q may not be empty" );
        _q = Double.parseDouble( q );

        // String S = _simState.getProperties().getProperty("S");
        // Validate.notEmpty(S, "S may not be empty");
        // _S = Integer.parseInt(S);

        String cancellationThreshold = _simState.getProperties().getProperty(
                "cancellation-threshold" );
        Validate.notEmpty( cancellationThreshold,
                "Use cancellation threshold may not be empty" );

        // add sueur info to root directory path
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY + "q=" + _q + "_"
                + "S=N-x" );
    }

    @Override
    public void calcInitiateProb( Decision decision )
    {
        decision.setProbability( _alpha );
    }

    @Override
    public void calcFollowProb( Decision decision )
    {
        Agent agent = decision.getAgent();
        Group group = decision.getLeader().getGroup();
        // probability to join this group
        double lambda = 0.0;

        // the number of agents currently in this group
        int X = 0;

        // calculate observed X value
        List<Agent> neighbors = agent.getNearestNeighbors();
        for( int i = 0; i < neighbors.size(); i++ )
        {
            if( agent.getObservedGroupHistory().get( neighbors.get( i ).getId() ) == group.getId() )
            {
                X++;
            }
        }

        // calculate S as S = N - X where N is total number of agents around
        _S = neighbors.size() - X;

        // calculate lambda
        lambda = _alpha
                + ( ( _beta * Math.pow( X, _q ) ) / ( Math.pow( _S, _q ) + Math.pow(
                        X, _q ) ) );

        decision.setProbability( lambda );
    }

    @Override
    public void calcCancelProb( Decision decision )
    {
        Agent agent = decision.getAgent();
        // probability to cancel
        double psiC = 0.0;

        // the number of agents currently in this group
        int X = 1;

        // calculate observed X value
        List<Agent> neighbors = agent.getNearestNeighbors();
        for( int i = 0; i < neighbors.size(); i++ )
        {
            if( agent.getObservedGroupHistory().get( neighbors.get( i ).getId() ) == agent.getGroup().getId() )
            {
                X++;
            }
        }

        // calculate S as S = N - X where N is total number of agents around
        _S = neighbors.size() - X;

        // calculate psiC
        if( ( (double) X / neighbors.size() ) >= ( agent.getCancelThreshold() ) )
        {
            // if threshold is reached then will not cancel
            psiC = 0;
        }
        else
        {
            psiC = _alphaC
                    + ( ( _betaC * Math.pow( X, _q ) ) / ( Math.pow( _S, _q ) + Math.pow(
                            X, _q ) ) );
        }

        decision.setProbability( psiC );
    }

    @Override
    public double[] getPreCalculatedFollowProbabilities()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double[] getPreCalculatedCancelProbabilities()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
