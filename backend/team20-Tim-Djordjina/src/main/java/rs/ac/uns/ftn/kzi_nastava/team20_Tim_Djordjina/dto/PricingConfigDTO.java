package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConfigDTO {
    private double standardPrice;
    private double luxuryPrice;
    private double vanPrice;
    private double pricePerKm;
}
