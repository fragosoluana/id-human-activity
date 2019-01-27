'''
Name: Flaviu Vadan
Date: Mon, Jun 17th, 2016
Email: flaviuvadan@gmail.com
DISCUS Lab

    n-Dimensional Tree implementation
            
'''
import itertools as itr
import numpy as np
import pandas as pd


#=============================================================================#
# nTree implementation
#=============================================================================#
class nDTree:

    def __init__(self, hypercube, depth=0):
        '''
        Initializer - defines the Tree 
        Parameters:
        ----------
            hypercube : vector of the form [(a0, b0), (a1, b1), ..., (a_n, b_n)]
                    where a_i, b_i = min and max values coordinates defining
                    the hypercube
            depth : default value of 0 for the root but incremented as we 
                advance further down the tree
        '''
        self.data = []
        self.children = []
        self.depth = depth
        self.hypercube = hypercube


    def _insert(self, to_insert):
        '''
        Insert data into Tree. The node hypercube coordinates will be assigned 
            based on the current hypercube
        Parameters:
        ----------
            to_insert : data tuple of the form [a0, a1, a2, ... , a_n] 
                    where a_i represents the coordinates of one point
        '''

        for each in to_insert:
            current_node = self

            #if we are not at a leaf, insert the data, then insert into the children
            while(current_node._isLeaf() is False):

                current_node.data.append(each)
                current_node = current_node._findChildThatFitsBounds(each)

            #if the current node contains more than one data point
            #split and further insert into children
            if current_node._hasData():

                #we have more than one data point/node so we split
                self._splitAndInsert(current_node, each)
            else:
                current_node.data.append(each)


    def _findChildThatFitsBounds(self, data):
        '''
        If the data point fits in any of the child nodes return the child
        Parameters:
        ----------
            data : data tuple of the form [a0, a1, a2, ... , a_n]
                where a_i represents the coordinates of one point
        '''
        for child in self.children:
            if(self._isEnclosed(data, child)):
                return child


    def _splitAndInsert(self, current_node, data_point):
        '''
        Splits the current node and inserts the data into the proper children 
            according to the coordinates
        Parameters:
        ----------
            current_node : the node that will be split 
            data_point : data to insert
        '''
        current_node._split()

        #find the right child for the first data point and append the data point
        #to the child
        for parent_data in current_node.data:
            child = current_node._findChildThatFitsBounds(parent_data)
            child.data.append(parent_data)
            if len(child.data) > 1:
                self._splitAndInsert(child, data_point)

        child = current_node._findChildThatFitsBounds(data_point)
        if not data_point in child.data:
            #now also append the data to the parent
            child.data.append(data_point)
            if len(child.data) > 1:
                self._splitAndInsert(child, data_point)


        if not data_point in current_node.data:
            #also append the data point to the current node since we want every parent
            #to contain the data points that their children contain
            current_node.data.append(data_point)


    def _isEnclosed(self, data, child):
        '''
        Check if a datapoint is within the boundaries of a hypercube   
        Parameters:
        ----------
            data : data tuple of the form [a0, a1, a2, ... , a_n]
                where a_i represents the coordinates of one point
            child : child reference 
            
        Notes:
        ----------
            The boundaries checking can be done with nested for loops but it
            produced uncosistent results so we stick with checking the boundaries
            for 7D (our case). If a solution is found for dynamically checking 
            the boundaries then the whole program will dynamically check the num.
            of dimensions and the boundaries (FV 14/07/2016)
        '''
        #special case when we have cubes that have a side coordinate of 0
        #so we allow data points to be placed exactly on the axis
        if(child.hypercube[0][0]==0 or child.hypercube[1][0]==0 or
           child.hypercube[2][0]==0 or child.hypercube[3][0]==0 or
           child.hypercube[4][0]==0 or child.hypercube[5][0]==0 or
           child.hypercube[6][0]==0 or child.hypercube[0][1]==0 or
           child.hypercube[1][1]==0 or child.hypercube[2][1]==0 or
           child.hypercube[3][1]==0 or child.hypercube[4][1]==0 or
           child.hypercube[5][1]==0 or child.hypercube[6][1]==0):
               if(data[0] >= child.hypercube[0][0] and data[0] <= child.hypercube[0][1] and
                  data[1] >= child.hypercube[1][0] and data[1] <= child.hypercube[1][1] and
                  data[2] >= child.hypercube[2][0] and data[2] <= child.hypercube[2][1] and
                  data[3] >= child.hypercube[3][0] and data[3] <= child.hypercube[3][1] and
                  data[4] >= child.hypercube[4][0] and data[4] <= child.hypercube[4][1] and
                  data[5] >= child.hypercube[5][0] and data[5] <= child.hypercube[5][1] and
                  data[6] >= child.hypercube[6][0] and data[6] <= child.hypercube[6][1]):
                      return True
        #second case when data points are within the hypercube i.e. between boundaries
        #we still allow datapoints to be placed on axes but only on one
        elif(data[0] > child.hypercube[0][0] and data[0] <= child.hypercube[0][1] and
             data[1] > child.hypercube[1][0] and data[1] <= child.hypercube[1][1] and
             data[2] > child.hypercube[2][0] and data[2] <= child.hypercube[2][1] and
             data[3] > child.hypercube[3][0] and data[3] <= child.hypercube[3][1] and
             data[4] > child.hypercube[4][0] and data[4] <= child.hypercube[4][1] and
             data[5] > child.hypercube[5][0] and data[5] <= child.hypercube[5][1] and
             data[6] > child.hypercube[6][0] and data[6] <= child.hypercube[6][1]):
               return True


    def _isLeaf(self):
        '''
        Checks if a node is a leaf. Returns true if leaf, false otherwise   
        Parameters:
        ----------
            None
        '''
        if not self.children:
            return True
        else:
            return False


    def _hasData(self):
        '''
        Checks if a tree contains data. If tree contains data, return false, 
            otherwise true
        Parameters:
        ----------
            None
        '''
        if not self.data and len(self.data) != 1:
            return False
        else:
            return True


    def _split(self):
        '''
        Splits the current node into 2^n nodes according to the number of 
            dimensions we are working with and the number of data points given
        Parameters:
        ----------
            None
        '''
        new_coordinates = self._getNewCoordinates()
        self.children = [nDTree(hypercube=coords, depth=self.depth+1) for coords in new_coordinates]


    def _getNewCoordinates(self):
        '''
        Returns the coordinates of the new hypercube
        Parameters:
        ----------
            None
        '''
        new_coords = []

        for coord in self.hypercube:
            low = coord[0]
            top = coord[1]
            mid = (low+top)/2
            new_coords.append([(low,mid), (mid,top)])

        #returning the cartesian product of the input iterables i.e. new_coords
        #itr.product is the equivalent of nested for loops
        #the loops cycle with the rightmost element advancing on every iteration
        #this pattern creates a lexicographic ordering such that if the input's
        #iterables are sorted, the resulting tuples are sorted as well
        #see itertools.product
        return (list(itr.product(*new_coords)))


    def _printTree(self, level=0):
        '''
        Prints the tree in a hierarchic fashion. Only used for testing purposes.
        Do not print trees with a great number of data points.
        Parameters:
        ----------
            level=0 : default parameter - we start with the root
        '''
        print('\t' * level + repr(self.data))
        for child in self.children:
            child._printTree(level+1)


    def _countNodes(self, depth, nodes_data=0, leaves=0, total=0):
        '''
        Returns the number of nodes containing data at a specific level
        Parameters:
        ----------
            depth : the depth at which we are counting
            count : standard value for the counter will be incremeneted and returned
    '''
        if depth == 0:
            total = 1

            if self.data:
                nodes_data = 1

                if self.children:
                    leaves = 0
                else:
                    leaves = 1
                return nodes_data, leaves, total
        else:
            for child in self.children:
                if child.depth == depth:
                    total += 1

                    if child.data:
                        nodes_data += 1

                        if len(child.data) == 1:
                            leaves += 1

                        nodes_data, leaves, total = child._countNodes(depth, nodes_data, leaves, total)
                else:
                    nodes_data, leaves, total = child._countNodes(depth, nodes_data, leaves, total)
        return nodes_data, leaves, total


    def _getEpsilon(self, treeLevel):
        '''
        Returns epsilon (side length of all the boxes at level n)  
        Parameters:
        ----------
            treeLevel : the depth for which we are computing epsilon
        '''
        return 1/(len(self.hypercube)*(2**treeLevel))


    def _buildDimensionalityTupleSet(self, treeLevel, table):
        '''
        Returns a set of tuples of the form [E, log(N(E))/log(E)]
            where E is epsilon and N(E) is the number of boxes
            of side length E (see Wiki documentation)
        Parameters:
        ----------
            treeLevel : maximum depth of tree to calculate epsilon
        '''

        numBoxesLevel = []
        print("Data size: " + str(len(self.data)))

        #we need epsilon to know N(E), where N(E) is the number of hypercubes of side length epsilon
        #and E is defined by _getEpsilon
        #we loop through and calculate epsilon for each level of the tree
        #once we have epsilon, we get the number of hypercubes of side length epsilon
        #and append it to the tuple list

        for each_level in range(0, treeLevel):
            epsilon = self._getEpsilon(each_level)
            numBoxesWithData, numLeaves, numBoxes = self._countNodes(each_level)

            if numBoxesWithData == 0:
                break

            numBoxesLevel.append((each_level, epsilon, numBoxes, numBoxesWithData, numLeaves, numBoxesWithData/numBoxes))

        numBoxesLevel = pd.DataFrame(numBoxesLevel, columns=['level', 'epsilon', 'total_nodes', 'new_nodes_data', 'leaves_data', 'new_nodes_data_perc'])
        numBoxesLevel = numBoxesLevel[numBoxesLevel['new_nodes_data'] > 0]
        numBoxesLevel['nodes_data'] = numBoxesLevel['new_nodes_data'] + numBoxesLevel['leaves_data'].cumsum().shift().fillna(0)
        numBoxesLevel['nodes_data_perc'] = numBoxesLevel['nodes_data']/numBoxesLevel['total_nodes'].cumsum()
        numBoxesLevel.to_csv("results/shed" + table + "-numBoxesLevel.csv", index=False)

        print("Number of leaves (should match the dataset size): " + str(numBoxesLevel['leaves_data'].sum()))

        numBoxesLevel = numBoxesLevel[numBoxesLevel['epsilon'] > 0]
        return pd.DataFrame({'dim_box': (np.log(numBoxesLevel['nodes_data'])/np.log(1/numBoxesLevel['epsilon'])).values,
                             'epsilon': numBoxesLevel['epsilon'].values})

