#!/bin/bash

# Set scan options
# This project doesn't require any special scan settings
scanOpts="-scan" 

# Load and execute actual scan script from GitHub
curl -s https://raw.githubusercontent.com/fortify-ps/gradle-helpers/1.0/fortify-scan.sh | bash -s - ${scanOpts}
