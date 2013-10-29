#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Get each of the data files
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
my @dataFiles = grep { /\.dat$/ } readdir( DIR );
@dataFiles = sort( @dataFiles );
closedir( DIR );

#print "Found ",($#dataFiles + 1)," files\n";


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
# Create an input file for R
my $rInputFile = "/tmp/one-mean-dir-analysis.r";
my $rOutputFile = $rInputFile.".out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print INPUT "library('Matching')\n";
print INPUT "library('igraph')\n";

print INPUT "# dataDir=[$dataDir]\n";
print INPUT "# outputFile=[$outputFile]\n";
print INPUT "# epsFilePrefix=[$epsFilePrefix]\n";


# -------------------------------------------------------------------
# Dump the data
print INPUT "# Successes\n";
print INPUT "successes <- scan()\n";
print INPUT "@successes\n\n";

print INPUT "# Directions\n";
print INPUT "directions <- scan()\n";
print INPUT "@directions\n\n";

print INPUT "absdiffdirections = abs( directions )\n\n";


# -------------------------------------------------------------------
# Output to the statistics file
print INPUT "sink(\"$outputFile\")\n";
print INPUT "cat(\"# =====================================\\n\")\n";
print INPUT "cat(\"# Correlation: Absolute diff of direction from mean vs. Success\\n\")\n";
print INPUT "cat(\"correlation = \", format( cor(absdiffdirections,successes) ), \"\\n\")\n";
print INPUT "cat(\"correlation.pearson.r = \", format( cor(absdiffdirections,successes, method=\"pearson\") ), \"\\n\")\n";
print INPUT "cor = cor.test(absdiffdirections,successes, method=\"pearson\")\n";
print INPUT "cat(\"correlation.pearson.p-value = \", format( cor\$p.value ), \"\\n\")\n";
print INPUT "cat(\"correlation.spearman.rho = \", format( cor(absdiffdirections,successes, method=\"spearman\") ), \"\\n\")\n";
print INPUT "cor = cor.test(absdiffdirections,successes, method=\"spearman\")\n";
print INPUT "cat(\"correlation.spearman.p-value = \", format( cor\$p.value ), \"\\n\")\n";
print INPUT "cat(\"correlation.kendall.tau = \", format( cor(absdiffdirections,successes, method=\"kendall\") ), \"\\n\")\n";
print INPUT "cor = cor.test(absdiffdirections,successes, method=\"kendall\")\n";
print INPUT "cat(\"correlation.kendall.p-value = \", format( cor\$p.value ), \"\\n\")\n";
print INPUT "cat(\"\\n\")\n";


# -------------------------------------------------------------------
# Turn off specialized output
print INPUT "sink()\n";

# -------------------------------------------------------------------
# Plot the data
my $tmpREPSFile = "/tmp/one-mean-dir-analysis.eps";
if( -e $tmpREPSFile )
{
    `rm $tmpREPSFile`;
}
print INPUT "postscript( file = \"$tmpREPSFile\", height=3.5, width=4, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
print INPUT "par(mar=c(5.1,5.1,1.1,1.1))\n";
print INPUT "plot( absdiffdirections, successes, col=c(\"#666666\"),\n";
print INPUT "    xlab=\"Absolute difference from mean direction\", ylab=\"\", las = 1,\n";
print INPUT "    ylim=c(0,max(successes)),";
print INPUT "    xlim=c(0,max(absdiffdirections)) )\n";
print INPUT "mtext(\"Leadership success\", side=2, line=4, las=0 )\n";
print INPUT "dev.off()\n\n\n";


# -------------------------------------------------------------------
# Close the input file
close( INPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;

# -------------------------------------------------------------------
# Fix and move the plots
my $tmpREPSFile = "/tmp/one-mean-dir-analysis.eps";
my $fixedREPSFile = "/tmp/fixed-one-mean-dir-analysis.eps";
my $epsFile = $epsFilePrefix."-one-mean-dir-analysis.eps";

if( -e $fixedREPSFile )
{
    `rm $fixedREPSFile`;
}

`./replace-fonts-in-eps.pl $tmpREPSFile $fixedREPSFile`;
`/usr/bin/epstool --copy --bbox $fixedREPSFile $epsFile`;
`epstopdf $epsFile`;


