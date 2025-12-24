package ru.Edje_7.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.entity.User;
import ru.Edje_7.repository.UserRepository;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    log.error("User not found with username/email: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found with username/email: " + usernameOrEmail);
                });

        if (!user.getEnabled()) {
            log.error("User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is disabled");
        }

        if (user.getLocked()) {
            log.error("User account is locked: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is locked");
        }

        log.debug("User found: {}", user.getUsername());

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR")); // Admin is also moderator
        }

        if (user.isModerator()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });

        if (!user.getEnabled()) {
            log.error("User account is disabled, id: {}", id);
            throw new UsernameNotFoundException("User account is disabled");
        }

        if (user.getLocked()) {
            log.error("User account is locked, id: {}", id);
            throw new UsernameNotFoundException("User account is locked");
        }

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        }

        if (user.isModerator()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}