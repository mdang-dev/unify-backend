package com.unify.app.security;

import com.unify.app.users.domain.Role;
import com.unify.app.users.domain.User;
import com.unify.app.users.domain.UserService;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserService userService;

  CustomUserDetailsService(@Lazy UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userService.findByEmail(username);
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), mapRoles(user.getRoles()));
  }

  private Collection<GrantedAuthority> mapRoles(Set<Role> roles) {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
        .collect(Collectors.toList());
  }
}
