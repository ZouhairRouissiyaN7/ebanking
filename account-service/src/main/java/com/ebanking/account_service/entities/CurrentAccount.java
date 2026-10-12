package com.ebanking.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CA")
@Getter
@Setter
@NoArgsConstructor
public class CurrentAccount extends BankAccount {

    @Column(scale = 2)
    private BigDecimal overdraft = BigDecimal.ZERO;
}
