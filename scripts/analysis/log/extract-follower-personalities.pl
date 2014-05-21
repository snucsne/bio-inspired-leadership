#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the (processed) evaluation log file
my $procLogFile = shift( @ARGV );

# Get the number of simulations ignored
my $ignoredSimCount = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# -------------------------------------------------------------------
# Gather the personality order stats
my %results;
analyzeLogData( $procLogFile, \%results );

# -------------------------------------------------------------------
# Send the results to a file
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

my $timeStr = localtime;
print OUTPUT "# Date: ",$timeStr,"\n";
print OUTPUT "# Processor: $0\n";
print OUTPUT "# Processed log file: $procLogFile\n";
print OUTPUT "# Ignored sims before bold: $ignoredSimCount\n";
print OUTPUT "# =====================================================\n\n";

# Print the simulation count
my $simCount = $results{"sim-count"};
print OUTPUT "sim-count = $simCount\n\n";

# Print the follower information
foreach my $followerIdx (sort {$a <=> $b} keys( %{$results{"personalities"}} ) )
{
    my $prefix = "follower-".sprintf( "%03d", $followerIdx );
    print OUTPUT "$prefix.personalities = ",join( " ", @{$results{"personalities"}{$followerIdx}}),"\n";
}

# ===================================================================
# Analyze the log data
sub analyzeLogData
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
