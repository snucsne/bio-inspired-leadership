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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

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
    
    private List<PredationEvent> _predationEvents = null;
    
    private boolean _usePredationThreshold = false;
    
    private float _predationThreshold = 0.0f;
    
    private float _predationProbabilityMinimum = 0.0f;
    
    private boolean _usePredationByPopulation = false;
    
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
        
        String stringUsePredationThreshold = _simState.getProperties().getProperty("use-predation-threshold" );
        Validate.notEmpty( stringUsePredationThreshold, "use predation threshold required" );
        _usePredationThreshold = Boolean.parseBoolean( stringUsePredationThreshold );
        
        String stringPredationThreshold = _simState.getProperties().getProperty("predation-threshold" );
        Validate.notEmpty( stringPredationThreshold, "predation threshold required" );
        _predationThreshold = Float.parseFloat( stringPredationThreshold );
        
        String stringPredationByPopulation = _simState.getProperties().getProperty("predation-by-population" );
        Validate.notEmpty( stringPredationByPopulation, "predation by population required" );
        _usePredationByPopulation = Boolean.parseBoolean( stringPredationByPopulation );
        
        String stringPredationProbabilityMinimum = _simState.getProperties().getProperty("predation-probability-minimum" );
        Validate.notEmpty( stringPredationProbabilityMinimum, "predation probability minumum required" );
        _predationProbabilityMinimum = Float.parseFloat( stringPredationProbabilityMinimum );
        
        _agents = new ArrayList<Agent>(_simState.getAgentCount());
        Iterator<Agent> iter = _simState.getAgentIterator();
        while(iter.hasNext()){
            _agents.add(iter.next());
        }
        
        _predationEvents = new ArrayList<PredationEvent>(_agents.size());
    }
    
    public void hunt(){
        double P = 0; //predation probability
        double m = _predationMultiplier; //predation modifier
        if( _usePredationByPopulation ){
            m /= 100;
        }
        double n = 0; //group size
        double random = 0; //random number
        int agentCount = _simState.getAgentCount();
        
        //randomize the agents for predation
        List<Agent> agents = Utils.shuffleAgents( _agents, _simState.getRandomGenerator() );
        
        for(int i = 0; i < agents.size(); i++){
            Agent temp = agents.get( i );
            //if its alive and hasn't reached their destination
            if(temp.isAlive() && !temp.hasReachedDestination()){
                n = temp.getGroup().getSize();
                //predation by population modification
                if( _usePredationByPopulation ){
                    n = n / agentCount;
                }
                
                //probability calculation
                P = m / Math.pow( n, 2 );
                
                //predation probability minimum
                if(P < _predationProbabilityMinimum ){
                    P = _predationProbabilityMinimum;
                }
                
                //predation threshold
                if( _usePredationThreshold ){
                    if( n > agentCount * _predationThreshold ){
                        P = _predationProbabilityMinimum;
                    }
                }
            
                //if the agent is in the start zone it is safe
                if( temp.getCurrentLocation().distance1(
                        Destination.startingDestination.getVector() ) < Destination.startingDestination.getRadius() )
                {
                    //agent is in a safe place
                }
                else{
                    random = Utils.getRandomNumber( _simState.getRandomGenerator(), 0, 1 );
                    //if we have fewer eaten this time step than allowed, eat it
                    if( P > random && _agentsEatenThisTimeStep < _maxAgentsEatenPerStep ){
                        PredationEvent predEvent = new PredationEvent(_simState.getCurrentSimulationRun(), temp.getTime(), _id,
                                temp.getId(), temp.getGroup().getSize(), temp.getGroup().getId(),
                                temp.getCurrentLocation(), temp.getPreferredDestinationId(), temp.getLeader().getPreferredDestinationId());
                        _predationEvents.add( predEvent );
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
    
    public Iterator<PredationEvent> predationEventIterator(){
        return _predationEvents.iterator();
    }
    
    public class PredationEvent{
        public int run = 0;
        public int timeStep = 0;
        public String predatorId = null;
        public Object agentId = null;
        public int groupSize = 0;
        public Object groupId = null;
        public Vector2D location = null;
        public String destinationId = null;
        public String leaderDestinationId = null;
        
        public PredationEvent(int run, int timeStep, String predatorId, Object agentId, int groupSize, Object groupId, Vector2D location, String destinationId, String leaderDestinationId){
            this.run = run;
            this.timeStep = timeStep;
            this.predatorId = predatorId;
            this.agentId = agentId;
            this.groupSize = groupSize;
            this.groupId = groupId;
            this.location = location;
            this.destinationId = destinationId;
            this.leaderDestinationId = leaderDestinationId;
        }
    }
}
