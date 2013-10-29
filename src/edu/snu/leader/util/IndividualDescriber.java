/*
 * COPYRIGHT
 */
package edu.snu.leader.util;

// Imports
import ec.Individual;

/**
 * IndividualDescriber
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public interface IndividualDescriber
{
    /** System newline */
    public static final String NEWLINE = System.getProperty( "line.separator" );

    /**
     * Returns a description of the specified individual using the specified
     * line prefix.
     *
     * @param ind The individual to describe
     * @param prefix The prefix for every line in the description
     * @param statDir The statistics directory
     * @return A description of the individual
     */
    public String describe( Individual ind,
            String prefix,
            String statDir );

}
