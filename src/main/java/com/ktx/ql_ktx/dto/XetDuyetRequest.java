package com.ktx.ql_ktx.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XetDuyetRequest {
    private boolean dongY; // true = duyet, false = tu choi
    private String ghiChu;
}
