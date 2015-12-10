#!/bin/bash

for f in ./*/*/*.csv; do
  case $f in
      *-*mb-[0-9]*_*.csv)
       # echo "$f"
#      sed -i -e '1,40d' "$f"
       echo "`cat ./label-and-filter.csv "$f"`" > "$f"
        ;;
  esac
done