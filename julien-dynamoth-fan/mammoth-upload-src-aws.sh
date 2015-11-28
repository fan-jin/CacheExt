#!/bin/sh
rsync --delete-before --exclude='.svn*' -ravze "ssh -l ubuntu -i /home/julien/Dropbox/McGill-Thesis/Dynamoth/key_pair/Julien1.pem" ./ 54.179.164.143:~/Documents/mammoth_src/
