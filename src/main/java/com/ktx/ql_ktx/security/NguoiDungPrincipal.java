package com.ktx.ql_ktx.security;

import com.ktx.ql_ktx.entity.NguoiDung;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Boc NguoiDung thanh UserDetails de Spring Security su dung.
 * Vai tro duoc gan tien to "ROLE_" theo quy uoc Spring Security.
 * Vi du: VaiTro.SINH_VIEN -> quyen "ROLE_SINH_VIEN"
 */
public class NguoiDungPrincipal implements UserDetails {

    private final NguoiDung nguoiDung;

    public NguoiDungPrincipal(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + nguoiDung.getVaiTro().name()));
    }

    @Override
    public String getPassword() {
        return nguoiDung.getMatKhau();
    }

    @Override
    public String getUsername() {
        return nguoiDung.getTenDangNhap();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return nguoiDung.isActive();
    }
}
