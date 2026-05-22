package id.ac.ui.cs.advprog.bidmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
@EnableAsync
public class BidmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidmartApplication.class, args);
    }

}
