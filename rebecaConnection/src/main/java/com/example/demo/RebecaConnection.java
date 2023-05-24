package com.example.demo;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RebecaConnection {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(RebecaConnection.class, args);
		
		Rebeca rebeca = new Rebeca();
		
		rebeca.buscarLibroPorISBN();

	}

	

}
