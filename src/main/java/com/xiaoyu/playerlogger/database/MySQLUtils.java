package com.xiaoyu.playerlogger.database;

import com.xiaoyu.playerlogger.Config;
import com.xiaoyu.playerlogger.PlayerLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.DEDICATED_SERVER)
public class MySQLUtils {
    private static HikariDataSource MySqlDataSource;

    public static void initMySQLConnect() {
        HikariConfig hikariConfig = new HikariConfig();

        // 从配置文件获取基础信息
        hikariConfig.setJdbcUrl(Config.DATABASE_URL.get() + "/" + Config.DATABASE_NAME.get());
        hikariConfig.setUsername(Config.DATABASE_USERNAME.get());
        hikariConfig.setPassword(Config.DATABASE_PASSWORD.get());

        // ChatGPT 推荐配置
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setConnectionTimeout(10000);

        MySqlDataSource = new HikariDataSource(hikariConfig);

        PlayerLogger.LOGGER.info("MySQL Init Connect Completed");
    }

    public static Connection getMySQLConnect() throws SQLException {
        return MySqlDataSource.getConnection();
    }

    public static void shutdownConnect() {
        MySqlDataSource.close();
    }

    public static Boolean tableExists(Connection connect, String tableName) {
        try {
            DatabaseMetaData dbMeta = connect.getMetaData();

            try (ResultSet rs = dbMeta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }

        } catch (SQLException e) {
            PlayerLogger.LOGGER.error("Check table existence failure!", e);
            return false;
        }
    }

    public static List<Map<String, Object>> searchAllTableData(Connection connect, String tableName) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (PreparedStatement pstmt = connect.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            PlayerLogger.LOGGER.error("search error!", e);
        }
        return results;
    }
}