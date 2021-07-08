package com.example.ecc_library.cryptography

interface EllipticCurve {

    fun encrypt(value:String) : String;

    fun decrypt(value:String) : String;
}