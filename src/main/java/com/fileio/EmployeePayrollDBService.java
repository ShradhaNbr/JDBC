package com.fileio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollDBService  {

    private PreparedStatement employeePayrollDataStatement;
    private  static  EmployeePayrollDBService employeePayrollDBService;   //to make this singleton
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
            return statement.executeUpdate(sql); //return how may rows got updated
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
    public int updateEmployeeDataPreparedStatement(String name, double salary)  {
        return this.updateEmployeeDataUsingPreparedStatement(name, salary);
    }

    private int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
        String sql = "UPDATE employee_payroll SET salary = ? where name = ?";
        try  (Connection connection = this.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1,salary);
            statement.setString(2,name);
            return statement.executeUpdate(sql); //return how may rows got updated
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<EmployeePayrollData> getEmployeePayrollData(String name) {
        List<EmployeePayrollData> employeePayrollDataList = new ArrayList<>();
        if (this.employeePayrollDataStatement==null)
            this.preparedStatementforEmployeeData();
        String sql = "SELECT * FROM employee_payroll WHERE name=?";
        try (Connection connection = this.getConnection()){
            PreparedStatement preparedStatement =connection.prepareStatement(sql);
            preparedStatement.setString(1,name);
            ResultSet resultSet = preparedStatement.executeQuery();
            employeePayrollDataList = this.getEmployeePayrollData(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollDataList;
    }
    private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql)  {
        List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                double salary = result.getDouble("salary");
                LocalDate startDate = result.getDate("start").toLocalDate();
                employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollList;
    }

    //Method to read from database for a date range
    public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate startDate, LocalDate endDate)  {
        String sql = String.format("SELECT * FROM employee_payroll WHERE START BETWEEN '%s' AND '%s';",
                Date.valueOf(startDate), Date.valueOf(endDate));
        return getEmployeePayrollDataUsingDB(sql);
    }

    public Map<String, Double> getAverageSalaryByGender() {
        String sql = "SELECT gender,AVG(salary) AS average_salary FROM employee_payroll  GROUP BY gender;";
        Map<String,Double> genderToAverageSalaryMap = new HashMap<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double salary = resultSet.getDouble("average_salary");
                genderToAverageSalaryMap.put(gender,salary);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return genderToAverageSalaryMap;
    }

    public EmployeePayrollData addEmployeeToPayroll(String name, String gender, double salary, LocalDate startDate) {
        int employeeId = -1;
        EmployeePayrollData employeePayrollData = null;
        Connection connection = null;
        try {
            connection = this.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Statement statement = connection.createStatement()) {
            String sql = String.format("INSERT INTO employee_payroll (name,gender,salary,start) " +
                    "VALUES ('%s','%s',%s,'%s')", name, gender, salary, Date.valueOf(startDate));
            int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
            if (rowAffected == 1) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) employeeId = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Statement statement = connection.createStatement()) {
            double deductions = salary * 0.2;
            double taxablePay = salary - deductions;
            double tax = taxablePay * 0.1;
            double netPay = salary - tax;
            String sql = String.format("INSERT INTO payroll_details " +
                    "(employee_id,basic_pay,deductions,taxable_pay,tax,net_pay) VALUES " +
                    "(%s,%s,%s,%s,%s,%s);", employeeId, salary, deductions, taxablePay, tax, netPay);
            int rowAffected = statement.executeUpdate(sql);
            if (rowAffected == 1) {
                employeePayrollData = new EmployeePayrollData(employeeId, name, salary, startDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeePayrollData;
    }
}