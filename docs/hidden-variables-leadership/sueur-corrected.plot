set key top left
set size 0.75,0.75
set xlabel "Moving individuals"
set ylabel "Move Probability"
set format y "%5.3f"

set xrange [0:9]
# Original Sueur
#plot (1/162.3) + (1/74.5)*x/(10) title "Default"
#replot 1.8 * (1/162.3) + 0.2* (1/74.5)*x/(10) title "Bold"
#replot 0.2 * (1/162.3) + 1.8* (1/74.5)*x/(10) title "Shy"


# Corrected Sueur
# N = 10
set xrange [0:9]
plot 1/(162.3+75.4*(10-x)/x) title "Petit and Gautrais" lw 2 lt 2 lc rgb "#000000"
replot (1/162.3) + (1/74.5)*x/(10) title "Sueur" lt 1 lw 2 lc rgb "#999933"

replot 0.000775+0.0079*(x**1.4)/(((6)**1.4+x**1.4)) title "Sueur corrected" lt 1 lw 2 lc rgb "#FF00FF"
replot 1.8*0.000775+0.2*0.0079*(x**1.4)/(((6)**1.4+x**1.4)) title "Bold - Sueur corrected" lt 1 lw 2 lc rgb "#882255"
replot 0.2*0.000775+1.8*0.0079*(x**1.4)/(((6)**1.4+x**1.4)) title "Shy - Sueur corrected" lt 1 lw 2 lc rgb "#117733"

replot 1.8*0.000775+1.8*0.0079*(x**1.4)/(((6)**1.4+x**1.4)) title "Bold - Sueur corrected" lt 1 lw 2 lc rgb "#882255"
replot 0.2*0.000775+0.2*0.0079*(x**1.4)/(((6)**1.4+x**1.4)) title "Shy - Sueur corrected" lt 1 lw 2 lc rgb "#117733"

replot 1.8*(0.000775+0.0079*(x**1.4)/(((1.8*6)**1.4+x**1.4))) title "Bold - Sueur corrected" lt 1 lw 2 lc rgb "#882255"
replot 0.2*(0.000775+0.0079*(x**1.4)/(((0.2*6)**1.4+x**1.4))) title "Shy - Sueur corrected" lt 1 lw 2 lc rgb "#117733"

replot 1.8*0.000775+0.0079*(x**1.4)/(((1.8*6)**1.4+x**1.4)) title "Bold - Sueur corrected" lt 1 lw 2 lc rgb "#882255"
replot 0.2*0.000775+0.0079*(x**1.4)/(((0.2*6)**1.4+x**1.4)) title "Shy - Sueur corrected" lt 1 lw 2 lc rgb "#117733"

replot 0.000775+0.0079*(x**1.4)/(((1.8*6)**1.4+x**1.4)) title "Bold - Sueur corrected" lt 1 lw 2 lc rgb "#882255"
replot 0.000775+0.0079*(x**1.4)/(((0.2*6)**1.4+x**1.4)) title "Shy - Sueur corrected" lt 1 lw 2 lc rgb "#117733"


set key top left
set size 0.75,0.75
set xlabel "Moving individuals"
set ylabel "Move Probability"
set format y "%5.3f"
set xrange [0:9]
plot 0.009 - 0.009 * x**(2.3)/(2**2.3+x**2.3) title "Sueur - Cancel" lt 2 lc rgb "#FF00FF"
replot 0.5*(0.009 - 0.009 * x**(2.3)/((0.5*2)**2.3+x**2.3)) title "Sueur - Cancel Bold" lt 2 lc rgb "#882255"
replot 1.8*(0.009 - 0.009 * x**(2.3)/((1.8*2)**2.3+x**2.3)) title "Sueur - Cancel Shy" lt 2 lc rgb "#117733"
replot (0.009/(1+(x/2)**2.3)) title "Petit and Gautrais - Cancel"


# N = 100
set xrange [0:99]
plot 1/(162.3+75.4*(100-x)/x) title "Follow" lc rgb "#000000"
replot 0.000775+0.008*(x**1.4)/(((60)**1.4+x**1.4)) title "Sueur corrected" lc rgb "#0000FF"
replot 1.8*0.000775+0.2*0.008*(x**1.4)/(((60)**1.4+x**1.4)) title "Bold - Sueur corrected" lc rgb "#882255"
replot 0.2*0.000775+1.8*0.008*(x**1.4)/(((60)**1.4+x**1.4)) title "Shy - Sueur corrected" lc rgb "#117733"
replot 0.001*(x**2.3)/((4)**2.3+x**2.3) title "Sueur" lc rgb "#999933"

set terminal postscript eps enhanced color "NimbusSanL-Regu,16" fontfile "/usr/share/texlive/texmf-dist/fonts/type1/urw/helvetic/uhvr8a.pfb"
set output "/tmp/tmp.eps"
replot





plot 0.009 - 0.009 * x**(2.3)/(2**2.3+x**2.3) title "Cancel" lt 2 lc rgb "#FF00FF"
replot 0.2*(0.009 - 0.009 * x**(0.2*2.3)/((0.2*2)**(0.2*2.3)+x**(0.2*2.3))) title "Cancel Bold" lt 2 lc rgb "#882255"
replot 1.8*(0.009 - 0.009 * x**(1.8*2.3)/((1.8*2)**(1.8*2.3)+x**(1.8*2.3))) title "Cancel Shy" lt 2 lc rgb "#117733"
replot 1.8*(0.009 - 0.009 * x**(1.8*2.3)/((2)**(1.8*2.3)+x**(1.8*2.3))) title "Cancel Shy" lt 2 lc rgb "#117733"
replot 0.2*(0.009 - 0.009 * x**(0.2*2.3)/((2)**(0.2*2.3)+x**(0.2*2.3))) title "Cancel Bold" lt 2 lc rgb "#882255"

replot 0.000775+0.0079*(x**1.4)/(((6)**1.4+x**1.4)) title "Follow" lt 1 lw 2 lc rgb "#FF00FF"

replot 0.2*0.000775+0.0079*(x**1.4)/(((1.8*6)**1.4+x**1.4)) title "Follow Bold" lt 1 lw 2 lc rgb "#882255"
replot 1.8*0.000775+0.0079*(x**1.4)/(((0.2*6)**1.4+x**1.4)) title "Follow Shy" lt 1 lw 2 lc rgb "#117733"


replot 0.2*0.000775+0.0079*(x**(1.4*1.8))/(((0.2*6)**(1.8*1.4)+x**(1.8*1.4))) title "Follow Shy" lt 1 lw 2 lc rgb "#117733"
replot 1.8*0.000775+0.0079*(x**(0.2*1.4))/(((1.8*6)**(0.2*1.4)+x**(0.2*1.4))) title "Follow Bold" lt 1 lw 2 lc rgb "#882255"

