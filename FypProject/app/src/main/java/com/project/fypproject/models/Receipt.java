package com.project.fypproject.models;

public class Receipt {
    public String employerName;
    public String employeeName;
    public String holidays;
    public String salary;
    public String bonus;
    public String total;
    public String fromDate;
    public String toDate;

    // Default constructor required for calls to DataSnapshot.getValue(Receipt.class)
    public Receipt() {}

    public Receipt(String employerName, String employeeName, String holidays, String salary, String bonus, String total, String fromDate, String toDate) {
        this.employerName = employerName;
        this.employeeName = employeeName;
        this.holidays = holidays;
        this.salary = salary;
        this.bonus = bonus;
        this.total = total;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
}
