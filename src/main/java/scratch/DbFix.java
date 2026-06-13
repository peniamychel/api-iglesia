package scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbFix {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3307/iglev3", "root", "root");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("ALTER TABLE plantilla_certificado MODIFY COLUMN configuracion_json TEXT;");
            System.out.println("Column altered successfully.");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
