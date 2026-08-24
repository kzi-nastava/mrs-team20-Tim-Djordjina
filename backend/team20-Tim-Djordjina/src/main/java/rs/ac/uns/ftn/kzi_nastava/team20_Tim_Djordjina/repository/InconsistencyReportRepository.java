package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.InconsistencyReport;

import java.util.List;

public interface InconsistencyReportRepository extends JpaRepository<InconsistencyReport, Long> {
    List<InconsistencyReport> findByRideId(Long rideId);
}
