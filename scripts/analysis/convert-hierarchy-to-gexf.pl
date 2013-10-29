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
    if( $key =~ /individual/ )
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
    elsif( $key =~ /simulation/ )
    {
        my ($trash, $dataKey) = split( /\./, $key );
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
      <attribute id=\"node0\" title=\"distance\" type=\"integer\" />
      <attribute id=\"node1\" title=\"total-followers\" type=\"integer\" />
      <attribute id=\"node2\" title=\"threshold\" type=\"float\" />
      <attribute id=\"node3\" title=\"group-id\" type=\"String\" />
    </attributes>\n";
print GEXF "    <attributes class=\"edge\">
      <attribute id=\"edge0\" title=\"distance\" type=\"integer\" />
      <attribute id=\"edge1\" title=\"total-followers\" type=\"integer\" />
      <attribute id=\"edge2\" title=\"type\" type=\"integer\" />
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
            "\" z=\"".
            (-1 * $data{$id}->{"distance-to-initiator"}).
            "\" />\n";
    print GEXF "        <attvalues>\n";
    print GEXF "          <attvalue for=\"node0\" value=\"".
            $data{$id}->{"distance-to-initiator"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node1\" value=\"".
            $data{$id}->{"total-follower-count"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node2\" value=\"".
            $data{$id}->{"threshold"}.
            "\" />\n";
    print GEXF "          <attvalue for=\"node3\" value=\"".
            $data{$id}->{"group-id"}.
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
    my $foundLeader = 0;
    my $leader = $data{$id}{"leader"};

    my @neighbors = split( /\s/, $data{$id}{"nearest-neighbors"} );
    foreach my $neighbor (@neighbors)
    {
        if( $neighbor eq $leader )
        {
            $foundLeader = 1;
            print GEXF "      <edge id=\"Edge".sprintf( "%05d", $count++ )."\" source=\"".
                    $id.
                    "\" target=\"".
                    $leader.
                    "\" type=\"directed\" weight=\"".
                    (1 + $data{$id}->{"total-follower-count"}).
                    "\" >\n";
            print GEXF "        <attvalues>\n";
            print GEXF "          <attvalue for=\"edge0\" value=\"".
                    $data{$id}->{"distance-to-initiator"}.
                    "\" />\n";
            print GEXF "          <attvalue for=\"edge1\" value=\"".
                    $data{$id}->{"total-follower-count"}.
                    "\" />\n";
            print GEXF "          <attvalue for=\"edge2\" value=\"1\" />\n";
            print GEXF "        </attvalues>\n";
            print GEXF "      </edge>\n";
        }
        else
        {
            print GEXF "      <edge id=\"Edge".sprintf( "%05d", $count++ )."\" source=\"".
                    $id.
                    "\" target=\"".
                    $neighbor.
                    "\" type=\"directed\" weight=\"0.1\">\n";
            print GEXF "        <attvalues>\n";
            print GEXF "          <attvalue for=\"edge2\" value=\"2\" />\n";
            print GEXF "        </attvalues>\n";
            print GEXF "      </edge>\n";
        }
    }

    # Did we find the leader?
    if( ! $foundLeader && (length( $leader ) > 0) )
    {
        # Nope, make an edge
        print GEXF "      <edge id=\"Edge".sprintf( "%05d", $count++ )."\" source=\"".
                $id.
                "\" target=\"".
                $leader.
                "\" type=\"directed\" weight=\"".
                (1 + $data{$id}->{"total-follower-count"}).
                "\" >\n";
        print GEXF "        <attvalues>\n";
        print GEXF "          <attvalue for=\"edge0\" value=\"".
                $data{$id}->{"distance-to-initiator"}.
                "\" />\n";
        print GEXF "          <attvalue for=\"edge1\" value=\"".
                $data{$id}->{"total-follower-count"}.
                "\" />\n";
        print GEXF "          <attvalue for=\"edge2\" value=\"1\" />\n";
        print GEXF "        </attvalues>\n";
        print GEXF "      </edge>\n";
    }
}
print GEXF "    </edges>\n";

# -------------------------------------------------------------------
# Close out the graph
print GEXF "  </graph>\n";
print GEXF "</gexf>\n";

close( GEXF );
