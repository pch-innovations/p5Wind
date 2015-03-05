#!/bin/sh
ffmpeg -f image2 -framerate 60 -i bin/frames/%06d.tga -vcodec qtrle -r 60 output.mov