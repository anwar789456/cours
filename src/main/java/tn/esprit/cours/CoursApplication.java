package tn.esprit.cours;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan("tn.esprit.cours.entity")
public class CoursApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoursApplication.class, args);
    }

}
