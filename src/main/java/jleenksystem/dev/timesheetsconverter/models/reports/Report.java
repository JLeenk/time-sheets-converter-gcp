package jleenksystem.dev.timesheetsconverter.models.reports;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Report {
	
	@Data
	public class Employee {
		private int id;
		private String fullName;
		private String period;
		private double hours;
	}
	
	private int id;
	private String fullClientName;
	
	// Use TreeSet to automatically keep the employees sorted by fullName
	@Builder.Default
    private SortedSet<Employee> employees = new TreeSet<>(Comparator.comparing(Employee::getFullName));

    // Method to add an employee (TreeSet will automatically sort)
    public void addEmployee(Employee employee) {
        employees.add(employee);
    }
}