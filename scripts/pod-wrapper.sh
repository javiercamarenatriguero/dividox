#!/bin/bash
exec env -i PATH=/opt/homebrew/bin:/usr/bin:/bin:/usr/sbin:/sbin HOME="$HOME" LANG=en_US.UTF-8 /opt/homebrew/bin/pod "$@"
