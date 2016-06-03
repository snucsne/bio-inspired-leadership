#!/usr/bin/perl
use strict;
use Data::Dumper;
use POSIX;

# -------------------------------------------------------------------
# Get the A input file
my $inputAFile = shift( @ARGV );

# Get the B input file
my $inputBFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the number of times to perform the randomized ANOVA
my $randomizedCount = shift( @ARGV );

# Get the significance percentiles
my @sigPercentiles;
foreach my $sigPercentile (@ARGV)
{
    push( @sigPercentiles, $sigPercentile );
}


# -------------------------------------------------------------------
my @aElapsedTimes;
readElapsedTimes( $inputAFile, \@aElapsedTimes );

my @bElapsedTimes;
readElapsedTimes( $inputBFile, \@bElapsedTimes );

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/elapsed-time-anova.r";
my $rOutputFile = $rInputFile.".out";

open( RINPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

print RINPUT "library(data.table)\n";
print RINPUT "\n";

# -------------------------------------------------------------------
# Create data frames for all the simulations
my %data = (
    "A" => { "times" => \@aElapsedTimes,
             "file" => $inputAFile },
    "B" => { "times" => \@bElapsedTimes,
             "file" => $inputBFile } );
foreach my $treatment (sort (keys %data) )
{
    my @elapsedTimes = @{$data{$treatment}{"times"}};
    for( my $i = 0; $i < (scalar @elapsedTimes); $i++ )
    {
        print RINPUT "# ====================================\n";
        print RINPUT "# A Elapsed times: sim[$i]\n";
        print RINPUT "time <- scan()\n";
        print RINPUT join( " ", @{$elapsedTimes[$i]} ),"\n\n";
        
        my @followers = (1..(scalar @{$elapsedTimes[$i]}) );
        print RINPUT "# Followers: sim[$i]\n";
        print RINPUT "follower <- scan()\n";
        print RINPUT join( " ", @followers ),"\n\n";

        my $dataFrameName = "df".sprintf( "%05d", $i ).$treatment;
        print RINPUT "$dataFrameName <- data.frame( follower, time )\n\n\n";
        push( @{$data{$treatment}{"dataframes"}}, $dataFrameName );
    }
}

# -------------------------------------------------------------------
# Create a list of dataframes
print RINPUT "aDataFrameList <- list(",
        join( ", ", @{$data{"A"}{"dataframes"}} ),
        ")\n";
print RINPUT "bDataFrameList <- list(",
        join( ", ", @{$data{"B"}{"dataframes"}} ),
        ")\n";
print RINPUT "allDataFrameList <- list(",
        join( ", ", @{$data{"A"}{"dataframes"}} ),
        ", ",
        join( ", ", @{$data{"B"}{"dataframes"}} ),
        ")\n";

# -------------------------------------------------------------------
# Perform the randomized ANOVA the requested number of times
my $dataFrameCount = (scalar @{$data{"A"}{"dataframes"}})
        + (scalar @{$data{"B"}{"dataframes"}});
print RINPUT "randomizedFollowerEffects <- c();\n";
print RINPUT "randomizedTreatmentEffects <- c();\n";
print RINPUT "randomizedCombinedEffects <- c();\n";
print RINPUT "for( i in 1:$randomizedCount )\n";
print RINPUT "{\n";
print RINPUT "    randomizedFrameList <- sample( allDataFrameList, $dataFrameCount, replace=FALSE )\n";
#print RINPUT "    randomizedFrameList <- allDataFrameList\n";
print RINPUT "    randomADataFrame <- rbindlist( randomizedFrameList[1:",
        ($dataFrameCount / 2),
        "] )\n";
print RINPUT "    randomADataFrame\$treatment <- 'A'\n";
print RINPUT "    randomBDataFrame <- rbindlist( randomizedFrameList[",
        (($dataFrameCount / 2) + 1),
        ":$dataFrameCount] )\n";
print RINPUT "    randomBDataFrame\$treatment <- 'B'\n";
print RINPUT "    randomDataFrame <- rbind( randomADataFrame, randomBDataFrame )\n";
print RINPUT "    randomANOVAResults <- aov( time~follower*treatment, data=randomDataFrame )\n";
print RINPUT "    summary( randomANOVAResults )\n";
print RINPUT "    followerEffect <- summary(randomANOVAResults)[[1]][[\"F value\"]][[1]]\n";
print RINPUT "    randomizedFollowerEffects <- c(randomizedFollowerEffects, followerEffect)\n";
print RINPUT "    treatmentEffect <- summary(randomANOVAResults)[[1]][[\"F value\"]][[2]]\n";
print RINPUT "    randomizedTreatmentEffects <- c(randomizedTreatmentEffects, treatmentEffect)\n";
print RINPUT "    combinedEffect <- summary(randomANOVAResults)[[1]][[\"F value\"]][[3]]\n";
print RINPUT "    randomizedCombinedEffects <- c(randomizedCombinedEffects, combinedEffect)\n";
print RINPUT "}\n";

# -------------------------------------------------------------------
# Sort the effects and find the critical F values
print RINPUT "sortedRandomizedFollowerEffects <- sort( randomizedFollowerEffects, decreasing=FALSE )\n";
print RINPUT "sortedRandomizedTreatmentEffects <- sort( randomizedTreatmentEffects, decreasing=FALSE )\n";
print RINPUT "sortedRandomizedCombinedEffects <- sort( randomizedCombinedEffects, decreasing=FALSE )\n";

print RINPUT "sortedRandomizedFollowerEffects\n";
print RINPUT "sortedRandomizedTreatmentEffects\n";
print RINPUT "sortedRandomizedCombinedEffects\n";

foreach my $sigPercentile (@sigPercentiles)
{
    my $sigPercentageIndex = ceil($randomizedCount * $sigPercentile);
    print RINPUT "randomizedFollowerEffect.$sigPercentile <- sortedRandomizedFollowerEffects[[$sigPercentageIndex]]\n";
    print RINPUT "randomizedTreatmentEffect.$sigPercentile <- sortedRandomizedTreatmentEffects[[$sigPercentageIndex]]\n";
    print RINPUT "randomizedCombinedEffect.$sigPercentile <- sortedRandomizedCombinedEffects[[$sigPercentageIndex]]\n";
}

# -------------------------------------------------------------------
# Perform the regular ANOVA
print RINPUT "baseADataFrame <- rbindlist( aDataFrameList )\n";
print RINPUT "baseADataFrame\n";
print RINPUT "baseADataFrame\$treatment <- 'A'\n";
print RINPUT "baseBDataFrame <- rbindlist( bDataFrameList )\n";
print RINPUT "baseBDataFrame\$treatment <- 'B'\n";
print RINPUT "baseDataFrame <- rbind( baseADataFrame, baseBDataFrame )\n";
print RINPUT "baseDataFrame\n";
print RINPUT "baseANOVAResults <- aov( time~follower*treatment, data=baseDataFrame )\n";
print RINPUT "summary( baseANOVAResults )\n";
print RINPUT "followerEffect <- summary(baseANOVAResults)[[1]][[\"F value\"]][[1]]\n";
print RINPUT "treatmentEffect <- summary(baseANOVAResults)[[1]][[\"F value\"]][[2]]\n";
print RINPUT "combinedEffect <- summary(baseANOVAResults)[[1]][[\"F value\"]][[3]]\n";

print RINPUT "followerEffect\n";
print RINPUT "treatmentEffect\n";
print RINPUT "combinedEffect\n";

# -------------------------------------------------------------------
# Dump the results to our output file
print RINPUT "sink(\"$outputFile\")\n";

print RINPUT buildRCatText( "# A input file     [$inputAFile]" );
print RINPUT buildRCatText( "# B input file     [$inputBFile]" );
print RINPUT buildRCatText( "# Randomized count [$randomizedCount]" );
print RINPUT buildRCatText( "# Significance     [".join( ", ", @sigPercentiles)."]" );
my $nowString = localtime;
print RINPUT buildRCatText( "# Time             [$nowString]" );
print RINPUT buildRCatText( "# ===========================================" );
print RINPUT buildRCatText( "" );


# Print the results of the base ANOVA
print RINPUT buildRCatText( "# Base two-way ANOVA" );
print RINPUT buildRCatText( "base.follower.f-value                 = \", format( followerEffect ),\"" );
print RINPUT buildRCatText( "base.treatment.f-value                = \", format( treatmentEffect ),\"" );
print RINPUT buildRCatText( "base.combined.f-value                 = \", format( combinedEffect ),\"" );
print RINPUT buildRCatText( "" );
print RINPUT buildRCatText( "" );

print RINPUT buildRCatText( "# ===========================================" );
print RINPUT buildRCatText( "# Randomized two-way ANOVA" );

# Print the results of the randomized ANOVA
foreach my $sigPercentile (@sigPercentiles)
{
    my $formattedSigPercentile = sprintf( "%04.2f", $sigPercentile );
    $formattedSigPercentile =~ s/\./-/;

    print RINPUT buildRCatText( "" );
    print RINPUT buildRCatText( "# Significance percentile [$formattedSigPercentile]" );
    print RINPUT buildRCatText( "randomized.follower.sig-$formattedSigPercentile.f-value  = \", format( randomizedFollowerEffect.".$sigPercentile." ),\"" );
    print RINPUT buildRCatText( "randomized.treatment.sig-$formattedSigPercentile.f-value = \", format( randomizedTreatmentEffect.".$sigPercentile." ),\"" );
    print RINPUT buildRCatText( "randomized.combined.sig-$formattedSigPercentile.f-value  = \", format( randomizedCombinedEffect.".$sigPercentile." ),\"" );
}
print RINPUT "sink()\n";

# -------------------------------------------------------------------
# Close the input file
close( RINPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;


# ===================================================================
sub readElapsedTimes
{
    my ($inputFile, $elapsedTimesRef) = @_;

    # Open the input file
    open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

    # Read each line
    while( <INPUT> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Only look at elapsed times
        unless( /elapsed-time/ )
        {
            next;
        }

        # Split the line into a key and value
        my ($key,$value) = split( /\s+=\s+/, $_ );

        # Get the following index
        $key =~ /elapsed-time\.0+(\d+)\./;
        my $follower = $1;
        next unless (0 < $follower);

        # Is it the mean?
        if( $key =~ /data/ )
        {
            my @times = split( /\s+/, $value );

            for( my $i = 0; $i < (scalar @times ); $i++ )
            {
                push( @{$elapsedTimesRef->[$i]}, $times[$i] );
            }
        }
    }

    # Close the file
    close( INPUT );
    
}

# ===================================================================
sub buildRCatText
{
    my ($text) = @_;
    return "cat(\"".$text."\\n\")\n";
}

