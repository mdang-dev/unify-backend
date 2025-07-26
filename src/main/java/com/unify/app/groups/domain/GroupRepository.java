package com.unify.app.groups.domain;

import com.unify.app.groups.domain.models.GroupStatus;
import com.unify.app.groups.domain.models.PrivacyType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface GroupRepository extends JpaRepository<Group, String> {
  List<Group> findByPrivacyType(PrivacyType privacyType);

  List<Group> findByStatus(GroupStatus status);
}
