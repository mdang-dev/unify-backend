package com.unify.app.users.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface AvatarRepository extends JpaRepository<Avatar, String> {

  Optional<Avatar> findByUserId(String userId);
}
