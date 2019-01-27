#!/bin/bash

for dset in $datasets; do
	# echo "ssv2csv: dset is $dset"
	sort -k 1g -k 2.1d,2.19d -k 3g $dataDir/$dset/accel.ssv | tr '\t' ',' >   $dataDir/$dset/accel.csv
	sort -k 1g -k 2.1d,2.19d     $dataDir/$dset/battery.ssv | tr '\t' ',' > $dataDir/$dset/battery.csv
	sort -k 1g -k 2.1d,2.19d      $dataDir/$dset/btooth.ssv | tr '\t' ',' >  $dataDir/$dset/btooth.csv
	sort -k 1g -k 2.1d,2.19d -k 3g   $dataDir/$dset/gps.ssv | tr '\t' ',' >     $dataDir/$dset/gps.csv
	sort -k 1g -k 2.1d,2.19d        $dataDir/$dset/wifi.ssv | tr '\t' ',' >    $dataDir/$dset/wifi.csv
done
