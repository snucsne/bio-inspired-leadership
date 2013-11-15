package edu.snu.leader.discrete.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Reporter
{
    /** Root directory of all reporters */
    public static String ROOT_DIRECTORY = "";

    /** Sub directory for this reporter */
    private String _subDirectory = "";

    /** Simulation run for additional sub directories */
    public static int SIMULATION_RUN = 0;

    /** The actual file name */
    private String _filename = null;

    /** The string builder that will manage all the added text */
    private StringBuilder _builder = null;

    /** The spacer for new sections */
    private final String _spacer = "==============================";

    /** New line character for any os */
    private String endl = null;

    /** Variable to toggle whether or not run directories are used */
    private boolean _useRunDirectories = true;

    /**
     * Creates a reporter that will output to a given file. Defaulted to use run
     * subdirectories
     * 
     * @param filename The file to output to
     */
    public Reporter( String filename, String subDirectory )
    {
        _subDirectory = subDirectory;
        _filename = filename;
        _builder = new StringBuilder();
        endl = System.getProperty("line.separator");
    }

    /**
     * Builds this Reporter object
     * 
     * @param filename The filename
     * @param subDirectory The subdirectory
     * @param useRunDirectories Whether or not run subdirectories are used
     */
    public Reporter( String filename,
            String subDirectory,
            boolean useRunDirectories )
    {
        this( filename, subDirectory );
        _useRunDirectories = useRunDirectories;
    }

    /**
     * Appends text and adds a new line at the end
     * 
     * @param line Line to be added
     */
    public void appendLine( String line )
    {
        _builder.append( line + endl );
    }

    /**
     * Appends new text without creating a new line
     * 
     * @param text The text to add
     */
    public void append( String text )
    {
        _builder.append( text );
    }

    /**
     * Creates a new section by placing a divider that looks like
     * ===============
     */
    public void createSection( String title )
    {
        _builder.append( '#' + _spacer + title + _spacer + endl );
    }

    /**
     * Outputs all added lines to filename given if it is enabled to do so
     * 
     * @param shouldReport Enable or disable reporting
     */
    public void report( boolean shouldReport )
    {
        if( shouldReport )
        {
            // make root directory for all Reporters
            File dir = new File( ROOT_DIRECTORY );
            dir.mkdir();
            // make subdirectory for this reporter
            dir = new File( ROOT_DIRECTORY + "/" + _subDirectory );
            dir.mkdir();
            String temp = null;
            // make directories for each different run if we want them
            if( _useRunDirectories )
            {
                // dir = new File( ROOT_DIRECTORY + "/" + _subDirectory + "/"
                // + "Run" + SIMULATION_RUN );
                // dir.mkdir();
                temp = ROOT_DIRECTORY + "/" + _subDirectory + "/" + "Run"
                        + SIMULATION_RUN + _filename;
            }
            else
            {
                temp = ROOT_DIRECTORY + "/" + _subDirectory + "/" + _filename;
            }
            // print out the stringbuilder
            PrintWriter out = null;
            try
            {
                out = new PrintWriter( new BufferedWriter( new FileWriter(
                        temp, true ) ) );
            }
            catch( FileNotFoundException e )
            {
                throw new RuntimeException( "Could not open " + _filename
                        + " output file." );
            }
            catch( IOException e )
            {
                throw new RuntimeException( "Could not write to " + _filename
                        + " output file." );
            }
            out.print( _builder.toString() );
            out.close();
        }
    }

    /**
     * Clears the reporter's text
     */
    public void clear()
    {
        _builder = new StringBuilder();
    }

    @Override
    public String toString()
    {
        return _builder.toString();
    }
}
