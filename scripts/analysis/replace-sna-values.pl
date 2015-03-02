#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the data file
my $dataFile = shift( @ARGV );

# Get the SNA file
my $snaFile = shift( @ARGV );


# -------------------------------------------------------------------
# Read the R SNA file
my %eigValues;
my %betValues;
my %values;

# Open the file
open( ROUTPUT, "$snaFile" ) or die "Unable to open R SNA file [$snaFile] for data file [$dataFile]: $!\n";

# Read each line
my $gatherEigValues = 0;
my $gatherBetValues = 0;
while( <ROUTPUT> )
{
    # Remove any other comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    /^(\w+)\s(\d+)\s(.*)$/;
    my $type = $1;
    my $id = "Ind".sprintf("%05d", $2);
    my $value = $3;

    if( $type =~ /modularity/ )
    {
        $values{$type} = $value;
    }
    elsif( $type =~ /^shortestpath/ )
    {
        # Split the value
        my ($trash, $shortestPath) = split( /\s+/, $value );
        push( @{$values{"shortestpathsarray"}{$id}}, $shortestPath );
    }
    else
    {
        $values{$type}{$id} = $value;
    }

#    print "[$type]-[$id]=[$value]\n";

}
close( ROUTPUT );

# -------------------------------------------------------------------
foreach my $id (sort keys %{$values{"shortestpathsarray"}})
{
    $values{"shortestpaths"}{$id} = join( " ", @{$values{"shortestpathsarray"}{$id}} );
#    print "shortestpaths[$id] = [",$values{"shortestpaths"}{$id},"]\n";
}

# -------------------------------------------------------------------
# Read the input file again
my $tmpOutputFile = "/tmp/temp-insert.dat";

# Open the data file
open( INPUT, "$dataFile" ) or die "Unable to open input file [$dataFile]: $!\n";

# Open the temp output file
open( OUTPUT, "> $tmpOutputFile" ) or die "Unable to open tmp output file [$tmpOutputFile]: $!\n";

# Read each line
while( <INPUT> )
{
    # Replace the eigenvector centrality placeholder
    s/%%%(Ind\d+)-EIGENVECTOR-CENTRALITY%%%/$values{eigenvector}{$1}/;

    # Replace the betweenness placeholder
    s/%%%(Ind\d+)-BETWEENNESS%%%/$values{betweenness}{$1}/;

    # Replace the  placeholder
    s/%%%(Ind\d+)-ALPHA-CENTRALITY%%%/$values{alpha}{$1}/;

    # Replace the degree placeholder
    s/%%%(Ind\d+)-DEGREE%%%/$values{degree}{$1}/;

    # Replace the  placeholder
    s/%%%(Ind\d+)-CLOSENESS%%%/$values{closeness}{$1}/;

    # Replace the  placeholder
    s/%%%(Ind\d+)-NORMALIZED-CLOSENESS%%%/$values{customcloseness}{$1}/;

    # Replace the  placeholder
#    s/%%%(Ind\d+)-TRANSITIVITY%%%/$values{}{$1}/;
    if( /TRANSITIVITY/ )
    {
        next;
    }

    # Replace the pagerank placeholder
    s/%%%(Ind\d+)-PAGERANK%%%/$values{pagerank}{$1}/;

    # Replace the kleinberg placeholder
    s/%%%(Ind\d+)-KELINBERG%%%/$values{klein}{$1}/;

    # Replace the out degree placeholder
    s/%%%(Ind\d+)-OUT-DEGREE%%%/$values{outdegree}{$1}/;

    # Replace the in degree placeholder
    s/%%%(Ind\d+)-IN-DEGREE%%%/$values{indegree}{$1}/;

    # Replace the mean shortest paths placeholder
    s/%%%(Ind\d+)-MEAN-SHORTEST-PATH%%%/$values{meanshortestpath}{$1}/;

    # Replace the shortest paths placeholder
    s/%%%(Ind\d+)-SHORTEST-PATHS%%%/$values{shortestpaths}{$1}/;

    # Replace the modularity placeholder
    s/%%%MODULARITY%%%/$values{modularity}/;

    # Print it to the output file
    print OUTPUT $_;
}

close( OUTPUT );
close( INPUT );


# Move it over
`mv $dataFile $dataFile.bak`;
`mv $tmpOutputFile $dataFile`;


