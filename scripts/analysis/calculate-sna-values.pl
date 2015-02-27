#!/usr/bin/perl
use strict;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the output SNA file
my $snaFile = shift( @ARGV );

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

# Calculate the shortest paths
print RINPUT "shortestpaths <- shortest.paths( graph, mode=c(\"in\") )\n\n";
#print RINPUT "shortestpaths\n\n";
print RINPUT "meanshortestpaths <- rowMeans( shortestpaths )\n\n";

# Calculate the closeness
print RINPUT "closenessallnodes <- closeness( graph, vids=V(graph), mode=c(\"in\"), normalized=TRUE )\n\n";
#print RINPUT "closenessallnodes\n\n";
print RINPUT "customnormalizedcloseness <- closenessallnodes / max(closenessallnodes)\n\n";
#print RINPUT "mynormalizedcloseness\n\n";

# Calculate the diameter
print RINPUT "farthestnodes <- farthest.nodes( graph, directed = TRUE )\n\n";
#print RINPUT "farthestnodes\n\n";


# -------------------------------------------------------------------
# Output the values
print RINPUT "sink(\"$snaFile\")\n";

print RINPUT "cat(\"# Data file [$inputFile]\\n\")\n";
my $nowString = localtime;
print RINPUT "cat(\"# $nowString\\n\")\n";

print RINPUT "cat(\"\\n\")\n";

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
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Shortest paths\\n\")\n";
print RINPUT "for( i in 1:nrow(shortestpaths)) {\n";
print RINPUT "for( j in 1:ncol(shortestpaths)) {\n";
print RINPUT "cat(\"shortestpath\",(i-1),(j-1),shortestpaths[i,j],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Mean shortest paths\\n\")\n";
print RINPUT "for( i in 1:length(meanshortestpaths)) {\n";
print RINPUT "cat(\"meanshortestpath\",(i-1),meanshortestpaths[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Standard normalized closeness\\n\")\n";
print RINPUT "for( i in 1:length(closenessallnodes)) {\n";
print RINPUT "cat(\"closeness\",(i-1),closenessallnodes[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";

print RINPUT "cat(\"# Custom normalized closeness\\n\")\n";
print RINPUT "for( i in 1:length(customnormalizedcloseness)) {\n";
print RINPUT "cat(\"customcloseness\",(i-1),customnormalizedcloseness[i],\"\\n\")\n";
print RINPUT "}\n";
print RINPUT "cat(\"\\n\")\n";
print RINPUT "cat(\"\\n\")\n";


# -------------------------------------------------------------------
# Close the input file
close( RINPUT );

# -------------------------------------------------------------------
# Run it
`R --no-save < $rInputFile > $rOutputFile 2>&1`;


