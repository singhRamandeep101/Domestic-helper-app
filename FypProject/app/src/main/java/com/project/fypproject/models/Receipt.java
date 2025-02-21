package com.project.fypproject.models;

public class Receipt {
    public String getEmployerEmail() {
        return employerEmail;
    }

    public void setEmployerEmail(String employerEmail) {
        this.employerEmail = employerEmail;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getNumOfHoliday() {
        return numOfHoliday;
    }

    public void setNumOfHoliday(String numOfHoliday) {
        this.numOfHoliday = numOfHoliday;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String bonus) {
        this.bonus = bonus;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String employerEmail;
    public String employeeEmail;
    public String numOfHoliday;
    public String salary;
    public String bonus;
    public String fromDate;
    public String toDate;
    public String year;
    public String month;
    public String status;

    // Default constructor required for calls to DataSnapshot.getValue(Receipt.class)
    public Receipt() {}

    public Receipt(String employerEmail, String employeeEmail, String numOfHoliday, String salary, String bonus, String fromDate, String toDate, String year, String month, String status) {
        this.employerEmail = employerEmail;
        this.employeeEmail = employeeEmail;
        this.numOfHoliday = numOfHoliday;
        this.salary = salary;
        this.bonus = bonus;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
}
