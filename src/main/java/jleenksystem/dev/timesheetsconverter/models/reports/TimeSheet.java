package jleenksystem.dev.timesheetsconverter.models.reports;

import java.util.HashSet;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSheet {

	@Data
	public class Client {
		private int id;
		private String name;
		private String address;
		private double hours;
	}

	private int id;
	private String fullName;
	private String period;
	
	@Builder.Default
	private Set<Client> clients = new HashSet<>();
	
	// Method to add an employee (TreeSet will automatically sort)
    public void addClient(Client client) {
        clients.add(client);
    }
}