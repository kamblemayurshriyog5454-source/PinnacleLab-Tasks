import backend.DBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class ShowDB {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("       FULL DATABASE DUMP");
        System.out.println("=========================================");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Could not connect to database.");
                return;
            }

            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet tables = dbMeta.getTables(null, null, "%", new String[] {"TABLE"});
            
            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }

            if (tableNames.isEmpty()) {
                System.out.println("No tables found in the database.");
            } else {
                for (String tableName : tableNames) {
                    printTable(conn, tableName);
                    System.out.println("\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printTable(Connection conn, String tableName) {
        System.out.println(">>> TABLE: " + tableName + " <<<");
        try {
            String query = "SELECT * FROM " + tableName;
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Print Header
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("%-20s", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("-".repeat(colCount * 20));

            // Print Rows
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    if (val == null) val = "NULL";
                    // Truncate long text for display to keep alignment decent
                    if (val.length() > 18) {
                        val = val.substring(0, 15) + "...";
                    }
                    System.out.printf("%-20s", val);
                }
                System.out.println();
            }

            if (rowCount == 0) {
                System.out.println("(Empty Table)");
            } else {
                System.out.println("Total Rows: " + rowCount);
            }

        } catch (Exception e) {
            System.out.println("Error reading table " + tableName + ": " + e.getMessage());
        }
    }
}
