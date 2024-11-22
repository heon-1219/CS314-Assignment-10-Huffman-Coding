Q) What kinds of files lead to lots of compressions?
A) First of all, files with more content led to higher compression rates. This makes sense, because if there isn't much to compress, both SCF and STF formatting and all the header work becomes pointless. Also, we found out that structured files, like the html files with much more patterns resulted in higher compression rates. 

Q) What kind of files had little or no compression?
A) Again, in terms of file length, shorter files like the small test we ran, ended up having more content after compression, losing its point. Files that were already compressed also ended up with lesser compression. We also tried running .exe files for experiment, and we expected lots of compression but we noticed that .exe actually has little compression, due to the lack of structured redundancy needed. For compression to be effective, it has to be in between VERY structured or VERY MUCH NOT structured.

Q) What happens when you try and compress a huffman code file?
A) It led to redundant compression. No additional size reduction happend. 
