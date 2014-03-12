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
package edu.snu.leader.spatial.builder;

// Imports
import edu.snu.leader.spatial.Agent;
import edu.snu.leader.spatial.AgentCommunicationType;
import edu.snu.leader.spatial.AgentSpatialState;
import edu.snu.leader.spatial.ConflictTrait;
import edu.snu.leader.spatial.DecisionProbabilityCalculator;
import edu.snu.leader.spatial.Group;
import edu.snu.leader.spatial.MovementBehavior;
import edu.snu.leader.spatial.PersonalityTrait;
import edu.snu.leader.spatial.SimulationState;
import edu.snu.leader.spatial.trait.StandardUpdatePersonalityTrait;
import edu.snu.leader.spatial.trait.VoidConflictTrait;
import edu.snu.leader.util.MiscUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.log4j.Logger;
import java.util.Properties;


/**
 * BasicAgentBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class PersonalityAgentBuilder extends AbstractAgentBuilder
{

    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PersonalityAgentBuilder.class.getName() );

    /** Key for the initial personality value */
    private static final String _INITIAL_PERSONALITY_KEY = "initial-personality";

    /** Key for the communication type */
    private static final String _COMMUNICATION_TYPE_KEY = "communication-type";

    /** Key for the cancel threshold */
    private static final String _CANCEL_THRESHOLD_KEY = "cancel-threshold";

    /** Key for the initiation movement behavior class name */
    private static final String _INITIATE_MOVEMENT_BEHAVIOR_CLASS =
            "initiate-movement-behavior-class";

    /** Key for the following movement behavior class name */
    private static final String _FOLLOW_MOVEMENT_BEHAVIOR_CLASS =
            "follow-movement-behavior-class";

    /** Key for the cancel movement behavior class name */
    private static final String _CANCEL_MOVEMENT_BEHAVIOR_CLASS =
            "cancel-movement-behavior-class";

    /** Key for the decision probability calculator class */
    private static final String _DECISION_PROBABILITY_CALC_CLASS =
            "decision-probability-calculator-class";


    /** The initial personality value */
    private float _initialPersonality = 0.0f;

    /** The agent's default group */
    private Group _defaultGroup = Group.NONE;

    /** The type of communication between agents */
    private AgentCommunicationType _communicationType = null;

    /** Movement behavior for initiation decisions */
    private MovementBehavior _initiateMovementBehavior = null;

    /** Movement behavior for following decisions */
    private MovementBehavior _followMovementBehavior = null;

    /** Movement behavior for canceling decisions */
    private MovementBehavior _cancelMovementBehavior = null;

    /** Decision probability calculator */
    private DecisionProbabilityCalculator _calculator = null;

    /** The cancel threshold for agents */
    private float _cancelThreshold = 0.0f;


    /**
     * Initializes the builder
     *
     * @param simState The simulation's state
     * @see edu.snu.leader.spatial.builder.AbstractAgentBuilder#initialize(edu.snu.leader.spatial.SimulationState)
     */
    @Override
    public void initialize( SimulationState simState )
    {
        _LOG.trace( "Entering initialize( simState )" );

        // Call the superclass implementation
        super.initialize( simState );

        // Get the properties
        Properties props = simState.getProperties();

        // Get the initial personality value
        String initialPersonalityStr = props.getProperty( _INITIAL_PERSONALITY_KEY );
        Validate.notEmpty( initialPersonalityStr,
                "Initial personality (key=["
                        + _INITIAL_PERSONALITY_KEY
                        + "]) may not be empty" );
        _initialPersonality = Float.parseFloat( initialPersonalityStr );
        _LOG.info( "Using _initialPersonality=["
                + _initialPersonality
                + "]" );

        // Get the communication type
        String commTypeStr = props.getProperty( _COMMUNICATION_TYPE_KEY );
        Validate.notEmpty( commTypeStr,
                "Communication type (key=["
                        + _COMMUNICATION_TYPE_KEY
                        + "]) may not be empty" );
        _communicationType = AgentCommunicationType.valueOf(
                commTypeStr.toUpperCase() );
        _LOG.info( "Using _communicationType=["
                + _communicationType
                + "]" );

        // Load and instantiate the initiation movement behavior
        String inititateMovementBehaviorClassName = props.getProperty(
                _INITIATE_MOVEMENT_BEHAVIOR_CLASS );
        Validate.notEmpty( inititateMovementBehaviorClassName,
                "Initiate movement behavior class (key=["
                        + _INITIATE_MOVEMENT_BEHAVIOR_CLASS
                        + "]) may not be empty" );
        _initiateMovementBehavior = (MovementBehavior) MiscUtils.loadAndInstantiate(
                inititateMovementBehaviorClassName,
                "Initiation movement behavior" );
        _LOG.info( "Using inititateMovementBehaviorClassName=["
                + inititateMovementBehaviorClassName
                + "]" );

        // Load and instantiate the follow movement behavior
        String followMovementBehaviorClassName = props.getProperty(
                _FOLLOW_MOVEMENT_BEHAVIOR_CLASS );
        Validate.notEmpty( followMovementBehaviorClassName,
                "Follow movement behavior class (key=["
                        + _FOLLOW_MOVEMENT_BEHAVIOR_CLASS
                        + "]) may not be empty" );
        _followMovementBehavior = (MovementBehavior) MiscUtils.loadAndInstantiate(
                followMovementBehaviorClassName,
                "Follow movement behavior" );
        _LOG.info( "Using followMovementBehaviorClassName=["
                + followMovementBehaviorClassName
                + "]" );

        // Load and instantiate the cancel movement behavior
        String cancelMovementBehaviorClassName = props.getProperty(
                _CANCEL_MOVEMENT_BEHAVIOR_CLASS );
        Validate.notEmpty( cancelMovementBehaviorClassName,
                "Cancel movement behavior class (key=["
                        + _CANCEL_MOVEMENT_BEHAVIOR_CLASS
                        + "]) may not be empty" );
        _cancelMovementBehavior = (MovementBehavior) MiscUtils.loadAndInstantiate(
                cancelMovementBehaviorClassName,
                "Cancel movement behavior" );
        _LOG.info( "Using cancelMovementBehaviorClassName=["
                + cancelMovementBehaviorClassName
                + "]" );

        // Load and instantiate the decision probability calculator
        String decisionProbCalcClassName = props.getProperty(
                _DECISION_PROBABILITY_CALC_CLASS );
        Validate.notEmpty( decisionProbCalcClassName,
                "Decision probability class (key=["
                        + _DECISION_PROBABILITY_CALC_CLASS
                        + "]) may not be empty" );
        _calculator = (DecisionProbabilityCalculator) MiscUtils.loadAndInstantiate(
                decisionProbCalcClassName,
                "Decision probability calculator" );
        _LOG.info( "Using decisionProbCalcClassName=["
                + decisionProbCalcClassName
                + "]" );
        _calculator.initialize( simState );

        // Get the cancel threshold
        String cancelThresholdStr = props.getProperty( _CANCEL_THRESHOLD_KEY );
        Validate.notEmpty( cancelThresholdStr,
                "Cancel threshold (key=["
                        + _CANCEL_THRESHOLD_KEY
                        + "]) may not be empty" );
        _cancelThreshold = Float.parseFloat( cancelThresholdStr );
        _LOG.info( "Using _cancelThreshold=["
                + _cancelThreshold
                + "]" );

        _LOG.trace( "Leaving initialize( simState )" );
    }

    /**
     * Builds an individual
     *
     * @param index The index of the individual to build
     * @return The agent
     * @see edu.snu.leader.spatial.AgentBuilder#build(int)
     */
    @Override
    public Agent build( int index )
    {
        _LOG.trace( "Entering build( index )" );

        // Build the personality trait
        PersonalityTrait personalityTrait =
                new StandardUpdatePersonalityTrait( _initialPersonality );

        // Build the conflict trait
        ConflictTrait conflictTrait = new VoidConflictTrait();

        // Build the agent's spatial state
        Vector2D initialLocation = _locations.get( index );
        AgentSpatialState spatialState = new AgentSpatialState( initialLocation,
                Vector2D.ZERO );

        // Build the agent
        Agent agent = new Agent( generateUniqueIndividualID( index ),
                _defaultGroup,
                personalityTrait,
                conflictTrait,
                spatialState,
                _communicationType,
                _initiateMovementBehavior.copy(),
                _followMovementBehavior.copy(),
                _cancelMovementBehavior.copy(),
                _calculator,
                _cancelThreshold );

        _LOG.trace( "Leaving build( index )" );

        return agent;
    }

}
