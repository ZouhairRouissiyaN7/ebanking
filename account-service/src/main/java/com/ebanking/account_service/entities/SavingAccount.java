package com.ebanking.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SA")
@Getter
@Setter
@NoArgsConstructor
public class SavingAccount extends BankAccount {

    @Column(scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO;
}