#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );

# Get the result file regex
my $resultFileRegex = shift( @ARGV );

# Get the individual count
my $indCount = shift( @ARGV );

# Get all the data directories
my @dataDirs;
my @dataLabels;
while( @ARGV )
{
    push( @dataDirs, shift( @ARGV ) );
    push( @dataLabels, shift( @ARGV ) );
}

# -------------------------------------------------------------------
# Get the processed breakpoint result files for each data directory
my %data;
my @dataSetOrder;
foreach my $dataDir (@dataDirs)
{
    # Ensure there is a trailing slash
    unless( $dataDir =~ /\/$/ )
    {
        $dataDir = "$dataDir/";
    }

    # Get the data set identifier
    $dataDir =~ /(tw-tl-.*)\/personality/;
    my $dataSet = $1;
print "dataSet=[$dataSet]\n";

    # Read all the breakpoint result files
    my $analysisDir = $dataDir."analysis";
    readBreakpointResults( $analysisDir, $resultFileRegex, \%data, $dataSet, $indCount );

    # Store the data set order to improve readability of results
    push( @dataSetOrder, $dataSet );

}

# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/compare-specific-breakpoint-results.r";
my $rOutputFile = $rInputFile.".out";
my @epsFiles = createRInput( $rInputFile,
        $outputFilePrefix."-results.dat",
        $epsFilePrefix,
        $indCount,
        \@dataLabels,
        \@dataSetOrder,
        \%data);

#exit;

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Clean up the EPS files
my $tmpEPSFile = "/tmp/tmp.eps";
my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
foreach my $epsFile (@epsFiles)
{
    `cp $epsFile $tmpEPSFile`;
#    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
#    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `/usr/bin/epstool --copy --bbox $tmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
}



# ===================================================================
sub readBreakpointResults
{
    my ($dir, $resultFileRegex, $dataRef, $dataSet, $indCount) = @_;

    # Grab all the breakpoint result files
    opendir( DIR, $dir ) or die "Unable to open breakpoint results dir [$dir]: $!\n";
    my @resultFiles = sort( grep { /$resultFileRegex\.dat$/ } readdir( DIR ) );
    closedir( DIR );

    # Read each of the files
    foreach my $resultFile (@resultFiles)
    {
#print "\tReading results file [$dir/$resultFile]\n";
        my %fileData;

        # Open the file
        open( DATA, "$dir/$resultFile" ) or die "Unable to open result file [$resultFile]: $!\n";

        # Read each line
        while( <DATA> )
        {
            # Remove any other comments or whitespace
            s/#.*//;
            s/^\s+//;
            s/\s+$//;
            next unless length;

            # Split it into a key/value pair
            my ($key,$value) = split( /\s+=\s+/, $_ );

            # Store the data
            $fileData{$key} = $value;
        }

        # Close the file
        close( DATA );

        # Store all the file's data
        foreach my $key (sort (keys %fileData ) )
        {
            my $value = $fileData{$key};
            $value =~ s/^\s+//;
            $value =~ s/\s+$//;
            $dataRef->{$dataSet}{$indCount}{$key} = $fileData{$key};

            # Split it up to see if it is comprised of a
            # single value or multiple values
            my @splitValues = split( /\s+/, $value );
            if( $#splitValues > 1 )
            {
                $dataRef->{"multi"}{$dataSet}{$indCount}{$key} = 1;
            }
        }

        # Store the data keys
        $dataRef->{"data-keys"} = keys %fileData;
    }
}

# ===================================================================
sub createRInput
{
    my ($rInputFile,
            $resultsFile,
            $epsFilePrefix,
            $indCount,
            $dataLabelsRef,
            $dataSetOrderRef,
            $dataRef) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";
    print INPUT "library('Hmisc')\n";

    print INPUT "library(extrafont)\n";
    print INPUT "loadfonts(device = \"postscript\")\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # Build a function to handle the t-test error in case the data is constant
    print INPUT "tryttestpvalue <- function(...) {\n";
    print INPUT "    obj<-try(t.test(...), silent=TRUE)\n";
    print INPUT "    if (is(obj, \"try-error\")) return(NA) else return(obj\$p.value)\n";
    print INPUT "}\n";


    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    my %dataDescs;
    my %relDiffDataIDs;
    my @allDataIDs;
    my %dataSetHash;
    my %indCountHash;
    my @dataKeys;
    my $index = 0;
    foreach my $dataSet (@{$dataSetOrderRef})
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataRef->{$dataSet}} ) )
        {
            foreach my $dataKey (sort (keys %{$dataRef->{$dataSet}{$indCount}} ) )
            {
                if( $dataKey =~ /sim-bold-differentiation/
                        || $dataKey =~ /^bold-then-shy$/
                        || $dataKey =~ /^shy-then-bold$/ )
                {
                    next;
                }

                my $cleanDataKey = $dataKey;
                $cleanDataKey =~ s/\.//g;
                $cleanDataKey =~ s/-//g;

                my $id = $cleanDataSet."indcount".$indCount.$cleanDataKey;
                print INPUT "# ----------------------------------------------------------\n";
                print INPUT "# ${$dataLabelsRef}[$index]\n";
                print INPUT "$id <- scan()\n";
                print INPUT $dataRef->{$dataSet}{$indCount}{$dataKey},"\n\n";
                $dataIDs{$dataSet}{$indCount}{$dataKey} = $id;
                $dataDescs{$dataSet}{$indCount}{$dataKey} = ${$dataLabelsRef}[$index];
            }
            $indCountHash{$indCount} = 1;
        }
        $dataSetHash{$dataSet} = 1;

        # Increment the index
        $index++;
    }

    my @dataSets = @{$dataSetOrderRef};


    # ---------------------------------------------------------------
    # Start sending output to the results file
