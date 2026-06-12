package com.example.PRM.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthDetails {
    private final UUID userId;

    public static UUID getCurrentUserId() {
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder
                        .getContext().getAuthentication();
        return ((AuthDetails) auth.getDetails()).getUserId();
    }
}
