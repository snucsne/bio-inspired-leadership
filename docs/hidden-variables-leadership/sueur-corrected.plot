set xrange [0:9]
# Original Sueur
#plot (1/162.3) + (1/74.5)*x/(10) title "Default"
#replot 1.8 * (1/162.3) + 0.2* (1/74.5)*x/(10) title "Bold"
#replot 0.2 * (1/162.3) + 1.8* (1/74.5)*x/(10) title "Shy"


# Corrected Sueur
plot 1/(162.3+75.4*(10-x)/x) title "Follow"
replot 0.001*(x**2.3)/((4)**2.3+x**2.3) title "Sueur"
replot 0.000775+0.008*(x**1.5)/(((6)**1.5+x**1.5)) title "Sueur corrected"
replot 1.8*0.000775+0.2*0.008*(x**1.5)/(((6)**1.5+x**1.5)) title "Bold - Sueur corrected"
replot 0.2*0.000775+1.8*0.008*(x**1.5)/(((6)**1.5+x**1.5)) title "Shy - Sueur corrected"

replot 0.009 - 0.009 * x**(2.3)/(2**2.3+x**2.3) title "Cancel"

