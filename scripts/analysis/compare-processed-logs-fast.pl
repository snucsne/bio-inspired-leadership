#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the output file
my $outputDir = shift( @ARGV );

# Get the metric directory
my $metricDir = shift( @ARGV );

# Get the topological directory
my $topoDir = shift( @ARGV );



# -------------------------------------------------------------------
# Define the interesting datatypes
my %dataConfig = (
        "depth" => {
            "summary" => 1,
            "ks" => 1, },
        "followers" => {
            "summary" => 1,
            "ks" => 1, },
        "branching-data" => {
            "summary" => 1,
            "ks" => 1, },
        "branching-count" => {
            "summary" => 0,
            "ks" => 0, }
        "time" => {
            "summary" => 1,
            "ks" => 1, },
        "max-depth" => {
            "summary" => 1,
            "ks" => 1, },
    );

# Get some information from the metric directory
$metricDir =~ m/indcount-(\d+)-/;
my $indCount = $1;


# -------------------------------------------------------------------
# Get all the metric processed log files
opendir( METRICDIR, $metricDir ) or die "Unable to open metric dir [$metricDir]: $!\n";
my @metricProcessedLogFiles = sort( grep { /processed-log.stat$/ } readdir ( METRICDIR ) );
closedir( METRICDIR );

# Get all the topological processed log files
opendir( TOPODIR, $topoDir ) or die "Unable to open topological dir [$topoDir]: $!\n";
my @topoProcessedLogFiles = sort( grep { /processed-log.stat$/ } readdir ( TOPODIR ) );
closedir( TOPODIR );

# Turn the topological array into a hash to simplify lookups
my %topoProcessedLogFilesMap = map { $_ => 1 } @topoProcessedLogFiles;


# -------------------------------------------------------------------
# Iterate through all of the metric log files
foreach my $metricProcessedLog (@metricProcessedLogFiles)
{
    # Get the run ID
    $metricProcessedLog =~ /var\-(\d+)\-seed/;
    my $runID = $1;

    # Get the corresponding topological file
    my $topoProcessedLog = $metricProcessedLog;
    $topoProcessedLog =~ s/metric/topo/g;

    # Build the output filename
    my $outputFile = $outputDir."processed-log-comparison-indcount-$indCount-run-$runID.stats";

    # Build a header
    my $header = "# *****************************************************\n"
                ."# Run ID [$runID]\n"
                ."# Metric file [$metricDir$metricProcessedLog]\n"
                ."# Topological file [$topoDir$topoProcessedLog]\n"
                ."# *****************************************************\n\n";

    # Dump header information
    open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";
    print OUTPUT $header;
    close( OUTPUT );

    unless( $topoProcessedLogFilesMap{$topoProcessedLog} )
    {
        die "Unable to find corresponding topological file topo=[$topoProcessedLog] metric=[$metricProcessedLog]";
    }

    # Analyze each of the data types
    foreach my $dataType (sort (keys %dataConfig) )
    {
        my $resultsFile = "/tmp/compare-processed-logs.out";
        analyzeLogData( $metricDir.$metricProcessedLog,
                $topoDir.$topoProcessedLog,
                $runID,
                $dataType,
                $resultsFile,
                $header,
                \%{$dataConfig{$dataType}} );

        `cat $resultsFile >> $outputFile`;
    }
}


# ===================================================================
sub analyzeLogData
{
    my ($metricFile, $topoFile, $runID, $dataType, $outputFile, $header, $configRef ) = @_;

    # Parse the metric file
    my %metricData;
    parseProcessedLog( $metricFile, $dataType, \%metricData );

    # Parse the topological file
    my %topoData;
    parseProcessedLog( $topoFile, $dataType, \%topoData );

    # Extract all the data keys from both files
    my %tempHash = %metricData;
    @tempHash{keys %topoData}=1;
    my @dataKeys = sort keys %tempHash;

    # Analyze the data using R
    my $description = "# -----------------------------------------------------\\n"
                     ."# Data type [$dataType]\\n"
                     ."# -----------------------------------------------------\\n";
    analyzeWithR( $header, $description, $dataType, $outputFile,
                  \%metricData, \%topoData, $configRef, \@dataKeys );
}


# ===================================================================
sub parseProcessedLog
{
    my ($inputFile, $dataType, $dataRef) = @_;

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
        if( $dataType =~ m/^depth$/ && $key =~ m/depth\.(Ind\d+)\.data/ )
        {
            # Yup
            my $ind = $1;
            $dataRef->{$ind} = $value;
        }

        # Is it a followers data value?
        elsif( $dataType =~ m/followers/ && $key =~ m/followers\.(Ind\d+)\.data/ )
        {
            # Yup
            my $ind = $1;
            $dataRef->{$ind} = $value;
        }

        # Is it a branching data value?
        elsif( $dataType=~ m/branching-data/ && $key =~ m/branching\.(\d+)\.data/ )
        {
            # Yup
            my $depth = $1;
            $dataRef->{"depth$depth"} = $value;
        }

        # Is it a branching count value?
        elsif( $dataType=~ m/branching-count/ && $key =~ m/branching\.(\d+)\.count/ )
        {
            # Yup
            my $depth = $1;
            $dataRef->{"depth$depth"} = $value;
        }

        # Is it a time value?
        elsif( $dataType =~ m/time/ && $key =~ m/time\.(\d+)\.data/ )
        {
            # Yup
            my $order = $1;
            $dataRef->{"time"} = $value;
        }

        # Is it a max depth value?
        elsif( $dataType =~ m/maxdepth/ && $key =~ m/max-depth\.data/ )
        {
            # Yup
            $dataRef->{"maxdepth"} = $value;
        }
    }

    # ---------------------------------------------------------------
    close( INPUT );

}


