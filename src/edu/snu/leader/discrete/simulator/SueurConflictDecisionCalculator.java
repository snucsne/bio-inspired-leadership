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
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.behavior.Decision;
import edu.snu.leader.discrete.utils.Reporter;


/**
 * SueurDefaultDecisionProbablityCalculator Default Sueur Probability Calculator
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class SueurConflictDecisionCalculator implements
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

    private double _defaultConflictValue = .1;
    
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

        String S = _simState.getProperties().getProperty( "S" );
        Validate.notEmpty( S, "S may not be empty" );
        _S = Integer.parseInt( S );

        String cancellationThreshold = _simState.getProperties().getProperty(
                "cancellation-threshold" );
        Validate.notEmpty( cancellationThreshold,
                "Use cancellation threshold may not be empty" );

        String defConflictValue = _simState.getProperties().getProperty( "default-conflict-value" );
        Validate.notEmpty( defConflictValue, "default-conflict-value may not be empty" );
        _defaultConflictValue = Double.parseDouble( defConflictValue );
        
        // add sueur info to root directory path
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY + "SueurValues");

    }

    @Override
    public void calcInitiateProb( Decision decision )
    {
        double conflict = _defaultConflictValue;
        conflict = calculateConflict( decision );
        double k = 1 / kValue(conflict);
        decision.setProbability( _alpha / k );
    }

    @Override
    public void calcFollowProb( Decision decision )
    {
        Agent agent = decision.getAgent();
        Group group = decision.getLeader().getGroup();
        // probability to join this group
        double lambda = 0.0;
        double conflict = calculateConflict( decision );

        // the number of agents currently in this group
        int X = 0;

        // calculate observed X value
        List<Agent> neighbors = agent.getNearestNeighbors();
        for( int i = 0; i < neighbors.size(); i++ )
        {
            if( agent.getObservedGroupHistory().get( neighbors.get( i ).getId() ).groupId == group.getId() )
            {
                X++;
            }
        }

        // calculate lambda
        lambda = _alpha
                + ( ( _beta * Math.pow( X, _q ) ) / ( Math.pow( _S, _q ) + Math.pow(
                        X, _q ) ) );
        
        double k = 1/ kValue( 1 - conflict );
        lambda *= 1 / k;
        
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
            if( agent.getObservedGroupHistory().get( neighbors.get( i ).getId() ).groupId == agent.getGroup().getId() )
            {
                X++;
            }
        }

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
    
    /**
     * Calculate the k value for conflict
     * 
     * @param decision The decision that the k value is calculated for
     * @return The k value
     */
    private double kValue( double conflict )
    {
        double k = 2 * conflict;
        return k;
    }
    
  //TODO make sure this is working well :D
    private double calculateConflict(Decision decision){
        Agent agent = decision.getAgent();
        Agent leader = decision.getLeader();
        double Ci = 0.1;

        //calculate the leader's next location
        Vector2D leaderNextLocation = leader.getCurrentDestination().add( leader.getCurrentVelocity() );
        //calculate the sides of a triangle
        //calculate side from agent's preferred destination to leader's next
        double A = Vector2D.distance( agent.getPreferredDestination().getVector(), leaderNextLocation );
        //calculate side from agent's preferred destination to leader's current
        double B = Vector2D.distance( agent.getPreferredDestination().getVector(), leader.getCurrentLocation() );
        //calculate side from leader's current to leader's next
        double C = Vector2D.distance( leader.getCurrentLocation(), leaderNextLocation );
        
        //check if the leader is in the agent's preferred destination
        if(leader.getCurrentLocation().distance1(
                agent.getPreferredDestination().getVector() ) < SimulationState.getDestinationRadius()){
            Ci = 0.1;
        }
        //check if the leader is not moving
        else if(leader.getCurrentVelocity().equals( Vector2D.ZERO )){
            Ci = .9;
        }
        else{
            double angle = 0.0;
            
            if(A <= 0 || B <= 0 || C <= 0){
                //if a side is 0 then there is no triangle it is a line
                //if segment B is longer than C then the degree should be 180
                if(B > C){
                    angle = 180;
                }
                //if the segment B is shorter than C then the degree should be 0
                else{
                    angle = 0.0;
                }
            }
            //have three sides so use law of cosines
            else{
                //calculate angle between leader's current position and agent's preferred destination by law of cosines
                double lawOfCosines = (Math.pow( A, 2 ) - Math.pow( B, 2 ) - Math.pow( C, 2 ) ) / (-2 * B * C);
                //because of rounding error there can be lawOfCosines values that are oh so slightly larger or smaller than 1 or -1
                //this augments them to their correct values
                if(lawOfCosines < -1){
                    lawOfCosines = -1;
                }
                else if(lawOfCosines > 1){
                    lawOfCosines = 1;
                }
                angle = Math.acos( lawOfCosines );
            }
            
            //if angle is greater than 180 than it becomes 360 - angle
            if(angle > 180){
                angle = 360 - angle;
            }
            //make it into degrees
            angle = angle * 180 / Math.PI;
            //calculate conflict
            Ci = angle / 180;
        }
        
        //prevent K value from becoming 0
        if(Ci < .1){
            Ci = .1;
        }
        else if (Ci > .9){
            Ci = .9;
        }
        
        //set the conflict for the decision
        decision.setConflict( Ci );
        //return the conflict value for whatever needs to use it
        return Ci;
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
