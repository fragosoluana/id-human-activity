import matplotlib
import squarify
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from matplotlib.ticker import MaxNLocator
from pandas.plotting import scatter_matrix
from sklearn.decomposition import PCA

''''
Name: Flaviu Vadan
Date: Mon, Jun 17th, 2016
Email: flaviuvadan@gmail.com
DISCUS Lab

Modifications by: Luana Fragoso
Date: Mon, Jun 25th, 2017
Email: luana.fragoso@usask.ca
DISCUS Lab

This code plots important features to evaluate the intrinsic dimentionality using the box-counting algorithm at
BoxDimensionality.py script. Some parts of this plots.py script are hard-coded for the purpose of analyzing the SHED7-10
datasets. You will need to modify these parts to your needs.
'''

#=============================================================================#
# QuadTree implementation by Jake VanderPlas
#=============================================================================#
#----------------------------------------------------------------------
# This function adjusts matplotlib settings for a uniform feel in the textbook.
# Note that with usetex=True, fonts are rendered with LaTeX.  This may
# result in an error if LaTeX is not installed on your system.  In that case,
# you can set usetex to False.

#from astroML.plotting import setup_text_plots
#setup_text_plots(fontsize=8, usetex=False)


# We'll create a QuadTree class which will recursively subdivide the
# space into quadrants
class QuadTree:
    """Simple Quad-tree class"""

    # class initialization function
    def __init__(self, data, mins, maxs, depth=10):
        self.data = np.asarray(data)

        # data should be two-dimensional
        assert self.data.shape[1] == 2

        if mins is None:
            mins = data.min(0)
        if maxs is None:
            maxs = data.max(0)

        self.mins = np.asarray(mins)
        self.maxs = np.asarray(maxs)
        self.sizes = self.maxs - self.mins

        self.children = []

        mids = 0.5 * (self.mins + self.maxs)
        xmin, ymin = self.mins
        xmax, ymax = self.maxs
        xmid, ymid = mids

        if depth > 0:
            # split the data into four quadrants
            data_q1 = data[(data[:, 0] < mids[0])
                           & (data[:, 1] < mids[1])]
            data_q2 = data[(data[:, 0] < mids[0])
                           & (data[:, 1] >= mids[1])]
            data_q3 = data[(data[:, 0] >= mids[0])
                           & (data[:, 1] < mids[1])]
            data_q4 = data[(data[:, 0] >= mids[0])
                           & (data[:, 1] >= mids[1])]

            # recursively build a quad tree on each quadrant which has data
            if data_q1.shape[0] > 0:
                self.children.append(QuadTree(data_q1,
                                              [xmin, ymin], [xmid, ymid],
                                              depth - 1))
            if data_q2.shape[0] > 0:
                self.children.append(QuadTree(data_q2,
                                              [xmin, ymid], [xmid, ymax],
                                              depth - 1))
            if data_q3.shape[0] > 0:
                self.children.append(QuadTree(data_q3,
                                              [xmid, ymin], [xmax, ymid],
                                              depth - 1))
            if data_q4.shape[0] > 0:
                self.children.append(QuadTree(data_q4,
                                              [xmid, ymid], [xmax, ymax],
                                              depth - 1))

    def draw_rectangle(self, ax, depth):
        """Recursively plot a visualization of the quad tree region"""
        if depth is None or depth == 0:
            rect = plt.Rectangle(self.mins, *self.sizes, zorder=2,
                                 ec='#000000', fc='none')
            ax.add_patch(rect)
        if depth is None or depth > 0:
            for child in self.children:
                child.draw_rectangle(ax, depth - 1)


def draw_grid(ax, xlim, ylim, Nx, Ny, **kwargs):
    """ draw a background grid for the quad tree"""
    for x in np.linspace(xlim[0], xlim[1], Nx):
        ax.plot([x, x], ylim, **kwargs)
    for y in np.linspace(ylim[0], ylim[1], Ny):
        ax.plot(xlim, [y, y], **kwargs)


def plot_qTree(df):
    # Extract the two columns that we want to plot (FV)
    LatLongExtract = df[['lon','lat']]
    X = [list(tuple(x)) for x in LatLongExtract.values]
    X = np.array(X)

    #------------------------------------------------------------
    # Use our Quad Tree class to recursively divide the space
    mins = (0, 0)
    maxs = (1, 1)
    QT = QuadTree(X, mins, maxs, depth=10)

    #------------------------------------------------------------
    # Plot four different levels of the quad tree
    fig = plt.figure(figsize=(5, 5))
    fig.subplots_adjust(wspace=0.1, hspace=0.15,
                        left=0.1, right=0.9,
                        bottom=0.05, top=0.9)

    for level in range(1, 5):
        ax = fig.add_subplot(2, 2, level, xticks=[], yticks=[])
        ax.scatter(X[:, 0], X[:, 1], color=colors[table - 7])
        # plt.savefig('TEST' + str(table) + '.png', dpi=500)
        QT.draw_rectangle(ax, depth=level - 1)

        Nlines = 1 + 2 ** (level - 1)
        draw_grid(ax, (mins[0], maxs[0]), (mins[1], maxs[1]),
                  Nlines, Nlines, linewidth=1,
                  color='#CCCCCC', zorder=0)

        ax.set_xlim(-0.1, 1.1)
        ax.set_ylim(-0.1, 1.1)
        ax.set_title('level %i' % (level-1), fontsize=14)

    # suptitle() adds a title to the entire figure
    fig.suptitle('SHED' + str(table), fontsize=14)
    fig.savefig('results/shed' + str(table) + '-qTreeEx.png', dpi=500)


