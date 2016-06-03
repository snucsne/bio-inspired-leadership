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
# Read the data files
my %data;
foreach my $dataFile (@dataFiles)
{
    # Open the file
    open( DATA, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataDir/$dataFile]: $!\n";

    # Get the run ID
    $dataFile =~ /var-(\d\d)-seed/;
    my $runID = $1;

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
                $data{"move-total"} += $value;

                if( $id > $data{"ind-count"} )
                {
                    $data{"ind-count"} = $id;
                }
            }
        }
        if( $key =~ /^total-simulations$/ )
        {
            $data{"total-simulations"} = $value;
        }
        elsif( $key =~ /^initiation.*attempts/ )
        {
            $data{"attempts"} .= " ".$value;
        }
        elsif( $key =~ /^initiation.*successes/ )
        {
            $data{"successes"} .= " ".$value;
        }
        elsif( $key =~ /attempt-percentage/ )
        {
            $data{"attempt-percentage"} .= " ".$value;
            push( @{$data{"attempt-percentage-runs"}{$runID}}, $value );
        }
        elsif( $key =~ /success-percentage/ )
        {
            $data{"success-percentage"} .= " ".$value;
            push( @{$data{"success-percentage-runs"}{$runID}}, $value );
        }
        elsif( $key =~ /failed-followers-mean/ )
        {
            $data{"failed-followers-mean"} .= " ".$value;
        }
        elsif( $key =~ /eigenvector-centrality/ )
        {
            $data{"eigenvector-centrality"} .= " ".$value;
        }
        elsif( $key =~ /betweenness/ )
        {
            $data{"betweenness"} .= " ".$value;
        }
        elsif( $key =~ /mimicing-neighbor-count/ )
        {
            $data{"mimicing-neighbor-count"} .= " ".$value;
        }
        elsif( $key =~ /individual.*personality/ )
        {
            $data{"personality"} .= " ".$value;
            push( @{$data{"personality-runs"}{$runID}}, $value );
#            print "LINE: $_\n";
        }
        elsif( $key =~ /initiation-history/ )
        {
            push( @{$data{"initiation-history"}{$runID}}, $value );
        }
    }

    close( DATA );
}

# -------------------------------------------------------------------
# Create an input file for R
my $rInputFile = "/tmp/personality.r";
my $rOutputFile = "/tmp/personality.r.out";

open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

print INPUT "library(extrafont)\n";
print INPUT "loadfonts(device = \"postscript\")\n";

print INPUT "library(ggplot2)\n";
print INPUT "gg_color_hue <- function(n) {\n";
print INPUT "  hues = seq(15, 375, length=n+1)\n";
print INPUT "  hcl(h=hues, l=65, c=100)[1:n]\n";
print INPUT "}\n\n";



