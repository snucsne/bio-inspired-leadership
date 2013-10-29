#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

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

            # While we are here, save the numerical index
            $id =~ /Ind[0]+(\d+)/;
            $data{$id}{"index"} = $1;
        }
        else
        {
            $data{$id}{$dataKey} = $value;
        }
    }
}

close( INPUT );

# -------------------------------------------------------------------
# Create an Rinput file
my $rInputFile = "/tmp/sna-calcs.r";
my $rOutputFile = "/tmp/sna-calcs.r.out";
my $tmpOutputFile = "/tmp/sna-calcs.results";

open( RINPUT, "> $rInputFile" ) or die "Unable to open input file [$rInputFile]: $!\n";
print RINPUT "library('Matching')\n";
print RINPUT "library('igraph')\n";

print RINPUT "# File [$inputFile]\n";

# -------------------------------------------------------------------
# Create the graph using an edge list
print RINPUT "# Edge list\n";
print RINPUT "edges <- scan()\n";
foreach my $id ( sort( keys %data ) )
{
    my @neighbors = split( ' ', $data{$id}{"nearest-neighbors"} );
    foreach my $neighbor ( sort( @neighbors ) )
    {
        print RINPUT ($data{$id}{"index"} + 1)," ",($data{$neighbor}{"index"} + 1)," ";
    }
    print RINPUT "\n";
}
print RINPUT "\n\n";

# Turn the edges into a graph
print RINPUT "graph <- graph( edges )\n\n";

# Calculate the eigenvector centralities
print RINPUT "eigen <- evcent( graph )\n\n";

# Calculate the betweenness values
print RINPUT "bet <- betweenness( graph )\n\n";

# Calculate the alpha centrality values
print RINPUT "alpha <- alpha.centrality( graph )\n\n";
#print RINPUT "alpha\n\n";

# Calculate the degree values
print RINPUT "degree <- degree( graph )\n\n";
#print RINPUT "degree\n\n";

# Calculate the pagerank values
print RINPUT "pagerank <- page.rank( graph )\$vector\n\n";
#print RINPUT "pagerank\n\n";

# Calculate the kleinberg values
print RINPUT "klein <- authority.score( graph )\$vector\n\n";
#print RINPUT "klein\n\n";

# Calculate the out degree values
print RINPUT "outdegree <- degree( graph, mode=\"out\" )\n\n";
#print RINPUT "outdegree\n\n";

# Calculate the in degree values
print RINPUT "indegree <- degree( graph, mode=\"in\" )\n\n";
#print RINPUT "indegree\n\n";

# Calculate the Modularity
print RINPUT "graphcommunity <- edge.betweenness.community( graph, directed=TRUE, merges=TRUE, modularity=TRUE )\n";
#print RINPUT "memberships <- community.to.membership( graph, graphcommunity\$merges, steps=which.max(graphcommunity\$modularity)-1)\n\n";


# -------------------------------------------------------------------
# Output the values
print RINPUT "sink(\"$tmpOutputFile\")\n";

print RINPUT "cat(\"# Eigenvector centrality values\\n\")\n";
print RINPUT "for( i in 1:length(eigen\$vector)) {\n";
print RINPUT "cat(\"eigenvector\",(i-1),eigen\$vector[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Betweenness values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"betweenness\",(i-1),bet[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Alpha values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"alpha\",(i-1),alpha[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Total degree values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"degree\",(i-1),degree[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Page rank values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"pagerank\",(i-1),pagerank[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Kleinberg values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"klein\",(i-1),klein[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Out degree values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"outdegree\",(i-1),outdegree[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# In degree values\\n\")\n";
print RINPUT "for( i in 1:length(bet)) {\n";
print RINPUT "cat(\"indegree\",(i-1),indegree[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Modularity\\n\")\n";
#print RINPUT "cat(\"communities \",length(memberships\$csize),\"\\n\")\n";
print RINPUT "cat(\"modularity 0\",max(graphcommunity\$modularity),\"\\n\")\n";



# -------------------------------------------------------------------
# Close the input file
close( RINPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;


# -------------------------------------------------------------------
# Read the R output file
my %eigValues;
my %betValues;
my %values;

# Open the file
open( ROUTPUT, "$tmpOutputFile" ) or die "Unable to open R output file [$tmpOutputFile] for input file [$inputFile]: $!\n";

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
    else
    {
        $values{$type}{$id} = $value;
    }

#    print "[$type]-[$id]=[$value]\n";

}
close( ROUTPUT );

#exit;

# -------------------------------------------------------------------
# Read the input file again
my $tmpOutputFile = "/tmp/temp-insert.dat";

# Open the input file
open( INPUT, "$inputFile" ) or die "Unable to open input file [$inputFile]: $!\n";

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
#    s/%%%(Ind\d+)-CLOSENESS%%%/$values{}{$1}/;

    # Replace the  placeholder
#    s/%%%(Ind\d+)-TRANSITIVITY%%%/$values{}{$1}/;

    # Replace the pagerank placeholder
    s/%%%(Ind\d+)-PAGERANK%%%/$values{pagerank}{$1}/;

    # Replace the kleinberg placeholder
    s/%%%(Ind\d+)-KELINBERG%%%/$values{klein}{$1}/;

    # Replace the out degree placeholder
    s/%%%(Ind\d+)-OUT-DEGREE%%%/$values{outdegree}{$1}/;

    # Replace the in degree placeholder
    s/%%%(Ind\d+)-IN-DEGREE%%%/$values{indegree}{$1}/;

    # Replace the modularity placeholder
    s/%%%MODULARITY%%%/$values{modularity}/;

    # Print it to the output file
    print OUTPUT $_;
}

close( OUTPUT );
close( INPUT );


# Move it over
`mv $inputFile $inputFile.bak`;
`mv $tmpOutputFile $inputFile`;

