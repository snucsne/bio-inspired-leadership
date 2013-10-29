#!/usr/bin/perl
use strict;
use Time::localtime;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Read the input file
my %data;
my %simData;

# Open the file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

# Read each line
while( <INPUT> )
{
    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    my ($key,$value) = split( /\s+=\s+/, $_ );

    if( ($key =~ /^initiation\./)
        || ($key =~ /^individual\./) )
    {
        my ($trash, $id, $dataKey) = split( /\./, $key );

        if( $dataKey =~ /location/ )
        {
            my ($x, $y) = split( /\ /, $value );
            $data{$id}{$dataKey}{"x"} = $x;
            $data{$id}{$dataKey}{"y"} = $y;

            # While we are here, save the numerical index
            $id =~ /Ind[0]+(\d+)/;
            $data{$id}{"index"} = $1;
        }
        else
        {
            $data{$id}{$dataKey} = $value;
        }
    }
    elsif( $key =~ /^move/ )
    {
        my ($trash, $id) = split( /\./, $key );
        if( 0 < $id )
        {
            $simData{"move"} .= " ".$value;
            ($simData{"move-total"})++
        }
    }
    else
    {
        $simData{$key} = $value;
    }
}

close( INPUT );

# -------------------------------------------------------------------
# Create an input file
my $rInputFile = "/tmp/eigenvector-centrality.r";
my $rOutputFile = "/tmp/eigenvector-centrality.r.out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print INPUT "library('Matching')\n";
print INPUT "library('igraph')\n";


# -------------------------------------------------------------------
# Add the success percentages for each individual

print INPUT "# Success percentages\n";
print INPUT "success <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    print INPUT $data{$id}{"success-percentage"}," ";
}
print INPUT "\n\n";

# -------------------------------------------------------------------
# Add the attempt percentages for each individual

print INPUT "# Attempt percentages\n";
print INPUT "attempts <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    print INPUT $data{$id}{"attempt-percentage"}," ";
}
print INPUT "\n\n";

# -------------------------------------------------------------------
# Add the mean successful follower count for each individual

print INPUT "# Mean successful follower count\n";
print INPUT "successfulfollowersmean <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    print INPUT $data{$id}{"successful-followers-mean"}," ";
}
print INPUT "\n\n";

# -------------------------------------------------------------------
# Add the mean failed follower count for each individual

print INPUT "# Mean failed follower count\n";
print INPUT "failedfollowersmean <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    print INPUT $data{$id}{"failed-followers-mean"}," ";
}
print INPUT "\n\n";

# -------------------------------------------------------------------
# Add the number of individuals with a given individual as a 
# nearest neighbor

print INPUT "# Inds with this ind as a NN\n";
print INPUT "mimicingneighbors <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    print INPUT $data{$id}{"mimicing-neighbor-count"}," ";
}
print INPUT "\n\n";

# -------------------------------------------------------------------
# Create the graph using an edge list
print INPUT "# Edge list\n";
print INPUT "edges <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    my @neighbors = split( ' ', $data{$id}{"nearest-neighbors"} );
    foreach my $neighbor ( sort( @neighbors ) )
    {
        print INPUT $data{$id}{"index"}," ",$data{$neighbor}{"index"}," ";
    }
}
print INPUT "\n\n";

# Turn the edges into a graph
print INPUT "graph <- graph( edges )\n\n";

# Calculate the eigenvector centralities
print INPUT "eigen <- evcent( graph )\n\n";
print INPUT "mineigen <- (floor(10*min(eigen\$vector)))/10\n";

# -------------------------------------------------------------------
# Add the movement counts
print INPUT "# Movement counts\n";
print INPUT "movementcounts <- scan()\n";
print INPUT $simData{"move"},"\n";
print INPUT "\n\n";
print INPUT "movementcounts <- movementcounts / ",$simData{"total-simulations"},"\n\n";


