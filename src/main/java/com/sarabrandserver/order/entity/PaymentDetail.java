package com.sarabrandserver.order.entity;

import com.sarabrandserver.enumeration.GlobalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.LAZY;

@Table(name = "payment_detail")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_detail_id", nullable = false, unique = true)
    private Long paymentDetailId;

    @Column(name = "customer_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    // Represents the payment ID from payment provider
    @Column(name = "payment_id", nullable = false, unique = true)
    private String payment_id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_provider", nullable = false, length = 20)
    private String payment_provider;

    @Column(name = "payment_status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private GlobalStatus globalStatus;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createAt;

    @OneToMany(fetch = LAZY, cascade = { PERSIST, MERGE, REFRESH }, mappedBy = "paymentDetail")
    private Set<OrderDetail> orderDetail;

}
