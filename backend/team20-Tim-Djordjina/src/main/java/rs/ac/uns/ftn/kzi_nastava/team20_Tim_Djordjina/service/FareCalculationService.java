package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;

/** Computes the fare per US#2.4.1 */
@Service
@RequiredArgsConstructor
public class FareCalculationService {

    private final PricingService pricingService;

    public double calculateFare(VehicleType type, double distanceKm) {
        double base = pricingService.getBasePriceFor(type);
        double perKm = pricingService.getPricePerKm();
        return base + distanceKm * perKm;
    }
}
