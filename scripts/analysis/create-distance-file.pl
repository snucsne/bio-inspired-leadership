#!/usr/bin/perl
use strict;
use Time::localtime;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the "root" individual
my $rootIndividual = shift( @ARGV );

# Get the output file
my $outputFilePrefix = shift( @ARGV );

# -------------------------------------------------------------------
# Read the input file
#print "Reading input file [$inputFile]...\n";
my %data;

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
        }
        else
        {
            $data{$id}{$dataKey} = $value;
        }
    }
}

close( INPUT );


# -------------------------------------------------------------------
# Set default values for all individuals
my $indCount = scalar keys %data;
foreach my $id ( sort ( keys %data ) )
{
    $data{$id}{'distance'} = $indCount * 100;
    $data{$id}{'parents'} = [];
}

# -------------------------------------------------------------------
# Calculate how far individuals are from the root individual
#print "Calculating distance for [$indCount] individuals to [$rootIndividual]...\n";
$data{$rootIndividual}{'distance'} = 0;
my %visited = ( $rootIndividual => 1 );
while( $indCount > (scalar keys %visited) )
{
    # Iterate through each individual
    foreach my $id ( sort( keys %data ) )
    {
        # Has this neighbor already been visited?
        if( $visited{$id} )
        {
            # Yup
            next;
        }

        # Check each of its neighbors to see if any have been visited
        my @neighbors = split( /\s/, $data{$id}{"nearest-neighbors"} );
        foreach my $neighbor (@neighbors)
        {
            # Has the neighbor been visited and are they closer?
            if( $visited{$neighbor} && ($data{$id}{'distance'} > $data{$neighbor}{'distance'}) )
            {
                # Yup
                if( $data{$id}{'distance'} > $data{$neighbor}{'distance'} + 1 )
                {
                    $data{$id}{'parents'} = [];
                }
                $data{$id}{'distance'} = $data{$neighbor}{'distance'} + 1;
                push( @{$data{$id}{'parents'}}, $neighbor );
                $visited{$id} = 1;

#                print "$id -> $neighbor [",$data{$id}{'distance'},"]\n";
            }
        }
    }
}

# -------------------------------------------------------------------
# Get a count of the number of individuals at each distance
my %distanceCount;
foreach my $id ( keys %data )
{
    $distanceCount{ $data{$id}{'distance'} }++;
}

#foreach my $distance ( sort (keys %distanceCount) )
#{
#    print "Distance=[$distance]  count=[",$distanceCount{$distance},"]\n";
#}

# -------------------------------------------------------------------
my $tempTexFile = "/tmp/distance-tikz.tex";

# Open the output file and create the header
open( OUTPUTFILE, "> $tempTexFile" ) or die "Unable to open output file [$tempTexFile]: $!\n";

print OUTPUTFILE "\\documentclass{standalone}\n";
print OUTPUTFILE "\\usepackage{tikz}\n";
print OUTPUTFILE "\\usetikzlibrary{arrows}\n";
print OUTPUTFILE "\\definecolor{snu-crimson}{rgb}{0.64,0.043,0.21}\n";
print OUTPUTFILE "\\tikzset{>=latex}\n";
print OUTPUTFILE "\\tikzset{minimum size=0.8cm}\n";
print OUTPUTFILE "\\begin{document}\n";
print OUTPUTFILE "\\begin{tikzpicture}[auto]\n";
#print OUTPUTFILE "\\draw[help lines] (-1,-20) grid (8,20);\n";


# -------------------------------------------------------------------
# Dump the information for each node
my $nodeVerticalSpacing = 1;
my $nodeHorizontalSpacing = 1 + 3 * $indCount / 50;
my %distanceCounter;
my @indOrdered = sort {($data{$a}{'distance'} <=> $data{$b}{'distance'}) ||
                       ($a cmp $b) } ( keys %data );
