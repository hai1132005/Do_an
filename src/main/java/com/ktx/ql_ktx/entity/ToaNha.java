package com.ktx.ql_ktx.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "toa_nha")
@Getter
@Setter
@NoArgsConstructor
public class ToaNha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String tenToaNha; // vd: Toa A, Toa B

    private String diaChi;

    private Integer soTang;

    @JsonIgnore
    @OneToMany(mappedBy = "toaNha", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phong> danhSachPhong = new ArrayList<>();
}
