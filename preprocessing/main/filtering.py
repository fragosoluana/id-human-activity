'''
Author: Luana Fragoso
Date: Mon, Jun 25th, 2017
Email: luana.fragoso@usask.ca
University of Saskatchewan - DISCUS Lab
'''

import os
import pandas as pd
import numpy as np

table = input("Which shed?")
dir = "path to dataset (SHED) you want to filter" + table + "/merged/"

for file in os.listdir(dir):
    user = pd.read_csv(dir + "/" + file)
    user = user[["hour", "lat", "lon", "Count", "plugged", "mean", "stddev"]]

    #accel
    acc_mean = np.mean(user['mean'], axis=0)
    acc_sd = np.std(user['mean'], axis=0)

    std_mean = np.mean(user['stddev'], axis=0)
    std_sd = np.std(user['stddev'], axis=0)

    user = user[((user['lon'] > -106.7649138128) & (user['lon'] < -106.52225318)) &
               ((user['lat'] > 52.058367) & (user['lat'] < 52.214608)) &
               ((user['mean'] > (acc_mean - 3 * acc_sd)) & (user['mean'] < (acc_mean + 3 * acc_sd))) &
               ((user['stddev'] > (std_mean - 3 * std_sd)) & (user['stddev'] < (std_mean + 3 * std_sd)))]

    user.to_csv(dir + "/" + file, index=False)
