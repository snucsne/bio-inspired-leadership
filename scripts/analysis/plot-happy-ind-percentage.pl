#!/usr/bin/perl
use strict;
use Data::Dumper;
use Statistics::Descriptive;

# -------------------------------------------------------------------
# Get the individual count
my $indCount = shift( @ARGV );

# Get the max number of timesteps to plot
my $maxTimesteps = shift( @ARGV );

# Get the no conflict data file
my $noConflictDataFile = shift( @ARGV );

# Get the conflict data file
my $conflictDataFile = shift( @ARGV );

# Get the single, no conflict data file
my $singleNoConflictDataFile = shift( @ARGV );

# Get the eps file prefix
my $epsFilePrefix = shift( @ARGV );


# -------------------------------------------------------------------
# Load the results from the data files
my %data;
loadResults( $noConflictDataFile, \%data, "no-conflict", $indCount, $maxTimesteps );
loadResults( $conflictDataFile, \%data, "conflict", $indCount, $maxTimesteps );
loadResults( $singleNoConflictDataFile, \%data, "single-no-conflict", $indCount, $maxTimesteps );



# -------------------------------------------------------------------
# Plot them
plotResults( \%data, $maxTimesteps, $epsFilePrefix );


# ===================================================================
sub loadResults
{
    my ($resultsFile, $dataRef, $type, $indCount, $maxTimesteps ) = @_;

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

        # Is it the number of happy individuals?
        if( $key =~ m/^happy-inds\.(\d+)/ )
        {
            my $timestep = $1;
            unless( $maxTimesteps >= $timestep )
            {
                last;
            }

            # Split the value and get the means
#            my $stats = Statistics::Descriptive::Full->new();
#            $stats->add_data( split( /\s+/, $value ) );

            # Store the mean
#            push( @{$dataRef->{$type}{"means"}}, $stats->mean() / $indCount );
            $dataRef->{$type}{$timestep} = $value;
        }
    }

    # Close the file
    close( INPUT );
}

# ===================================================================
sub plotResults
{
    my ($dataRef, $maxTimesteps, $epsOutputPrefix) = @_;

    my $plotRunID = $$;

    # ---------------------------------------------------------------
    my $rSpacer = "# =====================================\n";
    my $rCatSpacer = "cat(\"# =====================================\\n\")\n";

    # ---------------------------------------------------------------
    # Create an input file for R
    my $rInputFile = "/tmp/happy-inds-TEST.r";
#    my $rInputFile = "/tmp/happy-inds-$plotRunID.r";
    my $rOutputFile = $rInputFile.".out";

    open( INPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";

    # ---------------------------------------------------------------
    # Add the results
    my %ids;
    foreach my $type (sort (keys %{$dataRef} ) )
    {
        # Build the ID
        my $id = $type."happyinds";
        $id =~ s/-//g;
        $ids{$type} = $id;

        print INPUT $rSpacer;
#        print INPUT "$id <- scan()\n";
#        print INPUT join( " ", @{$dataRef->{$type}{"means"}} );
#        print INPUT "\n\n";

        my @tsIDs;
        my $tsMeans = "c(";
        foreach my $timestep (sort (keys %{$dataRef->{$type}} ) )
        {
            if( $timestep =~ /means/ )
            {
                next;
            }

            my $tsID = $id.$timestep;
            push( @tsIDs, $tsID );
            print INPUT "$tsID <- scan()\n";
            print INPUT $dataRef->{$type}{$timestep};
            print INPUT "\n\n";
            
            $tsMeans .= " mean( $tsID ),";
        }

        $tsMeans =~ s/,$/ )/;

        print INPUT $id," = $tsMeans / $indCount\n\n";

    }

    my $epsFile = $epsOutputPrefix."-happy-inds.eps";

    my %colors = (
            "conflict" => "#882255",
            "no-conflict" => "#117733",
            "single-no-conflict" => "#999933",
            "single-conflict" => "#cc6677",
        );

    print INPUT "percentagepositions <- c(seq(0,1,0.25))\n";
    print INPUT "tmplabels <- percentagepositions * 100\n";
    print INPUT "percentagelabels <- paste( tmplabels, \"%\", sep=\"\" )\n";

    print INPUT "postscript( file=\"$epsFile\", height=4.5, width=5.5, onefile=FALSE, pointsize=12, horizontal=FALSE, paper=\"special\", colormodel=\"rgb\" )\n";
    print INPUT "par(mar=c(4,5.5,4,4))\n";
    print INPUT "plot(conflicthappyinds, type=\"l\", ylim=c(0,1), ",
            " col=\"".$colors{"conflict"}."\", lwd=2, ",
            " ylab=\"\", xlab=\"Timesteps\", yaxt='n' )\n";
    print INPUT "lines(noconflicthappyinds, lty=2, lwd=2, col=\"".$colors{"no-conflict"}."\" )\n";
    print INPUT "lines(singlenoconflicthappyinds, lty=4, lwd=2, col=\"".$colors{"single-no-conflict"}."\" )\n";
    print INPUT "title(ylab=\"Percentage moving towards\ndesired destination\", mgp = c(3.5, 1, 0) )\n";

    print INPUT "legend( \"bottomright\",",
            " c(\"With Conflict\", \"Without Conflict\", \"Baseline\"),",
            " col=c( \"#882255\", \"#117733\", \"#999933\" ), lwd=2,",
            " lty=c(1, 2, 4), bty='n' )\n";

    print INPUT "axis( 2, las=2, at=percentagepositions, labels=percentagelabels )\n";
    print INPUT "axis( 4, las=2, at=percentagepositions, labels=percentagelabels )\n";

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
    my $pdfFile = $epsFile;
    $pdfFile =~ s/eps$/pdf/;
    my $title = `basename $pdfFile`;
    `exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;

}

#legend( "bottomright", c("With Conflict", "Without Conflict", "Baseline"),
#            col=c( "#882255", "#117733", "#999933" ), lwd=2,
#            lty=c(1, 2, 4), bty="n" )

