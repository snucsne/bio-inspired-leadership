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


/**
 * Cancel The cancel decision. Agents will move towards the starting point and
 * their group will be dissolved. The agent is added to the none group.
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class Cancel extends Decision
{

    public Cancel( Agent agent )
    {
        super( DecisionType.CANCELLATION, agent, agent );
    }

    @Override
    public void choose()
    {
        // dissolve group
        _agent.getGroup().dissolve();
        // set leader to self
        _agent.setLeader( _agent );
        // set destination to starting location
        _agent.setCurrentDestination( _agent.getInitialLocation() );
        // move towards the initial location
        if( !_agent.getCurrentDestination().subtract(
                _agent.getCurrentLocation() ).equals( Vector2D.ZERO ) )
        {
            _agent.setCurrentVelocity( ( _agent.getCurrentDestination().subtract( 
                    _agent.getCurrentLocation() ) ).normalize().scalarMultiply(
                    _agent.getSpeed() ) );
        }
        // add group to the none group
        _agent.getSimState().noneGroup.addAgent( _agent, _agent.getTime() );
    }
}
