#!/usr/bin/perl
use strict;
use POSIX;

# -------------------------------------------------------------------
# Get the root data directory
my $rootDataDir = shift( @ARGV );
unless( $rootDataDir =~ /\/$/ )
{
    $rootDataDir = "$rootDataDir/";
}


# Get the data output file prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Get all the personality directories in the root data directory
opendir( DIR, $rootDataDir ) or die "Unable to open root data directory [$rootDataDir]: $!\n";
my @personalityDirs = sort( grep { /personality/ } readdir( DIR ) );
closedir( DIR );

# -------------------------------------------------------------------
# Get the emergence times for each personality directory
my %data;
my @dataSetOrder;
foreach my $personalityDir ( @personalityDirs )
{
    # Get the personality value
    $personalityDir =~ /personality-(\d\.\d+)/;
    my $personality = $1;

    # Get all the emergence times
    my $analysisDir = "$rootDataDir$personalityDir/analysis/";
    opendir( DIR, $analysisDir ) or die "Unable to open analysis directory [$analysisDir]: $!\n";
    my @emergenceTimeFiles = sort( grep { /emergence-times.dat$/ } readdir( DIR ) );
    closedir( DIR );

    # Process each one
    foreach my $emergenceTimeFile ( @emergenceTimeFiles )
    {
        # Get the individual count
        $emergenceTimeFile =~ /indcount-(\d+)-/;
        my $indcount = $1;

#        print "p=[$personality] n=[$indcount] file=[$emergenceTimeFile]\n";

        loadEmergenceTimes( $analysisDir.$emergenceTimeFile, $personality, $indcount, \%data );
#exit;
    }
}

# -------------------------------------------------------------------
# Create the R input file
my $rInputFile = "/tmp/compare-emergence-times.r";
my $rOutputFile = $rInputFile.".out";
my @epsFiles = createRInput( $rInputFile,
        $outputFilePrefix."-emergence-times-comparison.dat",
        $epsFilePrefix,
        \%data);


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
    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
}



# ===================================================================
sub loadEmergenceTimes
{
    my ($file, $personality, $indcount, $dataRef) = @_;

    # Open the file
    open( DATA, $file ) or die "Unable to open emergence times file [$file]: $!\n";

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

        # Get the emergence
        my ($trash, $emergence, $type) = split( /[-.]/, $key );
        #print "trash=[$trash] emergence=[$emergence] type=[$type]\n";

        # Store it
        $dataRef->{$personality}{$indcount}{$emergence}{$type} = $value;
    }    

    # Close the file
    close( DATA );
}

