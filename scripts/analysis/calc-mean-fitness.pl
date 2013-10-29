#!/usr/bin/perl
use strict;
use Statistics::Descriptive;


# -------------------------------------------------------------------
# Define the output headers
my $genHeader =  "# 1: Generation \n".
        "# 2: Mean best gen fitness\n".
        "# 3: Mean best gen fitness standard deviation\n".
        "# 4: Mean best so far fitness\n".
        "# 5: Mean best so far fitness standard deviation\n".
        "# 6: Mean best so far generation\n".
        "# 7: Mean best so far generation standard deviation\n";

my $bestHeader = "# 1: Mean best fitness\n".
        "# 2: Mean best fitness standard deviation\n".
        "# 3: Mean best generation\n".
        "# 4: Mean best generation standard deviation\n";


# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
unless( $dataDir =~ /\/$/ )
{
    $dataDir = "$dataDir/";
}
print "Data directory [$dataDir]\n";

# Get all the stats files
my $statsKeyword = shift( @ARGV );
opendir( DIR, $dataDir ) or die "Unable to open data dir [$dataDir]: $!\n";
my @statsFiles = grep { /.*$statsKeyword.*\.stats$/ } readdir ( DIR );
closedir( DIR );

print "Found ",($#statsFiles + 1)," stats files\n";


# -------------------------------------------------------------------
my $fullGenFile = $dataDir."full-generational-data.csv";
open( FULLGEN, "> $fullGenFile" ) or die "Unable to open full generational data [$fullGenFile]: $!\n";


# -------------------------------------------------------------------
my %genValues;
my %bestValues;
my $statsFile;
foreach $statsFile (@statsFiles)
{
    my $file = "$dataDir$statsFile";
    print "Stats file [$file]\n";

    my $genOutput;

    # Open the file
    open( STATS, "$file" ) or die "Unable to open stats file [$file]: $!\n";

    # Read each line
    while( <STATS> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        if( $key =~ /gen\[(\d+)\].*best-individual.fitness/ )
        {
            push( @{$genValues{$1}{'gen-fitness'}}, $value );
        }
        elsif( $key =~ /gen\[(\d+)\].*best-individual-found-so-far.fitness/ )
        {
            push( @{$genValues{$1}{'so-far-fitness'}}, $value );
            $genOutput .= sprintf( "% 5.3f, ", $value );
        }
        elsif( $key =~ /gen\[(\d+)\].*best-individual-found-so-far.generation/ )
        {
            push( @{$genValues{$1}{'so-far-generation'}}, $value );
        }
        elsif( $key =~ /best-individual-found\.fitness/ )
        {
            push( @{$bestValues{'fitness'}}, $value );
        }
        elsif( $key =~ /best-individual-found\.generation/ )
        {
            push( @{$bestValues{'generation'}}, $value );
        }

    }

    # Close the file
    close( DATA );

    # Dump the generational fitness values
    chop( $genOutput );
    chop( $genOutput );
    if( 0 == length( $genOutput ) )
    {
        print STDERR "ERROR: No output found for file [$file]\n";
        exit;
    }
    print FULLGEN $genOutput;
    print FULLGEN "\n";

}

close( FULLGEN );


# -------------------------------------------------------------------
# Calculate the generational stats
my $genFile = $dataDir."generational-analysis.stats";

open( GENSTATS, "> $genFile" ) or die "Unable to open generational analysis [$genFile]: $!\n";
print GENSTATS $genHeader;

# Iterate through all the generations
foreach my $gen (sort {$a <=> $b } keys %genValues)
{
    # Build the stats for this generation
    my $genFitnessStats = Statistics::Descriptive::Full->new();
    $genFitnessStats->add_data( @{$genValues{$gen}{'gen-fitness'}} );
    my $soFarFitnessStats = Statistics::Descriptive::Full->new();
    $soFarFitnessStats->add_data( @{$genValues{$gen}{'so-far-fitness'}} );
    my $soFarGenStats = Statistics::Descriptive::Full->new();
    $soFarGenStats->add_data( @{$genValues{$gen}{'so-far-generation'}} );

    # Print them
    printf( GENSTATS
            "%04d        %6.4f    %7.4f          %6.4f    %7.4f        %7.2f    %7.2f\n",
            $gen,
            $genFitnessStats->mean(),
            $genFitnessStats->standard_deviation(),
            $soFarFitnessStats->mean(),
            $soFarFitnessStats->standard_deviation(),
            $soFarGenStats->mean(),
            $soFarGenStats->standard_deviation() );
}
close( GENSTATS );


# -------------------------------------------------------------------
# Calculate the overall stats
my $bestFile = $dataDir."overall-analysis.stats";

open( BESTSTATS, "> $bestFile" ) or die "Unable to open overall best analysis [$bestFile]: $!\n";
print BESTSTATS $bestHeader;

my $bestFitnessStats = Statistics::Descriptive::Full->new();
$bestFitnessStats->add_data( @{$bestValues{'fitness'}} );
my $bestGenStats = Statistics::Descriptive::Full->new();
$bestGenStats->add_data( @{$bestValues{'generation'}} );

printf( BESTSTATS
        "%6.4f    %7.4f        %7.2f    %7.2f\n",
        $bestFitnessStats->mean(),
        $bestFitnessStats->standard_deviation(),
        $bestGenStats->mean(),
        $bestGenStats->standard_deviation() );

close( GENSTATS );


# -------------------------------------------------------------------
# Dump the raw overall fitness values
my $bestRawFile = $dataDir."overall-raw-fitness.data";

open( BESTRAW, "> $bestRawFile" ) or die "Unable to open overall best raw [$bestRawFile]: $!\n";
foreach my $value ( @{$bestValues{'fitness'}} )
{
    printf( BESTRAW "%6.4f    ", $value );
}
close( BESTRAW );

# -------------------------------------------------------------------
# Dump the raw overall generation values
$bestRawFile = $dataDir."overall-raw-generation.data";

open( BESTRAW, "> $bestRawFile" ) or die "Unable to open overall best raw [$bestRawFile]: $!\n";
foreach my $value ( @{$bestValues{'generation'}} )
{
    printf( BESTRAW "%6.4f    ", $value );
}
close( BESTRAW );


