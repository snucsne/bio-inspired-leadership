#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

print "Found ",($#dataFiles + 1)," files\n";

# -------------------------------------------------------------------
my %statData = (
    "attempts" => { "key" => "attempts",
            "r-data" => "attempts",
            "title" => "Attempts"},
    "successes" => { "key" => "successes",
            "r-data" => "successes",
            "title" => "Successes"},
    "attempt-percentage" => { "key" => "attempt-percentage",
            "r-data" => "attemptpercentage",
            "title" => "Attempt Percentage"},
    "success-percentage" => { "key" => "success-percentage",
            "r-data" => "successpercentage",
            "title" => "Leadership"},
#    "failed-followers-mean" => { "key" => "failed-followers-mean",
#            "r-data" => "failedfollowersmean",
#            "title" => "Failed Followers Mean"},
    "eigenvector-centrality" => { "key" => "eigenvector-centrality",
            "r-data" => "eigenvectorcentrality",
            "title" => "Eigenvector Centrality"},
    "betweenness" => { "key" =>"betweenness",
            "r-data" => "betweenness",
            "title" => "Betweenness"},
    "mimicing-neighbor-count" => { "key" => "mimicing-neighbor-count",
            "r-data" => "mimicingneighborcount",
            "title" => "Mimicking Neighbor Count"},
    "personality" => { "key" => "personality",
            "r-data" => "personality",
            "title" => "Personality"},
    "assertiveness" => { "key" => "assertiveness",
            "r-data" => "assertiveness",
            "title" => "Assertiveness"},
    "preferred-direction" => { "key" => "preferred-direction",
            "r-data" => "preferreddir",
            "title" => "Preferred direction"},
    "conflict" => { "key" => "conflict",
            "r-data" => "conflict",
            "title" => "conflict"},
    "degree" => { "key" => "degree",
            "r-data" => "degree",
            "title" => "Degree"},
    "pagerank" => { "key" => "pagerank",
            "r-data" => "pagerank",
            "title" => "Pagerank"},
    "kleinberg" => { "key" => "kleinberg",
            "r-data" => "kleinberg",
            "title" => "Kleinberg"},
    "out-degree" => { "key" => "out-degree",
            "r-data" => "outdegree",
            "title" => "Out Degree"},
    "in-degree" => { "key" => "in-degree",
            "r-data" => "indegree",
            "title" => "In Degree"},
    );


my @corrData = (
    [ "eigenvector-centrality", "success-percentage" ],
    [ "betweenness", "success-percentage" ],
    [ "mimicing-neighbor-count", "success-percentage" ],
    [ "personality", "success-percentage" ],
    [ "assertiveness", "success-percentage" ],
    [ "pagerank", "success-percentage" ],
    [ "kleinberg", "success-percentage" ],
    [ "out-degree", "success-percentage" ],
    [ "in-degree", "success-percentage" ],
    [ "assertiveness", "success-percentage" ],
    [ "preferred-direction", "success-percentage" ],
    [ "conflict", "success-percentage" ],
    [ "eigenvector-centrality", "mimicing-neighbor-count" ],
    [ "betweenness", "mimicing-neighbor-count" ],
    [ "pagerank", "mimicing-neighbor-count" ],
    [ "kleinberg", "mimicing-neighbor-count" ],
    [ "out-degree", "mimicing-neighbor-count" ],
    [ "in-degree", "mimicing-neighbor-count" ],
    [ "out-degree", "mimicing-neighbor-count" ],
    [ "eigenvector-centrality", "personality" ],
    [ "mimicing-neighbor-count", "personality" ],
#    [ "failed-followers-mean", "personality" ],
#    [ "eigenvector-centrality", "failed-followers-mean" ],
    );

# -------------------------------------------------------------------
# Read the data files
my %data;
foreach my $dataFile (@dataFiles)
{
    # Open the file
    open( DATA, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataDir/$dataFile]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

        if( $key =~ /^move/ )
        {
            my ($trash, $id) = split( /\./, $key );
            if( 0 < $id )
            {
                $data{"move"}[$id] += $value;
                $data{"move-total"} += $value
            }
        }
        elsif( $key =~ /^final-initiators/ )
        {
            my ($trash, $id) = split( /\./, $key );
            if( 0 < $id )
            {
                $data{"final-initiators"}[$id] += $value;
                $data{"final-initiators-total"} += $value
            }
        }
        elsif( $key =~ /^individual.Ind.*\.(.+)/ )
        {
            if( exists( $statData{$1} ) )
            {
                my $dataKey = $statData{$1}{"key"};
                $data{$dataKey} .= " ".$value;
            }
        }
        elsif( $key =~ /^initiation.Ind.*\.(.+)/ )
        {
            if( exists( $statData{$1} ) )
            {
                my $dataKey = $statData{$1}{"key"};
                $data{$dataKey} .= " ".$value;
            }
        }
        elsif( $key =~ /^total-leadership-success/ )
        {
            $data{"success"} .= "  ".$value;
        }
    }

    close( DATA );
}


