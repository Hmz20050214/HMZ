import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingService {

    // 1. 验证管理员登录 (保持不变)
    public boolean login(String username, String password) {
        String sql = "SELECT * FROM admin_users WHERE username = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 【核心修改】获取所有车位详情 (包含入场时间)
     * 返回列表格式：[spot_id, spot_number, status, entry_time_str]
     */
    public List<String[]> getAllSpotsDetailed() {
        List<String[]> list = new ArrayList<>();
        // 这个复杂的 SQL 是实现“显示时长”的关键
        // 它使用了 LEFT JOIN 来查询车位信息，同时看看有没有对应的“未出场”记录
        String sql = "SELECT s.spot_id, s.spot_number, s.status, r.entry_time " +
                "FROM parking_spots s " +
                "LEFT JOIN parking_records r ON s.spot_id = r.spot_id AND r.exit_time IS NULL " +
                "ORDER BY s.spot_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String entryTimeStr = "";
                // 如果有入场时间（说明有车），转换成字符串
                Timestamp ts = rs.getTimestamp("entry_time");
                if (ts != null) {
                    entryTimeStr = ts.toLocalDateTime().toString();
                }

                list.add(new String[]{
                        String.valueOf(rs.getInt("spot_id")),
                        rs.getString("spot_number"),
                        rs.getString("status"),
                        entryTimeStr // 新增：入场时间字符串 (可能是空的)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. 车辆入场 (保持不变)
    public boolean parkIn(int spotId, String plateNum) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT status FROM parking_spots WHERE spot_id = ? FOR UPDATE";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, spotId);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && !"FREE".equals(rs.getString("status"))) {
                conn.rollback(); return false;
            }

            String insertSql = "INSERT INTO parking_records (plate_num, spot_id, entry_time) VALUES (?, ?, NOW())";
            PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setString(1, plateNum);
            psInsert.setInt(2, spotId);
            psInsert.executeUpdate();

            String updateSql = "UPDATE parking_spots SET status = 'OCCUPIED' WHERE spot_id = ?";
            PreparedStatement psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setInt(1, spotId);
            psUpdate.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }

    // 4. 车辆出场 (保持不变，演示用固定费用)
    public double parkOut(int spotId) {
        Connection conn = null;
        double fee = 15.0; // 演示固定费用
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 计算时长的逻辑在这里其实也可以做，为了演示简单，我们这里只做状态更新
            // 实际项目中，应该先查询 entry_time，算出时长，再结合费率规则算钱

            String updateRecord = "UPDATE parking_records SET exit_time = NOW(), payment = ? WHERE spot_id = ? AND exit_time IS NULL";
            PreparedStatement psRecord = conn.prepareStatement(updateRecord);
            psRecord.setDouble(1, fee);
            psRecord.setInt(2, spotId);
            psRecord.executeUpdate();

            String updateSpot = "UPDATE parking_spots SET status = 'FREE' WHERE spot_id = ?";
            PreparedStatement psSpot = conn.prepareStatement(updateSpot);
            psSpot.setInt(1, spotId);
            psSpot.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        return fee;
    }
}