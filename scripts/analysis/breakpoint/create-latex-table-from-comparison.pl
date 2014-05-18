#!/usr/bin/perl
use strict;
use Data::Dumper;

# -------------------------------------------------------------------
# Get the input file
my $inputFile = shift( @ARGV );

# Get the output file
my $outputFile = shift( @ARGV );

# Do we only display statistically significant results?
my $onlySignificant = shift( @ARGV );


# -------------------------------------------------------------------
# Define some pretty labels for the data types
my %prettyLabels = (
        "bold-count" => "Count",
        "inits-to-bold" => "Initiations",
        "inits-to-bold-min" => "Initiations (min)",
        "sims-to-bold" => "Simulations",
        "sims-to-bold-min" => "Simulations (min)",
        );
my @changeIndices = ( "Initial", "Change" );

# -------------------------------------------------------------------
# Read the input file
my %data;
open( INPUT, $inputFile ) or die "Unable to open input file [$inputFile]: $!\n";

# Read each line
while( <INPUT> )
{
    # Remove any comments or whitespace
    s/#.*//;
    s/^\s+//;
    s/\s+$//;
    next unless length;

    # Split the line up into a key and a value
    my ($key,$value) = split( /\s+=\s+/, $_ );
    #print "[$key]=[$value]\n";

    # Check to see if it is a description
    if( $key =~ /(.+)\.desc$/ )
    {
        # Yup, save it
        $data{"desc"}{$1} = $value;
        next;
    }

    # Get the type of the data
    $key =~ /^(.+)\.personality-(\d\.\d)\.indcount-(\d+)\.env-change-idx-(\d+)\.(.+)\.(.+)/;
    my $id = $1;
    my $personality = $2;
    my $indCount = $3;
    my $envChangeIdx = $4;
    my $dataType = $5;
    my $statType = $6;
    #print "id=[$id] personality=[$personality] indCount=[$indCount] envChangeIdx=[$envChangeIdx] dataType=[$dataType] statType=[$statType]\n";

    # Is it a KS statistic?
    if( $id =~ /ks/ )
    {
        # Yup, we are only interested in the p-value
        if( $statType =~ /^p-value$/ )
        {
            $data{"ks"}{$personality}{$indCount}{$envChangeIdx}{$dataType} = $value;
        }
    }

    elsif( $id =~ /t-test/ )
    {
        if( $statType =~ /^p-value$/ )
        {
            $data{"ttest"}{$personality}{$indCount}{$envChangeIdx}{$dataType} = $value;
        }
    }

    # It is a primary or secondary data.  Is it a mean or std dev?
    elsif( $statType =~ /mean|sd/ )
    {
        $data{$statType}{$id}{$personality}{$indCount}{$envChangeIdx}{$dataType} = $value;
    }

}

# Close the file
close( INPUT );

# -------------------------------------------------------------------
# Open the output file
open( OUTPUT, "> $outputFile" ) or die "Unable to open output file [$outputFile]: $!\n";

