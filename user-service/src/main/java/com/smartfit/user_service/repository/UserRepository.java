package com.smartfit.user_service.repository;
import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByActive(Boolean active);
}
