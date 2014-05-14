package edu.snu.leader.discrete.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.snu.leader.discrete.utils.Utils;

public class Predator
{
    private String _id = null;
    
    private int _totalAgentsEaten = 0;
    
    private int _agentsEatenThisTimeStep = 0;
    
    private int _maxAgentsEatenPerStep = 0;
    
    private double _predationMultiplier = 0;
    
    private SimulationState _simState = null;
    
    private List<Agent> _agents = null;
    
    public Predator(String id){
        _id = id;
    }
    
    public void initialize(SimulationState simState){
        _simState = simState;
        
        String stringMaxAgentsEatenPerStep = _simState.getProperties().getProperty("max-agents-eaten-per-step" );
        Validate.notEmpty( stringMaxAgentsEatenPerStep, "max agents eater per step may not be empty" );
        _maxAgentsEatenPerStep = Integer.parseInt( stringMaxAgentsEatenPerStep );
        
        String stringPredationMultiplier = _simState.getProperties().getProperty("predation-multiplier" );
        Validate.notEmpty( stringPredationMultiplier, "predation multiplier required" );
        _predationMultiplier = Double.parseDouble( stringPredationMultiplier );
        
        _agents = new ArrayList<Agent>(_simState.getAgentCount());
        Iterator<Agent> iter = _simState.getAgentIterator();
        while(iter.hasNext()){
            _agents.add(iter.next());
        }
    }
    
    public void hunt(){
        double P = 0; //predation probability
        double m = _predationMultiplier; //predation modifier
        double n = 0; //group size
        double random = 0; //random number
        
        //randomize the agents for predation
        Collections.shuffle( _agents );
        
        for(int i = 0; i < _agents.size(); i++){
            Agent temp = _agents.get( i );
            //if its alive
            if(temp.isAlive() && !temp.hasReachedDestination()){
                n = temp.getGroup().getSize();
                P = m / Math.pow( n, 2 );
            
                //if the agent is in the start zone it is safe
                if( temp.getCurrentLocation().distance1(
                        Destination.startingDestination.getVector() ) < Destination.startingDestination.getRadius() )
                {
                    //agent is in a safe place
                }
                else{
                    random = Utils.getRandomNumber( _simState.getRandomGenerator(), 0, 1 );
                    System.out.println("P: " + P + " >? " + "R: " + random);//debug
                    //if we have fewer eaten this time step than allowed, eat it
                    if( P > random && _agentsEatenThisTimeStep < _maxAgentsEatenPerStep ){
                        temp.kill();
                        _totalAgentsEaten++;
                        _agentsEatenThisTimeStep++;
                    }
                }
            }
        }
    }
    
    public void setupNextTimeStep(){
        _agentsEatenThisTimeStep = 0;
    }
    
    public void setupNextRun(){
        _agentsEatenThisTimeStep = 0;
        _totalAgentsEaten = 0;
    }
    
    public int getTotalAgentsEaten(){
        return _totalAgentsEaten;
    }
    
    public String getId(){
        return _id;
    }
}
