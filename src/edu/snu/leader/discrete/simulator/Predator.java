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
        
        _predationEvents = new ArrayList<PredationEvent>(_agents.size());
    }
    
    public void hunt(){
        double P = 0; //predation probability
        double m = _predationMultiplier; //predation modifier
        double n = 0; //group size
        double random = 0; //random number
        
        //randomize the agents for predation
        List<Agent> agents = Utils.shuffleAgents( _agents, _simState.getRandomGenerator() );
        
        for(int i = 0; i < agents.size(); i++){
            Agent temp = agents.get( i );
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
//                    System.out.println("P: " + P + " >? " + "R: " + random);//debug
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
    
//    public double getPredationConstant(){
//        return _predationMultiplier;
//    }
    
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