# ===================================================================
sub createRInput
{
    my ($rInputFile,
            $resultsFile,
            $epsFilePrefix,
            $dataRef ) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# ==============================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";
    print INPUT "library('igraph')\n";
    print INPUT "library('Hmisc')\n";

    # Build a standard error function
    print INPUT "stderr <- function(x) sd(x)/sqrt(length(x))\n\n";

    # Build a function to handle the t-test error in case the data is constant
    print INPUT "tryttestpvalue <- function(...) {\n";
    print INPUT "    obj<-try(t.test(...), silent=TRUE)\n";
    print INPUT "    if (is(obj, \"try-error\")) return(NA) else return(obj\$p.value)\n";
    print INPUT "}\n\n";

    # ---------------------------------------------------------------
    # Add the data
    my %dataIDs;
    my %dataDescs;
    foreach my $personality ( sort( keys %{$dataRef} ) )
    {
        foreach my $indcount ( sort( keys %{$dataRef->{$personality}} ) )
        {
            foreach my $emergence ( sort ( keys %{$dataRef->{$personality}{$indcount}} ) )
            {
                # Build the ID
                my $id = "p".$personality."n".$indcount."e".$emergence."times";
                $dataIDs{$personality}{$indcount}{$emergence} = $id;

                #Add the data
                print INPUT "# ----------------------------------------------------------\n";
                print INPUT "$id <- scan()\n";
                print INPUT $dataRef->{$personality}{$indcount}{$emergence}{"times"};
                print INPUT "\n\n";

                $dataDescs{$personality}{$indcount}{$emergence} =
                        "P=[$personality] N=[$indcount] E=[$emergence]";
            }
        }
    }

    # ---------------------------------------------------------------
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";

    # ---------------------------------------------------------------
    # Add the summary statistics
    foreach my $personality ( sort( keys %{$dataRef} ) )
    {
        my $personalityPrint = $personality;
        $personalityPrint =~ s/\.//g;

        foreach my $indcount ( sort( keys %{$dataRef->{$personality}} ) )
        {

#            print INPUT "cat(\"\\n# *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-\\n\")\n";
            print INPUT $rSpacer;

            foreach my $emergence ( sort ( keys %{$dataRef->{$personality}{$indcount}} ) )
            {
                my $prefix = "summary.personality-$personalityPrint.indcount-$indcount.emergence-$emergence";
                my $id = $dataIDs{$personality}{$indcount}{$emergence};

#                print INPUT $rSpacer;
                print INPUT "cat(\"# ".$dataDescs{$personality}{$indcount}{$emergence}." summary\\n\")\n";
                print INPUT "cat(\"$prefix.mean =   \", format( mean($id) ), \"\\n\")\n";
                print INPUT "cat(\"$prefix.median = \", format( median($id) ), \"\\n\")\n";
                print INPUT "cat(\"$prefix.max =    \", format( max($id) ), \"\\n\")\n";
                print INPUT "cat(\"$prefix.min =    \", format( min($id) ), \"\\n\")\n";
                print INPUT "cat(\"$prefix.sd =     \", format( sd($id) ), \"\\n\")\n";
                print INPUT "cat(\"$prefix.se =     \", format( stderr($id) ), \"\\n\")\n";
                print INPUT "cat(\"$prefix.length = \", format( length($id) ), \"\\n\")\n";
                print INPUT "cat(\"\\n\")\n\n";
            }
        }
    }

    # ---------------------------------------------------------------
    # Add the SIG tests for the same emergence sequence between personalities
    my @personalities = sort( keys %{$dataRef} );
    foreach my $indcount ( sort( keys %{$dataRef->{$personalities[0]}} ) )
    {
        foreach my $emergence ( sort ( keys %{$dataRef->{$personalities[0]}{$indcount}} ) )
        {
            for( my $i = 0; $i < $#personalities; $i++ )
            {
                # Ensure that it exists
                unless( exists( $dataIDs{$personalities[$i]}{$indcount}{$emergence} ) )
                {
                    next;
                }

                my $personalityIPrint = $personalities[$i];
                $personalityIPrint =~ s/\.//g;

                for( my $j = $i + 1; $j <= $#personalities; $j++ )
                {
                    # Ensure that it exists
                    unless( exists( $dataIDs{$personalities[$j]}{$indcount}{$emergence} ) )
                    {
                        next;
                    }

                    my $personalityJPrint = $personalities[$j];
                    $personalityJPrint =~ s/\.//g;

                    my $firstID = $dataIDs{$personalities[$i]}{$indcount}{$emergence};
                    my $firstDesc = $dataDescs{$personalities[$i]}{$indcount}{$emergence};
                    my $secondID = $dataIDs{$personalities[$j]}{$indcount}{$emergence};
                    my $secondDesc = $dataDescs{$personalities[$i]}{$indcount}{$emergence};

                    print INPUT $rSpacer;
                    print INPUT "cat(\"# Sig tests: $firstDesc\\n\")\n";
                    print INPUT "cat(\"#         vs $secondDesc\\n\")\n";
                    printSIGTestText( *INPUT,
                            $firstID,
                            "p$personalityIPrint-n$indcount-e$emergence",
                            $secondID,
                            "p$personalityJPrint-n$indcount-e$emergence" );
                }
            }
        }
    }

    # ---------------------------------------------------------------
    # Add the SIG tests for the same emergence sequence between group sizes
    foreach my $personality ( sort( keys %{$dataRef} ) )
    {
        my $personalityPrint = $personality;
        $personalityPrint =~ s/\.//g;

        my @indcounts = sort( keys %{$dataRef->{$personality}} );

        foreach my $emergence ( sort ( keys %{$dataRef->{$personality}{$indcounts[0]}} ) )
        {
            for( my $i = 0; $i < $#indcounts; $i++ )
            {
                # Ensure that it exists
                unless( exists( $dataIDs{$personality}{$indcounts[$i]}{$emergence} ) )
                {
                    next;
                }

                for( my $j = $i + 1; $j <= $#indcounts; $j++ )
                {
                    # Ensure that it exists
                    unless( exists( $dataIDs{$personality}{$indcounts[$j]}{$emergence} ) )
                    {
                        next;
                    }

                    my $firstID = $dataIDs{$personality}{$indcounts[$i]}{$emergence};
                    my $firstDesc = $dataDescs{$personality}{$indcounts[$i]}{$emergence};
                    my $secondID = $dataIDs{$personality}{$indcounts[$j]}{$emergence};
                    my $secondDesc = $dataDescs{$personality}{$indcounts[$j]}{$emergence};

                    print INPUT $rSpacer;
                    print INPUT "cat(\"# Sig tests: $firstDesc\\n\")\n";
                    print INPUT "cat(\"#         vs $secondDesc\\n\")\n";
                    printSIGTestText( *INPUT,
                            $firstID,
                            "p$personality-n$indcounts[$i]-e$emergence",
                            $secondID,
                            "p$personality-n$indcounts[$j]-e$emergence" );
                }
            }
        }
    }

    # ---------------------------------------------------------------
    # Add the SIG tests for subsequent emergence events
    foreach my $personality ( sort( keys %{$dataRef} ) )
    {
        foreach my $indcount ( sort( keys %{$dataRef->{$personality}} ) )
        {
            my @emergenceTimes = sort( keys %{$dataRef->{$personality}{$indcount}} );
            for( my $i = 0; $i < $#emergenceTimes; $i++ )
            {

                unless( exists( $dataIDs{$personality}{$indcount}{$emergenceTimes[$i+1]} ) )
                {
                    next;
                }

                my $firstID = $dataIDs{$personality}{$indcount}{$emergenceTimes[$i]};
                my $firstDesc = $dataDescs{$personality}{$indcount}{$emergenceTimes[$i]};
                my $secondID = $dataIDs{$personality}{$indcount}{$emergenceTimes[$i+1]};
                my $secondDesc = $dataDescs{$personality}{$indcount}{$emergenceTimes[$i+1]};

                print INPUT $rSpacer;
                print INPUT "cat(\"# Sig tests: $firstDesc\\n\")\n";
                print INPUT "cat(\"#         vs $secondDesc\\n\")\n";
                printSIGTestText( *INPUT,
                        $firstID,
                        "p$personality-n$indcount-e$emergenceTimes[$i]",
                        $secondID,
                        "p$personality-n$indcount-e".$emergenceTimes[$i+1] );
            }
        }
    }

    # ---------------------------------------------------------------
    # Stop sending output to the results file
    print INPUT "sink()\n\n\n";

    # ---------------------------------------------------------------
    # Build the figures
    my @epsFiles;

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
#return;
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

