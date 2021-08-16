package com.fileio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDBService  {

    private PreparedStatement employeePayrollDataStatement;
    //to make this singleton
    private  static  EmployeePayrollDBService employeePayrollDBService;
    private EmployeePayrollDBService() {
    }
    public static EmployeePayrollDBService getInstance() {
        if (employeePayrollDBService == null)
            employeePayrollDBService = new EmployeePayrollDBService();
        return employeePayrollDBService;
    }

    //Method to read database using JDBC
    public List<EmployeePayrollData> readData() {
        String sql = "SELECT * from employee_payroll";
        List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
        try  (Connection connection = this.getConnection()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            employeePayrollList = this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollList;
    }
    private Connection getConnection() throws SQLException {
        String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?characterEncoding=latin1";
        String userName = "root";
        String password = "bridgelabz";
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded");
        }catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the class path", e);
        }
        System.out.println("Connecting to database :" +jdbcURL);
        connection = DriverManager.getConnection(jdbcURL, userName, password);
        System.out.println("Connection is successful!!!" + connection);
        return connection;
    }
    private int updateEmployeeDataUsingStatement(String name, double salary) {
        String sql = String.format("UPDATE employee_payroll SET salary = %.2f where name = '%s';", salary, name);
        try  (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
        List<EmployeePayrollData> employeePayrollDataList = new ArrayList<>();
        if (this.employeePayrollDataStatement == null)
            this.preparedStatementforEmployeeData();
        try {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double salary = resultSet.getDouble("salary");
                LocalDate startDate = resultSet.getDate("start").toLocalDate();
                employeePayrollDataList.add(new EmployeePayrollData(id, name, salary, startDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollDataList;
    }
    private void preparedStatementforEmployeeData() {
        try {
            Connection connection = this.getConnection();
            String sql = "SELECT * FROM employee_payroll WHERE name = ?";
            employeePayrollDataStatement = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //method to update the salary in the DB using Statement Interface

    public int updateEmployeeData(String name, double salary)  {
        return this.updateEmployeeDataUsingStatement(name, salary);
    }
    public List<EmployeePayrollData> getEmployeePayrollData(String name) {
        List<EmployeePayrollData> employeePayrollDataList = null;
        if (this.employeePayrollDataStatement==null)
            this.preparedStatementforEmployeeData();
        try {
            employeePayrollDataStatement.setString(1,name);
            ResultSet resultSet = employeePayrollDataStatement.executeQuery();
            employeePayrollDataList = this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollDataList;
    }
}
