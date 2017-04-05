#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data directory
my $dataDir = shift( @ARGV );
$dataDir =~ /indcount-(\d+)/;
my $indCount = $1;

# Get the eps file
my $epsFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Get each of the data files
my @dataFiles;

# Grab the map directories
opendir( DIR, $dataDir ) or die "Unable to open data directory [$dataDir]: $!\n";
@dataFiles = sort( grep { /^spatial.*\.dat$/ } readdir( DIR ) );
closedir( DIR );

print "Found ",(scalar @dataFiles)," files\n";

# -------------------------------------------------------------------
# Process each data file
my %data;
foreach my $dataFile (@dataFiles)
{
    # Create a variable in which data is stored
    my %data;

    # Open the file
    open( INPUT, "$dataDir/$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Get the map id
    $dataFile =~ /map-(\d+)/;
    my $mapID = $1;

    # Read each line
    while( <INPUT> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        # Get the data key and individual id
        my ($key,$value) = split( /\s+=\s+/, $_ );
        my ($trash, $id, $dataKey ) = split( /\./, $key, 3 );

        # Is it data we need to process?
        if( $dataKey =~ /location/ )
        {
            $value =~ s/\}//g;
            $value =~ s/\{//g;
            my ($x, $y) = split( /\s+/, $value );
            $data{$id}{"x"} = $x;
            $data{$id}{"y"} = $y;
        }
        elsif( $dataKey =~ /trait/ )
        {
            # Find out what type of trait
            my ($moreTrash, $traitType) = split( /\./, $dataKey );
            $traitType =~ s/(.*)_.*/\1/;
            $data{$id}{$traitType} = $value;
        }
    }

    # Close the file
    close( INPUT );
    
    # Process the data
    processData( $epsFilePrefix, $mapID, \%data );
}


# ===================================================================
sub processData
{
    my ($epsFilePrefix, $mapID, $dataRef) = @_;

    my %data = %{$dataRef};

    # Build arrays of each individual position and other data
    my @xPos;
    my @yPos;
    my @bold;
    my @active;
    my @fearful;
    my @social;
    foreach my $id (sort (keys %data))
    {
        push( @xPos, $data{$id}{"x"} );
        push( @yPos, $data{$id}{"y"} );

        # Store the traits
        push( @bold, $data{$id}{"bold"} );
        push( @active, $data{$id}{"active"} );
        push( @social, $data{$id}{"social"} );
        push( @fearful, $data{$id}{"fearful"} );
    }

    # -------------------------------------------------------------------
    # Create an Rinput file
    my $rInputFile = "/tmp/trait-figure.r";
    my $rOutputFile = $rInputFile.".out";

    open( RINPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
    print RINPUT "library(extrafont)\n";
    print RINPUT "loadfonts(device = \"postscript\")\n";
    print RINPUT "library(plotrix)\n";
    print RINPUT "library('CircStats')\n";
    print RINPUT "library('grDevices')\n";

    # Store the names of all the eps files we create
    my @epsFiles;

    # Build R variables
    print RINPUT "xPos <- scan()\n";
    print RINPUT join( " ", @xPos ),"\n\n";
    print RINPUT "yPos <- scan()\n";
    print RINPUT join( " ", @yPos ),"\n\n";
    print RINPUT "bold <- scan()\n";
    print RINPUT join( " ", @bold ),"\n\n";
    print RINPUT "active <- scan()\n";
    print RINPUT join( " ", @active ),"\n\n";
    print RINPUT "social <- scan()\n";
    print RINPUT join( " ", @social ),"\n\n";
    print RINPUT "fearful <- scan()\n";
    print RINPUT join( " ", @fearful ),"\n\n";

    print RINPUT "max0 <- function(x) {\n";
    print RINPUT "  max(0,x)\n";
    print RINPUT "}\n\n";
    print RINPUT "min1 <- function(x) {\n";
    print RINPUT "  min(1,x)\n";
    print RINPUT "}\n\n";
    print RINPUT "min66 <- function(x) {\n";
    print RINPUT "  min(0.66,x)\n";
    print RINPUT "}\n\n";


    # -------------------------------------------------------------------
    # Create a plot showing all the traits
    my @traits = ( "bold", "active", "fearful" );
    foreach my $trait (@traits)
    {
        my $epsFile = $epsFilePrefix."-map-$mapID-trait-$trait.eps";
        push( @epsFiles, $epsFile );

        print RINPUT "normalized$trait <- ($trait - 0.1)/0.8\n";
        print RINPUT "normalized$trait\n";
        print RINPUT "rcolor <- (1-normalized$trait)\n";
        print RINPUT "gcolor <- (1-normalized$trait)\n";
        print RINPUT "bcolor <- 1\n";
        print RINPUT "traitcolor <- rgb( rcolor, gcolor, bcolor)\n";
        print RINPUT "traitcolor\n";
        print RINPUT "traitsize <- 0.2 + 1 * $trait\n";
        print RINPUT "traitsize\n";

        print RINPUT "postscript( file=\"$epsFile\", height=6, width=6, family=\"Arial\", onefile=FALSE, pointsize=16, horizontal=FALSE, paper=\"special\" )\n";
        print RINPUT "par(mar=c(2,2,2,2)+0.1 )\n";

#        print RINPUT "plot( 0, 0, axes=FALSE, fg=\"white\", "
#            ."xlim=( c(min(xPos)-2, max(xPos)+2) ), ylim=( c(min(yPos)-2, max(yPos)+2) ) )\n";
        print RINPUT "symbols( xPos, yPos, ann=F, fg=\"black\", bg=traitcolor, "
            ."lwd=1.75, circles=traitsize, inches=1/5, axes=FALSE )\n";

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
    
}

# ===================================================================
sub buildRCatText
{
    my ($text) = @_;
    return "cat(\"".$text."\\n\")\n";
}


