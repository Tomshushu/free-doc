package com.freedoc.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private String userId;
    private String username;

    @Override
    public String getName() {
        return username;
    }

}
