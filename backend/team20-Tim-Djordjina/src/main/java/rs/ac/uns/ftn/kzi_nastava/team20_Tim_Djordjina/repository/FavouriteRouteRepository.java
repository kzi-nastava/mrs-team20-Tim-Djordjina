package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.FavouriteRoute;

import java.util.List;
import java.util.Optional;

public interface FavouriteRouteRepository extends JpaRepository<FavouriteRoute, Long> {

    List<FavouriteRoute> findByUserIdOrderByIdDesc(Long userId);
    Optional<FavouriteRoute> findByIdAndUserId(Long id, Long userId);
}
