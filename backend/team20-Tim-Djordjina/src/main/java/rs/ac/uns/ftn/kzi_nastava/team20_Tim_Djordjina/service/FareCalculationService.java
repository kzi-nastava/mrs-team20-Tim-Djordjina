package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;

/** Computes the fare per US#2.4.1 */
@Service
public class FareCalculationService {

    private static final double PRICE_PER_KM = 120.0;

    @Value("${ride.price.standard}")
    private double standardBasePrice;

    @Value("${ride.price.luxury}")
    private double luxuryBasePrice;

    @Value("${ride.price.van}")
    private double vanBasePrice;

    /** Base price for a vehicle type */
    public double getBasePrice(VehicleType type) {
        return switch (type){
            case STANDARD -> standardBasePrice;
            case LUXURY -> luxuryBasePrice;
            case VAN -> vanBasePrice;
        };
    }

    public double calculateFare(VehicleType type, double distanceKM) {
        return getBasePrice(type) + distanceKM * PRICE_PER_KM;
    }
}
