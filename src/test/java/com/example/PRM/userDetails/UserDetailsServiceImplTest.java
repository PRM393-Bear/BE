package com.example.PRM.userDetails;

import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.UsernameNotFoundException;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserDetailsServiceImpl}.
 * Covers both branches of loadUserByUsername: user found and user not found.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository);

        role = new Role();
        role.setRoleName("MEMBER");

        user = new User();
        user.setUserName("john.doe");
        user.setPassword("encodedPassword");
        user.setRole(role);
    }

    @Test
    void loadUserByUsername_success_returnsUserDetailsWithCorrectRole() {
        when(userRepository.findByUserName("john.doe")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("john.doe");

        assertEquals("john.doe", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        List<String> authorities = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertTrue(authorities.contains("ROLE_MEMBER"));
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost"));

        assertTrue(ex.getMessage().contains("User not found"));
        assertTrue(ex.getMessage().contains("ghost"));
    }
}
