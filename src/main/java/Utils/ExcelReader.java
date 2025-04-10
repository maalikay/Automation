package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel file reader utility that converts spreadsheet data into structured format.
 * Supports XLSX files and handles various data types including dates and numbers.
 * 
 * Features:
 * - Automatic type detection and conversion
 * - Empty cell handling
 * - Header validation and fallback naming
 * - Debug logging capabilities
 * - Trailing empty row detection
 */
public class ExcelReader {
    /**
     * Reads Excel file and converts it to list of row maps with header-based keys
     * 
     * @param filePath  Full path to Excel file (XLSX format)
     * @param sheetName Name of sheet to process
     * @return List of maps where each map represents a row with header-value pairs
     * @throws IOException On file read errors
     * @throws IllegalArgumentException If sheet not found
     */
    public static List<Map<String, String>> readExcel(String filePath, String sheetName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
            // Note: Currently XSSFWorkbook (XLSX only). For XLS support, use:
            // Workbook workbook = WorkbookFactory.create(fis)
            Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            
            // Debug: Find actual data end row (can be removed in production)
            int actualLastRow = findLastDataRow(sheet);
            System.out.println("[DEBUG] Actual last data row: " + actualLastRow);
            
            // Process header row with null safety
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            
            // Iterate using cell indexes for consistent column order
            int lastHeaderCell = headerRow.getLastCellNum();
            for (int j = 0; j < lastHeaderCell; j++) {
                Cell cell = headerRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                headers.add(cell.getStringCellValue());
            }
            
            // Process data rows with index-based validation
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row currentRow = sheet.getRow(i);
                Map<String, String> rowData = new LinkedHashMap<>(); // Preserves column order
                
                // Debug: Show row metadata (can be removed in production)
                if (currentRow == null) {
                    System.out.println("[WARN] Row " + i + " is null - skipping");
                    continue;
                }
                System.out.println("[DEBUG] Processing row " + i 
                    + " | First cell index: " + currentRow.getFirstCellNum() 
                    + " | Last cell index: " + currentRow.getLastCellNum());
                
                // Map cells to headers using header count for bounds safety
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = currentRow == null ? 
                        null : currentRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    
                    String header = headers.get(j);
                    if (header.isEmpty()) {
                        header = "Empty Column_" + (j + 1); // Fallback for missing headers
                    }
                    
                    rowData.put(header, getCellValueAsString(cell));
                }
                
                data.add(rowData);
            }
        }
        return data;
    }

    /**
     * Converts Excel cell content to formatted string value
     * 
     * @param cell Excel cell object
     * @return Formatted string representation of cell value
     */
    private static String getCellValueAsString(Cell cell) {
        CellType cellType = cell.getCellType();
        String value;
        
        switch (cellType) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Format dates as dd-MMM-yyyy (e.g., "01-Jan-2023")
                    java.util.Date date = cell.getDateCellValue();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                    value = dateFormat.format(date);
                } else {
                    double numValue = cell.getNumericCellValue();
                    value = cleanNumericFormat(numValue);
                }
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                // Handle formula results as numeric values
                double numValue = cell.getNumericCellValue();
                value = cleanNumericFormat(numValue);
                break;
            case BLANK:
                value = "null"; // Explicit null marker
                break;
            default:
                value = "Unsupported Cell Type";
        }
        return value;
    }
    
    /**
     * Normalizes numeric values to consistent string format
     * - Removes trailing .0 for integers
     * - Converts scientific notation to plain string
     * - Disables digit grouping (no commas)
     * 
     * @param numValue Raw numeric value
     * @return Clean string representation
     */
    private static String cleanNumericFormat(double numValue) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US));
        df.setMaximumFractionDigits(12); // Handle precise decimals
        df.setGroupingUsed(false); // Disable thousand separators
        
        String formatted = df.format(numValue);
        
        // Convert scientific notation (e.g., 1E+5) to plain format
        if (formatted.contains("E")) {
            return new BigDecimal(numValue).stripTrailingZeros().toPlainString();
        }
        return formatted;
    }
    
    /**
     * Finds the last row with actual data content
     * 
     * @param sheet Excel worksheet
     * @return Index of last non-empty row
     */
    private static int findLastDataRow(Sheet sheet) {
        int lastRow = sheet.getLastRowNum();
        while (lastRow >= 0) {
            Row row = sheet.getRow(lastRow);
            if (row != null && !isRowEmpty(row)) {
                break;
            }
            lastRow--;
        }
        return lastRow;
    }

    /**
     * Checks if a row contains no data
     * 
     * @param row Excel row
     * @return true if all cells are empty or null
     */
    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Test method for local execution and validation
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            String testFile = "src/test/resources/TestData/TestDataPsdEncrypt.xlsx";
            String sheetName = "LoginTests";
            System.out.println("Looking for file at: " + new File(testFile).getAbsolutePath());
            
            List<Map<String, String>> testData = readExcel(testFile, sheetName);
            
            System.out.println("Dry Run Results (Encrypted):");
            testData.forEach(row -> System.out.println(row));
            
            System.out.println("Accessing data from the map:");
            for(Map<String, String> row : testData) {
                String name = row.get("name");
                String password = row.get("Password");
                String date = row.get("date");
                
                String decryptedPassword = PasswordManager.decrypt(password);
                
                System.out.printf("%s (%s) has %s password encrypted and it is decrypted to %s%n", 
                                 name, date, password, decryptedPassword);
            }
        } catch (Exception e) {
            System.err.println("Dry run failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}