package ipos.pu.code.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConfig {

    private static final String URL = "jdbc:mysql://mysql:3306/ipos_db";
    private static final String USER = "root";
    private static final String PASSWORD = "rootpassword";

    public static Connection getConnection() throws Exception {
        System.out.println("getting connection");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}