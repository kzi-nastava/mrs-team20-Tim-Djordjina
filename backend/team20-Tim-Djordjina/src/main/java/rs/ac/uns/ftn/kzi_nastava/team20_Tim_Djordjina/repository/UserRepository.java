package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.Role;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /*
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /*
     * Find users by role
     */
    List<User> findByRole(Role role);

    /*
    * Find user by activation token
    * */
    Optional<User> findByActivationToken(String token);

    /*
    * Find user by reset password token
    * */
    Optional<User> findByResetPasswordToken(String token);
}
