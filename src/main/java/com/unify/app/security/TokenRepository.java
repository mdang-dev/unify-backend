package com.unify.app.security;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
interface TokenRepository extends JpaRepository<Token, String> {

  @Query("""
       SELECT t FROM Token t INNER JOIN User u ON t.user.id = u.id WHERE u.id =:userId
       AND (t.expired = false OR t.revoked = false)
      """)
  List<Token> findAllValidTokensByUser(@Param("userId") String userId);

  @Query("""
       SELECT t FROM Token t INNER JOIN User u ON t.user.id = u.id WHERE u.id =:userId
       AND (t.expired = true OR t.revoked = true)
      """)
  List<Token> findAllInvalidTokensByUser(@Param("userId") String userId);

  Optional<Token> findByJti(String jti);

  Optional<Token> findByToken(String token);
}
