package edu.snu.leader.discrete.simulator;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import edu.snu.leader.discrete.behavior.MovementBehavior;
import edu.snu.leader.discrete.behavior.NoMove;
import edu.snu.leader.discrete.utils.Reporter;
import edu.snu.leader.discrete.utils.Utils;
import edu.snu.leader.util.MiscUtils;


/**
 * SueurDefaultAgentBuilder Default Agent Builder
 * 
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class SueurDefaultAgentBuilder implements AgentBuilder
{

    private Point2D[] _locations = null;

    private SimulationState _simState = null;

    private int _numAgents = 0;

    private String _locationsFile = null;

    @Override
    public void initialize( SimulationState simState )
    {
        _simState = simState;

        String numAgents = _simState.getProperties().getProperty(
                "individual-count" );
        Validate.notEmpty( numAgents, "Individual count may not be empty" );
        _numAgents = Integer.parseInt( numAgents );

        _locationsFile = _simState.getProperties().getProperty(
                "locations-file" );
        Validate.notEmpty( _locationsFile, "Locations file may not be empty" );

        _locations = Utils.readPoints( _locationsFile, _numAgents );

        // add the agent count info to root directory
        _simState.setRootDirectory( Reporter.ROOT_DIRECTORY + "agent-count="
                + _numAgents + "_" );
    }

    @Override
    public List<Agent> build()
    {
        List<Agent> agents = new ArrayList<Agent>(_numAgents);
        // build them
        DecisionProbabilityCalculator temp = (DecisionProbabilityCalculator) MiscUtils.loadAndInstantiate(
                _simState.getProperties().getProperty( "decision-calculator" ),
                "Decision Probability Calculator Class" );
        temp.initialize( _simState );
        for( int i = 0; i < _numAgents; i++ )
        {
            agents.add( new Agent( temp ) );
        }

        MovementBehavior mb = new NoMove();
        // initialize them
        for( int i = 0; i < _numAgents; i++ )
        {
            agents.get( i ).initialize( _simState, _locations[i] );
            agents.get( i ).setMovementBehavior( mb );
        }
        return agents;
    }

}
