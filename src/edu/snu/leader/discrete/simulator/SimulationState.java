package edu.snu.leader.discrete.simulator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import ec.util.MersenneTwisterFast;
import edu.snu.leader.discrete.simulator.Agent.InitiationHistoryEvent;
import edu.snu.leader.discrete.utils.Reporter;


/**
 * The Simulation State for the simulator
 * 
 * @author Tim Solum
 */
public class SimulationState
{
    /** Used for the Eskridge reporter */
    private final String SPACER = "=========================================================";
    
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( SimulationState.class.getName() );

    /** Key for the the random number seed */
    private static final String _RANDOM_SEED_KEY = "random-seed";

    /** The simulation properties */
    private Properties _props = null;

    /** Random number generator */
    private MersenneTwisterFast _random = null;

    /** The number of simulation runs to perform */
    private int _simulationRunCount = 0;

    /** The current simulation run */
    private int _currentSimulationRun = 0;

    /** The max time steps per simulation */
    private long _maxSimulationTimeSteps = 0;

    /** The time of the current simulation */
    private int _simulationTime = 0;

    /** All the agents in the simulation */
    private List<Agent> _agents = new LinkedList<Agent>();

    /** All the groups in the simulation */
    private Set<Group> _groups = new HashSet<Group>();

    /** String for communication type */
    private String _communicationType = null;

    /** Reporter for reporting the results in a way that Dr. Eskridge's files can analyze */
    private Reporter _eskridgeResultsReporter = null;

    /**
     * Initialize the simulation state
     * 
     * @param props
     */
    public void initialize( Properties props )
    {
        _LOG.trace( "Entering initialize( props )" );

        // Save the properties
        _props = props;

        // Get the random number generator seed
        String randomSeedStr = props.getProperty( _RANDOM_SEED_KEY );
        Validate.notEmpty( randomSeedStr, "Random seed is required" );
        long seed = Long.parseLong( randomSeedStr );
        if(Simulator.getRandomSeedOverride() != -1){
            seed = Simulator.getRandomSeedOverride();
            _props.put( "random-seed", String.valueOf(seed) );
        }
        _random = new MersenneTwisterFast( seed );

        String simulationRunCount = _props.getProperty( "simulation-count" );
        Validate.notEmpty( simulationRunCount, "Simulation run count required" );
        _simulationRunCount = Integer.parseInt( simulationRunCount );

        String maxSimulationTimeSteps = _props.getProperty( "max-simulation-time-steps" );
        Validate.notEmpty( maxSimulationTimeSteps,
                "Max simulation time steps required" );
        _maxSimulationTimeSteps = Integer.parseInt( maxSimulationTimeSteps );

        _communicationType = getProperties().getProperty( "communication-type" );
        Validate.notEmpty( _communicationType,
                "Communication type may not be empty" );

        // Reporter.ROOT_DIRECTORY = getProperties().getProperty( "results-dir"
        // );
        // Validate.notEmpty( Reporter.ROOT_DIRECTORY, "results dir required" );

        // add communication type to root directory path
        setRootDirectory( "results_" + _communicationType + "_" );

        _groups.add( Group.NONE );
        
        _eskridgeResultsReporter = new Reporter( "short-spatial-hidden-var-" + String.format( "%05d", Main.run ) + "-seed-" + String.format("%05d", seed) + ".dat" , "", false);
        addPropertiesOutputToEskridgeResultsReporter();
        _LOG.trace( "Leaving initialize( props )" );
    }
    
    /**
     * Sets up the simulation for the next run
     */
    public void setupNextSimulationRun()
    {
        // report some stuff
        System.out.println( "Cleaning up sim run " + _currentSimulationRun );

        if( _currentSimulationRun < _simulationRunCount )
        {
            _simulationTime = 0;

            // reset the NONE group and clear the rest of the groups
            Group.NONE.reset();
            _groups.clear();
            _groups.add( Group.NONE );

            Iterator<Agent> agentIter = getAgentIterator();
            while( agentIter.hasNext() )
            {
                Agent temp = agentIter.next();
                // reset Agents
                temp.reset();
            }
            Agent.numInitiating = 0;
            Agent.numStopped = 0;

            // report the all run information and clear it for next run
            System.out.println( "Finished sim run " + _currentSimulationRun );
            System.out.println( "==========================================" );
            // increment current simulation run (in reporters too for directory
            // management)
            _currentSimulationRun++;
            Reporter.SIMULATION_RUN++;
            if( _currentSimulationRun == _simulationRunCount )
            {
                // report all of the group sizes before exiting
                System.out.println( Simulator.getSuccessCount() );
                
                //do stuff for the eskridge reporter
                addInitiationStatsToEskridgeResultsReporter();
                addMovementCountsToEskridgeResultsReporter();
                addFinalInitiatorCountsToEskridgeResultsReporter();
                addMaxInitiatorCountsToEskridgeResultsReporter();
                addIndividualInititationStatsToEskridgeResultsReporter();
                addIndividualDataToEskridgeResultsReporter();
                addAggregateDataToEskridgeResultsReporter();
                _eskridgeResultsReporter.report( true );
                System.out.println( "Done" );
            }
        }
    }
    
