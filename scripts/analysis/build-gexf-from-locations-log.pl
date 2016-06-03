#!/usr/bin/perl
use strict;
use warnings;
use Time::localtime;

# -------------------------------------------------------------------
# Get the input locations file
my $locationsFile = shift( @ARGV );

# Get the location log line number
my $locationLineNumber = shift( @ARGV );

# Get the output gexf file
my $outputFile = shift( @ARGV );
chomp( $outputFile );

# Get the preferred initiator
my $preferredInitiator = shift( @ARGV );


#print "Exists   [$outputFile]=[".(-e $outputFile ? "True" : "False")."]\n";
#print "Writable [$outputFile]=[".(-w $outputFile ? "True" : "False")."]\n";
#print "Readable [$outputFile]=[".(-r $outputFile ? "True" : "False")."]\n";

# -------------------------------------------------------------------
# Open the location log file
open( INPUT, "$locationsFile" ) or die "Unable to open location log file [$locationsFile]: $!\n";

# Read each line
my %data;
my %initiators;
while( <INPUT> )
{
    # Ignore the other lines
    next unless $. == $locationLineNumber;

    # Grab the current line
    my $locationsLine = $_;
    chomp( $locationsLine );

    # Rip out the starting information
    $locationsLine =~ s/^[SF]\s*\[//;

    # Get the initiators
    $locationsLine =~ s/^\s*([a-zA-Z0-9:]+)\]\s*\[//;
    my $initiatorsStr = $1;
    %initiators = map { $_ => 1 } split( /:/, $initiatorsStr);

    # Rip out the ending brackets
    $locationsLine =~ s/\]//g;
    
    # Split out the locations and neighbors
    my (@locations) = split( /\s*\[/, $locationsLine );

    # Process each one
    foreach my $locationDetails (@locations)
    {
        # Get each chunk of data
        my ($id, $location, @neighbors) = split( /:/, $locationDetails );
        $location =~ s/\(//;
        $location =~ s/\)//;
        my ($x, $y) = split( /\s*,\s*/, $location );

        # Store it
        $data{$id}{"x"} = $x;
        $data{$id}{"y"} = $y;
        $data{$id}{"neighbors"} = \@neighbors;

        # Increment the number of mimics for each individual
        foreach my $neighbor (@neighbors)
        {
            $data{$neighbor}{"mimics"}++;
        }
    }

    # Bail out since we have what we came for
    last;
}

close( INPUT );

# -------------------------------------------------------------------
# Create the GEXF file
open( GEXF, '>', "$outputFile" )
        or die "Unable to open output GEXF file [$outputFile]: $!\n";

# Get the time for the modification date
my $time = localtime;
my $date = sprintf( "%04d-%02d-%02d",
        ($time->year + 1900),
        ($time->mon + 1),
        $time->mday );

# Print the header
print GEXF "<?xml version=\"1.0\" encoding=\"UTF−8\"?>
<gexf xmlns=\"http://www.gexf.net/1.2draft\"
      xmlns:viz=\"http://www.gexf.net/1.2draft/viz\"
      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema−instance\"
      xsi:schemaLocation=\"http://www.gexf.net/1.2draft
                          http://www.gexf.net/1.2draft/gexf.xsd\"
      version=\"1.2\">
  <meta lastmodifieddate=\"".$date."\">
    <creator>Brent Eskridge</creator>
    <description>Generated from: locations-file=$locationsFile</description>
  </meta>\n";

# We have a directed graph
print GEXF "  <graph mode=\"dynamic\" defaultedgetype=\"directed\" timeformat=\"double\">\n";


# -------------------------------------------------------------------
# Print out attributes
print GEXF "    <attributes class=\"node\">
      <attribute id=\"node0\" title=\"initiator\" type=\"float\" />
      <attribute id=\"node1\" title=\"mimics\" type=\"int\" />
    </attributes>\n";


# -------------------------------------------------------------------
# Print out all the nodes
print GEXF "    <nodes>\n";


# -------------------------------------------------------------------
# Build some hidden nodes
my @hiddenX = ("-12","+12");
my @hiddenY = ("-12","+12");
my $hiddenIndex = 1;
foreach my $x (@hiddenX)
{
    foreach my $y (@hiddenY)
    {
        print GEXF "      <node id=\"hidden$hiddenIndex\" label=\"\" start=\"0\">\n".
                "        <viz:position x=\"$x\" y=\"$y\" />\n".
                "      </node>\n";
        $hiddenIndex++;
    }
}

# -------------------------------------------------------------------
# Iterate through all the nodes
foreach my $id ( sort( keys %data ) )
{
    print GEXF "      <node id=\"$id\" label=\"$id\" start=\"0\">\n";
    print GEXF "        <viz:position x=\"".
            $data{$id}{"x"}.
            "\" y=\"".
            $data{$id}{"y"}.
            "\" />\n";
    print GEXF "        <attvalues>\n";
    print GEXF "          <attvalue for=\"node0\" value=\"".
            ($initiators{$id} ? "1" : (($preferredInitiator eq $id) ? "0.5" : "0")).
            "\"/>\n";
    print GEXF "          <attvalue for=\"node1\" value=\"".
            $data{$id}{"mimics"}.
            "\"/>\n";
    print GEXF "        </attvalues>\n";
    print GEXF "      </node>\n";
}
print GEXF "    </nodes>\n";


# -------------------------------------------------------------------
# Print out all the edges
print GEXF "    <edges>\n";

# Iterate through all the nodes
my $count = 0;
foreach my $id ( sort( keys %data ) )
{
    my $neighborsRef = $data{$id}{"neighbors"};
    foreach my $neighbor (@{$neighborsRef})
    {
        print GEXF "      <edge id=\"Edge".sprintf( "%05d", $count++ )."\" source=\"".
                $id.
                "\" target=\"".
                $neighbor.
                "\" type=\"directed\" weight=\"0.1\">\n";
        print GEXF "      </edge>\n";
    }
}
print GEXF "    </edges>\n";



# -------------------------------------------------------------------
# Close out the graph
print GEXF "  </graph>\n";
print GEXF "</gexf>\n";

close( GEXF );

