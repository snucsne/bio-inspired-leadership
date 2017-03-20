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
package edu.snu.leader.hidden.event;

//Imports
import java.util.Properties;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;


/**
 * RestrictedInitiatorConflictEventTimeCalculator
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 */
public class RestrictedInitiatorConflictEventTimeCalculator
        extends AssertivenessAndDirectionEventTimeCalculator
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            RestrictedInitiatorConflictEventTimeCalculator.class.getName() );
    
    /** Key for the initiating individual's ID */
    public static final String INITIATOR_ID_KEY = "initiator-id";

    /** Key for the flag to restrict initiating */
    protected static final String _RESTRICT_INITIATING_KEY = "restrict-initiating";

    /** Key for the flag to restrict canceling */
    protected static final String _RESTRICT_CANCELING_KEY = "restrict-canceling";

    /** The ID of the initiating individual */
    protected Object _initiatorID = null;

    /** Flag to restrict initiating */
    protected boolean _restrictInitiating = false;

    /** Flag to restrict canceling */
    protected boolean _restrictCanceling = false;

    /**
     * Initializes the calculator
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.hidden.event.AbstractEventTimeCalculator#initialize(edu.snu.leader.hidden.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProps();

        // Get the ID of the initiating individual
        String initiatorIDStr = props.getProperty( INITIATOR_ID_KEY );
        Validate.notEmpty( initiatorIDStr,
                "Initiating ID (key="
                + INITIATOR_ID_KEY
                + ") may not be empty" );
        _initiatorID = initiatorIDStr;
        _LOG.info( "Using _initiatorID=[" + _initiatorID + "]" );

        // Get the flag to restrict initiating
        String restrictInitiatingStr = props.getProperty(
                _RESTRICT_INITIATING_KEY );
        Validate.notEmpty( restrictInitiatingStr,
                "Flag to restrict initiating (key="
                + _RESTRICT_INITIATING_KEY
                + ") may not be empty" );
        _restrictInitiating = Boolean.parseBoolean(
                restrictInitiatingStr );
        _LOG.info( "Using _restrictInitiating=["
                + _restrictInitiating
                + "]" );

        // Get the flag to restrict canceling
        String restrictCancelingStr = props.getProperty(
                _RESTRICT_CANCELING_KEY );
        Validate.notEmpty( restrictCancelingStr,
                "Flag to restrict canceling (key="
                + _RESTRICT_CANCELING_KEY
                + ") may not be empty" );
        _restrictCanceling = Boolean.parseBoolean(
                restrictCancelingStr );
        _LOG.info( "Using _restrictCanceling=["
                + _restrictCanceling
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Calculates the time at the specified individual will initiate movement
     *
     * @param ind The individual
     * @return The initiation time
     * @see edu.snu.leader.hidden.event.DefaultEventTimeCalculator#calculateInitiationTime(edu.snu.leader.hidden.SpatialIndividual)
     */
    @Override
    public float calculateInitiationTime( SpatialIndividual ind )
    {
        // Default to no initiation
        float initiationTime = Float.POSITIVE_INFINITY;

        // Do we restrict initiating?
        if( _restrictInitiating )
        {
            // Is this the initiator?
            if( ind.getID().equals( _initiatorID ) )
            {
                // Yup, initiate now
                initiationTime = 0.0f;
            }
        }
        else
        {
            // Nope, use the default
            initiationTime = super.calculateInitiationTime( ind );
        }

        return initiationTime;
    }

    /**
    * Calculates the time at the specified individual will cancel an initiation
    *
    * @param ind The individual
    * @param departed The number of individuals who have already departed
    * @return The cancellation time
     * @see edu.snu.leader.hidden.event.DefaultEventTimeCalculator#calculateCancelTime(edu.snu.leader.hidden.SpatialIndividual, int)
     */
    @Override
    public float calculateCancelTime( SpatialIndividual ind, int departed )
    {
        // Default to restricting since it involves no calculations
        float cancelTime = Float.POSITIVE_INFINITY;

        // Do we restrict canceling?
        if( !_restrictCanceling )
        {
            // Nope, get the real canceling time
            cancelTime = super.calculateCancelTime( ind, departed );
        }

        return cancelTime;
    }

}
