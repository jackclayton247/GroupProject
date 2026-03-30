package ipos.pu.code.PRMPackage.dto;

import java.time.LocalDate;

public class PromoRequest {
    private String name;
    private LocalDate start;
    private LocalDate end;

    //getters
    public String getName() {
        return name;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    //setters
    public void setName(String name) {
        this.name = name;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }
}