#    print INPUT "sink(\"$resultsFile\")\n";
#    print "Sending results to [$resultsFile]\n";

    # ---------------------------------------------------------------
    # Add the statistics
    my %dataKeyMap;
    foreach my $dataSet (sort (keys %dataIDs) )
    {
        # Sanitize the data set
        my $cleanDataSet = $dataSet;
        $cleanDataSet =~ s/\.//g;
        $cleanDataSet =~ s/-//g;

        foreach my $indCount (sort (keys %{$dataIDs{$dataSet}} ) )
        {
            foreach my $dataKey (sort (keys %{$dataIDs{$dataSet}{$indCount}} ) )
            {
                if( $dataKey =~ /sim-bold-differentiation/ )
                {
                    next;
                }

                if( $dataRef->{"multi"}{$dataSet}{$indCount}{$dataKey} )
                {
                    my $id = $dataIDs{$dataSet}{$indCount}{$dataKey};
                    my $description = $dataDescs{$dataSet}{$indCount}{$dataKey};
                    print INPUT "cat(\"# =====================================\\n\")\n";
                    print INPUT "cat(\"# $description summary\\n\")\n";
                    print INPUT "cat(\"summary.$cleanDataSet.indcount-$indCount.$dataKey.mean = \", format( mean($id) ), \"\\n\")\n";
                    print INPUT "cat(\"summary.$cleanDataSet.indcount-$indCount.$dataKey.max =  \", format( max($id) ), \"\\n\")\n";
                    print INPUT "cat(\"summary.$cleanDataSet.indcount-$indCount.$dataKey.min =  \", format( min($id) ), \"\\n\")\n";
                    print INPUT "cat(\"summary.$cleanDataSet.indcount-$indCount.$dataKey.sd =   \", format( sd($id) ), \"\\n\")\n";
                    print INPUT "cat(\"summary.$cleanDataSet.indcount-$indCount.$dataKey.se =   \", format( stderr($id) ), \"\\n\")\n";
                    print INPUT "cat(\"summary.$cleanDataSet.indcount-$indCount.$dataKey.length = \", format( length($id) ), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";

                    $dataKeyMap{$dataKey} = 1;
                }
                else
                {
                    my $id = $dataIDs{$dataSet}{$indCount}{$dataKey};
                    my $description = $dataDescs{$dataSet}{$indCount}{$dataKey};
                    print INPUT "cat(\"# =====================================\\n\")\n";
                    print INPUT "cat(\"# $description value\\n\")\n";
                    print INPUT "cat(\"value.$cleanDataSet.indcount-$indCount.$dataKey = \", format( $id ), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";
                }
            }
        }
    }
    @dataKeys = sort( keys( %dataKeyMap ) );

    # ---------------------------------------------------------------
    # Add the SIG tests for data with the same individual count
    for( my $i = 0; $i <= ($#dataSets - 1); $i++ )
    {
        for( my $j = $i + 1; $j <= ($#dataSets); $j++ )
        {
            my $firstDataSet = $dataSets[$i];
            my $secondDataSet = $dataSets[$j];

            foreach my $dataKey (sort (keys %{$dataIDs{$firstDataSet}{$indCount}} ) )
            {
                if( $dataKey =~ /sim-bold-differentiation/ )
                {
                    next;
                }

                if( $dataRef->{"multi"}{$firstDataSet}{$indCount}{$dataKey}
                        && $dataRef->{"multi"}{$secondDataSet}{$indCount}{$dataKey} )
                {
                    my $firstDesc = $dataDescs{$firstDataSet}{$indCount}{$dataKey};
                    my $secondDesc = $dataDescs{$secondDataSet}{$indCount}{$dataKey};

                    print INPUT $rSpacer;
                    print INPUT "cat(\"# Sig tests: $firstDesc\\n\")\n";
                    print INPUT "cat(\"#         vs $secondDesc\\n\")\n";
                    printSIGTestText( *INPUT,
                            $dataIDs{$firstDataSet}{$indCount}{$dataKey},
                            "$firstDataSet-indCount-$indCount-$dataKey",
                            $dataIDs{$secondDataSet}{$indCount}{$dataKey},
                            "$secondDataSet-indCount-$indCount-$dataKey" );
                }
            }
        }
    }

    # ---------------------------------------------------------------
    # Stop sending output to the results file
#    print INPUT "sink()\n\n\n";


    # ---------------------------------------------------------------
    # Build a combined plot for each data key
    my %dataFormat = (
        "bold-differentiation" => { "y-min" => 0,
                                   "y-max" => 20000,
                                   "y-title" => "Mean differentiation point\\n of bold personalities",
                                   "legend-loc" => "top" },
        "bold-differentiation-percentage" => { "y-min" => 0.0,
                                   "y-max" => 0.25,
                                   "y-title" => "Mean differentiation point\\n of bold personalities (percentage)",
                                   "legend-loc" => "top" },
        "bold-sim-percentage" => { "y-min" => 0.75,
                                   "y-max" => 1.0,
                                   "y-title" => "Percentage of simulations at bold",
                                   "legend-loc" => "bottom" },
        "bold-update-percentage" => { "y-min" => 0.75,
                                      "y-max" => 1.0,
                                      "y-title" => "Percentage of updates at bold",
                                      "legend-loc" => "bottom" },
        "bold-update-count" => { "y-min" => 2500,
                                 "y-max" => 17500,
                                 "y-title" => "Number of updates at bold",
                                 "legend-loc" => "top" },
        "bold-then-shy-percentage" => { "y-min" => 0,
                                 "y-max" => 0.02,
                                 "y-title" => "Percentage of population with personality\\n transition from bold to shy",
                                 "legend-loc" => "top" },
        "shy-then-bold-percentage" => { "y-min" => 0,
                                 "y-max" => 0.1,
                                 "y-title" => "Mean percentage of population with\\n personality transition from shy to bold",
                                 "legend-loc" => "top" },
        "shy-sim-percentage" => { "y-min" => 0.3,
                                  "y-max" => 1.0,
                                  "y-title" => "Percentage of simulations at shy",
                                  "legend-loc" => "top" },
        "shy-update-percentage" => { "y-min" => 0.25,
                                     "y-max" => 0.75,
                                     "y-title" => "Percentage of updates at shy",
                                     "legend-loc" => "top" },
        "shy-update-count" => { "y-min" => 0,
                                "y-max" => 1100,
                                "y-title" => "Number of updates at shy",
                                "legend-loc" => "bottom" } );


    foreach my $dataKey (@dataKeys)
    {
        if( $dataKey =~ /sim-bold-differentiation/ )
        {
            next;
        }

        unless( $dataFormat{$dataKey}{"y-max"} )
        {
            next;
        }

        # Set up some handy variables
        my @colors = ( "#117733", "#332288", "#9C0A2E", "#AA4499", "#999933", "#88CCEE", "#CC6677", "#44AA99", "#DDCC77", "#000000" );
#        my @colors = ( "#88CCEE", "#DDCC77", "#CC6677", "#44AA99", "#AA4499" );
        my @plotSymbols = ( 0, 1, 2, 5, 6 );
        my $dataSetNames = "";
        my $plotSymbolsStr = "";

        # Build some data to plot
        my $plotDataStr = "";
        my $plotNamesStr = "";
        my $plotColorsStr = "";
        my $index = 0;
        foreach my $dataSet (@dataSets)
        {
            $plotDataStr .= $dataIDs{$dataSet}{$indCount}{$dataKey}.", ";
            $plotNamesStr .= "\"$dataDescs{$dataSet}{$indCount}{$dataKey}\", ";
            $plotColorsStr .= "\"".$colors[$index]."\", ";

            $index++;
        }

        # Clean up the strings
        $plotDataStr =~ s/, $//;
        $plotNamesStr =~ s/, $//;
        $plotColorsStr =~ s/, $//;

        # Create the filename
        my $comparisonEPSFile = $epsFilePrefix."-$dataKey-breakpoint-comparison.eps";
        push( @epsFiles, $comparisonEPSFile );

        # Send the plot to the specified file and organize the layout
#        print INPUT "postscript( file=\"$comparisonEPSFile\", height=6, width=7.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
#        print INPUT "par(mar=c(8,8,3,5)+0.1)\n";
#        print INPUT "par(mgp=c(4,1,0))\n";
        print INPUT "postscript( file=\"$comparisonEPSFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
        print INPUT "par(mar=c(4,5,1,3)+0.1)\n";
        print INPUT "par(mgp=c(3,0.8,0))\n";

        # Get the plot formats
        my $yMin = 0;
        my $yMax = "ymaxvalue";
        my $yTitle = ucfirst($dataKey);
        my $legendLoc = "bottom";
        if( exists( $dataFormat{$dataKey} ) )
        {
            $yMin = $dataFormat{$dataKey}{"y-min"};
            $yMax = $dataFormat{$dataKey}{"y-max"};
            $yTitle = $dataFormat{$dataKey}{"y-title"};
            $legendLoc = $dataFormat{$dataKey}{"legend-loc"};
        }

        # Create the appropriate boxplot
        print INPUT "boxplot( $plotDataStr, col=c($plotColorsStr),\n",
                "    names=c($plotNamesStr), ylab=\"$yTitle\",\n",
                "    ylim=c($yMin,$yMax), yaxt='n' )\n";

        # Customize the x axis
#        print INPUT "xticklocations <- axTicks(1)\n";
#        print INPUT "text( xticklocations, labels=c($plotNamesStr), srt=30, xpd=NA, adj=1 )\n";
#        print INPUT "axis( 1, las=2, at=xticklocations, labels=c(rep(\" \", $index)) )\n";

        # Customize the y axes
        print INPUT "yticklocations <- axTicks(2)\n";

        if( $dataKey =~ /percentage/ )
        {
            print INPUT "templabels <- yticklocations * 100\n";
            print INPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
            print INPUT "axis( 2, las=2, at=yticklocations, labels=percentagelabels )\n";
            print INPUT "axis( 4, las=2, at=yticklocations, labels=percentagelabels )\n";
        }
        else
        {
            print INPUT "templabels <- format( yticklocations, big.mark=\",\", scientific=FALSE )\n";
            print INPUT "axis( 2, las=2, at=yticklocations, labels=templabels )\n";
            print INPUT "axis( 4, las=2, at=yticklocations, labels=templabels )\n";
        }

    }

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

    # ---------------------------------------------------------------
    # Return the EPS files built
    return @epsFiles;
}


# ===================================================================
sub printSIGTestText
{
    local *INPUT = shift;
    my ($firstID, $firstKey, $secondID, $secondKey) = @_;
return;
    print INPUT "ks <- ks.boot( ",
            $firstID,
            ",",
            $secondID,
            " )\n";
    print INPUT "cat(\"ks.",
            $firstKey,
            ".vs.",
            $secondKey,
            ".p-value =       \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
    print INPUT "cat(\"ks.",
            $firstKey,
            ".vs.",
            $secondKey,
            ".naive-pvalue =  \", format(ks\$ks\$p.value), \"\\n\")\n";
    print INPUT "cat(\"ks.",
            $firstKey,
            ".vs.",
            $secondKey,
            ".statistic =     \", format(ks\$ks\$statistic), \"\\n\")\n";
    print INPUT "ttest.p.value <- tryttestpvalue(",
            $firstID,
            ",",
            $secondID,
            ")\n";
    print INPUT "cat(\"t-test.",
            $firstKey,
            ".vs.",
            $secondKey,
            ".p-value =   \", format(ttest.p.value), \"\\n\")\n";
    print INPUT "cat(\"\\n\")\n";

}


