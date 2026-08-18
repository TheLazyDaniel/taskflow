// Create this class: TestSecurityUtils.java
package com.thelazydaniel.taskflow.common.utils;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithSecurityUserSecurityContextFactory.class)
public @interface WithSecurityUser {
    String username() default "testuser";
    UserRole role() default UserRole.USER;
    long userId() default 1L;
}

class WithSecurityUserSecurityContextFactory implements WithSecurityContextFactory<WithSecurityUser> {
    @Override
    public SecurityContext createSecurityContext(WithSecurityUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = new User();
        user.setId(annotation.userId());
        user.setUsername(annotation.username());
        user.setEmail(annotation.username() + "@email.com");
        user.setRole(annotation.role());
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        SecurityUser securityUser = new SecurityUser(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        context.setAuthentication(auth);

        return context;
    }
}