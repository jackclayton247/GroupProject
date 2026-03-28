package ipos.pu.code.model;

import java.time.LocalDateTime;

import jakarta.persistence.Id;

public class Promotions {
    @Id
    String name;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
