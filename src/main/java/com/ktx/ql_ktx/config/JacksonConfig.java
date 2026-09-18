package com.ktx.ql_ktx.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sua loi kinh dien: "Type definition error: ... ByteBuddyInterceptor".
 * Nguyen nhan: cac quan he @ManyToOne/@OneToOne FetchType.LAZY (vd: Phong.toaNha,
 * ChiSoDienNuoc.phong...) duoc Hibernate boc trong 1 "proxy" de tri hoan truy van.
 * Khi Jackson serialize entity, no vo tinh doc phai thuoc tinh noi bo cua proxy
 * (hibernateLazyInitializer, handler) - la doi tuong noi bo Hibernate/ByteBuddy,
 * khong the chuyen thanh JSON duoc.
 *
 * Cau hinh nay bao Jackson: gap 2 thuoc tinh do o BAT KY object nao thi bo qua,
 * khong can sua tung Entity rieng le.
 */
@Configuration
public class JacksonConfig {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    static class HibernateProxyMixIn {}

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer bienHibernateProxyThanhJsonAnToan() {
        return builder -> builder.mixIn(Object.class, HibernateProxyMixIn.class);
    }
}