def plot_nodes_data(x, y, table):
    '''
    Plots a scatter graph of the proportion of nodes containing data per level.

    :param x: series with the levels
    :param y: series with the percentage of nodes with data
    :param table: which SHED is being analyzed
    :return: nothing, only saves the plot
    '''

    if table == 7:
        plt.figure()
        plt.rc('xtick', labelsize=13)
        plt.rc('ytick', labelsize=13)
        plt.figure().gca().xaxis.set_major_locator(MaxNLocator(integer=True))

    plt.plot(x, y, "o", color=colors[table - 7])

    if table == 10:
        plt.yscale('log', basey=10)
        plt.legend(['SHED7', 'SHED8', 'SHED9', 'SHED10'], loc='upper right', numpoints=1)
        plt.ylabel("n_data / n_cells", fontsize=14)
        plt.xlabel("Level", fontsize=14)
        plt.savefig('results/sheds-numNodes.png', dpi=500)


def plot_correlation_matrix(df, names, table):
    '''
    Plot a colorful correlation matrix between all the variables.
    Code from: https://machinelearningmastery.com/visualize-machine-learning-data-python-pandas/

    :param df: the complete dataframe
    :param names: the name for each dimension
    :param table: which SHED is being analyzed
    :return: nothing, only saves the plot
    '''

    fig = plt.figure()
    plt.rc('xtick', labelsize=16)
    plt.rc('ytick', labelsize=16)

    correlations = df.corr()

    ax = fig.add_subplot(111)
    cax = ax.matshow(correlations, vmin=-1, vmax=1)
    fig.colorbar(cax)

    ticks = np.arange(0, 7, 1)
    ax.set_xticks(ticks)
    ax.set_yticks(ticks)
    ax.set_xticklabels(names, rotation=25)
    ax.set_yticklabels(names)

    plt.xlabel("SHED" + str(table), fontsize=16)
    fig.savefig('results/shed' + str(table) + '-corMatrix.png', dpi=500)


def plot_scatter_matrix(df, table):
    '''
    Plots a scatter plot for each dimensionality resulting in a scatter matrix.
    Code from: https://machinelearningmastery.com/visualize-machine-learning-data-python-pandas/

    :param df: the dataframe with all the data
    :param table: which shed is being analyzed
    :return: nothing, only saves the plot
    '''

    plt.figure()
    scatter_matrix(df)
    plt.title("Scatter Matrix and Histrograms")
    plt.savefig('results/shed' + str(table) + '-scatterMatrix.png', dpi=500)


def plot_treemap(df, table):
    '''
    Plot a tree map of the number of nodes with data per level.
    Code from: https://fcpython.com/visualisation/python-treemaps-squarify-matplotlib

    :param df: dataframe with nodes information per level
    :param table: which SHED is being analyzed
    :return: nothing, only saves the tree map plot
    '''

    plt.figure()

    # New dataframe, containing the interested levels. Including all the levels causes label overlapping.
    data_goals = df[df['new_nodes_data'] > 0]
    data_goals = data_goals[(data_goals["level"] > 2) & (data_goals["level"] < 15)]
    data_goals = data_goals.sort_values(by="new_nodes_data", ascending=False)

    norm = matplotlib.colors.Normalize(vmin=min(data_goals['new_nodes_data']), vmax=max(data_goals['new_nodes_data']))

    #Define the colour for each SHED
    if table == 7:
        colors = [matplotlib.cm.Reds(norm(value)) for value in data_goals['new_nodes_data']]
    if table == 8:
        colors = [matplotlib.cm.Blues(norm(value)) for value in data_goals['new_nodes_data']]
    if table == 9:
        colors = [matplotlib.cm.Purples(norm(value)) for value in data_goals['new_nodes_data']]
    if table == 10:
        colors = [matplotlib.cm.Greens(norm(value)) for value in data_goals['new_nodes_data']]

    #Create our plot and resize it.
    fig = plt.gcf()
    fig.add_subplot()
    fig.set_size_inches(6, 6)

    #Use squarify to plot our data, label it and add colours. We add an alpha layer to ensure black labels show through
    plt.rc('font', size=20)  # controls default text sizes
    squarify.plot(label=data_goals['level'], sizes=data_goals['new_nodes_data'], color=colors, alpha=.6)
    plt.title("SHED" + str(table))

    #Remove our axes and display the plot
    plt.axis('off')
    fig.savefig('results/shed' + str(table) + '-treemap.png', dpi=50)


