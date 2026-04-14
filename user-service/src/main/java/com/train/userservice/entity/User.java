package com.train.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "users_seq", allocationSize = 1)
    private Long userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String phone;
}