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
package edu.snu.leader.discrete.evolution;


/**
 * EvolutionInputParameters Input parameters for the evolving simulations
 *
 * @author Tim Solum
 * @version $Revision$ ($Author$)
 */
public class EvolutionInputParameters
{
    /**
     * DestinationRunCounts
     *
     * TODO Class description
     *
     * @author Brent Eskridge
     * @version $Revision$ ($Author$)
     */
    public static class DestinationRunCounts
    {
        /** TODO */
        public String destinationFilename = null;

        /** TODO */
        public int runCount = 0;

        /**
         * Builds this DestinationRunCounts object
         *
         * @param destinationFilename
         * @param runCount
         */
        public DestinationRunCounts( String destinationFilename, int runCount )
        {
            this.destinationFilename = destinationFilename;
            this.runCount = runCount;
        }
    }

    /** TODO */
    private float _alpha = 0.0f;

    /** TODO */
    private float _beta = 0.0f;

    /** TODO */
    private int _S = 0;

    /** TODO */
    private float _q = 0;

    /** TODO */
    private float _alphaC = 0.0f;

    /** TODO */
    private float _betaC = 0.0f;

    /** TODO */
    public DestinationRunCounts[] _destinationRunCounts = null;

    /**
     * Builds this EvolutionInputParameters object
     *
     * @param alpha
     * @param beta
     * @param S
     * @param q
     * @param alphaC
     * @param betaC
     * @param destinationRunCounts
     */
    public EvolutionInputParameters( float alpha,
            float beta,
            int S,
            float q,
            float alphaC,
            float betaC,
            DestinationRunCounts[] destinationRunCounts )
    {
        this._alpha = alpha;
        this._beta = beta;
        this._S = S;
        this._q = q;
        this._alphaC = alphaC;
        this._betaC = betaC;
        this._destinationRunCounts = destinationRunCounts;
    }

    /**
     * TODO Method description
     *
     * @return
     */
    public float getAlpha()
    {
        return _alpha;
    }

    /**
     * TODO Method description
     *
     * @return
     */
    public float getBeta()
    {
        return _beta;
    }

    /**
     * TODO Method description
     *
     * @return
     */
    public int getS()
    {
        return _S;
    }

    /**
     * TODO Method description
     *
     * @return
     */
    public float getQ()
    {
        return _q;
    }

    /**
     * TODO Method description
     *
     * @return
     */
    public float getAlphaC()
    {
        return _alphaC;
    }

    /**
     * TODO Method description
     *
     * @return
     */
    public float getBetaC()
    {
        return _betaC;
    }

    /**
     * TODO Method description
     *
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(
                this.getClass().getCanonicalName() );
        builder.append( ": " );

        // Describe the attributes
        builder.append( "alpha=[" );
        builder.append( _alpha );
        builder.append( "] beta=[" );
        builder.append( _beta );
        builder.append( "] S=[" );
        builder.append( _S );
        builder.append( "] q=[" );
        builder.append( _q );
        builder.append( "] alphaC=[" );
        builder.append( _alphaC );
        builder.append( "] betaC=[" );
        builder.append( _betaC );
        builder.append( "]" );

        return builder.toString();
    }


}
