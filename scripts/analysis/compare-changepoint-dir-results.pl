#!/usr/bin/perl
use strict;
use Data::Dumper;


# ===================================================================
# Get the primary data directory
my $primaryDataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $primaryDataDir =~ m/\/$/ )
{
    $primaryDataDir .= "/";
}

# Get the primary data description
my $primaryDataDesc = shift( @ARGV );

# Get the secondary data directory
my $secondaryDataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $secondaryDataDir =~ m/\/$/ )
{
    $secondaryDataDir .= "/";
}

# Get the primary data description
my $secondaryDataDesc = shift( @ARGV );

# Get the output data directory
my $outputDataDir = shift( @ARGV );

# Ensure that it ends with a "/"
unless( $outputDataDir =~ m/\/$/ )
{
    $outputDataDir .= "/";
}

# Ensure it exists
unless( -d $outputDataDir )
{
    print "Making dir [$outputDataDir]\n";
    mkdir $outputDataDir or die "Unable to make output data dir [$outputDataDir]: $!\n";
}

my $runDesc = "# Primary directory = [$primaryDataDir]\\n# Secondary directory = [$secondaryDataDir]\n";

# ===================================================================
# Load the data
my %data;
my $primaryDataID = "primary";
my $secondaryDataID = "secondary";
loadDataResults( $primaryDataDir, $primaryDataID, \%data );
loadDataResults( $secondaryDataDir, $secondaryDataID, \%data );

#print Dumper(%data);

# ===================================================================
# Create the R input file
my $rInputFile = "/tmp/compare-changepoint-dir-results.r";
my $rOutputFile = $rInputFile.".out";
my $outputFile = $outputDataDir."compare-changepoint-dir-results.dat";
createRInput( $rInputFile,
        $outputFile,
        \%data,
        $primaryDataID,
        $secondaryDataID,
        $primaryDataDesc,
        $secondaryDataDesc,
        $runDesc );

# ===================================================================
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;



# ===================================================================
sub loadDataResults
{
    my ($dataDir, $dataID, $dataRef) = @_;

    # Iterate through each personality value
    my @personalities = ( 0.2, 0.5, 0.8 );
    foreach my $personality (@personalities)
    {
        # Iterate through each group size
        my @groupSizes = ( 20, 30, 40, 50 );
        foreach my $groupSize (@groupSizes)
        {
            # Open the changepoint results file
            my $resultsFile = $dataDir."personality-".$personality.
                    "/analysis/indcount-".sprintf("%03d", $groupSize).
                    "-dynamic-env-changepoint-results.dat";
            open( INPUT, $resultsFile ) or die "Unable to open results file [$resultsFile]: $!\n";

            # Read the file
            while( <INPUT> )
            {
                # Remove any comments or whitespace
                s/#.*//;
                s/^\s+//;
                s/\s+$//;
                next unless length;

                # Split the line up into a key and a value
                my ($key,$value) = split( /\s+=\s+/, $_ );

                # Get the environmental change index
                $key =~ /env-change\.(\d+).+/;
                my $idx = $1;

                # Is it the number of simulations needed to be bold?
                if( $key =~ m/sims-to-bold\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"sims-to-bold"} = $value;
                }

                # Is it the minimum simulations needed to be bold in a run?
                elsif( $key =~ m/sims-to-bold\.run-min\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"sims-to-bold-min"} = $value;
                }

                # Is it the number of initiations needed to be bold?
                elsif( $key =~ m/inits-to-bold\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"inits-to-bold"} = $value;
                }

                # Is it the minimum initiations needed to be bold in a run?
                elsif( $key =~ m/inits-to-bold\.run-min\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"inits-to-bold-min"} = $value;
                }

                # Is it the number of initiations needed to be bold?
                elsif( $key =~ m/inits-to-bold\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"inits-to-bold"} = $value;
                }

                # Is it the minimum initiations needed to be bold in a run?
                elsif( $key =~ m/inits-to-bold\.run-min\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"inits-to-bold-min"} = $value;
                }

                # Is it the number of bold individuals in a run?
                elsif( $key =~ m/bold-count\.values/ )
                {
                    $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{"bold-count"} = $value;
                }
            }

            # Close the input file
            close( INPUT );
        }
    }
}