# -------------------------------------------------------------------
# Create a plot for each run
my @epsFigures;
my $idStr = "";
my $valueStr = "";
foreach my $runID (sort (keys %{$data{"personality-runs"}}))
{
    if( length($idStr) > 0 )
    {
        $idStr .= ", ";
    }
    $idStr .= "\"$runID\"";

    if( length( $valueStr ) > 0 )
    {
        $valueStr .= ", ";
    }
    $valueStr .= "personality$runID";

    # Build the personality histories
    my $currentInd = 1;
    foreach my $history (@{$data{"initiation-history"}{$runID}})
    {
        # Clean up and split the history
        $history =~ s/^\s+\[//;
        $history =~ s/\]\s+$//;
        my @initiations = split( /\]\s+\[/, $history );

        my $simIndices = "";
        my $personalityValues = "";

        # If it wasn't the first initiator, go ahead and add the initial personality
        my ($firstIdx, $initialPersonality, $trash) = split( /\s+/, $history );
        $simIndices .= "0 ";
        $personalityValues .= "$initialPersonality ";

        my $lastPersonality;
        foreach my $initiation (@initiations)
        {
            my ($idx, $beforePersonality, $result, $afterPersonality, $followers) =
                    split( /\s+/, $initiation );
#            printf DATA ("%08d    %05.3f\n", $idx, $beforePersonality);
#            printf DATA ("%08d    %05.3f\n", ($idx + 1), $afterPersonality);
            $simIndices .= ($idx + 1)." ";
            $personalityValues .= "$afterPersonality ";
            $lastPersonality = $afterPersonality;
        }
        $simIndices .= $data{"total-simulations"};
        $personalityValues .= $lastPersonality;

        print INPUT "personalityhistoryIndices".$runID."ind".$currentInd." <- scan()\n";
        print INPUT "$simIndices\n\n";
        print INPUT "personalityhistoryValues".$runID."ind".$currentInd." <- scan()\n";
        print INPUT "$personalityValues\n\n";

        $currentInd++;
    }

    # Build the final personality values
    print INPUT "personality$runID <- scan()\n";
    foreach my $personality (@{$data{"personality-runs"}{$runID}})
    {
        print INPUT "$personality  ";
    }
    print INPUT "\n\n";

    my $bothPlots = 0;
    my $personalityEPSFile = $epsFilePrefix."-personality-run-".$runID."-history.eps";
if( $bothPlots )
{
    $personalityEPSFile = $epsFilePrefix."-personality-run-".$runID.".eps";
}
    push( @epsFigures, $personalityEPSFile );

    # http://addictedtor.free.fr/graphiques/sources/source_151.R
#    print INPUT "pdf( file=\"$personalityEPSFile\", height=4, width=5, onefile=FALSE, pointsize=12, paper=\"special\" )\n";

if( $bothPlots )
{
#    print INPUT "postscript( file=\"$personalityEPSFile\", height=4, width=10, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    # For PLOS One
    print INPUT "postscript( file=\"$personalityEPSFile\", height=2.8, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=10.5, horizontal=FALSE, paper=\"special\" )\n";
#    print INPUT "par(mfrow=c(1,2),mar=c(5,4,4,4)+0.1 )\n";
    print INPUT "par(mfrow=c(1,2),mar=c(5,4,2,3)+0.1 )\n";

    print INPUT "h <- hist( personality$runID, plot=FALSE, breaks=seq(0.025,0.975,0.05) )\n";
#    print INPUT "d <- density( personality$runID )\n";
#    print INPUT "d\n";
    print INPUT "h\$density = h\$counts/sum(h\$counts)\n";
    print INPUT "plot( h, border=NA, freq=FALSE, xlab=\"Leadership Tendency (LT) value\", ylab=\"Frequency\", ylim=c(0,1), main=\"\", yaxt='n' )\n";
    print INPUT "usr <- par(\"usr\")\n";
    print INPUT "ncolors <- 100\n";
    print INPUT "dy <- (usr[4] - usr[3])/ncolors\n";
    print INPUT "colors <- colorRampPalette( c(\"yellow\",\"orange\",\"red\",\"red\") )(ncolors)\n";
    print INPUT "abline( h=axTicks(2), col=\"gray\", lwd=0.5)\n";
    print INPUT "for( i in 1:ncolors) {\n";
    print INPUT "  clip( usr[1], usr[2], usr[3] + (i-1) * dy, usr[3] + i*dy )\n";
    print INPUT "  plot( h, add=TRUE, axes=FALSE, ylab=\"\", xlab=\"\", col=colors[i], border=NA, freq=FALSE)\n";
    print INPUT "}\n";
    print INPUT "do.call( clip, as.list(usr) )\n";
    print INPUT "plot( h, add=TRUE, lwd=0.5, freq=FALSE, xlab=\"\", ylab=\"\", axes=FALSE )\n";
#    print INPUT "lines( d, lwd=4, col=\"#22222288\" )\n";
    print INPUT "rug( personality$runID, col=\"#000000\" )\n";
    print INPUT "box()\n";
    print INPUT "axis( 3, las=0, at=c(0.1,0.5,0.9), labels=c(\"Low\", \"Moderate\", \"High\"))\n";
    print INPUT "ticklocations <- axTicks(2)\n";
    print INPUT "templabels <- ticklocations * 100\n";
    print INPUT "percentagelabels <- paste(templabels, \"%\", sep=\"\" )\n";
    print INPUT "axis( 2, las=2, at=ticklocations, labels=percentagelabels )\n";
    print INPUT "axis( 4, las=2, at=ticklocations, labels=percentagelabels )\n";
    print INPUT "mtext( \"(a)\", side=1, line=4 )\n";
#    print INPUT "hist( personality$runID, breaks=seq(0,1,0.05), xlim=c(0,1), ylim=c(0,5), col=heat.colors(20), main=\"Run [$runID]\" );\n";

    print INPUT "opt <- options(scipen = 10)\n";
}
else
{
#    print INPUT "postscript( file=\"$personalityEPSFile\", height=4, width=5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\" )\n";
    print INPUT "postscript( file=\"$personalityEPSFile\", height=5, width=6.83, family=\"Arial\", onefile=FALSE, pointsize=18, horizontal=FALSE, paper=\"special\" )\n";
}
    print INPUT "par(xaxp=c(0,".$data{"total-simulations"}.",6))\n";
    print INPUT "par(mar=c(5,4,2,2)+0.1 )\n";
    print INPUT "opt <- options(scipen = 10)\n";
    print INPUT "plot( c(0,".$data{"total-simulations"}."), c(0,1), type=\"n\", xlab=\"Attempts\",\n";
    print INPUT "    ylab=\"\", yaxt=\"n\", xaxp=c(0,".$data{"total-simulations"}.",4) )\n";
    print INPUT "colors <- sample(rainbow(".($currentInd - 1).",v=0.6),".($currentInd - 1).",replace=FALSE,prob=NULL)\n";
#    print INPUT "colors <- sample(gg_color_hue(".($currentInd - 1)."),".($currentInd - 1).",replace=FALSE,prob=NULL)\n";
    print INPUT "abline( h=axTicks(2), col=\"gray\", lwd=0.5)\n";

    for( my $i = 1; $i < $currentInd; $i++ )
    {
        print INPUT "lines( personalityhistoryIndices".$runID."ind".$i.", personalityhistoryValues".$runID."ind".$i.",\n";
        print INPUT "  type=\"l\", col=colors[$i], lwd=1.5 )\n";
    }

    print INPUT "mtext( side=2, text=\"LT Value\", line=2.5 )\n";
    print INPUT "axis( 2, las=1)\n";
    print INPUT "axis( 4, las=0, at=c(0.1,0.5,0.9), labels=c(\"Low\", \"Moderate\", \"High\"))\n";
    print INPUT "mtext( \"(b)\", side=1, line=4 )\n";

    print INPUT "dev.off()\n\n";
#    print INPUT "h\n";
}

