package com.java.Project;


import java.sql.*;
import java.util.*;


class Account {
    private int accountNo;
    private String username;
    private double balance;
    private int pin;

    public Account(int accountNo, String username, double balance, int pin) {
        this.accountNo = accountNo;
        this.username = username;
        this.balance = balance;
        this.pin = pin;
    }

    public int getAccountNo() { return accountNo; }
    public String getUsername() { return username; }
    public double getBalance() { return balance; }
    public int getPin() { return pin; }
    public void setBalance(double balance) { this.balance = balance; }
}
class ATM extends Account {
    Connection con;

    public ATM(int accountNo, String username, double balance, int pin, Connection con) {
        super(accountNo, username, balance, pin);
        this.con = con;
    }

    public void checkBalance() {
        System.out.println("Your current balance: ₹" + getBalance());
    }

    public void deposit(double amount) throws SQLException {
        setBalance(getBalance() + amount);
        updateDatabase("Deposited ₹" + amount);
        System.out.println("Deposit successful!");
    }

    public void withdraw(double amount) throws SQLException {
        if (amount > getBalance()) {
            throw new ArithmeticException("Insufficient balance!");
        }
        setBalance(getBalance() - amount);
        updateDatabase("Withdrawn ₹" + amount);
        System.out.println("Withdrawal successful!");
    }

    public void miniStatement() throws SQLException {
        String sql = "SELECT last_transactions FROM accounts WHERE account_no = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, getAccountNo());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            System.out.println("Mini Statement: " + rs.getString("last_transactions"));
        }
    }

    private void updateDatabase(String transaction) throws SQLException {
        String sql = "UPDATE accounts SET balance=?, last_transactions=? WHERE account_no=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setDouble(1, getBalance());
        ps.setString(2, transaction);
        ps.setInt(3, getAccountNo());
        ps.executeUpdate();
    }
}
public class ATMSystem {
    static final String URL = "jdbc:postgresql://localhost:5432/College";
    static final String USER = "postgres";
    static final String PASS = "123";

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            Scanner sc = new Scanner(System.in);
            System.out.println("===== Welcome to ATM =====");

            System.out.print("Enter username: ");
            String uname = sc.next();
            System.out.print("Enter PIN: ");
            int pin = sc.nextInt();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE username=? AND pin=?");
            ps.setString(1, uname);
            ps.setInt(2, pin);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int accNo = rs.getInt("account_no");
                double balance = rs.getDouble("balance");
                ATM atm = new ATM(accNo, uname, balance, pin, con);

                while (true) {
                    System.out.println("\n1. Check Balance");
                    System.out.println("2. Deposit");
                    System.out.println("3. Withdraw");
                    System.out.println("4. Mini Statement");
                    System.out.println("5. Exit");
                    System.out.print("Choose option: ");
                    int choice = sc.nextInt();

                    try {
                        switch (choice) {
                            case 1:
                                atm.checkBalance();
                                break;
                            case 2:
                                System.out.print("Enter deposit amount: ");
                                atm.deposit(sc.nextDouble());
                                break;
                            case 3:
                                System.out.print("Enter withdrawal amount: ");
                                atm.withdraw(sc.nextDouble());
                                break;
                            case 4:
                                atm.miniStatement();
                                break;
                            case 5:
                                System.out.println("Thank you for using our ATM!");
                                sc.close();
                                System.exit(0);
                            default:
                                System.out.println("Invalid option!");
                        }
                    } catch (ArithmeticException e) {
                        System.out.println("Error: " + e.getMessage());
                    } catch (SQLException e) {
                        System.out.println("Database Error: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Invalid username or PIN!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
