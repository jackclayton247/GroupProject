package ipos.pu.code.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Promotion {
    @Id
    String name;

    private LocalDate start;
    private LocalDate end;
}
