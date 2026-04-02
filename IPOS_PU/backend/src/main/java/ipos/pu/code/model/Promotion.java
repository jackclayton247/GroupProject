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

    //getters
    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return start;
    }

    public LocalDate getEndDate() {
        return end;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(LocalDate startDate) {
        this.start = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.end = endDate;
    }
}
