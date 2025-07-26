package com.unify.app.users.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface RoleRepository extends JpaRepository<Role, Integer> {
  Optional<Role> findByName(String name);
}
