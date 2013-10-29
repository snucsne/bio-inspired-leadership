#!/usr/bin/perl
use strict;
use Time::localtime;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );


# -------------------------------------------------------------------
# Read the input file
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
# Create the GEXF file

open( GEXF, "> $outputFile" )
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
    <description>Generated from: $inputFile</description>
  </meta>\n";

# We have a directed graph
print GEXF "  <graph defaultedgetype=\"directed\">\n";

# -------------------------------------------------------------------
# Print out attributes
print GEXF "    <attributes class=\"node\">
      <attribute id=\"node0\" title=\"attempt-percentage\" type=\"float\" />
      <attribute id=\"node1\" title=\"success-percentage\" type=\"float\" />
      <attribute id=\"node2\" title=\"initiation-rate\" type=\"float\" />
      <attribute id=\"node3\" title=\"eigenvector-centrality\" type=\"float\" />
      <attribute id=\"node4\" title=\"mimicing-neighbors\" type=\"float\" />
      <attribute id=\"node5\" title=\"personality\" type=\"float\" />
    </attributes>\n";

# -------------------------------------------------------------------
# Print out all the nodes
print GEXF "    <nodes>\n";

# Iterate through all the nodes
foreach my $id ( sort( keys %data ) )
{
    print GEXF "      <node id=\"$id\" label=\"$id\">\n";
    print GEXF "        <viz:position x=\"".
            $data{$id}{"location"}{"x"}.
            "\" y=\"".
            $data{$id}{"location"}{"y"}.
            "\" />\n";
    print GEXF "        <attvalues>\n";
    print GEXF "          <attvalue for=\"node0\" value=\"".
            $data{$id}->{"attempt-percentage"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node1\" value=\"".
            $data{$id}->{"success-percentage"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node2\" value=\"".
            $data{$id}->{"initiation-rate"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node3\" value=\"".
            $data{$id}->{"eigenvector-centrality"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node4\" value=\"".
            $data{$id}->{"mimicing-neighbor-count"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node5\" value=\"".
            $data{$id}->{"personality"}.
            "\" />\n";
    print GEXF "        </attvalues>\n";
    print GEXF "      </node>\n";
}
print GEXF "    </nodes>\n";


# -------------------------------------------------------------------
# Print out all the edges
print GEXF "    <edges>\n";

my $useLeader = 0;

# Iterate through all the nodes
my $count = 0;
foreach my $id ( sort( keys %data ) )
{
    my @neighbors = split( /\s/, $data{$id}{"nearest-neighbors"} );
    foreach my $neighbor (@neighbors)
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
