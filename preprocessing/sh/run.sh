#!/bin/bash

# ********** CONFIGURATION
# CONFIG >>
export dataDir="path to the folder which contains all the shed datasets"
export datasets="SHED7"
export tables="wifi battery accel gps"
# export datasets=(SHED7 SHED8 SHED9 SHED10)
# << CONFIG


# ********** GET DATA FROM DATABASE

# ********** CONVERT 2 CSV
sh ./ssv2csv.sh


# ********** SPLIT INTO USER FILES
sh ./split_to_user_files.sh


# ********** CONVERT TIMESTAMP 2 DC
java -cp datasets/dist/mobility_analysis.jar dimensionality.main.TimeConverter


# ********** MERGE
java -cp datasets/dist/mobility_analysis.jar dimensionality.main.Merger


# ********** FILTER
python3 datasets/data/filtering.py


# ********** NORMALIZE
java -cp datasets/dist/mobility_analysis.jar dimensionality.main.Normalizer


# concatenate all participants' records:
fileName="$(find $dataDir/$datasets/merged/*.csv.normalized | head -n 1)"
head -1 $fileName > $dataDir/$datasets/merged/all_participants.normalized
tail -n +2 -q $dataDir/$datasets/merged/*.csv.normalized >> $dataDir/$datasets/merged/all_participants.normalized
mv $dataDir/$datasets/merged/all_participants.normalized $dataDir/$datasets/merged/all_participants.csv.normalized
