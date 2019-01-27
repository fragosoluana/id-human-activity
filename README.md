# Intrinsic Dimensionality

This project calculates the intrinsic dimensionality (ID) of a dataset based on the box-counting dimension formula (https://en.wikipedia.org/wiki/Minkowski%E2%80%93Bouligand_dimension). The steps to get the ID value are as follow:

1. Get your raw datasets

2. Preprocessing
    
    2.1. Modify the directory paths at DataSpec
    
    2.2. Generate the jar file
    
    2.3. Export the variables and run the commands from *sh/run.sh*

3. Box-Counting algorithm
    
    3.1. Modify the directory path
    
    3.2. Adapt the code to the number of dimensions you are working with
    
    3.3. Run *box-counting/Dimensionality/BoxDimensionality.py*

## Dataset
This code was built to find the ID for the smartphone sensor metrics from the SHED7-10 datasets. Therefore, some parts of the code are hard-coded, which you will need to change depending on your needs.

## Preprocessing
The preprocessing steps were developed in Java to aggregate, merge, and normalize the datasets. The code is at the *preprocessing* folder. First, you need to generate the jar file of the Java files and execute only the commands from *sh/run.sh*. I recommend to run command by command of this script, instead of running the whole script at once, since the paths and tables will be different depending on your needs. In the *run.sh* file and other files from the project, the directory path needs to be changed to where you want to store your databases and the generated files from this project. After running all the commands from *sh/run.sh*, you are going to notice new folders and files in the directory that you have provided. Under each folder on your directory, you are going to find the *merged* folder, where there are files of each participant and all participants with all the tables merged (GPS, accel, and so on), filtered, and normalized.

## Box-Counting
The dataset resulted from the preprocessing step above is used for the box-counting algorithm at *BoxDimensionality.py*. To run this script, you need to set the path to your preprocessed datasets and the number of elements of the *initialHypercubeCoordinates* variable to the same number of dimensions you are considering in your study (in our case, we used 7 dimensions: lat, lon, hour, wifi count, battery status, accel, stddev accel). You also need to change the number of levels that you think your tree will produce (you might need to run this script sometimes to figure out this number): *datasetTree.buildDimensionalityTupleSet(65, table)*, where 65 is the maximum number of levels we used in our study. The last modification is to set the number of conditions in the *_isEnclosed()* function at *nDTreeImplementation.py* to the same number of dimensions you are working with. At the end, the *BoxDimensionality.py* saves a plot with the ID value and some files that contain information of the n-D Tree structure at the *result* folder. You can also have more details about the box-counting results by plotting the many graphs/figures available at *plots.py*.

### Any question you may have, please contact: luana.fragoso@usask.ca

