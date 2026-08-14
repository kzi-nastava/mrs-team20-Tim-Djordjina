package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin managed pricing,
 * the per vehicle type base price
 * and the per km price.
 * Single-row config.
 * */
@Entity
@Table(name = "pricing_config")
@Getter
@Setter
public class PricingConfig {

    @Id
    private Long id = 1L;

    private double standardPrice;
    private double luxuryPrice;
    private double vanPrice;
    private double pricePerKm;
}