# -------------------------------------------------------------------
# Output statistics file
print INPUT "sink(\"$outputFile\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Eigenvector centrality summary\\n\")\n";
print INPUT "summary(eigen\$vector)\n\n";
print INPUT "cat(\"# Standard deviation\\n\")\n";
print INPUT "sd(eigen\$vector)\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Success summary\\n\")\n";
#print INPUT "success\n\n";
print INPUT "summary(success)\n";
print INPUT "cat(\"# Standard deviation\\n\")\n";
print INPUT "sd(success)\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Attempts summary\\n\")\n";
print INPUT "summary(attempts)\n";
print INPUT "cat(\"# Standard deviation\\n\")\n";
print INPUT "sd(attempts)\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Correlation: centrality vs. success\\n\")\n";
print INPUT "cor(eigen\$vector,success)\n";
print INPUT "cor.test(eigen\$vector,success,method=\"pearson\")\n\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Correlation: centrality vs. mean number of successful followers\\n\")\n";
print INPUT "cor(eigen\$vector,successfulfollowersmean)\n";
print INPUT "cor.test(eigen\$vector,successfulfollowersmean,method=\"pearson\")\n\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Correlation: centrality vs. mean number of failed followers\\n\")\n";
print INPUT "cor(eigen\$vector,failedfollowersmean)\n";
print INPUT "cor.test(eigen\$vector,failedfollowersmean,method=\"pearson\")\n\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Correlation: centrality vs. mimicing neighbors\\n\")\n";
print INPUT "cor(eigen\$vector,mimicingneighbors)\n";
print INPUT "cor.test(eigen\$vector,mimicingneighbors,method=\"pearson\")\n\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Correlation: mimicing neighbors vs. success\\n\")\n";
print INPUT "cor(mimicingneighbors,success)\n";
print INPUT "cor.test(mimicingneighbors,success,method=\"pearson\")\n\n";
print INPUT "cat(\"\\n\")\n";


print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Linear regression: centrality vs. success\\n\")\n";
print INPUT "summary(lm(success~eigen\$vector))\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Linear regression: centrality vs. mimicing neighbors\\n\")\n";
print INPUT "summary(lm(mimicingneighbors~eigen\$vector))\n";
print INPUT "cat(\"\\n\")\n";

print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Linear regression: success vs. mimicing neighbors\\n\")\n";
print INPUT "summary(lm(mimicingneighbors~success))\n";
print INPUT "cat(\"\\n\")\n";



print INPUT "cat(\"\\n\")\n";
print INPUT "cat(\"\\n\")\n";
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Eigenvector centrality values\\n\")\n";
print INPUT "cat(\"# XX-START-XX\\n\")\n";
print INPUT "for( i in 1:length(eigen\$vector)) {\n";
print INPUT "cat((i-1),eigen\$vector[i],\"\\n\")\n";
print INPUT "}\n";
print INPUT "cat(\"# XX-END-XX\\n\")\n";
print INPUT "cat(\"\\n\")\n";
print INPUT "cat(\"\\n\")\n";

#print INPUT "dev.off()\n\n\n";


# -------------------------------------------------------------------
# Create a number of scatterplots
my %plotData = (
    "success" => { "data"  => "success",
                   "altdata" => "eigen\$vector",
                   "title" => "Success Percentage",
                   "alttitle" => "Eigenvector Centrality",
                   "xmax" => "1.0",
                   "postfix" => "success" },
    "attempts" => { "data"  => "attempts",
                   "altdata" => "eigen\$vector",
                   "title" => "Attempts Percentage",
                   "alttitle" => "Eigenvector Centrality",
                   "xmax" => "1.0",
                   "postfix" => "attempts" },
    "successfulfollowersmean" => { "data"  => "successfulfollowersmean",
                   "altdata" => "eigen\$vector",
                   "title" => "Mean Success Followers",
                   "alttitle" => "Eigenvector Centrality",
                   "xmax" => "1.0",
                   "postfix" => "success-followers" },
    "failedfollowersmean" => { "data"  => "failedfollowersmean",
                   "altdata" => "eigen\$vector",
                   "title" => "Mean Failed Followers",
                   "alttitle" => "Eigenvector Centrality",
                   "xmax" => "1.0",
                   "postfix" => "failed-followers" },
    "mimicingneighbors" => { "data"  => "mimicingneighbors",
                   "altdata" => "eigen\$vector",
                   "title" => "Mimicing neighbors",
                   "alttitle" => "Eigenvector Centrality",
                   "xmax" => "1.0",
                   "postfix" => "mimicing-neighbors" },
    "success-mimic" => { "data"  => "success",
                   "altdata" => "mimicingneighbors",
                   "title" => "Success Percentage",
                   "alttitle" => "Mimicing neighbors",
                   "xmax" => "20",
                   "postfix" => "success-mimic" },
);

