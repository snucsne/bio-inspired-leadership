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
package edu.snu.leader.hidden.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

// imports
import org.apache.log4j.Logger;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import edu.snu.leader.hidden.Neighbor;
import edu.snu.leader.hidden.SimulationState;
import edu.snu.leader.hidden.SpatialIndividual;
import edu.snu.leader.util.MiscUtils;


/**
 * TODO Class description
 *
 * @author Brent Eskridge
 */
public class PositionAnalyzer
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger(
            PositionAnalyzer.class.getName() );
    
    /** Key for simulation properties file */
    private static final String _PROPS_FILE_KEY = "sim-properties";

    
    /** The simulation state */
    private SimulationState _simState = new SimulationState();

    /** The properties used to initialize the system */
    private Properties _props = new Properties();

    /** Flag indicating that the mean resultant vector should be scaled by the
     *  number of nearest neighbors */
    private boolean _scaleMeanResultantVector = false;
    
    /**
     * Main entry into the analyzer
     *
     * @param args
     */
    public static void main( String[] args )
    {
        try
        {
            PositionAnalyzer analyzer = new PositionAnalyzer();
            analyzer.initialize();
            analyzer.run();
        }
        catch( Exception e )
        {
            _LOG.error( "Unknown error", e );
        }
    }

    /**
     * Initialize the analyzer
     */
    public void initialize()
    {
        _LOG.trace( "Entering initialize()" );

        // Load the properties
        _props = MiscUtils.loadProperties( _PROPS_FILE_KEY );

        // Initialize the simulation state
        _simState.initialize( _props );
        
        _LOG.trace( "Leaving initialize()" );
    }
    
    public void run()
    {
        // Process all the individuals
        for( SpatialIndividual ind : _simState.getAllIndividuals() )
        {
            
        }
    }

    private List<Vector2D> calculateRelativePositionsOfNeighbors( SpatialIndividual ind )
    {
        // Get the individual's location
        Vector2D indLocation = ind.getLocation();
        
        // Iterate over all the nearest neighbors
        List<Vector2D> relativePositions = new LinkedList<Vector2D>();
        for( Neighbor neighbor : ind.getNearestNeighbors() )
        {
            // Get the relative position of the neighbor
            Vector2D neighborLocation = neighbor.getIndividual().getLocation();
            Vector2D relativePosition = neighborLocation.subtract( indLocation );
            
            // Add it to the list
            relativePositions.add( relativePosition );
        }

        return relativePositions;
    }
    
    /**
     * Calculates the circle statistic for the given individual
     *
     * @param ind The individual
     * @return The circle statistic
     */
    private Vector2D calculateMeanResultantVector( SpatialIndividual ind )
    {
        // Get the individual's location
        Vector2D indLocation = ind.getLocation();
        
        // Iterate over all the nearest neighbors
        Vector2D meanResultantVector = Vector2D.ZERO;
        for( Vector2D relPosition : calculateRelativePositionsOfNeighbors( ind ) )
        {
            // Normalize it and add it to the statistic
            meanResultantVector = meanResultantVector.add(
                    relPosition.normalize() );
        }
        
        // Do we scale it?
        if( _scaleMeanResultantVector )
        {
            // Yup
            meanResultantVector = meanResultantVector.scalarMultiply(
                    1.0f / ind.getNearestNeighborCount() );
        }

        return meanResultantVector;
    }
}
