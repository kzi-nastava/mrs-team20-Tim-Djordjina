package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.ProfileChangeRequest;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.ProfileChangeStatus;

import java.util.List;
import java.util.Optional;

public interface ProfileChangeRequestRepository extends JpaRepository<ProfileChangeRequest, Long> {

    List<ProfileChangeRequest> findByStatus(ProfileChangeStatus status);

    Optional<ProfileChangeRequest> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ProfileChangeStatus status);
}
