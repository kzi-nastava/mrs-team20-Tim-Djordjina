package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.entities;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "passengers")
public class Passenger {

    Integer canceledRidesCount;
    Double totalSpent;
}
