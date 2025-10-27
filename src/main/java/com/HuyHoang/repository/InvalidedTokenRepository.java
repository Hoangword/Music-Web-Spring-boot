package com.HuyHoang.repository;

import com.HuyHoang.Entity.InvalidedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidedTokenRepository extends JpaRepository<InvalidedToken,String> {
}