# -------------------------------------------------------------------
# Save the success percentage mean
my $indCount = $#{$data{"move"}};
my $successPercentage = $data{"move"}[$indCount] / $data{"move-total"};
my $successFilename = $outputFile;
$successFilename =~ s/\.stats/-success-percentage.dat/;
open( SUCCESS, "> $successFilename" ) or die "Unable to open success file [$successFilename]: $!\n";
print SUCCESS sprintf( "%04d   %06.4f\n", $indCount, $successPercentage );
close( SUCCESS );

# -------------------------------------------------------------------
# Save all the success percentages
$successFilename = $outputFile;
$successFilename =~ s/\.stats/-all-success-percentages.dat/;
open( SUCCESS, "> $successFilename" ) or die "Unable to open success file [$successFilename]: $!\n";
print SUCCESS $data{"success"};
close( SUCCESS );

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/spatial-hidden-var.r";
my $rOutputFile = "/tmp/spatial-hidden-var.r.out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print INPUT "library('Matching')\n";
print INPUT "library('igraph')\n";
print INPUT "outliercolors=c(rep(\"#666666\",1530),rep(\"#EEA500\",85),rep(\"#666666\",935))\n";
print INPUT "outliercolors=c(rep(\"#666666\",1530),rep(\"#3333FF\",85),rep(\"#666666\",935))\n";
print INPUT "\n\n";

print INPUT "# dataDir=[$dataDir]\n";
print INPUT "# outputFile=[$outputFile]\n";
print INPUT "# epsFilePrefix=[$epsFilePrefix]\n";


# -------------------------------------------------------------------
# Add all the necessary data
foreach my $statType (sort keys( %statData ) )
{
    my $statKey = $statData{$statType}{"key"};
    if( $data{$statKey} )
    {
        print INPUT "# ",$statData{$statType}{"title"},"\n";
        print INPUT $statData{$statType}{"r-data"}," <- scan()\n";
        print INPUT $data{$statKey},"\n\n\n";
    }
    else
    {
        print "OOPS: statType[$statType] key=[$statKey]\n";
    }
}

# -------------------------------------------------------------------
# Add some extra data
print INPUT "indsuccesspercentage <- ",
        $statData{"successes"}{"r-data"},
        " / ",
        $statData{"attempts"}{"r-data"},
        "\n\n";


# -------------------------------------------------------------------
# Add the movement counts
print INPUT "# Movement counts\n";
print INPUT "movementcounts <- scan()\n";
foreach my $value (@{$data{"move"}})
{
    print INPUT $value," ";
}
print INPUT "\n\n";
print INPUT "movementcounts <- movementcounts / ",$data{"move-total"},"\n\n";



# -------------------------------------------------------------------
# Add the final initiator counts
print INPUT "# Final initiator counts\n";
print INPUT "initiatorcounts <- scan()\n";
foreach my $value (@{$data{"final-initiators"}})
{
    print INPUT $value," ";
}
print INPUT "\n\n";
print INPUT "initiatorcounts <- initiatorcounts / ",$data{"final-initiators-total"},"\n\n";



# -------------------------------------------------------------------
# Output statistics file
print INPUT "sink(\"$outputFile\")\n";


