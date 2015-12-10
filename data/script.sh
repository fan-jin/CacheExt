#!/bin/bash

for f in ./*/*/*.csv; do
  case $f in
      *-*MB-[0-9]*_*.csv)
#        echo $f
      sed -i -e '1,40d' "$f"
        ;;
  esac
done