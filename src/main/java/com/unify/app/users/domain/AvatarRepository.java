package com.unify.app.users.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AvatarRepository extends JpaRepository<Avatar, String> {

  Optional<Avatar> findByUserId(String userId);

  @Query("SELECT a FROM Avatar a WHERE a.user.id = :userId ORDER BY a.createdAt DESC LIMIT 1")
  Optional<Avatar> findLatestByUserId(@Param("userId") String userId);
}
