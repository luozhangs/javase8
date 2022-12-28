package com.zhang.db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhang on 2019/2/26.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;
    private static Map<String,Object> propMap = new HashMap<>();

    static {
        Map<String, String> map = new HashMap();
        try {
            Yaml yaml = new Yaml();
            propMap = (Map) yaml.load(new FileInputStream(new File(DBConnection.class.getResource("/db.yml").getFile())));
            propMap = (Map) propMap.get("datasource");
            if (propMap != null) {
                map = (Map<String, String>) propMap.get("master");
                url = map.get("url");
                username = map.get("username");
                password = map.get("password");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主库
     *
     * @return
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 其他数据源
     *
     * @param key
     * @return
     */
    public static Connection getConnection(String key) {
        Connection conn = null;
        Map<String, String> map = (Map<String, String>) propMap.get(key);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(map.get("url"), map.get("username"), map.get("password"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }


    public static List<JSONObject> query(String sql, Object... obj) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        List<JSONObject> objects = new ArrayList<>();
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    pstm.setObject(i + 1, obj[i]);
                }
            }
            resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                JSONObject object = new JSONObject();
                ResultSetMetaData rsMeta = resultSet.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    object.put(rsMeta.getColumnLabel(i), resultSet.getObject(i));
                }
                objects.add(object);
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            closeAll(conn, pstm, resultSet);
        }

        return objects;
    }


    public static <T> T query(String sql, Object param, Class<T> result) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        List<Map> objects = new ArrayList<>();
        List<Object> list = new ArrayList<>();
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql.replaceAll("#\\{\\w+\\}", "?"), Statement.RETURN_GENERATED_KEYS);
            //判断参数类型
            if (param instanceof String || param instanceof Long || param instanceof Integer) {
                pstm.setObject(1, param);
            } else {
                Pattern p = Pattern.compile("#\\{\\w+\\}");
                Matcher m = p.matcher(sql);
                if (param != null) {
                    JSONObject object = JSON.parseObject(JSON.toJSONString(param));
                    int i = 1;
                    while (m.find()) {
                        String s = m.group();
                        pstm.setObject(i, object.get(s.substring(2, s.length() - 1)));
                        i++;
                    }
                }
            }
            resultSet = pstm.executeQuery();
            try {
                while (resultSet.next()) {
                    //判断返回类型
                    Map<String,Object> object = new HashMap<String,Object>();
                    Object model = null;
                    if (result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
                        object = new JSONObject();
                    } else if (!result.getTypeName().contains("java.lang")) {
                        model = result.newInstance();
                    }
                    ResultSetMetaData rsMeta = resultSet.getMetaData();
                    int columnCount = rsMeta.getColumnCount();
                    if (columnCount == 1) {
                        model = resultSet.getObject(1);
                    } else {
                        for (int i = 1; i <= columnCount; i++) {
                            if (model != null) {
                                Field field = result.getDeclaredField(rsMeta.getColumnLabel(i));
                                field.setAccessible(true);
                                field.set(model, resultSet.getObject(i));
                            } else {
                                object.put(rsMeta.getColumnLabel(i), resultSet.getObject(i));
                            }

                        }
                    }

                    list.add(model);
                    objects.add(object);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, pstm, resultSet);
        }
        if (result.getTypeName().equals("java.util.Map") || result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
            return (T) objects;
        } else {
            return (T) list;
        }
    }

    public static <T> T query(String sql, Object param, Class<T> result, String key) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        List<Map> objects = new ArrayList<>();
        List<Object> list = new ArrayList<>();
        try {
            conn = getConnection(key);
            pstm = conn.prepareStatement(sql.replaceAll("#\\{\\w+\\}", "?"), Statement.RETURN_GENERATED_KEYS);
            //判断参数类型
            if (param instanceof String || param instanceof Long || param instanceof Integer) {
                pstm.setObject(1, param);
            } else {
                Pattern p = Pattern.compile("#\\{\\w+\\}");
                Matcher m = p.matcher(sql);
                if (param != null) {
                    JSONObject object = JSON.parseObject(JSON.toJSONString(param));
                    int i = 1;
                    while (m.find()) {
                        String s = m.group();
                        pstm.setObject(i, object.get(s.substring(2, s.length() - 1)));
                        i++;
                    }
                }
            }
            resultSet = pstm.executeQuery();
            try {
                while (resultSet.next()) {
                    //判断返回类型
                    Map<String, Object> object = new HashMap<>();
                    Object model = null;
                    if (result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
                        object = new JSONObject();
                    } else if (!result.getTypeName().contains("java.lang")) {
                        model = result.newInstance();
                    }
                    ResultSetMetaData rsMeta = resultSet.getMetaData();
                    int columnCount = rsMeta.getColumnCount();
                    if (columnCount == 1) {
                        model = resultSet.getObject(1);
                    } else {
                        for (int i = 1; i <= columnCount; i++) {
                            if (model != null) {
                                Field field = result.getDeclaredField(rsMeta.getColumnLabel(i));
                                field.setAccessible(true);
                                field.set(model, resultSet.getObject(i));
                            } else {
                                object.put(rsMeta.getColumnLabel(i), resultSet.getObject(i));
                            }

                        }
                    }

                    list.add(model);
                    objects.add(object);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, pstm, resultSet);
        }
        if (result.getTypeName().equals("java.util.Map") || result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
            return (T) objects;
        } else {
            return (T) list;
        }
    }

    public static Object queryOne(String sql, Object... obj) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        Object o = null;
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    pstm.setObject(i + 1, obj[i]);
                }
            }
            resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                ResultSetMetaData rsMeta = resultSet.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                if (columnCount == 1) {
                    o = resultSet.getObject(1);
                } else {
                    JSONObject object = new JSONObject();
                    for (int i = 1; i <= columnCount; i++) {
                        object.put(rsMeta.getColumnLabel(i), resultSet.getObject(i));
                    }
                    o = object;
                }

            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            closeAll(conn, pstm, resultSet);
        }

        return o;
    }


    public static <T> T queryOne(String sql, Object param, Class<T> result) {
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        Map<String,Object> map = new HashMap<>();
        Object model = null;
        if (result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
            map = new JSONObject();
        } else if (!result.getTypeName().contains("java.lang")) {
            try {
                model = result.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql.replaceAll("#\\{\\w+\\}", "?"), Statement.RETURN_GENERATED_KEYS);
            //判断参数类型
            if (param instanceof String || param instanceof Long || param instanceof Integer) {
                pstm.setObject(1, param);
            } else {
                Pattern p = Pattern.compile("#\\{\\w+\\}");
                Matcher m = p.matcher(sql);
                if (param != null) {
                    JSONObject object = JSON.parseObject(JSON.toJSONString(param));
                    int i = 1;
                    while (m.find()) {
                        String s = m.group();
                        pstm.setObject(i, object.get(s.substring(2, s.length() - 1)));
                        i++;
                    }
                }
            }
            resultSet = pstm.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                ++count;
                ResultSetMetaData rsMeta = resultSet.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                if (columnCount == 1) {
                    model = resultSet.getObject(1);
                } else {
                    try {
                        for (int i = 1; i <= columnCount; i++) {
                            if (model != null) {
                                Field field = result.getDeclaredField(rsMeta.getColumnLabel(i));
                                field.setAccessible(true);
                                field.set(model, resultSet.getObject(i));
                            } else {
                                map.put(rsMeta.getColumnLabel(i), resultSet.getObject(i));
                            }

                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
            if (count > 1) {
                throw new SQLException("TooManyResultsException: Expected one result (or null) to be returned by queryOne(), but found: " + count);
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            closeAll(conn, pstm, resultSet);
        }

        if (result.getTypeName().equals("java.util.Map") || result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
            return (T) map;
        } else {
            return (T) model;
        }
    }

    public static <T> T queryOne(String sql, Object param, Class<T> result, String key) {
        Connection conn = null;
        PreparedStatement pstm = null;
        Map<String,Object> map = new HashMap<>();
        Object model = null;
        ResultSet resultSet = null;
        if (result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
            map = new JSONObject();
        } else if (!result.getTypeName().contains("java.lang")) {
            try {
                model = result.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        try {
            conn = getConnection(key);
            pstm = conn.prepareStatement(sql.replaceAll("#\\{\\w+\\}", "?"), Statement.RETURN_GENERATED_KEYS);
            //判断参数类型
            if (param instanceof String || param instanceof Long || param instanceof Integer) {
                pstm.setObject(1, param);
            } else {
                Pattern p = Pattern.compile("#\\{\\w+\\}");
                Matcher m = p.matcher(sql);
                if (param != null) {
                    JSONObject object = JSON.parseObject(JSON.toJSONString(param));
                    int i = 1;
                    while (m.find()) {
                        String s = m.group();
                        pstm.setObject(i, object.get(s.substring(2, s.length() - 1)));
                        i++;
                    }
                }
            }
            resultSet = pstm.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                ++count;
                ResultSetMetaData rsMeta = resultSet.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                if (columnCount == 1) {
                    model = resultSet.getObject(1);
                } else {
                    try {
                        for (int i = 1; i <= columnCount; i++) {
                            if (model != null) {
                                Field field = result.getDeclaredField(rsMeta.getColumnLabel(i));
                                field.setAccessible(true);
                                field.set(model, resultSet.getObject(i));
                            } else {
                                map.put(rsMeta.getColumnLabel(i), resultSet.getObject(i));
                            }

                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
            if (count > 1) {
                throw new SQLException("TooManyResultsException: Expected one result (or null) to be returned by queryOne(), but found: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeAll(conn, pstm, resultSet);
        }

        if (result.getTypeName().equals("java.util.Map") || result.getTypeName().equals("com.alibaba.fastjson.JSONObject")) {
            return (T) map;
        } else {
            return (T) model;
        }
    }

    /**
     * 通用的更新操作
     *
     * @param sql
     * @param obj
     * @return
     */
    public static Boolean update(Connection conn, String sql, Object... obj) throws SQLException {
        PreparedStatement pstm = null;
        boolean flag = false;
        try {
            conn.setAutoCommit(false);
            pstm = conn.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] instanceof Object[]) {
                        pstm.setArray(i + 1, conn.createArrayOf("bigint", (Object[]) obj[i]));
                    } else {
                        pstm.setObject(i + 1, obj[i]);
                    }
                }
            }
            flag = pstm.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        } finally {
            assert pstm != null;
            pstm.close();
        }

        return flag;
    }

    /**
     * 通用的更新操作
     *
     * @param sql
     * @param obj
     * @return
     */
    public static Boolean update(Connection conn, String sql, Object obj) throws SQLException {
        PreparedStatement pstm = null;
        boolean flag ;
        try {
            conn.setAutoCommit(false);
            if ((obj instanceof String || obj instanceof Long || obj instanceof Integer)) {
                pstm = conn.prepareStatement(sql);
                pstm.setObject(1, obj);
            } else {
                //将"#{替换成？}"
                pstm = conn.prepareStatement(sql.replaceAll("#\\{\\w+\\}", "?"), Statement.RETURN_GENERATED_KEYS);
                Pattern p = Pattern.compile("#\\{\\w+\\}");
                Matcher m = p.matcher(sql);
                if (obj != null) {
                    JSONObject object = JSON.parseObject(JSON.toJSONString(obj));
                    int i = 1;
                    while (m.find()) {
                        String s = m.group();
                        pstm.setObject(i, object.get(s.substring(2, s.length() - 1)));
                        i++;
                    }
                }
            }

            flag = pstm.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        } finally {
            assert pstm != null;
            pstm.close();
        }

        return flag;
    }


    /**
     * 通用的插入操作
     *
     * @param sql
     * @param obj
     * @return
     */
    public static Object insert(Connection conn, String sql, Object... obj) throws SQLException {
        boolean flag ;
        PreparedStatement pstm = null;
        Object id = null;
        try {
            conn.setAutoCommit(false);
            pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    pstm.setObject(i + 1, obj[i]);
                }
            }
            flag = pstm.executeUpdate() == 1;
            if (flag) {
                ResultSet resultSet = pstm.getGeneratedKeys();
                if (resultSet.next()) {
                    id = resultSet.getObject(1);
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        } finally {
            assert pstm != null;
            pstm.close();
        }
        return id;
    }

    public static Object insert(Connection conn, String sql, Object o) throws SQLException {
        boolean flag ;
        PreparedStatement pstm = null;
        Object id = null;
        try {
            conn.setAutoCommit(false);
            //  将"#{}替换成?占位符"
            pstm = conn.prepareStatement(sql.replaceAll("#\\{[\\w\u4e00-\u9fa5()（）]+\\}", "?"), Statement.RETURN_GENERATED_KEYS);
            Pattern p = Pattern.compile("#\\{[\\w\u4e00-\u9fa5()（）]+\\}");
            Matcher m = p.matcher(sql);
            if (o != null) {
                JSONObject object = JSON.parseObject(JSON.toJSONString(o));
                int i = 1;
                while (m.find()) {
                    String s = m.group();
                    pstm.setObject(i, object.get(s.substring(2, s.length() - 1)));
                    i++;
                }
            }
            flag = pstm.executeUpdate() == 1;
            if (flag) {
                ResultSet resultSet = pstm.getGeneratedKeys();
                if (resultSet.next()) {
                    id = resultSet.getObject(1);
                }
            }
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        } finally {
            assert pstm != null;
            pstm.close();
        }
        return id;
    }

    /**
     * 通用的删除方法
     *
     * @param sql
     * @param obj
     * @return
     */
    public static Boolean delete(Connection conn, String sql, Object... obj) throws SQLException {
        boolean flag = false;
        PreparedStatement pstm = null;
        try {
            conn.setAutoCommit(false);
            pstm = conn.prepareStatement(sql);
            if (obj != null) {
                for (int i = 0; i < obj.length; i++) {
                    pstm.setObject(i + 1, obj[i]);
                }
            }
            flag = pstm.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        } finally {
            assert pstm != null;
            pstm.close();
        }
        return flag;
    }


    /**
     * 关闭所有有关的数据库对象
     *
     * @param conn
     * @param pstm
     * @param rs
     */
    public static void closeAll(Connection conn, PreparedStatement pstm, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstm != null) {
                pstm.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 除了结果集对象的所有对象
     *
     * @param conn
     * @param pstm
     */
    public static void closeAll(Connection conn, PreparedStatement pstm) {
        try {

            if (pstm != null) {
                pstm.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
