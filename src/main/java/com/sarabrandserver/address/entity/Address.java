package com.sarabrandserver.address.entity;

import com.sarabrandserver.order.entity.PaymentDetail;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Table(name = "address")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Address implements Serializable {

    @Id
    @Column(name = "address_id", nullable = false, unique = true)
    private Long addressId;

    @Column(name = "unit_number")
    private long unitNumber;

    @Column(name = "street_number", nullable = false)
    private long streetNumber;

    @Column(name = "address_1", nullable = false)
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(nullable = false)
    private String city;

    @Column(name = "state_or_province", nullable = false)
    private String stateOrProvince;

    @Column(name = "postal_zip_code")
    private String postalZipCode;

    @Column(name = "country", nullable = false)
    private String country;

    @OneToOne
    @MapsId
    @JoinColumn(name = "address_id")
    private PaymentDetail paymentDetail;

}
