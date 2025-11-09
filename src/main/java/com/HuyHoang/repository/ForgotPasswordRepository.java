package com.HuyHoang.repository;

import com.HuyHoang.Entity.ForgotPassword;
import com.HuyHoang.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword,Integer> {

    Optional<ForgotPassword> findByUser(User user);

    @Query("Select fp From ForgotPassword fp Where fp.otp = ?1 AND fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);
}
