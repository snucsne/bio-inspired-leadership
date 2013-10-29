#!/bin/sh

#
# The script must be run from the directory containing the saved frames (ImagNNNN.x)
# Convert the frames to 'fmt' format (fmt can be jpg, png)
#
fmt=sgi
for i in $1*.png
do
  echo $i
  mogrify -format $fmt $i
done

#
# Uncomment this part if you have more than 1000 frame saved
#
#fixImag . $fmt > fixImag.sh
#sh fixImag.sh
#rm fixImag.sh

#
# Set the mplayer environment variables (change for your configuration)
#
LD_LIBRARY_PATH=/local/usr/lib; export LD_LIBRARY_PATH
PATH=${PATH}:/local/usr/bin; export PATH

#
# read the image dimensions
# image width and height must be multiple of 16
#
for i in $1*.$fmt
do
  img=$i
  break
done

width=`identify $img | awk '{print $3}' | awk -Fx '{print $1}'`
height=`identify $img | awk '{print $3}' | awk -Fx '{print $2}'`

#
# compute the optimal bitrate 
#	br = 50 * 25 * width * height / 256
#
# the 50 factor can vary between 40 and 60
#
obr=`expr $width \* $height \* 50 \* 25 / 256`

#
# set the MPEG4 codec options
#	you have to experiment!
#
#opt="vbitrate=$obr:mbd=2:keyint=132:v4mv:vqmin=3:lumi_mask=0.07:dark_mask=0.2:scplx_mask=0.1:tcplx_mask=0.1:naq:trell"
#codec="mpeg4"

#
# set the Microsoft MPEG4 V2 codec options
#
opt="vbitrate=$obr:mbd=2:keyint=132:vqblur=1.0:cmp=2:subcmp=2:dia=2:mv0:last_pred=3"
codec="msmpeg4v2"

#
# clean temporary files that can interfere with the compression phase
#
rm -f divx2pass.log frameno.avi

#
# compress
#
mencoder -ovc lavc -lavcopts vcodec=$codec:vpass=1:$opt -mf type=$fmt:w=$width:h=$height:fps=5 -nosound -o /dev/null mf://$1\*.$fmt
mencoder -ovc lavc -lavcopts vcodec=$codec:vpass=2:$opt -mf type=$fmt:w=$width:h=$height:fps=5 -nosound -o $2.avi mf://$1\*.$fmt

#
# cleanup
#
rm -f divx2pass.log
