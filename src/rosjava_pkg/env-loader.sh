#!/bin/bash

echo "Env loader"

export GSCAM_CONFIG="v4l2src device=/dev/video0 ! video/x-raw-rgb, width=160, height=120 ! ffmpegcolorspace"
export DISPLAY=":0"

exec "$@"