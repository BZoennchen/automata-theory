# Equivalence test for Linear Tree-to-Word Transducers
The goal of the project was to implement an algorithm with polynomial time complexity for testing the equivalence of linear tree-to-word transducers. To achieve this goal it was necessary to implement a fast equivalence test for straight line programs (SLPs). These SLPs are special context free grammars generating only one word. They are used to compress text. The implementation is based on the paper [Faster fully compressed pattern matching by recompression](https://arxiv.org/abs/1111.3244)

The next step was to solve the morphism equivalence problem for context free languages which is decribed in the [Ph.D. thesis](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.36.8729&rep=rep1&type=pdf) of Wojciech Plandowski. Combined with an reduction it is possible to reduce the equivalence problem of sequential tree-to-word transdsequential tree-to-word transducersucers to this problem. 

The last step was to reduce the orignal problem to the equivalence problem. This reduction is described in [Deciding Equivalence of Linear Tree-to-Word Transducers in Polynomial Time](https://arxiv.org/abs/1606.03758). 