foreach my $id ( @indOrdered )
{
    my $cleanName = $id;
    $cleanName =~ s/Ind0+//;
    $cleanName++;

    # Calculate the position
    my $distance = $data{$id}{'distance'};
    my $distanceIdx = $distanceCounter{$distance};

    my $xPosition = $distance * $nodeHorizontalSpacing;
    my $yPosition = (($distanceCount{$distance} - 1) / 2) * $nodeVerticalSpacing
            + -1 * $nodeVerticalSpacing * $distanceIdx;
    $data{$id}{'x'} = $xPosition;
    $data{$id}{'y'} = $yPosition;

    my $parentCount = (scalar (@{$data{$id}{'parents'}}));
    my $percentage = 100;
    if( $parentCount > 0 )
    {
        $percentage = 100 * $parentCount / $distanceCount{($data{$id}{'distance'}-1)};
    }

    print OUTPUTFILE "\\node[circle,draw,font=\\bf,line width=1,snu-crimson!$percentage!black] ($id) at ($xPosition,$yPosition) {$cleanName};\n";

    # Increment the index
    $distanceCounter{$distance}++;
}
print OUTPUTFILE "\n";

# Dump out edges
my $prefix = "\\path[->]  ";
my $printed = 0;
my $outputStr = "";
foreach my $id ( @indOrdered )
{
#    print $id," -> [",$data{$id}{'distance'},"]\n";
    my $parentCount = (scalar (@{$data{$id}{'parents'}}));
    my $parentIdx = 0;
    foreach my $parent ( @{$data{$id}{'parents'}} )
    {
        my $outY = $data{$id}{'y'};
        my $inY = $data{$parent}{'y'};
        my $yDiff = $outY - $inY;
        if( $yDiff > 5 )
        {
            $yDiff = 5;
        }
        elsif( $yDiff < -5 )
        {
            $yDiff = -5;
        }
$outputStr .= "% yDiff=[$yDiff] outY=[$outY] inY=[$inY]\n";
        my $inAngle = 0 + 10 * $yDiff;
        my $outAngle = 180 + 10 * $yDiff;
#        $outAngle = 180 - 25 + 50 * ($parentIdx / $parentCount);
        my $percentage = 100 * $parentCount / $distanceCount{($data{$id}{'distance'}-1)};
#print "Percentage: id=[$id]  parent=[$parent]  parentCount=[$parentCount]  total=[",$distanceCount{($data{$id}{'distance'}-1)},"] percentage=[$percentage]\n";
        my $lineWidth = 1 * $percentage / 100;
        my $edgeArgStr = "[in=$inAngle,out=$outAngle,snu-crimson!$percentage!gray,line width=$lineWidth]";
#        my $bendStr = "[out=180,in=0]";
#        $edgeArgStr = "";
#        if( $data{$id}{'y'} > $data{$parent}{'y'} )
#        {
#            $bendStr = "[out=180,in=0]";
#        }
        $outputStr .= $prefix."($id) edge$edgeArgStr ($parent)\n";
        if( $printed == 0 )
        {
            $prefix = "           ";
            $printed = 1;
        }
        $parentIdx++;
    }
}
chop( $outputStr );
$outputStr .= ";\n\n";
print OUTPUTFILE $outputStr;

# -------------------------------------------------------------------
# Finish the latex file
print OUTPUTFILE "\\end{tikzpicture}\n\\end{document}\n";

# -------------------------------------------------------------------
# Close the output tex file
close( OUTPUTFILE );

# -------------------------------------------------------------------
# Create the filenames for the temp files
my $tempDVIFile = $tempTexFile;
$tempDVIFile =~ s/\.tex/\.dvi/;
my $tempPSFile = $tempTexFile;
$tempPSFile =~ s/\.tex/\.ps/;
my $tempEPSFile = $tempPSFile;
$tempEPSFile =~ s/\.ps/\.eps/;
my $tempPDFFile = $tempPSFile;
$tempPDFFile =~ s/\.ps/\.pdf/;
my $outputFile = $outputFilePrefix."-distance.pdf";

# Build the latex file
chdir( "/tmp/" );
`pdflatex $tempTexFile`;
#`dvips -o $tempPSFile $tempDVIFile 2>&1`;
#`ps2eps -f $tempPSFile 2>&1`;
#`eps2eps $tempEPSFile $outputFile 2>&1`;
#`epstopdf $outputFile 2>&1`;
`mv $tempPDFFile $outputFile`;
