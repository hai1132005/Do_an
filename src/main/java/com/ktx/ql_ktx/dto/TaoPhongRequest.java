package com.ktx.ql_ktx.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TaoPhongRequest {
    private Long toaNhaId;
    private String soPhong;
    private Integer tang;
    private Integer soGiuongToiDa;
    private BigDecimal giaPhongThang;
}
