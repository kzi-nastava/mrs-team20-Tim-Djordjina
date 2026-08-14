package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PricingConfigDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.PricingConfig;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.VehicleType;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.PricingConfigRepository;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final long CONFIG_ID = 1L;

    private static final double DEFAULT_STANDARD = 200;
    private static final double DEFAULT_LUXURY = 500;
    private static final double DEFAULT_VAN = 350;
    private static final double DEFAULT_PER_KM = 120;

    private final PricingConfigRepository pricingConfigRepository;

    @Transactional(readOnly = true)
    public PricingConfigDTO getPricing() {
        return toDto(getCurrentOrDefault());
    }

    @Transactional
    public PricingConfigDTO updatePricing(PricingConfigDTO dto) {
        validate(dto);
        PricingConfig config = getCurrentOrDefault();
        config.setStandardPrice(dto.getStandardPrice());
        config.setLuxuryPrice(dto.getLuxuryPrice());
        config.setVanPrice(dto.getVanPrice());
        config.setPricePerKm(dto.getPricePerKm());
        return toDto(pricingConfigRepository.save(config));
    }

    /** Base price for a vehicle type */
    @Transactional(readOnly = true)
    public double getBasePriceFor(VehicleType type) {
        PricingConfig config = getCurrentOrDefault();
        return switch (type) {
            case STANDARD -> config.getStandardPrice();
            case LUXURY -> config.getLuxuryPrice();
            case VAN -> config.getVanPrice();
        };
    }

    @Transactional(readOnly = true)
    public double getPricePerKm() {
        return getCurrentOrDefault().getPricePerKm();
    }

    // ---------- Helpers ----------

    private PricingConfig getCurrentOrDefault() {
        return pricingConfigRepository.findById(CONFIG_ID).orElseGet(this::getNewDefault);
    }

    private PricingConfig getNewDefault() {
        PricingConfig config = new PricingConfig();
        config.setId(CONFIG_ID);
        config.setStandardPrice(DEFAULT_STANDARD);
        config.setLuxuryPrice(DEFAULT_LUXURY);
        config.setVanPrice(DEFAULT_VAN);
        config.setPricePerKm(DEFAULT_PER_KM);
        return config;
    }

    private void validate(PricingConfigDTO dto) {
        if (existsNegativePrice(dto)){
            throw new IllegalArgumentException("Prices must not be negative.");
        }
    }

    private static boolean existsNegativePrice(PricingConfigDTO dto) {
        return dto.getStandardPrice() < 0 || dto.getLuxuryPrice() < 0
                || dto.getVanPrice() < 0 || dto.getPricePerKm() < 0;
    }

    private PricingConfigDTO toDto(PricingConfig config) {
        return new PricingConfigDTO(config.getStandardPrice(), config.getLuxuryPrice(),
                config.getVanPrice(), config.getPricePerKm());
    }
}
