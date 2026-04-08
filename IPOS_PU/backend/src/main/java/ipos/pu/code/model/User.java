package ipos.pu.code.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private String email;
    private String password;
    private int orderNumber;
    private boolean merchant;

    public boolean isMerchant() { return merchant; }
    public void setMerchant(boolean merchant) { this.merchant = merchant; }
}
