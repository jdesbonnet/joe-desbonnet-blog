This is a modified version of the SoX audio utilities spectrogram generation
tool. This version supports plotting on a log frequency axis (-L) and also
allows the user to limit the frequency range of the plot (-R low:high).

Example:
sox mymusic.mp3 -n spectrogram -L -R 100:8k

More details from this blog post:
http://jdesbonnet.blogspot.ie/2014/02/sox-spectrogram-log-frequency-axis-and.html

Joe Desbonnet,
17 Feb 2014.

