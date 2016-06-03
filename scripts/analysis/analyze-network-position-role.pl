#!/usr/bin/perl
use strict;
use List::Util qw(sum);

# -------------------------------------------------------------------
# Get the individual level data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

my $performKSTests = 0;

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

            if( $key =~ /shortest-paths/ )
            {
                # Remove the 0 for the individual itself and recalculate the mean
                $value =~ s/(\s+0|\s+0\s+|0\s+)/ /;
                my @values = split( /\s+/, $value );
                my $sum = sum( @values ) / (scalar @values);
                $data{$mapID}{$id}{"mean-shortest-path"} = $sum;
            }
        }
    }

    close( DATA );
}

foreach my $mapID (sort (keys (%data) ) )
{
    foreach my $id (sort (keys (%{$data{$mapID}}) ) )
    {
        my @shortestPaths = split( /\s+/, $data{$mapID}{$id}{"shortest-paths"} );
#print "Paths=[",join( ", ", @shortestPaths ),"] length=[",(scalar @shortestPaths),"]\n";
        @shortestPaths = grep { $_ != 0 } @shortestPaths;
        my $mean = sum( @shortestPaths ) / (scalar @shortestPaths);
#print "Paths=[",join( ", ", @shortestPaths ),"] length=[",(scalar @shortestPaths),"]\n";
#print "\tMap=[$mapID] Ind=[$id]  Before=[",$data{$mapID}{$id}{"mean-shortest-path"},"] After=[$mean]\n";
        $data{$mapID}{$id}{"mean-shortest-path"} = $mean;
    }
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

print RINPUT "calctickspacing <- function (minValue, maxValue, maxTickCount) {\n";
print RINPUT "  valueRange = maxValue - minValue\n";
print RINPUT "  minTickSpacing = maxValue / maxTickCount\n";
print RINPUT "  magnitude = 10 ** floor( log10( minTickSpacing ) )\n";
print RINPUT "  residual = minTickSpacing / magnitude\n";
print RINPUT "  multTable = c(1, 1.5, 2, 3, 5, 7, 10)\n";
print RINPUT "  tickSpacing = which.min( abs( multTable - residual ) ) * magnitude\n";
print RINPUT "  return(tickSpacing)\n";
print RINPUT "}\n\n";




print RINPUT "# Date: $nowString\n\n";

# -------------------------------------------------------------------
my $maxTickCount = 8;
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

        # Build a separate bit of data linking eigenvector centrality and final elapsed time
        my $ec = $data{$mapID}{$id}{"eigenvector-centrality"};
        my @followers = sort (keys %{$data{$mapID}{$id}{"elapsed-time"}} );
        my $lastIndex = $followers[-1];
        $rElapsedTimeData{$ec} = $data{$mapID}{$id}{"elapsed-time"}{$lastIndex};
        $ecToInd{$ec} = "Map$mapID-$id";
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

my %ecNames;
if( $performKSTests )
{
    foreach my $ec (sort (keys %rElapsedTimeData))
    {
        my $formattedEC = sprintf( "%08.6f", $ec );
        $formattedEC =~ s/\.//;
        my $varID = "ec".$formattedEC."elapsedtimes";
        print RINPUT "# ------------------------\n";
        print RINPUT "# Elapsed times for eigenvector centrality [$ec]\n";
        print RINPUT "$varID <- scan()\n";
        print RINPUT join( " ", $rElapsedTimeData{$ec} ),"\n\n";
    
        $ecNames{$varID} = $ec;
    }
}


# -------------------------------------------------------------------
# Output the values
print RINPUT "sink(\"$outputFile\")\n";

print RINPUT buildRCatText( "# Data dir [$dataDir]" );
print RINPUT buildRCatText( "# Time     [$nowString]" );
print RINPUT buildRCatText( "# ===========================================" );
print RINPUT buildRCatText( "" );

# -------------------------------------------------------------------
my $finalElapsedTimeVarName = convertToRName( "final-elapsed-time-mean" );
my $finalElapsedTimeName = $relevantDataNames{ "final-elapsed-time-mean" };

print RINPUT buildRCatText( "" );
print RINPUT buildRCatText( "mean.elapsed-time-mean = \",format( mean( $finalElapsedTimeVarName ) ),\"" );
print RINPUT buildRCatText( "std-dev.elapsed-time-mean = \",format( sd( $finalElapsedTimeVarName ) ),\"" );
print RINPUT buildRCatText( "max.elapsed-time-mean = \",format( max( $finalElapsedTimeVarName ) ),\"" );
print RINPUT buildRCatText( "min.elapsed-time-mean = \",format( min( $finalElapsedTimeVarName ) ),\"" );
print RINPUT buildRCatText( "" );

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
if( $performKSTests )
{
    # Add the KS tests
    my @ecNamesArray = (sort (keys %ecNames) );
    for( my $i = 0; $i < (scalar @ecNamesArray); $i++ )
    {
        for( my $j = $i + 1; $j < (scalar @ecNamesArray); $j++ )
        {
            my $first = $ecNamesArray[$i];
            my $firstName = $ecNames{$first};
            $firstName =~ s/\./-/;
            my $second = $ecNamesArray[$j];
            my $secondName = $ecNames{$second};
            $secondName =~ s/\./-/;

            print RINPUT buildRCatText( "# -----------------------------------------" );
            print RINPUT buildRCatText( "# KS: ".$ecToInd{$first}."(ec=".$firstName.") vs ".$ecToInd{$first}."(ec=".$secondName.")" );
            print RINPUT "cat(\"elapsed-time.",
                    $firstName,
                    ".mean = \", format( mean( ".$first.") ), \"\\n\")\n";
            print RINPUT "cat(\"elapsed-time.",
                    $firstName,
                    ".stdev = \", format( sd( ".$first.") ), \"\\n\")\n";
            print RINPUT "cat(\"elapsed-time.",
                    $secondName,
                    ".mean = \", format( mean( ".$second.") ), \"\\n\")\n";
            print RINPUT "cat(\"elapsed-time.",
                    $secondName,
                    ".stdev = \", format( sd( ".$second.") ), \"\\n\")\n";
            print RINPUT "ks <- ks.boot( ",
                    $first,
                    ",",
                    $second,
                    " )\n";
            print RINPUT "cat(\"ks.",
                    $firstName,
                    ".vs.",
                    $secondName,
                    ".p-value = \", format( ks\$ks.boot.pvalue), \"\\n\")\n";
            print RINPUT "cat(\"ks.",
                    $firstName,
                    ".vs.",
                    $secondName,
                    ".naive-pvalue = \", format(ks\$ks\$p.value), \"\\n\")\n";
            print RINPUT "cat(\"ks.",
                    $firstName,
                    ".vs.",
                    $secondName,
                    ".statistic = \", format(ks\$ks\$statistic), \"\\n\")\n";
            print RINPUT "ttest.p.value <- tryttestpvalue(",
                    $first,
                    ",",
                    $second,
                    ")\n";
            print RINPUT "cat(\"t-test.",
                    $firstName,
                    ".vs.",
                    $secondName,
                    ".p-value =   \", format(ttest.p.value), \"\\n\")\n";
            print RINPUT buildRCatText( "" );
        }
    }
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
    print RINPUT "yTickSpacing = calctickspacing( min($finalElapsedTimeVarName), max($finalElapsedTimeVarName), $maxTickCount )\n";
    print RINPUT "yTickSpacing\n";
    print RINPUT "minYTickValue = min($finalElapsedTimeVarName) - (min($finalElapsedTimeVarName) %% yTickSpacing)\n";
    print RINPUT "maxYTickValue = max($finalElapsedTimeVarName) - (max($finalElapsedTimeVarName) %% yTickSpacing) + yTickSpacing\n";
#    print RINPUT "c(min($finalElapsedTimeVarName), max($finalElapsedTimeVarName))\n";
#    print RINPUT "c(minTickValue, maxTickValue)\n";
    print RINPUT "yTickCount = (maxYTickValue - minYTickValue) / yTickSpacing + 1\n";
#    print RINPUT "yTickCount\n";
#    print RINPUT "axisTicks( c(minTickValue, maxTickValue), log=FALSE, n = tickCount )\n\n\n";
    print RINPUT "xTickSequence = pretty( c(min($dataTypeName), max($dataTypeName)) )\n";
    print RINPUT "c( min($dataTypeName), max($dataTypeName) )\n";
    print RINPUT "xTickSequence\n";
    print RINPUT "yTickSequence = pretty( c(min($finalElapsedTimeVarName), max($finalElapsedTimeVarName)) )\n";
    print RINPUT "c( min($finalElapsedTimeVarName), max($finalElapsedTimeVarName) )\n";
    print RINPUT "c( min($finalElapsedTimeVarName), max($finalElapsedTimeVarName) )\n";
    print RINPUT "yTickSequence\n";
    print RINPUT "postscript( file=\"$epsFile\", height=5.5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print RINPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
    print RINPUT "plot( $dataTypeName, $finalElapsedTimeVarName, col=c(\"#882255\"),\n";
    print RINPUT "    xlab=\"",$relevantDataNames{$dataType},"\", ylab=\"\", las = 1,\n";
#    print RINPUT "    xlim=c(min($dataTypeName),max($dataTypeName)),\n";
    print RINPUT "    xlim=c(min(xTickSequence),max(xTickSequence)), xaxt='n',\n";
#    print RINPUT "    ylim=c(min($finalElapsedTimeVarName), max($finalElapsedTimeVarName)) )\n";
#    print RINPUT "    ylim=c(minTickValue, maxTickValue), yaxt='n',\n";
    print RINPUT "    ylim=c(min(yTickSequence), max(yTickSequence)), yaxt='n',\n";
#    print RINPUT "    panel.first = abline( h=seq( minTickValue, maxTickValue, by=tickSpacing), col=\"gray\", lwd=0.5) )\n";
    print RINPUT "    panel.first = abline( h=yTickSequence, col=\"gray\", lwd=0.5) )\n";
    print RINPUT "mtext(\"$finalElapsedTimeName\", side=2, line=4, las=0 )\n";
#    print RINPUT "axis(2, las=2, at=axisTicks( c(minTickValue, maxTickValue), log=FALSE, n = tickCount ))\n";
    print RINPUT "axis(1, las=0, at=xTickSequence )\n";
    print RINPUT "axis(2, las=2, at=yTickSequence )\n";
#    print RINPUT "axis(2, las=2, at=seq( minTickValue, maxTickValue, by=tickSpacing) )\n";
#    print RINPUT "axis(4, las=2, at=seq( minTickValue, maxTickValue, by=tickSpacing) )\n";
#    print RINPUT "abline( h=seq( minTickValue, maxTickValue, by=tickSpacing), col=\"gray\", lwd=0.5)\n";
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