    /**
     * Sets up the simulation for the next run step
     */
    public void setupNextSimulationRunStep()
    {
        _simulationTime++;
    }

    /**
     * Returns the number of simulation runs
     * 
     * @return The number of simulation runs
     */
    public int getSimulationRunCount()
    {
        return _simulationRunCount;
    }

    /**
     * Returns the maximum number of simulation time steps per run
     * 
     * @return Max number of time steps
     */
    public long getMaxSimulationTimeSteps()
    {
        return _maxSimulationTimeSteps;
    }

    /**
     * Returns the current simulation run
     * 
     * @return The current simulation run
     */
    public int getCurrentSimulationRun()
    {
        return _currentSimulationRun;
    }

    /**
     * Returns the current simulation time
     * 
     * @return The time of the simulation
     */
    public int getSimulationTime()
    {
        return _simulationTime;
    }

    /**
     * Returns simulation properties
     * 
     * @return The properties for this simulation
     */
    public Properties getProperties()
    {
        return _props;
    }

    /**
     * Returns random generator
     * 
     * @return The random generator used
     */
    public MersenneTwisterFast getRandomGenerator()
    {
        return _random;
    }

    /**
     * Adds the specified agent to the simulation
     * 
     * @param agent The agent to add
     */
    public void addAgent( Agent agent )
    {
        _agents.add( agent );
    }

    public void addGroup( Group group )
    {
        _groups.add( group );
    }

    /**
     * Returns an iterator over all the simulated agents
     * 
     * @return An iterator over all the simulated agents
     */
    public Iterator<Agent> getAgentIterator()
    {
        return _agents.iterator();
    }

    public String getCommunicationType()
    {
        return _communicationType;
    }

    public void setRootDirectory( String root )
    {
        Reporter.ROOT_DIRECTORY = root;
    }

    public int getAgentCount()
    {
        return _agents.size();
    }
    
    
    private void addPropertiesOutputToEskridgeResultsReporter(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        _eskridgeResultsReporter.appendLine( "# Started: " + dateFormat.format( date ) );
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Simulation properties" );
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        
        Enumeration<Object> keys = _props.keys();
        String[] keysArray = new String[_props.size()];
        int count = 0;
        while(keys.hasMoreElements()){
            String currentKey = (String) keys.nextElement();
            keysArray[count] = currentKey;
            count++;
        }
        Arrays.sort( keysArray );
        for(int i = 0; i < keysArray.length; i++){
            _eskridgeResultsReporter.appendLine( "# " + keysArray[i] + " = " + _props.getProperty( keysArray[i] ));
        }
        
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine("");
    }
    
    private void addInitiationStatsToEskridgeResultsReporter(){
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Initiation stats" );
        _eskridgeResultsReporter.appendLine( "initiations = " + _simulationRunCount);
        _eskridgeResultsReporter.appendLine( "successes = " + Simulator.getSuccessCount() );
        _eskridgeResultsReporter.appendLine( "total-simulations = " + _simulationRunCount);
        _eskridgeResultsReporter.appendLine( "total-successful-simulations = " + Simulator.getSuccessCount());
        _eskridgeResultsReporter.appendLine( "total-leadership-success = " + (Simulator.getSuccessCount() / (float)_simulationRunCount));
        _eskridgeResultsReporter.appendLine("");
    }
    
    private void addMovementCountsToEskridgeResultsReporter(){
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Movement counts" );
        int[] groupSizeCounts = Simulator.getGroupSizeCounts();
        for(int i = 0; i < groupSizeCounts.length; i++){
            _eskridgeResultsReporter.appendLine( "move." + String.format( "%02d", i ) + " = " + groupSizeCounts[i] );
        }
        _eskridgeResultsReporter.appendLine("");
    }
    
    private void addFinalInitiatorCountsToEskridgeResultsReporter(){
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Final initiator counts" );
        
        _eskridgeResultsReporter.appendLine("final-initiators.00 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.01 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.02 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.03 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.04 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.05 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.06 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.07 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.08 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.09 = 0");
        _eskridgeResultsReporter.appendLine("final-initiators.10 = 0");
        
        _eskridgeResultsReporter.appendLine("");
    }
    
