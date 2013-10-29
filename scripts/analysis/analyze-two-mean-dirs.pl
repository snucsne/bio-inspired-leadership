#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );

# Get the mean subgroup direction
my $subgroupDir = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

print "Found ",($#dataFiles + 1)," files\n";


# -------------------------------------------------------------------
# Read the data files
my @successes;
my @directions;
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

        if( $key =~ /^individual.Ind.*\.preferred-direction/ )
        {
            push( @directions, $value );
        }
        elsif( $key =~ /^initiation.Ind.*\.success-percentage/ )
        {
            push( @successes, $value );
        }

    }

    close( DATA );

}

#print "Found [",$#directions,"] directions\n";
#print "Found [",$#successes,"] successes\n";


# -------------------------------------------------------------------
# Build the left and right data
my %processedData;
my $negativeCount = 0;
for( my $i = 0; $i <= $#directions; $i++ )
{
    my $type = "positive";
    if( $directions[$i] < 0 )
    {
        $type = "negative";
        $negativeCount++;
    }
    $processedData{$type}{"directions"} .= $directions[$i]." ";
    $processedData{$type}{"successes"} .= $successes[$i]." ";
}

#print "Found [$negativeCount] negative directions\n";

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/two-mean-dir-analysis.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print INPUT "library('Matching')\n";
print INPUT "library('igraph')\n";

print INPUT "# dataDir=[$dataDir]\n";
print INPUT "# outputFile=[$outputFile]\n";
print INPUT "# epsFilePrefix=[$epsFilePrefix]\n";


# -------------------------------------------------------------------
# Dump the data
my @types = ( "negative", "positive" );
foreach my $type (@types)
{
    print INPUT "# ",ucfirst($type),": Successes\n";
    print INPUT $type,"successes <- scan()\n";
    print INPUT $processedData{$type}{"successes"},"\n\n";

    print INPUT "# ",ucfirst($type),": Directions\n";
    print INPUT $type,"directions <- scan()\n";
    print INPUT $processedData{$type}{"directions"},"\n\n";

    my $meanDir = $subgroupDir;
    if( $type eq "negative" )
    {
        $meanDir = -1 * $subgroupDir;
    }
    print INPUT "absdiff",$type,"directions = abs( ",$type,"directions - $meanDir )\n\n";
#    print INPUT "absdiff",$type,"directions\n";
}
print INPUT "\n\n";


# -------------------------------------------------------------------
# Output to the statistics file
print INPUT "sink(\"$outputFile\")\n";
foreach my $type (@types)
{
    print INPUT "cat(\"# =====================================\\n\")\n";
    print INPUT "cat(\"# Correlation: ",
            ucfirst($type),
            " Absolute diff of direction from mean vs. Success\\n\")\n";
    print INPUT "cat(\"correlation.$type = \", format( cor(absdiff",
            $type,
            "directions,",
            $type,
            "successes) ), \"\\n\")\n";
    print INPUT "cat(\"correlation.$type.pearson.r = \", format( cor(absdiff",
            $type,
            "directions,",
            $type,
            "successes, method=\"pearson\") ), \"\\n\")\n";
    print INPUT "cor = cor.test(absdiff",
            $type,
            "directions,",
            $type,
            "successes, method=\"pearson\")\n";
    print INPUT "cat(\"correlation.$type.pearson.p-value = \", format( cor\$p.value ), \"\\n\")\n";
    print INPUT "cat(\"correlation.$type.spearman.rho = \", format( cor(absdiff",
            $type,
            "directions,",
            $type,
            "successes, method=\"spearman\") ), \"\\n\")\n";
    print INPUT "cor = cor.test(absdiff",
            $type,
            "directions,",
            $type,
            "successes, method=\"spearman\")\n";
    print INPUT "cat(\"correlation.$type.spearman.p-value = \", format( cor\$p.value ), \"\\n\")\n";
    print INPUT "cat(\"correlation.$type.kendall.tau = \", format( cor(absdiff",
            $type,
            "directions,",
            $type,
            "successes, method=\"kendall\") ), \"\\n\")\n";
    print INPUT "cor = cor.test(absdiff",
            $type,
            "directions,",
            $type,
            "successes, method=\"kendall\")\n";
    print INPUT "cat(\"correlation.$type.kendall.p-value = \", format( cor\$p.value ), \"\\n\")\n";
    print INPUT "cat(\"\\n\")\n";
}


# -------------------------------------------------------------------
# Turn off specialized output
print INPUT "sink()\n";

# -------------------------------------------------------------------
# Plot the data
foreach my $type (@types)
{
    my $tmpREPSFile = "/tmp/two-mean-dir-analysis-".$type.".eps";
    if( -e $tmpREPSFile )
    {
        `rm $tmpREPSFile`;
    }
    print INPUT "postscript( file = \"$tmpREPSFile\", height=3.5, width=4, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
    print INPUT "plot( absdiff",$type,"directions, ",$type,"successes, col=c(\"#666666\"),\n";
    print INPUT "    xlab=\"Absolute difference from\nsubgroup mean direction\", ylab=\"\", las = 1,\n";
    print INPUT "    ylim=c(0,max(",$type,"successes)),";
    print INPUT "    xlim=c(0,max(absdiff",$type,"directions)) )\n";
    print INPUT "mtext(\"Leadership success\", side=2, line=4, las=0 )\n";
    print INPUT "dev.off()\n\n\n";

}


# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Fix and move the plots
foreach my $type (@types)
{
    my $tmpREPSFile = "/tmp/two-mean-dir-analysis-".$type.".eps";
    my $fixedREPSFile = "/tmp/fixed-two-mean-dir-analysis-".$type.".eps";
    my $epsFile = $epsFilePrefix."-two-mean-dir-analysis-".$type.".eps";

    if( -e $fixedREPSFile )
    {
        `rm $fixedREPSFile`;
    }

    `./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedREPSFile $epsFile`;
    `epstopdf $epsFile`;
}

