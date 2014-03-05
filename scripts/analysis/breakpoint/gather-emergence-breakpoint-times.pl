#!/usr/bin/perl
use strict;
use POSIX;
use Data::Dumper;


# -------------------------------------------------------------------
use constant BOLDTHRESHOLD => 0.85;
use constant PVBUFFERPERCENTAGE => 0.85;
use constant MINTHRESHOLDIDX => 500;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the individual count
my $indCount = shift( @ARGV );


# -------------------------------------------------------------------
# Get the breakpoint files
my $analysisDir = $dataDir."analysis/";
opendir( DIR, $analysisDir ) or die "Unable to open analysis directory [$analysisDir]: $!\n";
my @breakpointFiles = grep { /^indcount-0*$indCount-breakpoint-analysis-\d+.dat$/ } readdir( DIR );
@breakpointFiles = sort( @breakpointFiles );
closedir( DIR );

$breakpointFiles[0] =~ /indcount-(\d+)/;
my $formattedIndCount = $1;

# -------------------------------------------------------------------
# Get the emergence times from each breakpoint analysis
my %data;
foreach my $changePointFile (@breakpointFiles)
{
    # Process the file
    readEmergenceTimes( $analysisDir.$changePointFile, \%data );
}

#print Dumper( %data );
createEmergenceTimesFile( \%data,
        $analysisDir."indcount-$formattedIndCount-emergence-times.dat",
        $dataDir,
        $indCount );


# ===================================================================
sub readEmergenceTimes
{
    my ($changePointFile, $dataRef ) = @_;

    # ---------------------------------------------------------------
    # Get the run ID
    $changePointFile =~ /analysis-(\d+)\.dat/;
    my $runID = $1;

    # ---------------------------------------------------------------
    # Open the file
    open( DATA, "$changePointFile" ) or die "Unable to open breakpoint file [$changePointFile]: $!\n";

    # ---------------------------------------------------------------
    # Read each line
    my %rawData;
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Ignore the number of simulations
        if( /total-simulations/ )
        {
            next;
        }

        # Split the line into key and value
        my ($key,$value) = split( /\s+=\s+/, $_ );
        my ($ind,$dataType) = split( /\./, $key );

        # Is it the breakpoints?
        if( $dataType =~ /breaksims/ )
        {
            # Yup
            @{$rawData{$ind}{"breaksims"}} = split( /\s+/, $value );
        }
        # Is it the coefficients?
        elsif( $dataType =~ /coefficients/ )
        {
            # Yup
            @{$rawData{$ind}{"coefficients"}} = split( /\s+/, $value );
        }
    }

    # Close the file
    close( DATA );

    # ---------------------------------------------------------------
    # Find the emergence times of bold individuals
    my $deltaEmergenceTimesRef = findEmergenceTimes( \%rawData );

    # ---------------------------------------------------------------
    # Organize the times by emergence order
    for( my $i = 0; $i <= $#{$deltaEmergenceTimesRef}; $i++ )
    {
        push( @{$dataRef->{$i}}, $deltaEmergenceTimesRef->[$i] );
    }
}

# ===================================================================
sub findEmergenceTimes
{
    my ($rawDataRef) = @_;

    # ---------------------------------------------------------------
    my @rawEmergenceTimes;
    foreach my $ind (sort (keys %{$rawDataRef}) )
    {
        # Iterate through all the coefficients to find out if the
        # agent emerged as bold
        my $found = 0;
        for( my $i = 0; ($i <= $#{$rawDataRef->{$ind}{"coefficients"}}) && (0 == $found); $i++ )
        {
            if( BOLDTHRESHOLD < ${$rawDataRef->{$ind}{"coefficients"}}[$i] )
            {
#print "Ind=[$ind] coefficient=[",${$rawDataRef->{$ind}{"coefficients"}}[$i],"] i=[$i]\n";
                # Get the emergence time.  Note that the coefficients
                # start at timestep 0, so the breakpoint indices
                # are offset by 1.  If the coefficient index is 0,
                # then the individual "emerged" at time 0
                if( 0 == $i )
                {
                    push( @rawEmergenceTimes, 0 );
                }
                else
                {
                    push( @rawEmergenceTimes,
                            ${$rawDataRef->{$ind}{"breaksims"}}[$i-1] );
                }

                # Go on to the next individual
                $found = 1;
            }
        }
    }

    # ---------------------------------------------------------------
    # Sort the emergence times
    my @sortedEmergenceTimes = sort { $a <=> $b } @rawEmergenceTimes;
    #print "Emergence times [",join( " ", @sortedEmergenceTimes ),"]\n";

    # ---------------------------------------------------------------
    # Compute the time between emergences
    my @deltaTimes;
    my $previousEmergenceTime = 0;
    foreach my $time (@sortedEmergenceTimes)
    {
        push( @deltaTimes, ($time - $previousEmergenceTime) );
        $previousEmergenceTime = $time;
    }

    #print "Delta times [",join( " ", @deltaTimes ),"]\n";
    return \@deltaTimes;
}


# ===================================================================
sub createEmergenceTimesFile
{
    my ($dataRef, $outputFile, $dataDir, $indCount) = @_;

    # ---------------------------------------------------------------
    # Create the file
    open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

    # ---------------------------------------------------------------
    # Dump out header information
    print OUTPUT "# Datadir:   $dataDir\n";
    print OUTPUT "# IndCount:  $indCount\n";
    my $date = localtime;
    print OUTPUT "# Date:      $date\n";
    print OUTPUT "# ========================================\n\n";

    # ---------------------------------------------------------------
    # Dump the emergence times for each emergence in the sequence
    foreach my $seqIdx (sort {$a <=> $b} (keys %{$dataRef}) )
    {
        print OUTPUT "# Emergence times for emergence [$seqIdx]\n";
        print OUTPUT "emergence-",sprintf( "%03d", $seqIdx),".count = ",
                (scalar @{$dataRef->{$seqIdx}}),"\n";
        print OUTPUT "emergence-",sprintf( "%03d", $seqIdx),".times = ",
                join( " ", (sort {$a <=> $b} @{$dataRef->{$seqIdx}}) ),"\n\n";
    }

    # ---------------------------------------------------------------
    # Close the file
    close( OUTPUT );
}
