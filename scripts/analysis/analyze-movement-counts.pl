#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

# Get the outputfile prefix
my $outputFilePrefix = shift( @ARGV );

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
my @dataFiles;

# Grab the map directories
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
@dataFiles = sort( grep { /\.dat$/ } readdir( DIR ) );
closedir( DIR );

print "Found ",(scalar @dataFiles)," files\n";

# -------------------------------------------------------------------
# Process each data file
my %data;
my %totals;
foreach my $dataFile (@dataFiles)
{
    # Open the file
    open( INPUT, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Get the map id
    $dataFile =~ /map-(\d+)/;
    my $mapID = $1;

    my %fileTotals;
    # Read each line
    while( <INPUT> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

#print "Testing [$_]\n";

        # We are only interested in movement lines
        if( /move\./ )
        {
            # Grab the key and the value
            my ($key,$value) = split( /\s+=\s+/, $_ );
            my ($trash, $type, $count) = split( /\./, $key );

#print "    Found one!\n";

            next if (0 == $count);

            if( !exists( $data{$type}{$count} ) )
            {
                $data{$type}{$count} = [];
            }

#print "    type[$type]  count=[$count]  value=[$value]  data=[",(scalar @{$data{$type}{$count}} ),"]\n";

            # Save the value
            push( @{$data{$type}{$count}}, $value );
            $fileTotals{$type} += $value;
        }
    }

    # Close the file
    close( INPUT );

    # Store the totals
    foreach my $type (keys %fileTotals)
    {
        if( !exists( $totals{$type} ) )
        {
            $totals{$type} = [];
        }
        push( @{$totals{$type}}, $fileTotals{$type} );
    }
}


# -------------------------------------------------------------------
# Create an Rinput file
my $rInputFile = "/tmp/movement-count.r";
my $rOutputFile = $rInputFile.".out";

open( RINPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print RINPUT "library(extrafont)\n";
print RINPUT "loadfonts(device = \"postscript\")\n";

# Store the names of all the eps files we create
my @epsFiles;

# -------------------------------------------------------------------
print RINPUT "movementcounts <- seq(1, $indCount)\n\n";

# -------------------------------------------------------------------
# Build some variables to store the data we read
foreach my $type (sort (keys %data) )
{
    my @variables;
    my @percentvariables;

    print RINPUT "totals$type <- scan()\n";
    print RINPUT join( " ", @{$totals{$type}} ),"\n\n";

    foreach my $count (sort (keys %{$data{$type}}) )
    {
        print RINPUT "$type$count <- scan()\n";
        print RINPUT join( " ", @{$data{$type}{$count}} ),"\n\n";
        push( @variables, "$type$count" );

        print RINPUT "percent$type$count <- $type$count / totals$type\n\n";
        push( @percentvariables, "percent$type$count" );
    }

    my $lastCumulativeVar;
    foreach my $count (reverse (sort (keys %{$data{$type}}) ) )
    {
        if( length( $lastCumulativeVar ) > 0 )
        {
            print RINPUT "cumulative$type$count <- $type$count + $lastCumulativeVar\n";
        }
        else
        {
            print RINPUT "cumulative$type$count <- $type$count\n";
        }
        print RINPUT "cumulativepercent$type$count <- cumulative$type$count / totals$type\n";
        $lastCumulativeVar = "cumulative$type$count";
#        print RINPUT "cumulative$type$count\n";
#        print RINPUT "mean(cumulativepercent$type$count)\n\n";
    }

    print RINPUT "all$type <- c( ",join( ", ", @variables )," )\n\n";

    # Create a boxplot
    my $dataframe = $type."df";
    print RINPUT "$dataframe <- data.frame( ",
        join( ", ", @percentvariables ),")\n\n";

    my $epsFile = $epsFilePrefix."$type-movement-count-boxplot.eps";
    push( @epsFiles, $epsFile );

    print RINPUT "postscript( file=\"$epsFile\", height=4, width=",(3+2*$indCount/10),", family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
    print RINPUT "par(mar=c(4,4.5,1,3)+0.1)\n";
    print RINPUT "par(mgp=c(2.5,0.8,0))\n";
    print RINPUT "boxplot( $dataframe, col=\"orange\", yaxt='n', ylim=c(0,1), \n",
                " xlab=\"Final participants\", xaxt='n', \n", #names=movementcounts,\n",
                " ylab=\"Percentage of attempts\", boxwex=0.5, srt=-40 )\n";
    print RINPUT "axis( 1, at=c(pretty(seq(1,$indCount))), las=0 )\n";
    print RINPUT "axis( 2, at=c(seq(0,1,0.2)), lab=(c(seq(0,1,0.2)) * 100), las=TRUE )\n";
    print RINPUT "axis( 4, at=c(seq(0,1,0.2)), lab=(c(seq(0,1,0.2)) * 100), las=TRUE )\n";
    print RINPUT "dev.off()\n\n\n";

}

# -------------------------------------------------------------------
# Analyze the data
my $outputFile = $outputFilePrefix."movement-count-analysis.dat";
print RINPUT "sink(\"$outputFile\")\n";

my $nowString = localtime;
print RINPUT buildRCatText( "# Data dir      [$dataDir]" );
print RINPUT buildRCatText( "# Time          [$nowString]" );
print RINPUT buildRCatText( "# Output prefix [$outputFilePrefix]" );
print RINPUT buildRCatText( "# EPS prefix    [$epsFilePrefix]" );
print RINPUT buildRCatText( "# ===========================================" );
print RINPUT buildRCatText( "" );


foreach my $type (sort (keys %data) )
{
    print RINPUT buildRCatText( "# ==========================================================" );
    print RINPUT buildRCatText( "# Type [$type]" );
    print RINPUT buildRCatText( "" );
    
    foreach my $count (sort (keys %{$data{$type}}) )
    {
        print RINPUT buildRCatText( "# ----------------------------------------------------------" );
        print RINPUT buildRCatText( "# Participants [$count]" );
        print RINPUT buildRCatText( "$type.$count.total.count = \",format( length( $type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.total.mean  = \",format( mean( $type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.total.sd    = \",format( sd( $type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.total.min   = \",format( min( $type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.total.max   = \",format( max( $type$count ) ),\"" );
        print RINPUT buildRCatText( "# ---------------------------" );
        print RINPUT buildRCatText( "$type.$count.percentage.count = \",format( length( percent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.percentage.mean  = \",format( mean( percent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.percentage.sd    = \",format( sd( percent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.percentage.min   = \",format( min( percent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.percentage.max   = \",format( max( percent$type$count ) ),\"" );
        print RINPUT buildRCatText( "# ---------------------------" );
        print RINPUT buildRCatText( "$type.$count.cumulative-percentage.count = \",format( length( cumulativepercent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.cumulative-percentage.mean  = \",format( mean( cumulativepercent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.cumulative-percentage.sd    = \",format( sd( cumulativepercent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.cumulative-percentage.min   = \",format( min( cumulativepercent$type$count ) ),\"" );
        print RINPUT buildRCatText( "$type.$count.cumulative-percentage.max   = \",format( max( cumulativepercent$type$count ) ),\"" );
        print RINPUT buildRCatText( "" );
        print RINPUT buildRCatText( "" );
    }
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
    if( -e $epsFile )
    {
       `cp $epsFile $tmpEPSFile`;
        `/usr/bin/epstool --copy --bbox $tmpEPSFile $epsFile`;
        `epstopdf $epsFile`;

        my $pdfFile = $epsFile;
        $pdfFile =~ s/eps$/pdf/;
        my $title = $epsFile;
        $title =~ s/.*\/(.*)\.eps$/\1/;
        `exiftool -Title="$title" -Author="Brent E. Eskridge" $pdfFile`;
        # See the link below for more exiftool help with PDFs
        # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PDF.html
        # See the link below for more exiftool help with PostScript
        # http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/PostScript.html
    }
}



# ===================================================================
sub buildRCatText
{
    my ($text) = @_;
    return "cat(\"".$text."\\n\")\n";
}


