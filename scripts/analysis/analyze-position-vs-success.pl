#!/usr/bin/perl
use strict;
use List::Util qw(sum);

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

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

    # Get the initiator ID
    $dataFile =~ /(Ind\d+)/;
    my $initiatorID = $1;

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
        elsif( $key =~/^initiation\.$initiatorID/ )
        {
            $data{$mapID}{$id}{$dataKey} = $value;
        }
    }

    close( DATA );
}

# Clean up the mean shortest paths and calculate the success percentage
foreach my $mapID (sort (keys (%data) ) )
{
    foreach my $id (sort (keys (%{$data{$mapID}}) ) )
    {
        my @shortestPaths = split( /\s+/, $data{$mapID}{$id}{"shortest-paths"} );
        @shortestPaths = grep { $_ != 0 } @shortestPaths;
        my $mean = sum( @shortestPaths ) / (scalar @shortestPaths);
        $data{$mapID}{$id}{"mean-shortest-path"} = $mean;

        $data{$mapID}{$id}{"success-percentage"} = 0.0;
        if( 0 < $data{$mapID}{$id}{"attempts" } )
        {
            $data{$mapID}{$id}{"success-percentage"} = $data{$mapID}{$id}{"successes"}
                    / $data{$mapID}{$id}{"attempts"};
        }
        else
        {
            print "No attempts for map=[$mapID} ind=[$id}\n";
        }
    }
}

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/position-vs-success.r";
my $rOutputFile = $rInputFile.".out";
my $nowString = localtime;

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

# Build a function to handle the t-test error in case the data is constant
print RINPUT "try_default <- function (expr, default = NA) {\n";
print RINPUT "  result <- default\n";
print RINPUT "  tryCatch(result <- expr, error = function(e) {})\n";
print RINPUT "  result\n";
print RINPUT "}\n";

print RINPUT "failwith <- function(default = NULL, f, ...) {\n";
print RINPUT "  function(...) try_default(f(...), default)\n";
print RINPUT "}\n";

print RINPUT "tryttest <- function(...) failwith(NA, t.test(...))\n";
print RINPUT "tryttestpvalue <- function(...) {\n";
print RINPUT "    obj<-try(t.test(...), silent=TRUE)\n";
print RINPUT "    if (is(obj, \"try-error\")) return(NA) else return(obj\$p.value)\n";
print RINPUT "}\n\n";

print RINPUT "# Date: $nowString\n\n";

# -------------------------------------------------------------------
my @correlationData = ( "mimicing-neighbor-count",
        "eigenvector-centrality",
        "closeness",
        "normalized-closeness",
        "mean-shortest-path" );
my @relevantData = @correlationData;
push( @relevantData, "success-percentage" );

my %relevantDataNames = (
        "mimicing-neighbor-count" => "Mimicing Neighbors",
        "eigenvector-centrality" => "Eigenvector Centrality",
        "closeness" => "Closeness",
        "normalized-closeness" => "Closeness (normalized)",
        "mean-shortest-path" => "Shortest Path To Initiator (mean)",
        "success-percentage" => "Initiation Success Percentage"
    );

# Build R variables
my %rData;
my %rElapsedTimeData;
my %ecToInd;
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
    print RINPUT "# Datatype [$dataType]\n";
    print RINPUT "$varID <- scan()\n";
    print RINPUT join( " ", @{$rData{$dataType}} ),"\n\n";
}


# -------------------------------------------------------------------
# Output the values
print RINPUT "sink(\"$outputFile\")\n";

print RINPUT buildRCatText( "# Data dir [$dataDir]" );
print RINPUT buildRCatText( "# Time     [$nowString]" );
print RINPUT buildRCatText( "# ===========================================" );
print RINPUT buildRCatText( "" );

# -------------------------------------------------------------------
my $successesVarName = convertToRName( "success-percentage" );
my $successesName = $relevantDataNames{ "success-percentage" };

print RINPUT buildRCatText( "" );
print RINPUT buildRCatText( "mean.success = \",format( mean( $successesVarName ) ),\"" );
print RINPUT buildRCatText( "std-dev.elapsed-time-mean = \",format( sd( $successesVarName ) ),\"" );
print RINPUT buildRCatText( "max.success = \",format( max( $successesVarName ) ),\"" );
print RINPUT buildRCatText( "min.success = \",format( min( $successesVarName ) ),\"" );
print RINPUT buildRCatText( "" );

foreach my $dataType (@correlationData)
{
    my $dataTypeName = convertToRName( $dataType );
    print RINPUT buildRCatText( "# -----------------------------------------" );
    print RINPUT buildRCatText( "# ".$relevantDataNames{$dataType}." vs $successesName" );
    print RINPUT buildRCatText( "correlation.$dataType = \", format( cor( "
            ."$dataTypeName, $successesVarName ) ),\"" );
    print RINPUT buildRCatText( "correlation.$dataType.pearson = \","
            ." format( cor( $dataTypeName, $successesVarName ), method=\"pearson\" ),\"" );
    print RINPUT buildRCatText( "correlation.$dataType.spearman = \","
            ." format( cor( $dataTypeName, $successesVarName ), method=\"spearman\" ),\"" );
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
    print RINPUT "postscript( file=\"$epsFile\", height=6, width=7, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print RINPUT "par(mar=c(5,4,2,3)+0.1 )\n";
#    print RINPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
    print RINPUT "plot( $dataTypeName, $successesVarName, col=c(\"#882255\"),\n";
    print RINPUT "    xlab=\"",$relevantDataNames{$dataType},"\", yaxt='n', ylab=\"\", las = 1,\n";
    print RINPUT "    xlim=c(min($dataTypeName),max($dataTypeName)),\n";
    print RINPUT "    ylim=c(min($successesVarName), max($successesVarName)) )\n";
    print RINPUT "mtext(\"$successesName\", side=2, line=3, las=0 )\n";
#    print RINPUT "templabels <- c(seq(min($successesVarName), max($successesVarName), 0.25)) * 100\n";
#    print RINPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
#    print RINPUT "axis( 2, las=2, at=c(seq(min($successesVarName), max($successesVarName), 0.25)), labels=percentagelabels)\n";
#    print RINPUT "axis( 4, las=2, at=c(seq(min($successesVarName), max($successesVarName), 0.25)), labels=percentagelabels)\n";
    print RINPUT "ticklocations <- axTicks(2)\n";
    print RINPUT "templabels <- ticklocations * 100\n";
    print RINPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
    print RINPUT "axis( 2, las=2, at=ticklocations, labels=percentagelabels )\n";
    print RINPUT "axis( 4, las=2, at=ticklocations, labels=percentagelabels )\n";
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