def plot_pca(df, table):
    '''
    Plot a graph with the explained variance ratio for PCA. This function can also calculates the eigenvalues and vectors.

    :param df: dataframe with the normalized dataset used on the intrinsic dimensionality calculation (in our case, the dataset used for the n-D Tree algorithm)
    :param table: integer indicating the current SHED (7, 8, 9, or 10)
    :return: nothing, only saves the pca explained variance graph
    '''

    if(table == 7):
        plt.figure()
        plt.rc('xtick', labelsize=14)
        plt.rc('ytick', labelsize=14)

    # This vector adjusts the principal component ID.
    # Instead of showing an x-axis from 0 to 6, it shows an x-axis from 1 to 7, where 7 is the number of the dimensionality in our study.
    x = [1, 2, 3, 4, 5, 6, 7]

    pca = PCA()

    pca.fit(df)
    var_cumsum = np.cumsum(np.round(pca.explained_variance_ratio_, decimals=4)*100)

    # To check the eigen vectors, discomment this line below
    # print(pca.components_)

    # Discomment these lines below to calculate the eigenvalues.
    # Code from: https://stackoverflow.com/questions/31909945/obtain-eigen-values-and-vectors-from-sklearn-pca
    # df -= np.mean(df, axis=0)
    # cov_matrix = np.dot(df.T, df) / len(df)
    # for eigenvector in pca.components_:
    #     print(np.dot(eigenvector.T, np.dot(cov_matrix, eigenvector)))

    plt.plot(x, var_cumsum, color=colors[(table - 7)])

    if(table == 10):
        plt.legend(["SHED7", "SHED8", "SHED9", "SHED10"], loc='lower right', numpoints=1)
        plt.xlabel("Principal Component", fontsize=16)
        plt.ylabel("Explained Variance Ratio (%)", fontsize=10)
        plt.grid()
        plt.savefig('pca.png', dpi=500)


def plot_nodes_level(nodes_level, table):
    '''
    Plot a graph of the total numbers of nodes per level

    :param nodes_level: series with the number of total nodes per level
    :param table: integer indicating the current SHED (7,8,9, or 10 - for the original purpose of this study)
    :return: nothing, only saves the number of nodes graph
    '''

    if table == 7:
        plt.figure()
        plt.rc('xtick', labelsize=13)
        plt.rc('ytick', labelsize=18)
        plt.figure().gca().xaxis.set_major_locator(MaxNLocator(integer=True))

    plt.plot(nodes_level, 'o', color=colors[table - 7])
    plt.yscale('log')
    plt.grid(True)

    if table == 10:
        plt.yscale('log', basey=10)
        plt.legend(['SHED7', 'SHED8', 'SHED9', 'SHED10'], loc='upper right', numpoints=1)
        plt.ylabel("Total number of nodes", fontsize=16)
        plt.xlabel("Level", fontsize=14)
        plt.savefig('results/sheds-nodesTotal.png', dpi=500)


#=============================================================================#
# Main program - Plot all graphs
#=============================================================================#

colors = ("red", "blue", "purple", "green")
names = ["hour", "lat", "lon", "acc", "stddev", "bat", "wifi"]
for table in range(7, 11):
    iEpi_df = pd.read_csv("path to the preprocessed dataset used for the n-D Tree structure")
    dfColumnsExtract = iEpi_df[["hour", "lat", "lon", "mean", "stddev", "plugged", "Count"]]
    dfColumnsExtract = dfColumnsExtract.rename(columns={dfColumnsExtract.columns[3]: "acc"})
    dfColumnsExtract = dfColumnsExtract.rename(columns={dfColumnsExtract.columns[5]: "bat"})
    dfColumnsExtract = dfColumnsExtract.rename(columns={dfColumnsExtract.columns[6]: "wifi"})

    # Plots of the data used for the n-D Tree structure
    plot_qTree(iEpi_df.copy(deep=True))
    plot_correlation_matrix(dfColumnsExtract.copy(deep=True), names, table)
    plot_scatter_matrix(dfColumnsExtract.copy(deep=True), table)
    plot_pca(dfColumnsExtract.copy(deep=True), table)

    # Plots underlying the number of boxes/nodes per level. This file is acquired after running the BoxDimensionality.py script.
    numBoxesLevel = pd.read_csv("results/shed" + str(table) + "-numBoxesLevel.csv")

    # Discomment the line below to remove potential GPS/accel noises
    # numBoxesLevel = numBoxesLevel[numBoxesLevel['level'] < 24]
    plot_nodes_data(numBoxesLevel['level'], numBoxesLevel['new_nodes_data_perc'], table)
    plot_treemap(numBoxesLevel.copy(deep=True), table)
    plot_nodes_level(numBoxesLevel['total_nodes'], table)

