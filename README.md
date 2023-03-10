Here is a coding challenge that I found online that was interesting to do to practice my data structures and algorithm skills.

## Task

### Write a Java program to parse a given CSV file and evaluate each cell by these rules
1. Each cell is an expression in postfix notation. 
2. Each number or operation will always be separated by one or more spaces.
3. A cell can refer to another cell, via the LETTER NUMBER notation (A2, B4, etc -
letters refer to columns, numbers to rows).
4. Support the basic arithmetic operators +, -, *, /
The output will be a CSV file of the same dimensions, where each cell is evaluated to its final
value. If any cell is an invalid expression, then for that cell only print #ERR.
For example, the following CSV input:

![image](https://user-images.githubusercontent.com/66478998/223704203-148a6509-5efd-4596-b9d4-40eabb41ebe5.png)

Might output something like this:

![image](https://user-images.githubusercontent.com/66478998/223704245-44521808-275a-4c06-a588-153d5eb52c30.png)


## Code Structure
### Steps 
1.	Read file and load csv data into a 2D array of a custom “Cell” class. The Cell class stores metadata for each cell, including expression, value and fields used for internal calculation. 
2.	Apply Depth First Search to each cell in the array to calculate how many unique child cells are embedded in the cell expression, either in the direct expression, or the expressions of the child cells and so on. 
3.	Evaluate cell values by order of increasing child cell dependencies, in this way, it guarantees that every valuation only deals with numeric values, or cells that have already been evaluated.
4.	Print out result.

### Assumptions/Limitations
-	Division by zero will output infinite symbol rather than “#ERR” 
-	Both upper and lower case cell names are acceptable and interchangeable (i.e. A1 & a1)
-	Decimal outputs are rounded to 1 decimal place
