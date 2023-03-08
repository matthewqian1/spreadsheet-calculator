import java.io.BufferedReader;
import java.text.DecimalFormat;
import java.util.*;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application  {

    public static List<List<Cell>> records = new ArrayList<>();
    public static List<Cell> cellsByOrderOfIncreasingCellDependencies = new ArrayList<>();
    public static final String ERROR_MSG = "#ERR";
    public static final DecimalFormat format = new DecimalFormat("0.#");
    public static final List<String> operators = Arrays.asList("+", "-", "*", "/");

    public static void main (String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing file name argument");
        }
        loadGrid(args[0]);
        populateCellDependencies();
        evaluateCells();
        printResult();
    }

    static class Cell {
        public String name;
        public String expression;
        public boolean hasError;
        //property to detect for infinite loops
        public boolean currentlyInDFSSearch;
        //evaluated expression
        public String value;
        //number of child cells within the cells expanded expression (i.e. number of unique cells visited from
        //recursively searching through cells within the expression)
        public Set<String> uniqueCellDependencies;

        public Cell (String expression) {
            this.expression = expression;
        }
    }

    //--------------------------------------- Core Functions -------------------------------------------//

    private static void loadGrid(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<Cell> cellRow = new ArrayList<>();
                for (String value : values) {
                    cellRow.add(new Cell(value));
                }
                records.add(cellRow);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * prints the result to STDOUT
     */
    private static void printResult() {
        for (List<Cell> record : records) {
            for (int col = 0; col < record.size(); col++) {
                Cell cell = record.get(col);
                System.out.print(cell.value);
                if (col != record.size() - 1) {
                    System.out.print(",");
                }
            }
            System.out.println();
        }
    }

    /**
     * evaluate each cell, starting with the cell which has the least child cells within its expanded expression.
     */
    private static void evaluateCells() {
        cellsByOrderOfIncreasingCellDependencies.sort((c1, c2) -> c1.uniqueCellDependencies.size() - c2.uniqueCellDependencies.size());
        for (Cell cell : cellsByOrderOfIncreasingCellDependencies) {
            if (cell.hasError) {
                cell.value = ERROR_MSG;
                continue;
            }
            evaluatePostfix(cell);
        }
    }

    public static void evaluatePostfix(Cell cell)
    {
        try {
            Stack<Double> stack = new Stack<>();

            //process elements one at a time
            for (String elem : cell.expression.split("\\s+")) {
                if (elem.length() == 0) {
                    continue;
                }

                //if element is a number, push to the stack
                if (isNumeric(elem)) {
                    stack.push(Double.parseDouble(elem));
                } else if (isCellName(elem)) {
                    String elemCellValue = getCellByName(elem).value;
                    //a child cell could have generated an error through the evaluation step, check for this
                    if (elemCellValue.equals(ERROR_MSG)) {
                        cell.value = ERROR_MSG;
                        return;
                    }
                    stack.push(Double.parseDouble(elemCellValue));
                } else {
                    // element is an operator, take the next two off the stack, evaluate and add back to the stack
                    double val1 = stack.pop();
                    double val2 = stack.pop();

                    switch (elem) {
                        case "+" -> stack.push(val2 + val1);
                        case "-" -> stack.push(val2 - val1);
                        case "/" -> stack.push(val2 / val1);
                        case "*" -> stack.push(val2 * val1);
                    }
                }
            }

            String result = format.format(stack.pop());
            //if stack isnt empty, the experession must have an incorrect format, set error message.
            cell.value = stack.empty() ? result : ERROR_MSG;
        } catch (EmptyStackException e) {
            cell.value = ERROR_MSG;
        }
    }

    /**
     * For every cell in the grid, calculate how many child cells each contains within its expanded expression,
     * terminating only when it reaches a cell has an invalid expression or valid expression consisting of only numbers.
     */
    private static void populateCellDependencies() {
        for (int row = 0; row < records.size() ; row++) {
            for (int col = 0; col < records.get(row).size(); col++) {
                Cell cell = records.get(row).get(col);
                cell.name = toCellName(col, row);
                calcCellDependenciesDFS(cell);
            }
        }
    }


    private static Set<String> calcCellDependenciesDFS(Cell cell) {
        Set<String> uniqueCellDependencies = new HashSet<>();
        String[] elements = cell.expression.split("\\s+");
        cell.currentlyInDFSSearch = true;
        for (String elem : elements) {
            if (!isValidElement(elem) || elem.equalsIgnoreCase(cell.name)) {
                cell.hasError = true;
                continue;
            }
            if (isCellName(elem)) {
                try {
                    Cell childCell = getCellByName(elem);
                    if (childCell.currentlyInDFSSearch) {
                        //cell is part of the same DFS search, indicating infinite loop
                        cell.hasError = true;
                        break;
                    }
                    uniqueCellDependencies.add(elem);
                    //if cell dependencies have already been calculated (non null), no need to search deeper. (indicates this cell has already been "visited")
                    uniqueCellDependencies.addAll(Objects.requireNonNullElseGet(childCell.uniqueCellDependencies, () -> calcCellDependenciesDFS(childCell)));
                    cell.hasError = childCell.hasError || cell.hasError;
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    cell.hasError = true;
                }
            }
        }

        cell.uniqueCellDependencies = uniqueCellDependencies;
        cell.currentlyInDFSSearch = false;
        cellsByOrderOfIncreasingCellDependencies.add(cell);
        return uniqueCellDependencies;
    }

    //---------------------------------------Helper Functions -------------------------------------------//

    private static boolean isValidElement(String elem) {
        //check element in expression is valid
        return elem.length() == 0 || operators.contains(elem) || isCellName(elem) || isNumeric(elem);
    }

    private static boolean isCellName(String expression) {
        //check this string is a cell name e.g. A1
        Pattern cellNameMatcher = Pattern.compile("^[a-zA-Z]+[\\d]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = cellNameMatcher.matcher(expression);
        return expression.length() > 0 && matcher.find();
    }

    private static boolean isNumeric(String expression) {
        boolean valid = true;
        try {
            Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            valid = false;
        }
        return valid && expression.matches("^-?[0-9]\\d*(\\.\\d+)?$");
    }

    private static Cell getCellByName(String cellName) {
        //e.g. get cell A1 from 2D array
        int col = Character.toLowerCase(cellName.charAt(0)) - 'a';
        int row = Integer.parseInt(cellName.substring(1)) - 1;
        return records.get(row).get(col);
    }

    private static String toCellName(int colNumber, int rowNumber) {
        //e.g. from (0,0) to A1
        StringBuilder sb = new StringBuilder();
        colNumber++;
        while (colNumber-- > 0) {
            sb.append((char)('A' + (colNumber % 26)));
            colNumber /= 26;
        }
        return sb.reverse().toString() + (rowNumber + 1);
    }



}