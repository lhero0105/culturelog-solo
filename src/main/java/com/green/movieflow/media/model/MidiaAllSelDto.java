package com.green.movieflow.media.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "메인 페이지 출력 데이터")

public class MidiaAllSelDto {
    @Schema(title = "유저번호")
    private int iuser;
    @Schema(title = "년월")
    private String ym; // ex) 2023-12
}
