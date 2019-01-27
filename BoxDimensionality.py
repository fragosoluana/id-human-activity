'''
Name: Flaviu Vadan
Date: Mon, Jun 17th, 2016
Email: flaviuvadan@gmail.com
DISCUS Lab

Modifications by: Luana Fragoso
Date: Mon, Jun 25th, 2017
Email: luana.fragoso@usask.ca
DISCUS Lab

    Algorithm for determining the intrinsic dimensionality, or box-counting
    dimensionality, of a data set. Given a database that is stored in MySQL 
    (this can be changed by making adaptation to the code) we follow these steps:
    1. Import the database after preprocessing (aggregation, filtering, normalization)
    2. Place the database/data set into a Pandas dataframe
    3. Extract only the desired columns. The number of columns represents the
       initial number of dimensions of the data
    4. Remove duplicates
    5. Declare an n-D Tree with hypercubes' dimensions equal to the number of 
       columns of your data
    6. Insert all the data points in the n-D Tree
    7. Using the n-D Tree, calculate the number of hypercubes containing data (N(epsilon)) and the size of the
       hypercubes (epsilon)
    8. Plot the slope of the linear part of log(N(epsilon)) x log(epsilon) which is the intrinsic dimensionality
'''

#=============================================================================#
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import pickle
import os
import psutil
import time

from nDTreeOperations import Tree

#=============================================================================#
# Now we extract the columns that we wan and pickle dump the dataframe
# for faster loading later
#=============================================================================#
# Get the columns that we want
table = input("Which SHED is being analyzed? ")
df = pd.read_csv("INSERT path to the preprocessed dataset")
dfColumnsExtract = df[["hour", "lat", "lon", "Count", "plugged", "mean", "stddev"]]
dfColumnsExtract = dfColumnsExtract.drop_duplicates(keep='first')

# Now dump the dataframe into a pickle object for faster loading when graphing
with open('DimensionalityDataFrame.pickle', 'wb') as dfHandler:
    pickle.dump(dfColumnsExtract, dfHandler, protocol=pickle.HIGHEST_PROTOCOL)

# Extract the values as tuples
finalExtractOfData = [tuple(x) for x in dfColumnsExtract.values]

#=============================================================================#
# Now build the tree
#=============================================================================#
process = psutil.Process(os.getpid())
start_time = time.time()

#The number of elements in this list must match the number of dimensions in the dataset
initialHypercubeCoordinates = [(0.0, 1.0), (0.0, 1.0), (0.0, 1.0), (0.0, 1.0),
                               (0.0, 1.0), (0.0, 1.0), (0.0, 1.0)]

datasetTree = Tree(hypercube=initialHypercubeCoordinates)
datasetTree.insertIntoTree(finalExtractOfData)

# Now build the tuple set and export it to csv for later use
coordinates_set = datasetTree.buildDimensionalityTupleSet(65, table)

print("Runtime (min): " + str((time.time() - start_time)/60))
print("Memory usage (GB): " + str(round(((process.memory_info().rss)/(10**9)), 2)))

coordinates_set.to_csv("results/shed" + table + '-boxdimen.csv')

#=============================================================================#
# Now plot the slope of the log(N(epsilon)) x log(epsilon) which is the
# intrinsic dimensionality
#=============================================================================#
numBoxesLevel = pd.read_csv("results/shed" + table + "-numBoxesLevel.csv")
# We want a graph of log(N(epsilon)) x log(epsilon)
xlog = np.log(1/numBoxesLevel['epsilon'])
ylog = np.log(numBoxesLevel['nodes_data'])

dimen = pd.DataFrame({'x': xlog, 'y': ylog, 'new_nodes_data': numBoxesLevel['new_nodes_data']})
max_index = dimen['new_nodes_data'].idxmax()

# Get the linear part which is between when the number of nodes with data starts to decrease. So we calculate the slope
# of the maximum number of nodes w/ data and the number which starts to decrease and take the average slope.
dimen_1 = dimen.iloc[0:max_index+1,:]
m1, c1 = np.polyfit(dimen_1['x'], dimen_1['y'], 1)

dimen_2 = dimen.iloc[0:max_index+2,:]
m2, c2 = np.polyfit(dimen_2['x'], dimen_2['y'], 1)

# The slope (mfinal) is the intrinsic dimensionality
mfinal = (m1+m2)/2
cfinal = (c1+c2)/2

# The dots in black are the points considered for the slope calculation
plt.figure()
plt.rc('xtick', labelsize=14)
plt.rc('ytick', labelsize=14)
plt.plot(dimen['x'], dimen['y'], 'o', color='red')
plt.plot(xlog, mfinal*xlog + 0, 'black', mew=5, ms=12)
plt.title('SHED' + table, fontsize=14)
plt.xlabel(r'log(1/${\epsilon}$)', fontsize=14)
plt.ylabel(r'log(N(${\epsilon}$))', fontsize=14)
plt.ylim((0,13))
plt.legend(['slope: ' + str(round(mfinal, 4))], loc='lower right', numpoints=1, markerscale=0, handlelength=0)
plt.plot(dimen_2['x'], dimen_2['y'], 'o', color='black')
plt.grid(True)
plt.savefig('results/shed' + table + '-id.eps', format='eps', dpi=500)
