import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // 记得改成你自己的密码！
    private static final String URL = "jdbc:mysql://localhost:3306/smart_parking_db?serverTimezone=Asia/Shanghai&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "20050214"; // <--- 只有这里需要改

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}