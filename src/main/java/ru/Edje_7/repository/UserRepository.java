package ru.Edje_7.repository;



import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.Edje_7.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Cacheable(value = "users", key = "#email")
    Optional<User> findByEmail(String email);

    @Cacheable(value = "users", key = "#username")
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.subscribers WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username")String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    List<User> findAdmins();

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.subscriptions s WHERE u.id = :subscriberId AND s.id = :authorId")
    boolean isSubscribed(@Param("subscriberId") Long subscriberId, @Param("authorId") Long authorId);

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.locked = false order by u.username asc")
    Page<User> findActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.subscriptions s WHERE s.id = :userId")
    Page<User> findFollowers(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM User u JOIN u.subscriptions s WHERE u.id = :userId")
    Page<User> findFollowing(@Param("userId") Long userId, Pageable pageable);
}