# Print a line for each piece of data
my $significant = 0;
my $indCountRowsStr = "%%INDCOUNTROWS%%";
my $envChangeIdxStr = "%%ENVCHANGEIDXROWS%%";
foreach my $personality ( sort( keys %{$data{"mean"}{"primary"}} ) )
{
    my $tableStr;

    $tableStr .= "% ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
    $tableStr .= "\\begin{table}[!h]\n";
#    $tableStr .= "\\renewcommand\\arraystretch{1.1}\n";
    $tableStr .= "\\renewcommand\\tabcolsep{0.1cm}\n";
    $tableStr .= "\\center{\n";
    $tableStr .= "\\begin{tabular}{|cclrrcrrc|}\n";
    $tableStr .= "  \\hline\n";
    $tableStr .= "  {\\bf Group} & & & & & & & & \\\\\n";
    $tableStr .= "  {\\bf Size} & {\\bf Adaptation} & {\\bf Bold statistic} & \\multicolumn{2}{r}{\\bf ".
            $data{"desc"}{"primary"}.
            "} &   & \\multicolumn{2}{r}{\\bf ".
            $data{"desc"}{"secondary"}.
            "} &\\\\\n";
    $tableStr .= "  \\hline\n";

    foreach my $indCount ( sort( keys %{$data{"mean"}{"primary"}{$personality}} ) )
    {
        my $indCountPrefix = "\\multirow{$indCountRowsStr}{*}{$indCount}\n   & ";
        my $intCountRows = 0;
        
        foreach my $envChangeIdx ( sort( keys %{$data{"mean"}{"primary"}{$personality}{$indCount}} ) )
        {
            my $envChangeIdxPrefix = "\\multirow{$envChangeIdxStr}{*}{".sprintf( "%-7s", $changeIndices[$envChangeIdx])."}\n      & ";
            my $envChangeIdxRows = 0;

            foreach my $dataType ( sort( keys %{$data{"mean"}{"primary"}{$personality}{$indCount}{$envChangeIdx}} ) )
            {
                # Omit bold counts
                if( $dataType =~ /count/ )
                {
                    next;
                }

                # Build a hash to store the info
                my %comparisonData = (
                        "dataType" => $dataType,
                        "primaryMean" => $data{"mean"}{"primary"}{$personality}{$indCount}{$envChangeIdx}{$dataType},
                        "primarySD" => $data{"sd"}{"primary"}{$personality}{$indCount}{$envChangeIdx}{$dataType},
                        "secondaryMean" => $data{"mean"}{"secondary"}{$personality}{$indCount}{$envChangeIdx}{$dataType},
                        "secondarySD" => $data{"sd"}{"secondary"}{$personality}{$indCount}{$envChangeIdx}{$dataType},
                        "ksPValue" => $data{"ks"}{$personality}{$indCount}{$envChangeIdx}{$dataType},
                        "ttestPValue" => $data{"ttest"}{$personality}{$indCount}{$envChangeIdx}{$dataType},
                        );

                # Only proceed if it is statistically significant
                $significant = 0;
#                if( $onlySignificant || $comparisonData{"ksPValue"} < 0.05 )
                if( $onlySignificant || $comparisonData{"ttestPValue"} < 0.05 )
                {
                    $significant = 1;

                    # Is it a line with a new group size?
                    if( $indCountPrefix =~ /\d/ )
                    {
                        # Yup, print out a line
                        $tableStr .= "  \\hline\n";
                    }
                    # How about a new change index?
                    elsif( $envChangeIdxPrefix =~ /\w/ )
                    {
                        # Yup, but only print out a partial line
                        $tableStr .= "  \\cline{2-9}\n";
                    }

                    # Build a comparison line
                    my $comparisonLine = buildComparisonLine( \%comparisonData );

                    # Combine it with the other data
                    my $line = "  ".
                            $indCountPrefix.
                            $envChangeIdxPrefix.
                            sprintf( "%-26s", $prettyLabels{$dataType}).
                            " & ~ ".$comparisonLine;
                    $tableStr .= $line;

                    # Reset the environment change idx prefix so we don't show it again
                    $envChangeIdxPrefix = " & ";
                    $envChangeIdxRows++;

                    # Reset the indCount prefix so we don't show it again
                    $indCountPrefix = " & ";
                    $intCountRows++;
                }
            }

            # Replace the environmental change index rows
            $tableStr =~ s/$envChangeIdxStr/$envChangeIdxRows/;
        }

        # Replace the indCount rows
        $tableStr =~ s/$indCountRowsStr/$intCountRows/;
    }

    $tableStr .= "  \\hline\n";
    $tableStr .= "\\end{tabular}}\n";
    $tableStr .= "\\caption{Personality $personality (mean \$\\pm\$ std. deviation).  Statistically significant differences with \$p \\leq 0.05\$ are denoted with {\\bf *}.}\n";
    $tableStr .= "\\label{tbl:comparison-change-dir:$personality}\n";
    $tableStr .= "\\end{table}\n";
    $tableStr .= "% ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
    $tableStr .= "\n\n";

    # Print the table
    print OUTPUT $tableStr;

}

# Close the output file
close( OUTPUT );


# -------------------------------------------------------------------
#print Dumper(%data);


# ===================================================================
sub buildComparisonLine
{
    my ($dataRef) = @_;

    # Which is smaller: primary or secondary?
    my $primaryValue = "\$".
            sprintf("%7.1f", $dataRef->{"primaryMean"}).
            "\$ & \$ \\pm ".
            sprintf("%7.1f", $dataRef->{"primarySD"}).
            "\$";
    my $secondaryValue = "\$".
            sprintf("%7.1f", $dataRef->{"secondaryMean"}).
            "\$ & \$ \\pm ".
            sprintf("%7.1f", $dataRef->{"secondarySD"}).
            "\$";
#    if( ($dataRef->{"ksPValue"} < 0.05) && ($dataRef->{"primaryMean"} < $dataRef->{"secondaryMean"}) )
    if( ($dataRef->{"ttestPValue"} < 0.05) && ($dataRef->{"primaryMean"} < $dataRef->{"secondaryMean"}) )
    {
        $primaryValue .=   " & {\\bf *}~";
        $secondaryValue .= " &        ";
    }
#    elsif( $dataRef->{"ksPValue"} < 0.05 )
    elsif( $dataRef->{"ttestPValue"} < 0.05 )
    {
        $primaryValue .=   " &         ";
        $secondaryValue .= " & {\\bf *}";
    }
    else
    {
        $primaryValue .=   " &         ";
        $secondaryValue .= " &        ";
    }

    # Build the line
    my $line = "$primaryValue & $secondaryValue \\\\\n";
    
    return $line;
}