# -------------------------------------------------------------------
# Iterate through each stat data type
foreach my $statType (sort keys( %statData ) )
{
    my $title = $statData{$statType}{"title"};
    my $rData = $statData{$statType}{"r-data"};
    print INPUT "cat(\"# =====================================\\n\")\n";
    print INPUT "cat(\"# $title summary\\n\")\n";
    print INPUT "cat(\"summary.$rData.mean = \", format( mean($rData) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$rData.max = \", format( max($rData) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$rData.min = \", format( min($rData) ), \"\\n\")\n";
    print INPUT "cat(\"summary.$rData.sd = \", format( sd($rData) ), \"\\n\")\n";
    print INPUT "cat(\"\\n\")\n";
}


# -------------------------------------------------------------------
# Iterate through each correlation
for my $corrRef ( @corrData )
{
    my $aID = ${$corrRef}[0];
    my $bID = ${$corrRef}[1];

    my $dataName = $statData{$aID}{"r-data"}.".".$statData{$bID}{"r-data"};

    print INPUT "cat(\"# =====================================\\n\")\n";
    print INPUT "cat(\"# Correlation: ",
            $statData{$aID}{"title"},
            " vs. ",
            $statData{$bID}{"title"},
            "\\n\")\n";
    print INPUT "cat(\"correlation.$dataName = \", format( cor(",
            $statData{$aID}{"r-data"},
            ",",
            $statData{$bID}{"r-data"},
            ") ), \"\\n\")\n";
    print INPUT "cat(\"correlation.$dataName.pearson = \", format( cor(",
            $statData{$aID}{"r-data"},
            ",",
            $statData{$bID}{"r-data"},
            ", method=\"pearson\") ), \"\\n\")\n";
    print INPUT "cat(\"correlation.$dataName.spearman = \", format( cor(",
            $statData{$aID}{"r-data"},
            ",",
            $statData{$bID}{"r-data"},
            ", method=\"spearman\") ), \"\\n\")\n";
    print INPUT "cat(\"\\n\")\n";
}
print INPUT "sink()\n";


