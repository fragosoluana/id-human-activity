'''
Name: Flaviu Vadan
Date: Mon, Jun 17th, 2016
Email: flaviuvadan@gmail.com
DISCUS Lab

    User methods for n-D Tree
    
'''

from nDTreeImplementation import nDTree

class Tree(nDTree):
    
    def _init(self, hypercube, depth=0):
        '''
        Initializer - defines the Tree    
        Parameters:
        ----------
            hypercube : vector of the form [(a0, b0), (a1, b1), ..., (a_n, b_n)]
                    where a_i, b_i = min and max values coordinates defining
                    the hypercube
        '''
        self.data = []
        self.children = []
        self.depth = depth
        self.hypercube = hypercube
        
        
    def insertIntoTree(self, elementsToInsert):
        '''
        Insert data into the Tree
        Parameters:
        ----------
            elementsToInsert : data tuple of the form [a0, a1, a2, ... , a_n]
                            where a_i are the coordinates of every point
        '''
        self._insert(elementsToInsert)
                  

    def buildDimensionalityTupleSet(self, treeLevel, table):
        '''
        Returns a set of tuples of the form [log(N(E)),log(E)], E is epsilon
        Parameters:
        ----------
            treeLevel : level of tree for calculation of epsilon
        '''
        return self._buildDimensionalityTupleSet(treeLevel, table)
        

