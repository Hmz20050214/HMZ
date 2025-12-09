import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ParkingFrame extends JFrame {
    private JPanel gridPanel;
    private ParkingService service = new ParkingService();

    // 定义一些美化用的颜色
    private static final Color BG_DARK = new Color(45, 45, 45); // 深色背景
    private static final Color FREE_BG = new Color(225, 245, 230); // 空闲浅绿背景
    private static final Color FREE_BORDER = new Color(76, 175, 80); // 空闲深绿边框
    private static final Color OCCUPIED_BG = new Color(255, 235, 238); // 占用浅红背景
    private static final Color OCCUPIED_BORDER = new Color(229, 57, 53); // 占用深红边框

    public ParkingFrame() {
        setTitle("智能停车场监控中心 (Smart Parking Dashboard)");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. 顶部标题栏 (美化版)
        JPanel topPanel = new JPanel();
        topPanel.setBackground(BG_DARK);
        topPanel.setPreferredSize(new Dimension(getWidth(), 60));
        JLabel title = new JLabel("🅿️ 实时车位监控大屏", JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("微软雅黑", Font.BOLD, 26));
        topPanel.add(title);
        add(topPanel, BorderLayout.NORTH);

        // 2. 中间车位网格
        gridPanel = new JPanel();
        // 设置网格布局：自动换行，5列，水平间距20，垂直间距20
        gridPanel.setLayout(new GridLayout(0, 5, 20, 20));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        gridPanel.setBackground(new Color(240, 242, 245)); // 设置底色

        // 添加滚动条，防止车位太多显示不全
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 底部操作栏
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JButton btnRefresh = new JButton("🔄 刷新实时状态");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(new Color(245, 245, 245));
        btnRefresh.addActionListener(e -> loadSpots());
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        // 初始化加载数据
        loadSpots();
    }

    // 核心：读取数据库，创建自定义漂亮面板
    private void loadSpots() {
        gridPanel.removeAll(); // 清空旧面板

        // --- 重点更正：这里调用的是新的详细信息接口 ---
        List<String[]> spots = service.getAllSpotsDetailed();

        for (String[] spotData : spots) {
            int spotId = Integer.parseInt(spotData[0]);
            String spotNum = spotData[1];
            String status = spotData[2];
            String entryTimeStr = spotData[3]; // 可能为空

            // 创建我们自定义的漂亮面板
            ParkingSpotPanel spotPanel = new ParkingSpotPanel(spotId, spotNum, status, entryTimeStr);
            gridPanel.add(spotPanel);
        }

        // 强制刷新界面布局
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // ==========================================
    //  【核心美化】自定义内部类：车位显示面板
    // ==========================================
    private class ParkingSpotPanel extends JPanel {
        private int spotId;
        private String spotNum;

        public ParkingSpotPanel(int id, String num, String status, String entryTimeStr) {
            this.spotId = id;
            this.spotNum = num;

            setLayout(new BorderLayout());
            // 设置首选大小，让卡片看起来是个方形
            setPreferredSize(new Dimension(180, 150));

            // 根据状态设置样式
            boolean isOccupied = "OCCUPIED".equals(status);
            Color bgColor = isOccupied ? OCCUPIED_BG : FREE_BG;
            Color borderColor = isOccupied ? OCCUPIED_BORDER : FREE_BORDER;
            String iconEmoji = isOccupied ? "🚗" : "🅿️";

            // 1. 设置背景和边框
            setBackground(bgColor);
            // 创建一个复合边框：外层是实线，内层是空白边距
            Border lineBorder = BorderFactory.createLineBorder(borderColor, 2);
            Border emptyBorder = BorderFactory.createEmptyBorder(10, 15, 10, 15);
            setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

            // 2. 顶部：车位号 (大字体，清爽)
            JLabel numLabel = new JLabel(spotNum, JLabel.CENTER);
            numLabel.setFont(new Font("Arial Black", Font.BOLD, 22));
            numLabel.setForeground(borderColor); // 字体颜色和边框一致
            add(numLabel, BorderLayout.NORTH);

            // 3. 中间：大图标
            JLabel iconLabel = new JLabel(iconEmoji, JLabel.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            add(iconLabel, BorderLayout.CENTER);

            // 4. 底部：状态和时长
            String statusText;
            if (isOccupied && entryTimeStr != null && !entryTimeStr.isEmpty()) {
                // 计算停车时长
                try {
                    LocalDateTime entryTime = LocalDateTime.parse(entryTimeStr);
                    String durationStr = calculateDuration(entryTime);
                    statusText = "<html><center>已停: <font color=red>" + durationStr + "</font></center></html>";
                } catch (Exception e) {
                    statusText = "<html><center>已停</center></html>";
                }
            } else {
                statusText = "<html><center><font color=green>空闲可停</font></center></html>";
            }

            JLabel statusLabel = new JLabel(statusText, JLabel.CENTER);
            statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            add(statusLabel, BorderLayout.SOUTH);

            // 5. 添加鼠标点击事件 (把面板当按钮用)
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isOccupied) {
                        handleOut(spotId, spotNum);
                    } else {
                        handleIn(spotId, spotNum);
                    }
                }
                // 添加鼠标悬停效果，看起来更有交互感
                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR)); // 变小手
                    setBackground(bgColor.darker()); // 颜色加深一点
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    setBackground(bgColor); // 恢复原色
                }
            });
        }
    }

    // --- 辅助方法：计算停车时长 ---
    private String calculateDuration(LocalDateTime entryTime) {
        Duration duration = Duration.between(entryTime, LocalDateTime.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        if (hours == 0 && minutes == 0) {
            return "刚刚";
        }
        return hours + "小时" + minutes + "分";
    }

    // 处理入场逻辑
    private void handleIn(int spotId, String spotNum) {
        String plate = JOptionPane.showInputDialog(this, "准备在【" + spotNum + "】号车位停车\n请输入车牌号：");
        if (plate != null && !plate.trim().isEmpty()) {
            boolean success = service.parkIn(spotId, plate);
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ 入场成功！");
                loadSpots();
            } else {
                JOptionPane.showMessageDialog(this, "❌ 入场失败！可能被抢占。");
            }
        }
    }

    // 处理出场逻辑
    private void handleOut(int spotId, String spotNum) {
        int choice = JOptionPane.showConfirmDialog(this, "确认结算【" + spotNum + "】号车位的车辆吗？", "出场确认", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            double fee = service.parkOut(spotId);
            JOptionPane.showMessageDialog(this, "👋 离场成功！\n模拟收费： " + fee + " 元");
            loadSpots();
        }
    }

    // 主入口
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> {
            new ParkingFrame().setVisible(true);
        });
    }
}