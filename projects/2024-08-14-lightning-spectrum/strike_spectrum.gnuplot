

# Function to convert wavelength to RGB

# UV -> gray
# IR -> dark red
wavelength_to_rgb(wl) = \
    (wl >= 280 && wl < 380) ? (220 << 16) + (200 << 8) + int(220) : \
    (wl >= 380 && wl < 440) ? (int( -(wl-440)/60.0 * 255) << 16) + (0 << 8) + int(255) : \
    (wl >= 440 && wl < 490) ? (0 << 16) + (int((wl-440)/50.0 * 255) << 8) + int(255) : \
    (wl >= 490 && wl < 510) ? (0 << 16) + (int(255) << 8) + int(-(wl-510)/20.0 * 255) : \
    (wl >= 510 && wl < 580) ? (int((wl-510)/70.0 * 255) << 16) + (int(255) << 8) + 0 : \
    (wl >= 580 && wl < 645) ? (int(255) << 16) + (int(-(wl-645)/65.0 * 255) << 8) + 0 : \
    (wl >= 645 && wl <= 780) ? (int(255-(wl-645)) << 16) + (0 << 8) + 0 : \
    (66 << 16) + (0 << 8) + 0  # Black for out of visible range


set terminal pngcairo size 800,400
set output "strike_spectrum.png"
set grid
set title "Lightning strike spectrum (diffuse atmospheric reflection through glass window).\nMeasured with C12880MA UV/vis spectrometer."
set xlabel "wavelength (nm)"
set ylabel "intensity (strike ADC value - dark ADC value)"
#plot 'strike_spectrum.dat' using 1:2 with linespoints title ''


set style ellipse 
set object 1 ellipse center 400, 880  size 60, 28  angle 0  front fillcolor "red" linewidth 2
set label 2 at  500,880 'amplifier saturation' center rotate by 0 front

plot 'strike_spectrum.dat' using 1:2:(exp(-((x-550)**2)/10000.0)):(wavelength_to_rgb($1)) with linespoints pt 7 ps 1 lc rgb variable title ""



