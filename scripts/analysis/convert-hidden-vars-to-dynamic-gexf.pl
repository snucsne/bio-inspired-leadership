#!/usr/bin/perl
use strict;
use Time::localtime;

# -------------------------------------------------------------------
# Get the input network file
my $networkFile = shift( @ARGV );

# Get the input history file
my $historyFile = shift( @ARGV );

# Get the initiation log line number
my $historyLineNumber = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );


# -------------------------------------------------------------------
# Read the input file
my %data;

# Open the file
open( INPUT, "$networkFile" ) or die "Unable to open input network file [$networkFile]: $!\n";

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
# Open the history file
open( INPUT, "$historyFile" ) or die "Unable to open input history file [$historyFile]: $!\n";

my @events;

# Read each line
while( <INPUT> )
{
    # Ignore the other lines
    next unless $. == $historyLineNumber;

    # Grab the current line
    my $history = $_;

    # Rip out the starting information
    $history =~ s/^[^\[]+\[/\[/;

    # Rip out the ending brackets
    $history =~ s/\]//g;

    # Pull out the events
    my (@historyEvents) = split( /\s*\[/, $history );
    
    # Iterate through the events in reverse order so we get the final 
    # hierarchy and ignore failed attempts
    my %processed;
    foreach my $event (reverse @historyEvents)
    {
        next unless length( $event );

        # Pull out the details
        $event =~ /(\w+)\s+(\w+|\*+)\s+(\w)\s+(-?\d+\.\d+)\s+(\d+\.\d+)/;
        my $departed = $1;
        my $leader = $2;
        my $eventType = $3;
        my $time = $4;
        my $personality = $5;

        # Has this individual already been processed?
        if( exists( $processed{$departed} ) )
        {
            # Yup, go on
            # Note that this happens because an individual could have been
            # an initiator or follower in a failed initiation
            next;
        }

        # Mark this individual as processed
        $processed{$departed} = 1;

        # Store the link from departed to leader
        if( $leader =~ /\*/ )
        {
            # Ignore intitiation
        }
        else
        {
            my %event;
            $event{"departed"} = $departed;
            $event{"leader"} = $leader;
            $event{"time"} = $time;
            $event{"personality"} = $personality;
            push( @events, {%event} );
        }

    }

    # Stop now
    last;
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
    <description>Generated from: network=$networkFile  history=$historyFile</description>
  </meta>\n";

# We have a directed graph
print GEXF "  <graph mode=\"dynamic\" defaultedgetype=\"directed\" timeformat=\"double\">\n";

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
    print GEXF "      <node id=\"$id\" label=\"$id\" start=\"0\">\n";
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
#    print GEXF "          <attvalue for=\"node3\" value=\"".
#            $data{$id}->{"eigenvector-centrality"}.
#            "\" />\n";
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

# Iterate through all the events
my $count = 1;
my $time = 0;
foreach my $eventRef ( reverse @events )
{
    $time += $eventRef->{"time"};

    print GEXF "      <edge id=\"Edge".sprintf( "%05d", $count++ )."\" source=\"".
            $eventRef->{"departed"}.
            "\" target=\"".
            $eventRef->{"leader"}.
            "\" start=\"".
            $time.
            "\" type=\"directed\" weight=\"0.1\">\n";
    print GEXF "      </edge>\n";
}

print GEXF "    </edges>\n";

# -------------------------------------------------------------------
# Close out the graph
print GEXF "  </graph>\n";
print GEXF "</gexf>\n";

close( GEXF );
