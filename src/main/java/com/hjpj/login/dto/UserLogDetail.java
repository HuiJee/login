package com.hjpj.login.dto;

import com.hjpj.login.entity.User;
import com.hjpj.login.util.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor
public class UserLogDetail implements UserDetails {
    // Authentication에 담을 user 정보 형태

    private Long userId;
    private String userLogId;
    private String userLogPw;
    private String userNickname;
    private String userRole;

    private Boolean rememberMe;
    @Setter
    private Boolean autoLogin;

    public UserLogDetail(UserDTO user) {
        this.userId = user.getUserId();
        this.userLogId = user.getUserLogId();
        this.userLogPw = user.getUserLogPw();
        this.userNickname = user.getUserNickname();
        this.userRole = UserRole.getUserRole(user.getUserRole());
        this.autoLogin = false;
    }

    // 카톡 로그인 용
    public UserLogDetail(User user) {
        this.userId = user.getUserId();
        this.userLogId = user.getUserLogId();
        this.userLogPw = user.getUserLogPw();
        this.userNickname = user.getUserNickname();
        this.userRole = UserRole.getUserRole(user.getUserRole());
        this.autoLogin = false;
    }

//    public UserLogDetail(UserProjection userProjection) {
//        this.userId = userProjection.getUserId();
//        this.userLogId = userProjection.getUserLogId();
//        this.userLogPw = userProjection.getUserLogPw();
//        this.userNickname = userProjection.getUserNickname();
//        this.roles = Arrays.asList(userProjection.getRoleNames().split(","));
//    }

    public UserLogDetail(String userLogId) {
        this.userLogId = userLogId;
    }

    // role이 여러 개인 경우
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole));
    }

    @Override
    public String getPassword() {
        return userLogPw.trim();
    }

    @Override
    public String getUsername() {
        return userLogId.trim();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;    // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;    // 계정 잠김 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;    // 자격 증명의 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true;    // 계정의 활성화 여부
    }
}