package ipos.pu.code.MembersPackage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private String email;
    private String password;

}
