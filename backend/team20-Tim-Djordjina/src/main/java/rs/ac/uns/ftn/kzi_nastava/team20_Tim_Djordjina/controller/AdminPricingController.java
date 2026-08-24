package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PricingConfigDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.PricingService;

@RestController
@RequestMapping("/api/admin/pricing")
@RequiredArgsConstructor
public class AdminPricingController {

    private final PricingService pricingService;

    @GetMapping
    public ResponseEntity<PricingConfigDTO> get() {
        return ResponseEntity.ok(pricingService.getPricing());
    }

    @PutMapping
    public ResponseEntity<PricingConfigDTO> update(@RequestBody PricingConfigDTO dto) {
        return ResponseEntity.ok(pricingService.updatePricing(dto));
    }


}
