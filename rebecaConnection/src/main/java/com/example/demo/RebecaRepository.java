package com.example.demo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.yaz4j.CQLQuery;
import org.yaz4j.Connection;
import org.yaz4j.ResultSet;


public interface RebecaRepository {

	public void buscarLibroPorISBN() throws IOException;
	public void test();
}
