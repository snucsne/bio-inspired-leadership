#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the output file
my $outputFile = shift( @ARGV );

# Get the metric directory
my $metricDir = shift( @ARGV );

# Get the topological directory
my $topoDir = shift( @ARGV );


# -------------------------------------------------------------------
# Create the necessary variables
my %data;

# -------------------------------------------------------------------
# Get all the metric processed log files
opendir( METRICDIR, $metricDir ) or die "Unable to open metric dir [$metricDir]: $!\n";
my @metricProcessedLogFiles = sort( grep { /processed-log.stat$/ } readdir ( METRICDIR ) );
closedir( METRICDIR );

# Get all the topological processed log files
opendir( TOPODIR, $topoDir ) or die "Unable to open topological dir [$topoDir]: $!\n";
my @topoProcessedLogFiles = sort( grep { /processed-log.stat$/ } readdir ( TOPODIR ) );
closedir( TOPODIR );


# -------------------------------------------------------------------
#Parse each of the processed log files
foreach my $metricProcessedLog (@metricProcessedLogFiles)
{
    $metricProcessedLog =~ /var\-(\d+)\-seed/;
    my $runID = $1;
    parseProcessedLog( $metricDir.$metricProcessedLog, \%data, "metric", $runID );
last;
}

foreach my $topoProcessedLog (@topoProcessedLogFiles)
{
    $topoProcessedLog =~ /var-(\d+)-seed/;
    my $runID = $1;
    parseProcessedLog( $topoDir.$topoProcessedLog, \%data, "topo", $runID );
last;
}


# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/compare-processed-logs.r";
my $rOutputFile = "/tmp/compare-processed-logs.r.out";
createRInput( $rInputFile, $rOutputFile, $outputFile, \%data );

# -------------------------------------------------------------------
# Run the R file
#`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Clean up the R files
#`rm $rInputFile $rOutputFile`;


# ===================================================================
sub parseProcessedLog
{
    my ($inputFile, $dataRef, $expType, $runID) = @_;

    # ---------------------------------------------------------------
    # Read the file
    open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";
    while( <INPUT> )
    {
        # Remove any comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Split the line up into a key and a value
        my ($key,$value) = split( /\s+=\s+/, $_ );

        # Is it a depth data value?
        if( $key =~ m/depth\.(Ind\d+)\.data/ )
        {
            # Yup
            my $ind = $1;
#            $dataRef->{$expType}{$runID}{$ind}{"depth"} = $value;
        }

        # Is it a followers data value?
        elsif( $key =~ m/followers\.(Ind\d+)\.data/ )
        {
            # Yup
            my $ind = $1;
#            $dataRef->{$expType}{$runID}{$ind}{"followers"} = $value;
        }

        # Is it a branching data value?
        elsif( $key =~ m/branching\.(\d+)\.data/ )
        {
            # Yup
            my $depth = $1;
            $dataRef->{$expType}{$runID}{"Depth$depth"}{"branching"} = $value;
        }

        # Is it a branching count value?
        elsif( $key =~ m/branching\.(\d+)\.count/ )
        {
            # Yup
            my $depth = $1;
#            $dataRef->{$expType}{$runID}{"Depth$depth"}{"branchingcount"} = $value;
        }
    }
    
    # ---------------------------------------------------------------
    close( INPUT );
    
}

# ===================================================================
sub createRInput
{
    my ($rInputFile, $rOutputFile, $resultsFile, $dataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    foreach my $expType (sort (keys %{$dataRef} ) )
    {
        foreach my $runID (sort (keys %{$dataRef->{$expType}} ) )
        {
            foreach my $dataID (sort (keys %{$dataRef->{$expType}{$runID}} ) )
            {
                foreach my $dataType (sort (keys %{$dataRef->{$expType}{$runID}{$dataID}} ) )
                {
                    print INPUT "Run$runID$dataID$dataType$expType <- scan()\n";
                    print INPUT $dataRef->{$expType}{$runID}{$dataID}{$dataType},"\n\n";
                    push( @{$dataIDs{$expType}}, "Run$runID$dataID$dataType$expType" );
                }
            }
        }
    }

    # ---------------------------------------------------------------
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the KS tests
    for( my $i = 0; $i <= $#{$dataIDs{"metric"}}; $i++ )
    {
        print "KS Test [$i]\n";

        print INPUT "ks <- ks.boot( ",
                ${$dataIDs{"metric"}}[$i],
                ",",
                ${$dataIDs{"topo"}}[$i],
                " )\n";
        print INPUT "cat(\"# KS: ",
                ${$dataIDs{"metric"}}[$i],
                " vs ",
                ${$dataIDs{"topo"}}[$i],
                "\\n\")\n";
        print INPUT "cat(\"ks.p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
        print INPUT "cat(\"ks.naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
        print INPUT "cat(\"ks.statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
        print INPUT "cat(\"\\n\")\n";
    }

    print "Done...\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

}