# ===================================================================
sub createRInput
{
    my ($rInputFile, $resultsFile, $dataRef, $primaryDataID, $secondaryDataID, $primaryDataDesc, $secondaryDataDesc, $runDesc) = @_;

    # ---------------------------------------------------------------
    # Handy variables
    my $rSpacer = "cat(\"# =======================================================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create the file
    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print INPUT "library('Matching')\n";

    # Build a relative difference function
    print INPUT "reldiff <- function(x,y)\n";
    print INPUT "{\n";
    print INPUT "    return( abs( (x-y) / (max( abs(x), abs(y) ) ) ) )\n";
    print INPUT "}\n";
    print INPUT "\n";

    # Build a function to handle the t-test error in case the data is constant
    print INPUT "try_default <- function (expr, default = NA) {\n";
    print INPUT "  result <- default\n";
    print INPUT "  tryCatch(result <- expr, error = function(e) {})\n";
    print INPUT "  result\n";
    print INPUT "}\n";

    print INPUT "failwith <- function(default = NULL, f, ...) {\n";
    print INPUT "  function(...) try_default(f(...), default)\n";
    print INPUT "}\n";

    print INPUT "tryttest <- function(...) failwith(NA, t.test(...))\n";
    print INPUT "tryttestpvalue <- function(...) {\n";
    print INPUT "    obj<-try(t.test(...), silent=TRUE)\n";
    print INPUT "    if (is(obj, \"try-error\")) return(NA) else return(obj\$p.value)\n";
    print INPUT "}\n";


    # ---------------------------------------------------------------
    # Add the data
    my %rDataIDs;

    # Iterate through each set of data
    foreach my $dataID (sort (keys %{$dataRef}) )
    {
        # Iterate through each personality
        foreach my $personality (sort (keys %{$dataRef->{$dataID}}) )
        {
            # Iterate through each group size
            foreach my $groupSize (sort (keys %{$dataRef->{$dataID}{$personality}}) )
            {
                # Iterate through each direction change index
                foreach my $idx (sort (keys %{$dataRef->{$dataID}{$personality}{$groupSize}}) )
                {
                    # Iterate through each type of data
                    foreach my $type (sort (keys %{$dataRef->{$dataID}{$personality}{$groupSize}{$idx}}) )
                    {
                        # Sanitize the personality and type
                        my $sanitizedPersonality = $personality;
                        $sanitizedPersonality =~ s/\.//;
                        my $sanitizedType = $type;
                        $sanitizedType =~ s/-//g;

                        # Create the id
                        my $id = "$dataID$sanitizedPersonality$groupSize$idx$sanitizedType";
                        $rDataIDs{$dataID}{$personality}{$groupSize}{$idx}{$type} = $id;

                        # Add the data to the R script
                        print INPUT "$id <- scan()\n";
                        print INPUT $dataRef->{$dataID}{$personality}{$groupSize}{$idx}{$type},"\n\n";
                    }
                }
            }
        }
    }

    # ---------------------------------------------------------------
    # Start sending output to the results file
    print INPUT "sink(\"$resultsFile\")\n";

    # Note the descriptions
    print INPUT $rSpacer;
    print INPUT "cat(\"$runDesc\\n\")\n";
    print INPUT "cat(\"# Data descriptions\\n\")\n";
    print INPUT "cat(\"primary.desc = $primaryDataDesc\\n\")\n";
    print INPUT "cat(\"secondary.desc = $secondaryDataDesc\\n\")\n";
    print INPUT "cat(\"\\n\\n\")\n";


    # ---------------------------------------------------------------
    # Add the statistics

    # Iterate through each personality
    foreach my $personality (sort (keys %{$dataRef->{$primaryDataID}}) )
    {
        # Iterate through each group size
        foreach my $groupSize (sort (keys %{$dataRef->{$primaryDataID}{$personality}}) )
        {
            # Iterate through each direction change index
            foreach my $idx (sort (keys %{$dataRef->{$primaryDataID}{$personality}{$groupSize}}) )
            {
                # Iterate through each type of data
                foreach my $type (sort (keys %{$dataRef->{$primaryDataID}{$personality}{$groupSize}{$idx}}) )
                {
                    # Get the R data IDs
                    my $primaryRDataID = $rDataIDs{$primaryDataID}{$personality}{$groupSize}{$idx}{$type};
                    my $secondaryRDataID = $rDataIDs{$secondaryDataID}{$personality}{$groupSize}{$idx}{$type};

                    # Dump summary data
                    print INPUT $rSpacer;
                    print INPUT "cat(\"# Primary:  personality=[$personality]  indCount=[$groupSize] envChangeIDX=[$idx] type=[$type] summary\\n\")\n";
                    print INPUT "cat(\"primary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.mean =   \", format( mean($primaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"primary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.median = \", format( median($primaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"primary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.max =    \", format( max($primaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"primary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.min =    \", format( min($primaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"primary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.sd =     \", format( sd($primaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";

                    print INPUT "cat(\"# Secondary:  personality=[$personality]  indCount=[$groupSize] envChangeIDX=[$idx] type=[$type] summary\\n\")\n";
                    print INPUT "cat(\"secondary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.mean =   \", format( mean($secondaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"secondary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.median = \", format( median($secondaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"secondary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.max =    \", format( max($secondaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"secondary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.min =    \", format( min($secondaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"secondary.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.sd =     \", format( sd($secondaryRDataID) ), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";

                    # Use the KS test compare them
                    print INPUT "cat(\"# KS-test:  personality=[$personality]  indCount=[$groupSize] envChangeIDX=[$idx] type=[$type]\\n\")\n";
                    print INPUT "ks <- ks.boot( $primaryRDataID, $secondaryRDataID )\n";
                    print INPUT "cat(\"ks.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.p-value =         \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
                    print INPUT "cat(\"ks.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.naive-p-value =   \", format( ks\$ks\$p.value), \"\\n\")\n";
                    print INPUT "cat(\"ks.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.statistic =       \", format( ks\$ks\$statistic), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";

                    # Use the T-test compare them
                    print INPUT "cat(\"# T-test:  personality=[$personality]  indCount=[$groupSize] envChangeIDX=[$idx] type=[$type]\\n\")\n";
                    print INPUT "ttest.p.value <- tryttestpvalue( $primaryRDataID, $secondaryRDataID )\n";
                    print INPUT "cat(\"t-test.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.p-value =     \", format( ttest.p.value), \"\\n\")\n";
                    print INPUT "cat(\"\\n\")\n";

                    # Compute the relative difference
#                    print INPUT "df = data.frame( primary=$primaryRDataID, secondary=$secondaryRDataID )\n";
#                    print INPUT "successreldiff = apply( df, 1, function(x,y) reldiff(x[1],x[2]))\n";
#                    print INPUT "meansuccessreldiff = reldiff( mean($primaryRDataID), mean($secondaryRDataID) )\n";
#                    print INPUT "cat(\"# Relative difference:  personality=[$personality]  indCount=[$groupSize] envChangeIDX=[$idx] type=[$type]\\n\")\n";
#                    print INPUT "cat(\"rel-diff-of-means.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type =  \", format( meansuccessreldiff ), \"\\n\")\n";
#                    print INPUT "cat(\"relative-diff.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.mean = \", format( mean(successreldiff) ), \"\\n\")\n";
#                    print INPUT "cat(\"relative-diff.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.max =  \", format( max(successreldiff) ), \"\\n\")\n";
#                    print INPUT "cat(\"relative-diff.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.min =  \", format( min(successreldiff) ), \"\\n\")\n";
#                    print INPUT "cat(\"relative-diff.personality-$personality.indcount-$groupSize.env-change-idx-$idx.$type.sd =   \", format( sd(successreldiff) ), \"\\n\")\n";
#                    print INPUT "cat(\"\\n\\n\")\n";

                    print INPUT "cat(\"\\n\")\n";
                }
            }
        }
    }

    # ---------------------------------------------------------------
    # Stop sending output to the results file
    print INPUT "sink()\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

}

