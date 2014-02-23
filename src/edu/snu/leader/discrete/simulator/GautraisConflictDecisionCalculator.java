package edu.snu.leader.discrete.simulator;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.behavior.Decision;
import edu.snu.leader.discrete.utils.Reporter;


public class GautraisConflictDecisionCalculator implements
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
    
    /** The conflict value for agents that have zero velocity */
    private double _defaultConflictValue = .1;
    
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
        
        String defConflictValue = _simState.getProperties().getProperty( "default-conflict-value" );
        Validate.notEmpty( defConflictValue, "default-conflict-value may not be empty" );
        _defaultConflictValue = Double.parseDouble( defConflictValue );
        
        // add gautrais info to root directory path
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY + "GautraisValues" );
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
        double tauI = _tauO;
        double conflict = _defaultConflictValue;
        //calculate conflict
        conflict = calculateConflict( decision );
        //calculate k value
        double k = kValue(conflict);
        //calculate tauI
        tauI /= k;
        //set probability
        decision.setProbability( 1 / tauI );
    }

    @Override
    public void calcFollowProb( Decision decision )
    {
        double conflict = calculateConflict( decision );
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
        //1-conflict for follow
        tauR *= 1 / kValue( 1 - conflict );
        decision.setProbability( 1 / tauR );
    }

    @Override
    public void calcCancelProb( Decision decision )
    {
        //conflict is not used for cancellation rates
//        double conflict = calculateConflict( decision );
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

        //1-conflict for cancel
//        Cr *= kValue( 1 - conflict );//conflict does not affect canceling rates
        decision.setProbability( Cr );

    }
    
    /**
     * Calculate the k value for conflict
     * 
     * @param decision The decision that the k value is calculated for
     * @return The k value
     */
    private double kValue( double conflict )
    {
        // k = 2 * conflict
        double k = 2 * conflict ;
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
        double A = Vector2D.distance( agent.getPreferredDestination(), leaderNextLocation );
        //calculate side from agent's preferred destination to leader's current
        double B = Vector2D.distance( agent.getPreferredDestination(), leader.getCurrentLocation() );
        //calculate side from leader's current to leader's next
        double C = Vector2D.distance( leader.getCurrentLocation(), leaderNextLocation );
        
        //check if the leader is in the agent's preferred destination
        if(leader.getCurrentLocation().distance1(
                agent.getPreferredDestination() ) < SimulationState.getDestinationRadius()){
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
}
