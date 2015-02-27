#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the individual level data directory
my $dataDir = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
my @dataFiles;

# Grab the map directories
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @mapDirs = grep { /map/ } readdir( DIR );
closedir( DIR );

# Grab the data files in each map directory
foreach my $mapDir (@mapDirs)
{
    opendir( DIR, "$dataDir/$mapDir" ) or die "Unable to open map dir [$dataDir/$mapDir]: $!\n";
    my @tempDataFiles = grep { /\.dat$/ } readdir( DIR );
    closedir( DIR );

    foreach my $dataFile (@tempDataFiles)
    {
        push( @dataFiles, "$dataDir$mapDir/$dataFile" );
    }
}

@dataFiles = sort @dataFiles;

print "Found ",(scalar @dataFiles)," files\n";

# -------------------------------------------------------------------
# Read the data files
my %data;
foreach my $dataFile (@dataFiles)
{
    # Get the map ID
    $dataFile =~ /map-(\d+)/;
    my $mapID = "Map".$1;
    if( exists $data{$mapID} )
    {
        # Only need to reach a data file once for each map
        next;
    }
    
    # Open the file
    open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        my ($trash, $id, $dataKey) = split( /\./, $key );
        if( $key =~ /^individual\./ )
        {
            $data{$mapID}{$id}{$dataKey} = $value;
        }
    }

    close( DATA );
}

# -------------------------------------------------------------------
# Grab data from the processed logs
foreach my $dataFile (@dataFiles)
{
    # Build the processed log file name
    my $processedLogFile = $dataFile;
    $processedLogFile =~ s/\.dat/-processed-log.stat/;

    # Get the map ID
    $dataFile =~ /map-(\d+)/;
    my $mapID = "Map".$1;

    unless( -e $processedLogFile )
    {
        # Try to find out who the initiator was
        $dataFile =~ /initiator-(Ind\d+)/;
        my $initiatorID = $1;

        # Remove all their data
        undef $data{$mapID}{$initiatorID};

        next;
    }

    my $initiatorID;

    # Open the processed log file
    open( LOG, "$processedLogFile" ) or die "Unable to open processed log file [$processedLogFile]: $!\n";

    # Read each line
    while( <LOG> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        # Is this our initiator?
        if( $key =~ /frequency.*initiate/ && ($value == 1.0) )
        {
            # Yup
            $key =~ /frequency\.(.*)\.initiate/;
            $initiatorID = $1;
#            print " InitiatorID=[$initiatorID]\n\n";
        }
        elsif( $key =~ /^time\.(\d+)\.data/ )
        {
            if( 0 < $1 )
            {
                $data{$mapID}{$initiatorID}{"time"}{$1} = $value;
            }
        }
        elsif( $key =~ /^time\.(\d+)\.mean/ )
        {
            if( 0 < $1 )
            {
                $data{$mapID}{$initiatorID}{"time-mean"}{$1} = $value;
                $data{$mapID}{$initiatorID}{"final-time-mean"} = $value;
            }
        }
        elsif( $key =~ /^elapsed-time\.(\d+)\.data/ )
        {
            if( 0 < $1 )
            {
                $data{$mapID}{$initiatorID}{"elapsed-time"}{$1} = $value;
            }
        }
        elsif( $key =~ /^elapsed-time\.(\d+)\.mean/ )
        {
            if( 0 < $1 )
            {
                $data{$mapID}{$initiatorID}{"elapsed-time-mean"}{$1} = $value;
                $data{$mapID}{$initiatorID}{"final-elapsed-time-mean"} = $value;
            }
        }
        elsif( $key =~ /^max-depth\.data/ )
        {
            $data{$mapID}{$initiatorID}{"max-depth"} = $value;
        }
        elsif( $key =~ /^max-depth\.mean/ )
        {
            $data{$mapID}{$initiatorID}{"max-depth-mean"} = $value;
        }
    }

    close( LOG );
}

# -------------------------------------------------------------------
# Dump out some information
#foreach my $mapID (sort (keys (%data) ) )
#{
#    foreach my $id (sort (keys (%{$data{$mapID}}) ) )
#    {
#        print "Map=[$mapID] Ind=[$id]\n";
#        print "\tMimics=[",$data{$mapID}{$id}{"mimicing-neighbor-count"},"]\n";
#        print "\tEigenvector centrality=[",$data{$mapID}{$id}{"eigenvector-centrality"},"]\n";
#        print "\tCloseness=[",$data{$mapID}{$id}{"closeness"},"]\n";
#        print "\tNormalized closeness=[",$data{$mapID}{$id}{"normalized-closeness"},"]\n";
#        print "\tMean shortest path=[",$data{$mapID}{$id}{"mean-shortest-path"},"]\n";
#        print "\tMean max depth=[",$data{$mapID}{$id}{"max-depth-mean"},"]\n";
#
#        my @times;
#        foreach my $follower (sort (keys (%{$data{$mapID}{$id}{"time-mean"}}) ) )
#        {
#            push( @times, $data{$mapID}{$id}{"time-mean"}{$follower} );
#        }
#        print "\tFollower times=[",join( " ", @times ),"]\n\n";
#    }
#}


# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/network-position.r";
my $rOutputFile = "/tmp/network-position.r.out";

open( RINPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

print RINPUT "library(extrafont)\n";
print RINPUT "loadfonts(device = \"postscript\")\n";

print RINPUT "library('Matching')\n";
print RINPUT "library('igraph')\n";

print RINPUT "library(ggplot2)\n";
print RINPUT "gg_color_hue <- function(n) {\n";
print RINPUT "  hues = seq(15, 375, length=n+1)\n";
print RINPUT "  hcl(h=hues, l=65, c=100)[1:n]\n";
print RINPUT "}\n\n";


# -------------------------------------------------------------------
my @correlationData = ( "mimicing-neighbor-count",
        "eigenvector-centrality",
        "closeness",
        "normalized-closeness",
        "mean-shortest-path",
        "max-depth-mean" );
my @relevantData = @correlationData;
push( @relevantData, "final-elapsed-time-mean" );

my %relevantDataNames = (
        "mimicing-neighbor-count" => "Mimicing Neighbors",
        "eigenvector-centrality" => "Eigenvector Centrality",
        "closeness" => "Closeness",
        "normalized-closeness" => "Closeness (normalized)",
        "mean-shortest-path" => "Shortest Path To Initiator (mean)",
        "max-depth-mean" => "Max Depth of Follower Hierarchy (mean)",
        "final-elapsed-time-mean" => "Elapsed Time to Consensus (mean)"
    );

# Build R variables
my %rData;
foreach my $mapID (sort (keys (%data) ) )
{
    foreach my $id (sort (keys (%{$data{$mapID}}) ) )
    {
        foreach my $dataType (@relevantData)
        {
            push( @{$rData{$dataType}}, $data{$mapID}{$id}{$dataType} );
        }
    }
}

foreach my $dataType (@relevantData)
{
    my $varID = convertToRName( $dataType );
    print RINPUT "# ------------------------\n";
    print RINPUT "$varID <- scan()\n";
    print RINPUT join( " ", @{$rData{$dataType}} ),"\n\n";
}


# -------------------------------------------------------------------
# Output the values
print RINPUT "sink(\"$outputFile\")\n";

print RINPUT buildRCatText( "# Data dir [$dataDir]" );
my $nowString = localtime;
print RINPUT buildRCatText( "# Time     [$nowString]" );
print RINPUT buildRCatText( "# ===========================================" );
print RINPUT buildRCatText( "" );

# -------------------------------------------------------------------
my $finalElapsedTimeVarName = convertToRName( "final-elapsed-time-mean" );
my $finalElapsedTimeName = $relevantDataNames{ "final-elapsed-time-mean" };

foreach my $dataType (@correlationData)
{
    my $dataTypeName = convertToRName( $dataType );
    print RINPUT buildRCatText( "# -----------------------------------------" );
    print RINPUT buildRCatText( "# ".$relevantDataNames{$dataType}." vs $finalElapsedTimeName" );
    print RINPUT buildRCatText( "correlation.$dataType = \", format( cor( "
            ."$dataTypeName, $finalElapsedTimeVarName ) ),\"" );
    print RINPUT buildRCatText( "correlation.$dataType.pearson = \","
            ." format( cor( $dataTypeName, $finalElapsedTimeVarName ), method=\"pearson\" ),\"" );
    print RINPUT buildRCatText( "correlation.$dataType.spearman = \","
            ." format( cor( $dataTypeName, $finalElapsedTimeVarName ), method=\"spearman\" ),\"" );
    print RINPUT buildRCatText( "" );
}

# -------------------------------------------------------------------
print RINPUT "sink()\n";

# -------------------------------------------------------------------
# Create plots
my @epsFiles;
foreach my $dataType (@correlationData)
{
    my $epsFile = $epsFilePrefix.$dataType.".eps";
    push( @epsFiles, $epsFile );

    my $dataTypeName = convertToRName( $dataType );
    print RINPUT "postscript( file=\"$epsFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print RINPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
    print RINPUT "plot( $dataTypeName, $finalElapsedTimeVarName, col=c(\"#882255\"),\n";
    print RINPUT "    xlab=\"",$relevantDataNames{$dataType},"\", ylab=\"\", las = 1,\n";
    print RINPUT "    xlim=c(min($dataTypeName),max($dataTypeName)),\n";
    print RINPUT "    ylim=c(min($finalElapsedTimeVarName), max($finalElapsedTimeVarName)) )\n";
    print RINPUT "mtext(\"$finalElapsedTimeName\", side=2, line=4, las=0 )\n";
    print RINPUT "dev.off()\n\n";
}

# -------------------------------------------------------------------
# Close the input file
close( RINPUT );

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
    `/usr/bin/epstool --copy --bbox $tmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
}



# ===================================================================
sub convertToRName
{
    my ($text) = @_;

    $text =~ s/-(\w)/\U$1/g;
    $text =~ s/^(\w)/\U$1/g;

    return $text;
}

# ===================================================================
sub buildRCatText
{
    my ($text) = @_;
    return "cat(\"".$text."\\n\")\n";
}
