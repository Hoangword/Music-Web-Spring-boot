package com.HuyHoang.repository;

import com.HuyHoang.Entity.Role;
import com.HuyHoang.Entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    Page<User> findAllByRoles(Role role, Pageable pageable);

    @Query( value = """
            SELECT DISTINCT u 
            FROM User u 
            JOIN u.roles r 
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username ,'%'))  
            AND r.name = :role 
            AND LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))
            """)
    Page<User> searchUser(@Param("username") String keyword,@Param("role") String roleName,@Param("email") String email, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE User u set u.password = ?2 where u.email = ?1")
    void updatePassword(String email, String password);
}
