package com.conn;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnect 
{

	private static Connection conn = null;
	
	public static Connection getConn()
	{
		try {
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			Class.forName("org.sqlite.JDBC");
			String dbPath = System.getenv("SQLITE_DB_PATH");
			if (dbPath == null || dbPath.isEmpty()) {
				dbPath = "mydatabase.db";
			}
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		return conn;
	}
	
}
