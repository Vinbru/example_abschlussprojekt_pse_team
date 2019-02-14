package propra2.model;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Lob;
import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
public class ProPayAccount {
    private String account;
    private double amount;

    @Lob
    @ElementCollection
    private List<Reservation> reservations = new ArrayList<>();
}
