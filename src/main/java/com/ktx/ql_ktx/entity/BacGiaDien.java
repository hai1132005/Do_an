package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Bang gia dien theo bac (luy tien), ap dung chung cho toan ky tuc xa.
 * VD: Bac 1: 0-50kWh gia 1800d, Bac 2: 51-100kWh gia 2000d, Bac 3: >100kWh gia 2500d
 */
@Entity
@Table(name = "bac_gia_dien")
@Getter
@Setter
@NoArgsConstructor
public class BacGiaDien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer bacThu; // thu tu bac: 1, 2, 3...

    @Column(nullable = false)
    private Double tuSoKw; // gioi han duoi (kWh)

    private Double denSoKw; // gioi han tren, null = khong gioi han (bac cuoi)

    @Column(nullable = false)
    private BigDecimal donGia; // dong/kWh
}