#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the conflict data file
my $conflictDataFile = shift( @ARGV );

# Get the no conflict data file
my $noConflictDataFile = shift( @ARGV );

# Get the single, no conflict data file
my $singleNoConflictDataFile = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Load the results from the data files
my %data;
loadResults( $noConflictDataFile, \%data, "no-conflict" );
loadResults( $conflictDataFile, \%data, "conflict" );
loadResults( $singleNoConflictDataFile, \%data, "single-no-conflict" );

# -------------------------------------------------------------------
# Plot it
plotResults(\%data, $epsFilePrefix);


# ===================================================================
sub loadResults
{
    my ($resultsFile, $dataRef, $type ) = @_;

#    print "Processing [$resultsFile]\n";

    # Open the file
    open( INPUT, $resultsFile ) or die "Unable to open results file [$resultsFile]: $!\n";

    # Read the file
    while( <INPUT> )
    {
        # Remove any comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Split the line up into a key and a value
        my ($key, $value) = split( /\s+=\s+/, $_ );

        # Is it the time to the destination by destination?
        if( $key =~ /time-to-destination\.rank-(\d+)/ )
        {
            # Yup
            $dataRef->{$type}{"rank"}{$1} = $value;
        }

        # Is it the time to the destination for all?
        elsif( $key =~ /time-to-destination\.all/ )
        {
            # Yup
            $dataRef->{$type}{"all"} = $value;
        }
    }

    # Close the file
    close( INPUT );
}


