#!/usr/bin/perl
use strict;

my $inEPS = shift( @ARGV );
my $outEPS = shift( @ARGV );


open( IN, "$inEPS" ) or die "Unable to open input EPS [$inEPS]: $!\n";
open( OUT, "> $outEPS" ) or die "Unable to open output EPS [$outEPS]: $!\n";

while( <IN> )
{
    s/TIMES-BOLD/NimbusSanL-Bold/g;
    s/Times-Roman/NimbusSanL-Regu/g;
    s/Times/NimbusSanL-Regu/g;
    s/Helvetica-BoldOblique/NimbusSanL-BoldItal/g;
    s/Helvetica-Oblique/NimbusSanL-ReguItal/g;
    s/Helvetica-Bold-iso/NimbusSanL-Bold/g;
    s/Helvetica-Bold/NimbusSanL-Bold/g;
    s/Helvetica-iso/NimbusSanL-Regu/g;
    s/Helvetica/NimbusSanL-Regu/g;
    s/Symbol/StandardSymL/g;

    print OUT $_;
}


close( IN );
close( OUT );
