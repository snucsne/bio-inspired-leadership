/*
 * COPYRIGHT
 */
package edu.snu.leader.util;

// Imports
import ec.util.MersenneTwisterFast;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * MiscUtils
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class MiscUtils
{
    /** Our logger */
    private static final Logger _LOG = Logger.getLogger( MiscUtils.class.getName() );


    /**
     * Generates a random gaussian in the interval [0,1]
     *
     * @param random
     * @return The random value
     */
    public static double getUnitConstrainedGaussian( MersenneTwisterFast random )
    {
        double value = ( random.nextGaussian() / 8.0 ) + 0.5;
        value = Math.min( 0.0, Math.max( 1.0, value) );

        return value;
    }

    /**
     * Generates a random gaussian that is guaranteed to lie in the specified
     * interval
     *
     * @param random
     * @param min
     * @param max
     * @return The random value
     */
    public static double getConstrainedGaussian( MersenneTwisterFast random,
            double min,
            double max )
    {
        double value = getUnitConstrainedGaussian( random );
        double range = max - min;
        value = ( value * range ) + min;

        return value;
    }

    /**
     * Decodes an integer from an array of bits (booleans)
     *
     * @param bitArray The array to decode
     * @param startIdx The index of the starting value
     * @param length The length of the sub array to decode
     * @return The decoded integer value
     */
    public static int decodeBitArray( boolean[] bitArray,
            int startIdx,
            int length )
    {
        int decoded = 0;
        for( int i = 0; i < length; i++ )
        {
            int tmp = (bitArray[(i+ startIdx)] ? 1: 0) << (length - i -1);
            decoded = decoded | tmp;
        }

        return decoded;
    }

    /**
     * Converts the gray code value to standard binary
     *
     * @param grayCode The gray code value to convert
     * @return The binary value
     */
    public static int convertGrayCodeToBinary( int grayCode )
    {
        int binary = 0;
        while( grayCode != 0 )
        {
            binary ^= grayCode;
            grayCode >>>= 1;
        }

        return binary;
    }

    /**
     *
     * @param propsFileKey The property key corresponding to the experiment properties
     * @return The properties
     */
    public static Properties loadProperties( String propsFileKey)
    {
        _LOG.trace( "Entering loadProperties( propsFileKey )" );

        // Load the specified properties file
        Properties props = new Properties();
        String propsFilename = System.getProperty( propsFileKey );
        Validate.notEmpty( propsFilename, "Property filename (key=["
                + propsFileKey
                + "] may not be empty" );
        File propsFile = new File( propsFilename );
        if( !propsFile.exists() )
        {
            _LOG.error( "Unable to find properties file with key ["
                    + propsFileKey
                    + "]" );
            throw new RuntimeException(
                    "Unable to find properties file with key ["
                    + propsFileKey
                    + "]" );
        }
        try
        {
            props.load( new FileInputStream( propsFile ) );
        }
        catch( IOException ioe )
        {
            _LOG.error( "Unable to load properties file ["
                    + propsFile.getAbsolutePath()
                    + "]", ioe );
            throw new RuntimeException( "Unable to load properties file ["
                    + propsFile.getAbsolutePath()
                    + "]", ioe );
        }

        // Iterate through all the properties from the command line
        Properties systemProps = System.getProperties();
        Iterator<String> iter = props.stringPropertyNames().iterator();
        while( iter.hasNext() )
        {
            String key = iter.next();

            // Was there an override specified on the command line?
            String value = systemProps.getProperty( key );
            if( null != value )
            {
                // Yup
                props.setProperty( key, value );
            }
        }

        _LOG.trace( "Leaving loadProperties( propsFileKey )" );

        return props;
    }


    /**
     * Loads and instantiates the class with the specified filename
     *
     * @param className The name of the class to load and instantiate
     * @param desc Description of the class for error logging
     * @return The instantiated class
     */
    static public Object loadAndInstantiate( String className, String desc )
    {
        Object instantiated = null;
        try
        {
            // Get the class
            Class instantiatedClass = Class.forName( className );

            // Instantiate it
            instantiated = instantiatedClass.newInstance();
        }
        catch( ClassNotFoundException cnfe )
        {
            _LOG.error( "Unable to find "
                    + desc
                    + " class with name ["
                    + className
                    + "]", cnfe );
            throw new RuntimeException(
                    "Unable to find "
                    + desc
                    + " class with name ["
                    + className
                    + "]", cnfe );
        }
        catch( IllegalAccessException iae )
        {
            _LOG.error( "Unable to access constructor for "
                    + desc
                    + " class ["
                    + className
                    + "]", iae );
            throw new RuntimeException(
                    "Unable to access constructor for "
                    + desc
                    + " class ["
                    + className
                    + "]", iae );
        }
        catch( InstantiationException ie )
        {
            _LOG.error( "Unable to instantiate "
                    + desc
                    + " class ["
                    + className
                    + "]", ie );
            throw new RuntimeException(
                    "Unable to instantiate "
                    + desc
                    + " class ["
                    + className
                    + "]", ie );
        }

        return instantiated;
    }

}
