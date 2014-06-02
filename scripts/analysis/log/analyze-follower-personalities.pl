#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the (processed) evaluation log file
my $procLogFile = shift( @ARGV );

# Get the number of simulations ignored
my $ignoredSimCount = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps output file
my $epsOutputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Gather the personality order stats
my %results;
extractLogData( $procLogFile, \%results );

# -------------------------------------------------------------------
# Process it with R
my $rInputFile = "/tmp/follower-personalities.r";
my $rOutputFile = $rInputFile.".out";
analyzePersonalities( $rInputFile, $outputFile, $epsOutputFile, \%results );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;



# ===================================================================
# Extract the log data
sub extractLogData
{
    my ($procLogFile, $dataRef) = @_;

    # Count the number of simulations analyzed
    my $simCount = 0;

    # Open the file
    open( DATA, "$procLogFile" ) or die "Unable to open processed log file [$procLogFile]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        if( $key =~ /personalities/ )
        {
            # Get the individual personalities
            my @personalities = split( /\s+/, $value );

            # Process all the personalities
            my $idx = 0;
            foreach my $personality (@personalities)
            {
                push( @{$dataRef->{"personalities"}{$idx}}, $personality );
                $idx++;
            }

            $simCount++;
        }
    }

    $dataRef->{"sim-count"} = $simCount;
}

# ===================================================================
# Analyze the follower personalities
sub analyzePersonalities
{
    my ($rInputFile, $outputFile, $epsOutputFile, $dataRef) = @_;

    # ---------------------------------------------------------------
    # Send the results to a file
    open( INPUT, "> $rInputFile" ) or die "Unable to open r input file [$rInputFile]: $!\n";

    # Dump some information (just in case)
    my $timeStr = localtime;
    print INPUT "# Date: ",$timeStr,"\n";
    print INPUT "# Processor: $0\n";
    print INPUT "# Processed log file: $procLogFile\n";
    print INPUT "# Ignored sims before bold: $ignoredSimCount\n";
    my $simCount = $results{"sim-count"};
    print INPUT "# sim-count = $simCount\n\n";
    print INPUT "# =====================================================\n\n";

    # Print the follower information
    my @ids;
    my @labels = ("\"Init\"");
    foreach my $followerIdx (sort {$a <=> $b} keys( %{$results{"personalities"}} ) )
    {
        if( $followerIdx > 0 )
        {
            push( @labels, "\"$followerIdx\"" );
        }
        my $id = "follower".sprintf( "%03d", $followerIdx );
        push( @ids, $id );
        print INPUT "$id <- scan()\n",join( " ", @{$results{"personalities"}{$followerIdx}}),"\n\n";
    }

    # ---------------------------------------------------------------
    # Dump some statistics
    my $rCatSpacer = "cat(\"# =====================================\\n\")\n";

    # Output statistics file
    print INPUT "sink(\"$outputFile\")\n";

    foreach my $id (@ids)
    {
        print INPUT $rCatSpacer;
        print INPUT "cat(\"summary.$id.mean =   \", format( mean($id) ), \"\\n\")\n";
        print INPUT "cat(\"summary.$id.median = \", format( median($id) ), \"\\n\")\n";
        print INPUT "cat(\"summary.$id.max =    \", format( max($id) ), \"\\n\")\n";
        print INPUT "cat(\"summary.$id.min =    \", format( min($id) ), \"\\n\")\n";
        print INPUT "cat(\"summary.$id.sd =     \", format( sd($id) ), \"\\n\")\n";
        print INPUT "cat(\"summary.$id.count =  \", format( length($id) ), \"\\n\")\n";
        print INPUT "cat(\"\\n\")\n";
    }

    print INPUT "sink()\n";

    # ---------------------------------------------------------------
    # Print a boxplot
    print INPUT "postscript( file = \"$epsOutputFile\", height=4, width=9, onefile=FALSE, pointsize=11, horizontal=FALSE, paper=\"special\" )\n";

    print INPUT "boxplot( ",
            join( ",", @ids ),
            ", names=c(",
            join( ",", @labels ),
            "), ylim=c(0,1), outline=FALSE )\n";

    print INPUT "dev.off()\n\n\n";

    # ---------------------------------------------------------------
    # Close the input file
    close( INPUT );

}
