package com.pado.domain.user.repository;

import com.pado.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);
    @EntityGraph(attributePaths = "interests")
    Optional<User> findWithInterestsById(Long id);
    Optional<User> findByNickname(String nickname);
}