# -------------------------------------------------------------------
# Create a number of scatterplots
my %plotData = (
    "eig-vs-success" => { "x-data" => $statData{"eigenvector-centrality"}{"r-data"},
                   "y-data"  => $statData{"success-percentage"}{"r-data"},
                   "x-title" => $statData{"eigenvector-centrality"}{"title"},
                   "y-title" => $statData{"success-percentage"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "0.0",
                   "postfix" => "eig-vs-success" },
    "eig-vs-personality" => { "x-data"  => $statData{"personality"}{"r-data"},
                   "y-data" => $statData{"eigenvector-centrality"}{"r-data"},
                   "x-title" => $statData{"personality"}{"title"},
                   "y-title" => $statData{"eigenvector-centrality"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "0.0",
                   "postfix" => "eig-vs-personality" },
#    "eig-vs-failed-followers" => { "x-data"  => $statData{"failed-followers-mean"}{"r-data"},
#                   "y-data" => $statData{"eigenvector-centrality"}{"r-data"},
#                   "x-title" => $statData{"failed-followers-mean"}{"title"},
#                   "y-title" => $statData{"eigenvector-centrality"}{"title"},
#                   "xmax" => "1.0",
#                   "xmin" => "0.0",
#                   "postfix" => "eig-vs-failedfollowers" },
    "eig-vs-attempts" => { "x-data"  => $statData{"eigenvector-centrality"}{"r-data"},
                   "y-data" => $statData{"attempts"}{"r-data"},
                   "x-title" => $statData{"eigenvector-centrality"}{"title"},
                   "y-title" => $statData{"attempts"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "0.0",
                   "postfix" => "eig-vs-attempts" },
    "eig-vs-mimics" => { "x-data"  => $statData{"eigenvector-centrality"}{"r-data"},
                   "y-data" => $statData{"mimicing-neighbor-count"}{"r-data"},
                   "x-title" => $statData{"eigenvector-centrality"}{"title"},
                   "y-title" => $statData{"mimicing-neighbor-count"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "0.0",
                   "postfix" => "eig-vs-mimics" },
    "mimics-vs-success" => { "x-data"  => $statData{"mimicing-neighbor-count"}{"r-data"},
                   "y-data" => $statData{"success-percentage"}{"r-data"},
                   "x-title" => $statData{"mimicing-neighbor-count"}{"title"},
                   "y-title" => $statData{"success-percentage"}{"title"},
                   "xmax" => "20",
                   "xmin" => "0.0",
                   "postfix" => "mimics-vs-success" },
#    "mimics-vs-failedfollowers" => { "x-data"  => $statData{"mimicing-neighbor-count"}{"r-data"},
#                   "y-data" => $statData{"failed-followers-mean"}{"r-data"},
#                   "x-title" => $statData{"mimicing-neighbor-count"}{"title"},
#                   "y-title" => $statData{"failed-followers-mean"}{"title"},
#                   "xmax" => "20",
#                   "xmin" => "0.0",
#                   "postfix" => "mimics-vs-failedfollowers" },
    "mimics-vs-personality" => { "x-data"  => $statData{"mimicing-neighbor-count"}{"r-data"},
                   "y-data" => $statData{"personality"}{"r-data"},
                   "x-title" => $statData{"mimicing-neighbor-count"}{"title"},
                   "y-title" => $statData{"personality"}{"title"},
                   "xmax" => "20",
                   "xmin" => "0.0",
                   "postfix" => "mimics-vs-personality" },
    "personality-vs-success" => { "x-data"  => $statData{"personality"}{"r-data"},
                   "y-data" => $statData{"success-percentage"}{"r-data"},
                   "x-title" => $statData{"personality"}{"title"},
                   "y-title" => $statData{"success-percentage"}{"title"},
                   "xmax" => "1",
                   "xmin" => "0.0",
                   "postfix" => "personality-vs-success" },
    "assertiveness-vs-success" => { "x-data" => $statData{"assertiveness"}{"r-data"},
                   "y-data" => $statData{"success-percentage"}{"r-data"},
                   "x-title" => $statData{"assertiveness"}{"title"},
                   "y-title" => $statData{"success-percentage"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "0.0",
                   "postfix" => "assertiveness-vs-success" },
    "preferreddir-vs-success" => { "x-data"  => $statData{"preferred-direction"}{"r-data"},
                   "y-data" => $statData{"success-percentage"}{"r-data"},
                   "x-title" => $statData{"preferred-direction"}{"title"},
                   "y-title" => $statData{"success-percentage"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "-1.0",
                   "postfix" => "preferreddir-vs-success" },
    "conflict-vs-success" => { "x-data"  => $statData{"conflict"}{"r-data"},
                   "y-data" => $statData{"success-percentage"}{"r-data"},
                   "x-title" => $statData{"conflict"}{"title"},
                   "y-title" => $statData{"success-percentage"}{"title"},
                   "xmax" => "1.0",
                   "xmin" => "0.0",
                   "postfix" => "conflict-vs-success" },
);

foreach my $type (sort keys( %plotData ) )
{
    my $tmpREPSFile = "/tmp/spatial-hidden-var-".$plotData{$type}{"postfix"}.".eps";
    if( -e $tmpREPSFile )
    {
        `rm $tmpREPSFile`;
    }
    
    print INPUT "postscript( file = \"$tmpREPSFile\", height=3.5, width=4, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
    print INPUT "plot( ",$plotData{$type}{"x-data"},", ",$plotData{$type}{"y-data"},", col=c(\"#666666\"),\n";
#    print INPUT "plot( ",$plotData{$type}{"y-data"},", ",$plotData{$type}{"x-data"},", col=outliercolors,\n";#c(\"#666666\"),\n";
    print INPUT "    xlab=\"",$plotData{$type}{"x-title"},"\", ylab=\"\", las = 1,\n";
    print INPUT "    ylim=c(0,max(",$plotData{$type}{"y-data"},")),";
#    print INPUT "    xlim=c(mineigen,1.0) )\n";
    print INPUT "    xlim=c(",$plotData{$type}{"xmin"},",",$plotData{$type}{"xmax"},") )\n";
#    print INPUT "if( 0 < sd(",$plotData{$type}{"x-data"},") ) abline( lm(",$plotData{$type}{"y-data"},"~",$plotData{$type}{"x-data"},"), col=\"#0000CC\", lty=1 )\n";
    print INPUT "mtext(\"",$plotData{$type}{"y-title"},"\", side=2, line=4, las=0 )\n";

    print INPUT "dev.off()\n\n\n";
}


# -------------------------------------------------------------------
# Create a box plot for the movement count
#{
#    my $tmpREPSFile = "/tmp/spatial-hidden-var-movement-counts.eps";
#    if( -e $tmpREPSFile )
#    {
#        `rm $tmpREPSFile`;
#    }
#    print INPUT "postscript( file = \"$tmpREPSFile\", height=3.75, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
#    print INPUT "movementcolors <- c(rep(\"black\", (length(movementcounts)- 1)), \"grey\")\n";
#    print INPUT "maxmovementcount <- (ceiling(10*max(movementcounts)))/10\n";
#    print INPUT "barplot(movementcounts, col=movementcolors, mgp=c(2.5,1,0),\n";
#    print INPUT "    xlab=\"Group Size\", ylab=\"Frequency\",\n";
#    print INPUT "    names.arg=c(seq(1,length(movementcounts))), axis.lty=1,\n";
##    print INPUT "    ylim=c(-0.02,maxmovementcount) )\n";
#    print INPUT "    ylim=c(-0.02,0.8) )\n";
#
#    print INPUT "dev.off()\n\n\n";
#}

# -------------------------------------------------------------------
# Create a box plot for the final initiators count
#{
#    my $tmpREPSFile = "/tmp/spatial-hidden-var-final-initiator-counts.eps";
#    if( -e $tmpREPSFile )
#    {
#        `rm $tmpREPSFile`;
#    }
#    print INPUT "postscript( file = \"$tmpREPSFile\", height=3.75, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
#    print INPUT "initiatorcolors <- c(\"black\", rep(\"grey\", (length(initiatorcounts)- 1)))\n";
#    print INPUT "maxinitiatorcount <- (ceiling(10*max(initiatorcounts)))/10\n";
#    print INPUT "barplot(initiatorcounts, col=initiatorcolors, mgp=c(2.5,1,0),\n";
#    print INPUT "    xlab=\"Final Initiators\", ylab=\"Frequency\",\n";
#    print INPUT "    names.arg=c(seq(1,length(initiatorcounts))), axis.lty=1,\n";
##    print INPUT "    ylim=c(-0.02,initiatorcount) )\n";
#    print INPUT "    xlim=c(0.25,10.0),\n";
#    print INPUT "    ylim=c(-0.02,1.0) )\n";
#
#    print INPUT "dev.off()\n\n\n";
#}



# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;


## -------------------------------------------------------------------
# Fix and move the plots
foreach my $type (keys( %plotData ) )
{
    my $tmpREPSFile = "/tmp/spatial-hidden-var-".$plotData{$type}{"postfix"}.".eps";
    my $fixedREPSFile = "/tmp/fixed-spatial-hidden-var-".$plotData{$type}{"postfix"}.".eps";
    my $epsFile = $epsFilePrefix."-".$plotData{$type}{"postfix"}.".eps";

    if( -e $fixedREPSFile )
    {
        `rm $fixedREPSFile`;
    }

    `./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedREPSFile $epsFile`;
    `epstopdf $epsFile`;
}

#my $tmpREPSFile = "/tmp/spatial-hidden-var-movement-counts.eps";
#my $fixedREPSFile = "/tmp/fixed-spatial-hidden-var-movement-counts.eps";
#my $epsFile = $epsFilePrefix."-movement-counts.eps";

#if( -e $fixedREPSFile )
#{
#    `rm $fixedREPSFile`;
#}

#`./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
#`eps2eps $fixedREPSFile $epsFile`;
#`epstopdf $epsFile`;

#$tmpREPSFile = "/tmp/spatial-hidden-var-final-initiator-counts.eps";
#$fixedREPSFile = "/tmp/fixed-spatial-hidden-var-final-initiator-counts.eps";
#$epsFile = $epsFilePrefix."-final-initiator-counts.eps";
#
#if( -e $fixedREPSFile )
#{
#    `rm $fixedREPSFile`;
#}
#
#`./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
#`eps2eps $fixedREPSFile $epsFile`;
#`epstopdf $epsFile`;

