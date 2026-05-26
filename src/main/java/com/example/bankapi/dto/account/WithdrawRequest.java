package com.example.bankapi.dto.account;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
