package com.jemyeonso.app.jemyeonsobe.api.user.repository;

import com.jemyeonso.app.jemyeonsobe.api.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}