foreach my $type (keys( %plotData ) )
{
    my $tmpREPSFile = "/tmp/eigenvector-centrality-".$plotData{$type}{"postfix"}.".eps";
    `rm $tmpREPSFile`;
    
    print INPUT "postscript( file = \"$tmpREPSFile\", height=3.5, width=4, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
    print INPUT "plot( ",$plotData{$type}{"altdata"},", ",$plotData{$type}{"data"},", \n";
    print INPUT "    xlab=\"",$plotData{$type}{"alttitle"},"\", ylab=\"\", las = 1,\n";
    print INPUT "    ylim=c(0,max(",$plotData{$type}{"data"},")),";
#    print INPUT "    xlim=c(mineigen,1.0) )\n";
    print INPUT "    xlim=c(0,",$plotData{$type}{"xmax"},") )\n";
    print INPUT "if( 0 < sd(eigen\$vector) ) abline( lm(",$plotData{$type}{"data"},"~",$plotData{$type}{"altdata"},"), col=\"#666666\", lty=2 )\n";
    print INPUT "mtext(\"",$plotData{$type}{"title"},"\", side=2, line=4, las=0 )\n";

    print INPUT "dev.off()\n\n\n";
}

# -------------------------------------------------------------------
# Create a box plot for the movement count
{
    my $tmpREPSFile = "/tmp/eigenvector-centrality-movement-counts.eps";
    `rm $tmpREPSFile`;
    print INPUT "postscript( file = \"$tmpREPSFile\", height=3.75, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "movementcolors <- c(rep(\"black\", ",($simData{"move-total"} - 1),"), \"grey\")\n";
    print INPUT "maxmovementcount <- (ceiling(10*max(movementcounts)))/10\n";
    print INPUT "barplot(movementcounts, col=movementcolors, mgp=c(2.5,1,0),\n";
    print INPUT "    xlab=\"Group Size\", ylab=\"Frequency\",\n";
    print INPUT "    names.arg=c(seq(1,",$simData{"move-total"},")), axis.lty=1,\n";
#    print INPUT "    ylim=c(-0.02,maxmovementcount) )\n";
    print INPUT "    ylim=c(-0.02,0.8) )\n";

    print INPUT "dev.off()\n\n\n";
}

# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# Fix and move the plots
foreach my $type (keys( %plotData ) )
{
    my $tmpREPSFile = "/tmp/eigenvector-centrality-".$plotData{$type}{"postfix"}.".eps";
    my $fixedREPSFile = "/tmp/fixed-eigenvector-centrality-".$plotData{$type}{"postfix"}.".eps";
    my $epsFile = $epsFilePrefix."-".$plotData{$type}{"postfix"}.".eps";

    `rm $fixedREPSFile`;

    `./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
    `eps2eps $fixedREPSFile $epsFile`;
    `epstopdf $epsFile`;
}

my $tmpREPSFile = "/tmp/eigenvector-centrality-movement-counts.eps";
my $fixedREPSFile = "/tmp/fixed-eigenvector-centrality-movement-counts.eps";
my $epsFile = $epsFilePrefix."-movement-counts.eps";

`rm $fixedREPSFile`;

`./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
`eps2eps $fixedREPSFile $epsFile`;
`epstopdf $epsFile`;


