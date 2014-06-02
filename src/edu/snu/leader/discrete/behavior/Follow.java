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

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.discrete.simulator.Agent;


public class Follow extends Decision
{

    public Follow( Agent agent, Agent leader )
    {
        super( DecisionType.FOLLOW, agent, leader );
    }

    @Override
    public void choose()
    {
        // set leader to the new leader
        _agent.setLeader( _leader );
        // set group to the leader's group
        _leader.getGroup().addAgent( _agent, _agent.getTime() );
        // set destination to the leader's destination
        _agent.setCurrentDestination( _leader.getCurrentLocation() );
        // set velocity to move towards leader's current position
        if( !_agent.getCurrentDestination().subtract(
                _agent.getCurrentLocation() ).equals( Vector2D.ZERO ) )
        {
            _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( 
                    _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
                    _agent.getSpeed() ) );
        }
    }
}
