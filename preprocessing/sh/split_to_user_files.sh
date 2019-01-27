#!/bin/bash

mysplit() {
    
    dataDir=$1
         db=$2
        tbl=$3
        dir=$dataDir/$db/$tbl

    # make sure $dir exists
    mkdir -p $dir
    # first remove all files in $dir
    #rm -rf $dir/*

    gawk -F',' -v dir=$dir 'NR>1{print > dir"/"$1".csv"}' $dir.csv
    
    # copy the header to individual files
    for user_file in $( ls "$dir" ); do
        head -n 1 $dir.csv > tmpfile
        cat $dir/$user_file >> tmpfile
        mv tmpfile $dir/$user_file
    done
}

for db in $datasets; do
    for tbl in $tables; do
        echo "splitting into user files: $db/$tbl";
        mysplit $dataDir $db $tbl
    done
done

