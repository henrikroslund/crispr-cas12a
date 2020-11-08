#!/bin/bash

sourceFile="Cas12a_gRNAs_10xgenomes_-_All.csv"
ls -l "$sourceFile"

output=output/Sun\ Nov\ 01\ 23:11:26\ PST\ 2020
rm result.tmp
index=0
iteration=0

cat $sourceFile | tr -d '\r' | while read line || [[ -n $line ]];
do
  result=$(grep -lr "$line" "$output" | wc -l)
  if [ $result -ge 9 ]; then
    printf "$line $result \n$(grep -lr "$line" "$output") \n" >> result.tmp
  fi
  ((iteration++))
  if [ $iteration -eq 100 ]; then
    iteration=0
    ((index++))
    echo "$((index*100))"
  fi
done