    private void addMaxInitiatorCountsToEskridgeResultsReporter(){
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Max initiator counts" );
        
        _eskridgeResultsReporter.appendLine("max-initiators.00 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.01 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.02 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.03 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.04 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.05 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.06 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.07 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.08 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.09 = 0");
        _eskridgeResultsReporter.appendLine("max-initiators.10 = 0");
        
        _eskridgeResultsReporter.appendLine("");
    }
    
    private void addIndividualInititationStatsToEskridgeResultsReporter(){
        String indInitStatPreceeding = "initiation.Ind";
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Individual initiation stats" );
        Iterator<Agent> iter = _agents.iterator();
        int i = 0;
        while(iter.hasNext()){
            Agent temp = iter.next();
            _eskridgeResultsReporter.appendLine( indInitStatPreceeding + String.format("%05d", i) + ".attempts = " + temp.getNumberTimesInitiated() );
            _eskridgeResultsReporter.appendLine( indInitStatPreceeding + String.format("%05d", i) + ".successes = " + temp.getNumberTimesSuccessful() );
            _eskridgeResultsReporter.appendLine( indInitStatPreceeding + String.format("%05d", i) + ".attempt-percentage = " + (temp.getNumberTimesInitiated() / (float)_simulationRunCount) );
            _eskridgeResultsReporter.appendLine( indInitStatPreceeding + String.format("%05d", i) + ".success-percentage = " + (temp.getNumberTimesSuccessful() / (float)_simulationRunCount) );
            _eskridgeResultsReporter.appendLine("");
            _eskridgeResultsReporter.appendLine("");
            i++;
        }
        _eskridgeResultsReporter.appendLine("");
    }
    
    private void addIndividualDataToEskridgeResultsReporter(){
        String indDataPreceeding = "individual.";
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Individual data" );
        Iterator<Agent> iter = _agents.iterator();
        int i = 0;
        while(iter.hasNext()){
            Agent temp = iter.next();
            List<Agent> neighbors = temp.getNearestNeighbors();
            StringBuilder neighborBuilder = new StringBuilder();
            Iterator<Agent> neighborIter = neighbors.iterator();
            while(neighborIter.hasNext()){
                String agentName = (String) neighborIter.next().getId();
                agentName =  agentName.replaceAll( "Agent", "");
                neighborBuilder.append( "Ind" + String.format( "%05d", Integer.parseInt( agentName )) + " ");
            }
            
            StringBuilder initiationHistoryBuilder = new StringBuilder();
            Iterator<InitiationHistoryEvent> initHistory = temp.getInitiationHistory().iterator();
            while(initHistory.hasNext()){
                InitiationHistoryEvent event = initHistory.next();
                initiationHistoryBuilder.append( "[" + event.simRun + " "
                        + event.beforePersonality + " "
                        + event.wasSuccess + " "
                        + event.afterPersonality + " "
                        + event.participants + "] ");
            }
            
            String agentId = "Ind" + String.format("%05d", i);
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".location = " + String.format("%1.4f", temp.getCurrentLocation().getX()) + " " + String.format("%1.4f", temp.getCurrentLocation().getY()));
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".personality = " + temp.getPersonalityTrait().getPersonality() );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".assertiveness = " + temp.getPersonalityTrait().getPersonality() );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".preferred-direction = 0.0" );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".conflict = 0.0" );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".nearest-neighbors = " + neighborBuilder.toString() );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".eigenvector-centrality = %%%" + agentId + "-EIGENVECTOR-CENTRALITY%%%" );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".betweenness = %%%" + agentId + "-BETWEENNESS%%%");
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".sna-todo = %%%" + agentId + "-sna-todo%%%" );
            _eskridgeResultsReporter.appendLine( indDataPreceeding + agentId + ".initiation-history = " + initiationHistoryBuilder.toString() );
            _eskridgeResultsReporter.appendLine("");
            _eskridgeResultsReporter.appendLine("");
            i++;
        }
        
        _eskridgeResultsReporter.appendLine("");
    }

    private void addAggregateDataToEskridgeResultsReporter(){
        _eskridgeResultsReporter.appendLine( "# " + SPACER);
        _eskridgeResultsReporter.appendLine( "# Aggregate data" );

        double averageX = 0.0;
        double averageY = 0.0;
        
        Iterator<Agent> iter = _agents.iterator();
        int agentCount = 0;
        while(iter.hasNext()){
            Agent temp = iter.next();
            averageX += temp.getCurrentLocation().getX();
            averageY += temp.getCurrentLocation().getY();
            
            agentCount++;
        }
        
        averageX /= agentCount;
        averageY /= agentCount;
        
        _eskridgeResultsReporter.appendLine("modularity = 0");
        _eskridgeResultsReporter.appendLine("mean.position = " + String.format("%1.4f", averageX) + " " + String.format("%1.4f", averageY));
        
        _eskridgeResultsReporter.appendLine("");
    }
    
}
