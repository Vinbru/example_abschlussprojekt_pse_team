package propra2.database;

import lombok.Data;
import propra2.model.Address;
import propra2.model.ProPayAccount;

import javax.persistence.*;
import java.util.List;

@Data
@Table (name = "customer")
@Entity (name = "Customer")

public class Customer {

    @Id
    @GeneratedValue
    private Long customerId;

    private String username;
    private String password;
    @Transient
    private String retypePassword;
    private String mail;

    @Lob
    private Address address;
  
    @Lob
    private ProPayAccount proPay;
    private String role;

    @Lob
    private List<Long> borrowedProductIds;

    void addBorrowedProduct(Product newProduct){
        borrowedProductIds.add(newProduct.getId());
    }
}