# -------------------------------------------------------------------
#print INPUT "chisqresults <- chisq.test( personality01, personality03 )\n";
#print INPUT "chisqresults\$p.value\n";

print "    Building all dists combined...\n";
my $personalityEPSFile = $epsFilePrefix."-personality-all-dists-combined.pdf";
print INPUT "df <- data.frame( cond = factor( rep(c($idStr), each=".$data{"indCount"}.") ),\n";
print INPUT "  values=c($valueStr))\n";
print INPUT "pdf( file=\"$personalityEPSFile\", height=4, width=5, onefile=FALSE, pointsize=12, paper=\"special\" )\n";
print INPUT "ggplot(df, aes(x=values,fill=cond)) + geom_histogram(binwidth=0.05,position=\"dodge\") + opts(legend.position=\"none\")\n";

#print INPUT "ggplot(df, aes(x=values,fill=cond)) + geom_density(alpha=0.2)\n";
#print INPUT "ggplot(df, aes(x=values,fill=cond)) + geom_histogram(binwidth=0.05,alpha=0.3,position=\"identity\")\n";
#print INPUT "ggplot(df, aes(x=values,colour=cond)) + geom_density()\n";
print INPUT "dev.off()\n\n";

push( @epsFigures, $personalityEPSFile );


# -------------------------------------------------------------------
# Close the input file
close( INPUT );


# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;


# -------------------------------------------------------------------
# Convert to PDF
my $pdfFigures;
foreach my $epsFile (sort (@epsFigures))
{
    `cp $epsFile /tmp/tmp.eps`;
    `/usr/bin/epstool --copy --bbox /tmp/tmp.eps $epsFile`;
    `epstopdf $epsFile`;
    my $tmpPDF = $epsFile;
    $tmpPDF =~ s/eps/pdf/;
    $pdfFigures .= $tmpPDF." ";
}

my $allPDFFile = $epsFilePrefix."-all-combined.pdf";
`pdftk $pdfFigures cat output $allPDFFile`;
