#!/usr/bin/perl
use strict;
use Data::Dumper;


# -------------------------------------------------------------------
# Get the data file
my $dataFile = shift( @ARGV );

# Get the number format string
my $formatString = shift( @ARGV );

# Get the threshold value
my $threshold = shift( @ARGV );

# -------------------------------------------------------------------
# Load the data file
my %data;
loadDataFile( $dataFile, \%data );


# -------------------------------------------------------------------
# Build the table header
my $header = buildTableHeader( \%data );
print $header;
print "\\hline\n\\hline\n";


# -------------------------------------------------------------------
# Build the rows organized by individual count
foreach my $indCount (sort keys( %{$data{"summary"}} ) )
{
    my $row = buildIndCountRow( $indCount, \%data, $formatString, $threshold );
    print $row;
    print "\\hline\n";
}

print "\\end{tabular}\n";



# ===================================================================
sub loadDataFile
{
    my ($dataFile, $dataRef) = @_;

    # Open the data file
    open( DATA, "$dataFile" ) or die "Unable to open data file [$dataFile]: $!\n";

    # Read each line
    while( <DATA> )
    {
        # Remove any other comments or whitespace
        s/#.*//;
        s/^\s+//;
        s/\s+$//;
        next unless length;

        my ($key,$value) = split( /\s+=\s+/, $_ );

            # Yup.  Is it summary data?
            if( $key =~ /summary/ )
            {
                # Yup.  Parse out information from the key
                $key =~ /personality-(.+)\.indcount-(\d+)\.(.+)/;
                my $personality = $1;
                my $indCount = $2;
                my $type = $3;
if( $type =~ /se/ )
{
    print "key=[$key] indCount=[$indCount] personality=[$personality] type=[$type] value=[$value]\n";
}

                # Store it
                $dataRef->{"summary"}{$indCount}{$personality}{$type} = $value;
            }

            # Is it the t-test?
            elsif( $key =~ /t-test.*p-value/ )
            {
                # Get the information from the key
                $key =~ /personality-(.*)-indCount-(\d+).*personality-(.*)-indCount-(\d+)/;
                my $firstPersonality = $1;
                my $firstIndCount = $2;
                my $secondPersonality = $3;
                my $secondIndCount = $4;

                # Performa a sanity check
                if( $firstIndCount != $secondIndCount )
                {
#                    print "ERROR: firstIndCount=[$firstIndCount] != secondIndCount=[$secondIndCount]\n";
                    next;
                }
print "firstIndCount=[$firstIndCount] firstPersonality=[$firstPersonality] secondPersonality=[$secondPersonality] value=[$value]\n";
                $dataRef->{"t-test"}{$firstIndCount}{$firstPersonality}{$secondPersonality} = $value;
            }
    }

    # Close the data file
    close( DATA );
}


# ===================================================================
sub buildTableHeader
{
    my ($dataRef) = @_;

    my $columnCount = 1;
    my $headerRow1 = "{\\bf Group}";
    my $headerRow2 = "{\\bf Size}";

    # We don't care about the actual indcounts, just grab the first one
    my @indCounts = keys( %{$dataRef->{"summary"}} );
    my $indCount = $indCounts[0];

    # Walk through all the personalities
    my @personalities = ("None", "0.2", "0.5", "0.8");
#    foreach my $personality (sort keys( %{$dataRef->{"summary"}{$indCount}} ) )
    foreach my $personality (@personalities)
    {
        # Use the linguistic term if applicable
        $headerRow1 .= " & ";
        $headerRow2 .= " & {\\bf ".getPersonalityName( $personality )."}";
        $columnCount++;
    }

    # Walk through all the t-test combinations
    @personalities = ("0.2", "0.5", "0.8");
#    foreach my $firstPersonality (sort keys( %{$dataRef->{"summary"}{$indCount}} ) )
    foreach my $firstPersonality (@personalities)
    {
        foreach my $secondPersonality (sort keys ( %{$dataRef->{"t-test"}{$indCount}{$firstPersonality} }  ) )
        {
            $headerRow1 .= " & {\\bf ".getPersonalityName( $firstPersonality )." vs.}";
            $headerRow2 .= " & {\\bf ".getPersonalityName( $secondPersonality )."}";
            $columnCount++;
        }
    }

    # End the header
    $headerRow1 .= "\\\\\n";
    $headerRow2 .= "\\\\\n";

    # Build the column string
    my $columnStr = 'c' x $columnCount;
    my $tabular = "\\begin{tabular}{$columnStr}\n\\hline\n";

    return $tabular.$headerRow1.$headerRow2;
}


# ===================================================================
sub buildIndCountRow
{
    my ($indCount, $dataRef, $formatString, $threshold ) = @_;

    # Get the relevant data
    my %indCountSummaryData = %{$dataRef->{"summary"}{$indCount}};
    my %indCountTTestData = %{$dataRef->{"t-test"}{$indCount}};
#print "TTestData [".ref(%indCountTTestData)."]\n";
#print Dumper(%indCountTTestData);

    # Build the row
    $indCount =~ s/^0*//;
    my $row = $indCount;

    # Iterate through all the personality values
    my @personalities = ("None", "0.2", "0.5", "0.8");
#    foreach my $personality (sort keys( %indCountSummaryData ) )
    foreach my $personality (@personalities)
    {
        $row .= " & ".
                sprintf( $formatString, $indCountSummaryData{$personality}{"mean"} ).
                " \$\\pm\$ ".
                sprintf( $formatString, $indCountSummaryData{$personality}{"se"} );
    }

    # Iterate through all the t-test p-values
#    foreach my $firstPersonality (sort keys( %indCountSummaryData ) )
    @personalities = ("0.2", "0.5", "0.8");
    foreach my $firstPersonality (@personalities)
    {
#print "firstPersonality=[$firstPersonality]\n";
        foreach my $secondPersonality (sort keys ( %{$indCountTTestData{$firstPersonality} }  ))
        {
#print "firstPersonality=[$firstPersonality] secondPersonality=[$secondPersonality]\n";
            my $pvalue = $indCountTTestData{$firstPersonality}{$secondPersonality};
            if( $pvalue =~ /NaN/ )
            {
                $pvalue = 1;
            }
            elsif( $pvalue < $threshold )
            {
                $pvalue = "\$< $threshold\$";
            }
            else
            {
                $pvalue = sprintf( $formatString, $pvalue );
            }
            $row .= " & ".$pvalue;
        }
    }    

    # End the row
    $row .= "\\\\\n";

    return $row;
}

# ===================================================================
sub getPersonalityName
{
    my ($personality) = @_;

    my $name = $personality;
    if( $personality =~ /2/ )
    {
        $name = "Shy";
    }
    elsif( $personality =~ /5/ )
    {
        $name = "Moderate";
    }
    elsif( $personality =~ /8/ )
    {
        $name = "Bold";
    }

    return $name;
}