# ===================================================================
sub analyzeWithR
{
    my ($header, $description, $dataType, $resultsFile,
        $metricDataRef, $topoDataRef, $configRef, $dataKeysRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rInputFile = "/tmp/compare-processed-logs.r";
    my $rOutputFile = "/tmp/compare-processed-logs.r.out";
    my $rSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n\n";
    print INPUT $header;

    # ---------------------------------------------------------------
    # Add the data
    foreach my $dataKey ( @$dataKeysRef )
    {
        print INPUT "# Metric [$dataKey]\n";
        if( $metricDataRef->{$dataKey} )
        {
            print INPUT "metric$dataKey <- scan()\n";
            print INPUT $metricDataRef->{$dataKey},"\n\n";
        }
        else
        {
            print INPUT "# NONE\n\n";
        }
        print INPUT "# Topological [$dataKey]\n";
        if( $topoDataRef->{$dataKey} )
        {
            print INPUT "topo$dataKey <- scan()\n";
            print INPUT $topoDataRef->{$dataKey},"\n\n\n";
        }
        else
        {
            print INPUT "# NONE\n\n";
        }
    }

    # ---------------------------------------------------------------
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";
    print INPUT "cat(\"$description\")\n";

    # ---------------------------------------------------------------
    # Analyze the data
    foreach my $dataKey ( sort keys %{$metricDataRef} )
    {
        # Metric summary
        if( $metricDataRef->{$dataKey} )
        {
            if( $configRef->{"summary"} )
            {
                print INPUT "cat(\"# Data summary: metric",$dataKey,"\\n\")\n";
                print INPUT "cat(\"summary.$dataType.metric.$dataKey.mean = \",
                        format( mean(metric$dataKey) ), \"\\n\")\n";
                print INPUT "cat(\"summary.$dataType.metric.$dataKey.sd = \",
                        format( sd(metric$dataKey) ), \"\\n\")\n";
                print INPUT "cat(\"summary.$dataType.metric.$dataKey.var = \",
                        format( var(metric$dataKey) ), \"\\n\\n\")\n";
            }
            else
            {
                print INPUT "cat(\"# Data: metric",$dataKey,"\\n\")\n";
                print INPUT "cat(\"value.$dataType.metric.$dataKey = \",
                        format( mean(metric$dataKey) ), \"\\n\\n\")\n";
            }
        }

        # Topological summary
        if( $topoDataRef->{$dataKey} )
        {
            if( $configRef->{"summary"} )
            {
                print INPUT "cat(\"# Data summary: topo",$dataKey,"\\n\")\n";
                print INPUT "cat(\"summary.$dataType.topo.$dataKey.mean = \",
                        format( mean(topo$dataKey) ), \"\\n\")\n";
                print INPUT "cat(\"summary.$dataType.topo.$dataKey.sd = \",
                        format( sd(topo$dataKey) ), \"\\n\")\n";
                print INPUT "cat(\"summary.$dataType.topo.$dataKey.var = \",
                        format( var(topo$dataKey) ), \"\\n\\n\")\n";
            }
            else
            {
                print INPUT "cat(\"# Data: topo",$dataKey,"\\n\")\n";
                print INPUT "cat(\"value.$dataType.topo.$dataKey = \",
                        format( mean(topo$dataKey) ), \"\\n\\n\")\n";
            }
        }

        if( $configRef->{"ks"} )
        {
            # Perform KS tests if both have data
            print INPUT "cat(\"# KS: metric",
                    $dataKey,
                    " vs topo",
                    $dataKey,
                    "\\n\")\n";
            if( $metricDataRef->{$dataKey} && $topoDataRef->{$dataKey} )
            {
                print INPUT "ks <- ks.boot( metric",
                        $dataKey,
                        ",topo",
                        $dataKey,
                        " )\n";
                print INPUT "cat(\"ks.$dataType.$dataKey.p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
                print INPUT "cat(\"ks.$dataType.$dataKey.naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
                print INPUT "cat(\"ks.$dataType.$dataKey.statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
            }
            else
            {
                print INPUT "cat(\"ks.$dataType.$dataKey.p-value = NA\\n\")\n";
                print INPUT "cat(\"ks.$dataType.$dataKey.naive-pvalue = NA\\n\")\n";
                print INPUT "cat(\"ks.$dataType.$dataKey.statistic = NA\\n\")\n";
            }
        }
        print INPUT "cat(\"\\n\")\n";
    }

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

    # -------------------------------------------------------------------
    # Run the R file
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;

    # -------------------------------------------------------------------
    # Clean up the R files
    #`rm $rInputFile $rOutputFile`;

}


# ===================================================================
sub commifySeries {
    (@_ == 0) ? ''                                      :
    (@_ == 1) ? $_[0]                                   :
                join("\n", @_);
}

