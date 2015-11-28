#!/bin/sh
#rsync -v -e ssh mammoth-test.jar jgasco2@ubuntu.cs.mcgill.ca:~/Documents/mammoth2/
rsync -ravze "ssh -l jgasco2" ./ linux.cs.mcgill.ca:~/Documents/mammoth_src/
