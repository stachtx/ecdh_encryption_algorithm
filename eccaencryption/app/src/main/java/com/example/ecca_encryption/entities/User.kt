package com.example.ecca_encryption.entities

import javax.persistence.*

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val surname: String,

    @Column(nullable = false)
    val login: String,

    @Column(nullable = false)
    val password: String
)