# ===================================================================
sub plotResults
{
    my ($dataRef, $epsOutputPrefix) = @_;

    my $plotRunID = $$;

    # ---------------------------------------------------------------
    my $rSpacer = "# =====================================\n";
    my $rCatSpacer = "cat(\"# =====================================\\n\")\n";


    # ---------------------------------------------------------------
    # Create an input file for R
    my $rInputFile = "/tmp/time-to-dest-TEST.r";
#    my $rInputFile = "/tmp/time-to-dest-$plotRunID.r";
    my $rOutputFile = $rInputFile.".out";

    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

    # ---------------------------------------------------------------
    # Add the results
    my %ids;
    foreach my $type (sort (keys %{$dataRef} ) )
    {
        my $cleanType = $type;
        $cleanType =~ s/-/\./g;

        foreach my $dataset (sort (keys %{$dataRef->{$type}} ) )
        {
            print INPUT $rSpacer;
            if( $dataset =~ /rank/ )
            {
                foreach my $dest (sort (keys %{$dataRef->{$type}{$dataset}} ) )
                {
                    my $id = "$cleanType.$dataset.$dest";
                    $ids{$type}{$dataset}{$dest} = $id;

                    print INPUT "$id <-scan()\n";
                    print INPUT $dataRef->{$type}{$dataset}{$dest};
                    print INPUT "\n\n";
                }
            }
            else
            {
                my $id = "$cleanType.$dataset";
                $ids{$type}{$dataset} = $id;

                print INPUT "$id <-scan()\n";
                print INPUT $dataRef->{$type}{$dataset};
                print INPUT "\n\n";
            }
        }
    }

    # ---------------------------------------------------------------
    my $epsFile = $epsOutputPrefix."-time-to-dest.eps";
    my $pdfFile = $epsFile;
    $pdfFile =~ s/eps$/pdf/;

    my %colors = (
            "conflict" => "#aa4455",
            "no-conflict" => "#44aa77",
            "single-no-conflict" => "#666666",
            "single-conflict" => "#cc6677",
        );

    my $scatterAttr = " method=\"jitter\", jitter=0.3, vertical=TRUE, yaxt='n', cex=0.8, pch=21, col=\"black\", ";
    my $boxAttr = " add=TRUE, outline=FALSE, boxwex=1.5, border=(c(\"black\")), yaxt='n', staplewex=0.75, lwd=1.25, ";

    print INPUT "postscript( file=\"$epsFile\", height=5, width=5.5, onefile=FALSE, pointsize=11, horizontal=FALSE, paper=\"special\", colormodel=\"rgb\" )\n";
#    print INPUT "pdf( file=\"$pdfFile\", height=5, width=5.5, onefile=FALSE, pointsize=10, paper=\"special\" )\n";

    print INPUT "par(mar=c(8.5,5,4,4)+0.1)\n";
    print INPUT "maxvalue <- max(",$ids{"no-conflict"}{"all"},",",$ids{"conflict"}{"all"},")\n";

    print INPUT "stripchart( ",$ids{"conflict"}{"rank"}{"00"},", ", $scatterAttr,
            " at=(0.25), xlim=c(-0.15,5.15), ylim=c(0,maxvalue*1.15), ",
            " bg=\"",$colors{"conflict"},"\" )\n";
    print INPUT "stripchart( ",$ids{"conflict"}{"rank"}{"01"},", ", $scatterAttr,
            " at=(1.75), add=TRUE, ",
            " bg=\"",$colors{"conflict"},"\" )\n";
    print INPUT "boxplot( ",$ids{"conflict"}{"all"},", at=(1),",
            $boxAttr,
            " col=\"",$colors{"conflict"},"\" )\n";

    print INPUT "stripchart( ",$ids{"no-conflict"}{"rank"}{"00"},", ", $scatterAttr,
            " at=(3.25), add=TRUE, ",
            " bg=\"",$colors{"no-conflict"},"\" )\n";
    print INPUT "stripchart( ",$ids{"no-conflict"}{"rank"}{"01"},", ", $scatterAttr,
            " at=(4.75), add=TRUE, ",
            " bg=\"",$colors{"no-conflict"},"\" )\n";
    print INPUT "boxplot( ",$ids{"no-conflict"}{"all"},", at=(4),",
            $boxAttr,
            " col=\"",$colors{"no-conflict"},"\" )\n";


    print INPUT "ranklabels <- c( \"First goal\", \"Second goal\", \"First goal\", \"Second goal\" )\n";
    print INPUT "rankpositions <- c( 0.25, 1.75, 3.25, 4.75 )\n";

    print INPUT "axis( 1, at=c(rankpositions), labels=c(ranklabels) )\n";
    print INPUT "axis( 2, las=2 )\n";
    print INPUT "axis( 4, las=2 )\n";
    print INPUT "mtext( c(\"With Conflict\", \"Without Conflict\" ), at=c(1,4), side=1, cex=1.5, line=3 )\n";
    print INPUT "title(ylab=\"Timesteps\", mgp = c(3.75, 1, 0), cex=1.5 )\n";

#    print INPUT "abline( h=c(min(singlencall), mean(singlencall), max(singlencall)), lty=2, lwd=2, col=c(singlenccolor) )\n";
    print INPUT "abline( h=c(min(",$ids{"single-no-conflict"}{"all"}
            ,"), mean(",$ids{"single-no-conflict"}{"all"}
            ,"), max(",$ids{"single-no-conflict"}{"all"}
            ,")), lty=2, lwd=1, col=c(\"",
            $colors{"single-no-conflict"},"\") )\n";


#    print INPUT "lengths <- c(length(crank0), length(crank1), length(ncrank0), length(ncrank1))\n";
    print INPUT "lengths <- c(length(",$ids{"conflict"}{"rank"}{"00"},
            "), length(",$ids{"conflict"}{"rank"}{"01"},
            "), length(",$ids{"no-conflict"}{"rank"}{"00"},
            "), length(",$ids{"no-conflict"}{"rank"}{"01"},
            "))\n";
    print INPUT "arrivals <- c( \"\nArrivals\", \"\nArrivals\", \"\nArrivals\", \"\nArrivals\" )\n";
    print INPUT "counts <- paste( lengths, arrivals )\n";

    print INPUT "text( rankpositions, (maxvalue*1.1), labels=counts )\n";

    print INPUT "dev.off()\n\n";

    close( INPUT );

    # -------------------------------------------------------------------
    # Run it
    `R --no-save < $rInputFile > $rOutputFile 2>&1`;

    # -------------------------------------------------------------------
    # Clean up the EPS files
    my $tmpEPSFile = "/tmp/tmp.eps";
    my $fixedTmpEPSFile = "/tmp/fixed-tmp.eps";
    `cp $epsFile $tmpEPSFile`;
    `./replace-fonts-in-eps.pl $tmpEPSFile $fixedTmpEPSFile`;
    `/usr/bin/epstool --copy --bbox $fixedTmpEPSFile $epsFile`;
    `epstopdf $epsFile`;
    my $title = `basename $pdfFile`;
    `exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;

}
