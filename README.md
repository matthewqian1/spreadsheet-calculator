
Code Structure
Steps 
1.	Read file and load csv data into a 2D array of a custom “Cell” class. The Cell class stores metadata for each cell, including expression, value and fields used for internal calculation. 
2.	Apply Depth First Search to each cell in the array to calculate how many unique child cells are embedded in the cell expression, either in the direct expression, or the expressions of the child cells and so on. 
3.	Evaluate cell values by order of increasing child cell dependencies, in this way, it guarantees that every valuation only deals with numeric values, or cells that have already been evaluated.
4.	Print out result.

Assumptions/Limitations
-	Division by zero will output infinite symbol rather than “#ERR” 
-	Both upper and lower case cell names are acceptable and interchangeable (i.e. A1 & a1)
-	Decimal outputs are rounded to 1 decimal place
