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

package edu.snu.leader.discrete.behavior;

import java.util.Iterator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;


public class SimpleAngularMovement implements MovementBehavior
{
    private Agent _agent;

    @Override
    public void move()
    {
        boolean isInDestination = false;
        // if agents should stop at any destination
        if( _agent.getSimState().shouldStopAnywhere())
        {
            Iterator<Vector2D> destinations = _agent.getSimState().getDestinationsIterator();
            while(destinations.hasNext()){
                Vector2D temp = destinations.next();
                if( _agent.getCurrentLocation().distance( temp ) < _agent.getSimState().getDestinationRadius() )
                {
                    _agent.reachedDestination();
                    _agent.setReachedGoodDestination( _agent.getLeader().getPreferredDestination().isGood() );
                    isInDestination = true;
                }
            }
        }
        
        if( isInDestination ) 
        {
            // nothing to do here
        }
        // if agent is initiating and is within their destination then they have
        // reached their destination
        else if( !_agent.getPreferredDestination().getID().equals( "D-N" )
                && _agent.getCurrentLocation().distance1(
                _agent.getPreferredDestination().getVector() ) < _agent.getPreferredDestination().getRadius() )
        {
            _agent.reachedDestination();
        }
        // if they are moving back towards the start, make sure the stop before
        // going through it
        else if( _agent.getGroup().getId().equals(
                _agent.getSimState().noneGroup.getId() )
                && _agent.getCurrentLocation().distance(
                        _agent.getInitialLocation() ) < _agent.getSimState().startingDestination.getRadius() )
        {
            _agent.setCurrentVelocity( Vector2D.ZERO );
        }
        // otherwise move normally
        else
        {
            _agent.setCurrentLocation( _agent.getCurrentLocation().add(
                    _agent.getCurrentVelocity() ) );
        }
    }

    @Override
    public void initialize( Agent agent )
    {
        _agent = agent;
    }

}
