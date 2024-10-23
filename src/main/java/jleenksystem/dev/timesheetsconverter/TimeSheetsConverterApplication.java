package jleenksystem.dev.timesheetsconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TimeSheetsConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeSheetsConverterApplication.class, args);
	}

}
