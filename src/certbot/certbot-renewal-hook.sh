#!/bin/sh

#
# CONFIG
#

ROYAL_UR_SERVER_PID_FILE="~/royal_ur_server_pid.txt"


#
# HOOK
#

kill -HUP "$(PID_FILE)"
