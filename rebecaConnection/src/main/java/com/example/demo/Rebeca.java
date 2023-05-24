package com.example.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcFieldAdapter;
import org.xbib.marc.MarcListener;
import org.xbib.marc.io.BufferedSeparatorInputStream;
import org.xbib.marc.io.BytesReference;
import org.xbib.marc.io.Chunk;
import org.yaz4j.CQLQuery;
import org.yaz4j.Connection;
import org.yaz4j.PrefixQuery;
import org.yaz4j.Query;
import org.yaz4j.Record;
import org.yaz4j.ResultSet;
import org.yaz4j.exception.ZoomException;


public class Rebeca implements RebecaRepository{
	
	//private static final Logger log = LoggerFactory.getLogger(Rebeca.class);
	
	
	public void buscarLibroPorISBN() throws IOException {
		
		
		Connection conn = new Connection("catalogos.mecd.es", 220);
		try {
			conn.setDatabaseName("ABNET_REBECA");
			conn.setSyntax("USmarc");
			System.out.println("Funciona 1");
			conn.connect();
			System.out.println("Funciona 2");
			
			
			//1=5 TITULO
			//1=7 ISBN13 \"978-84-2720-213-9\", \"978-84-7888-445-2\" /Solo 84 (España)
			////979-84-2642-080-9
			Query query = new PrefixQuery("@attr 1=7 \"978-84-216-1862-2\"");
			//Query query = new PrefixQuery(" 'title' contains 'galaxy' ");
			
			System.out.println("Funciona 3");
			ResultSet rs = conn.search(query);
			
	        System.out.println("Funciona 4");
	        
	        Iterator<Record> it = rs.iterator();
	        		
	        while (it.hasNext()) {
	        	//System.out.println("Existen Records");
	        	Record record = it.next(); 
	        	byte[] bites = record.getContent();
	        	InputStream inputStream = new ByteArrayInputStream(bites);
	        	
	        	//Convertir Bytes a codificacion ISO 8859-1
	        	
	        
	        	/*
	        	BufferedSeparatorInputStream b = new BufferedSeparatorInputStream(inputStream, 4000);
	        	Stream<Chunk<byte[], BytesReference>> chunks = b.chunks();
	        	Optional<Chunk<byte[], BytesReference>> first = chunks.findFirst();
	        	Chunk<byte[], BytesReference> get = first.get();
	        	BytesReference data = get.data();
	        	bites = data.toBytes();
	        */
	        	String content = new String(bites, StandardCharsets.ISO_8859_1);
	        	System.out.println(content);
	        	findISSNs(inputStream);
	        	/*
	        		String content = new String(record.get("raw"), StandardCharsets.ISO_8859_1);
	        	
	        		*/
		            //System.out.println(record.render());
		        	//System.out.println(record.getSyntax());   
				
	        	
	        }
	        System.out.println("Funciona 5");
	        	
			rs.close();
		}catch(ZoomException  e) {
			System.out.println(e.getStackTrace());
			System.out.println(e.toString());
		}
		finally {
			conn.close();
			
		}
		
		
	}

	public void test() {
	    try (Connection con = new Connection("lx2.loc.gov/LCDB", 0)) {
	      con.setSyntax("usmarc");
	      con.connect();
	      
	      Query query = new  PrefixQuery("@attr 1=7 0253333490");
	      
	      ResultSet set = con.search(query);
	      Record rec = set.getRecord(0);
	      System.out.println(rec.render());
	    } catch (ZoomException ze) {
	      
	    }
	  }
	
	public void findISSNs(InputStream in) throws IOException {
		
		
	    Map<String, List<Map<String, String>>> result = new TreeMap<>();
	    // set up MARC listener
	    MarcListener marcListener = new MarcFieldAdapter() {
	        @Override
	        public void field(MarcField field) {
	        	System.out.println(field);
	            Collection<Map<String, String>> values = field.getSubfields().stream()
	                    .filter(f -> matchISSNField(field, f))
	                    .map(f -> Collections.singletonMap(f.getId(), new String(f.getValue().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1)))
	                    .collect(Collectors.toList());
	            if (!values.isEmpty()) {
	                result.putIfAbsent(field.getTag(), new ArrayList<>());
	                List<Map<String, String>> list = result.get(field.getTag());
	                list.addAll(values);
	                result.put(field.getTag(), list);
	            }
	        }
	    };
	    // read MARC file
	    Marc.builder()
	            .setInputStream(in)
	            .setMarcListener(marcListener)
	            .build()
	            .writeCollection();
	    // collect ISSNs
	    List<String> issns = result.values().stream()
	            .map(l -> l.stream()
	                    .map(m -> new String(m.values().iterator().next().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1))
	                    .collect(Collectors.toList()))
	            .flatMap(List::stream)
	            .distinct()
	            .collect(Collectors.toList());
	    
	    for (String string : issns) {
			System.out.println(string);
		}
	  
/*
	    // JSON output
	    XContentBuilder builder = contentBuilder().prettyPrint()
	            .startObject();
	    for (Map.Entry<String, List<Map<String, String>>> entry : result.entrySet()) {
	        builder.field(entry.getKey(), entry.getValue());
	    }
	    builder.array("issns", issns);
	    builder.endObject();

	    logger.log(Level.INFO, builder.string());
	    */
	}

	private static boolean matchISSNField(MarcField field, MarcField.Subfield subfield) {
	    switch (field.getTag()) {
	    //011-> ISSN
	    //020-> ISBN
	    //245-> Title Statement
	    //017 - Copyright or Legal Deposit Number (R)
	    //100 - Main Entry - Personal Name (Autor)
	    //260 - Publication, Distribution, etc. (Imprint)
	    //490 - coleccion + version (v2)
	    //700 - Traduccion
	    
	    //|| "b".equals(subfield.getId())|| "c".equals(subfield.getId()) || "e".equals(subfield.getId())|| "f".equals(subfield.getId()) || "z".equals(subfield.getId()
	        case "017": {
	            return "a".equals(subfield.getId());
	        }
	        case "421":
	        case "451":
	        case "452":
	        case "488":
	            return "x".equals(subfield.getId());
	    }
	    return false;
	}
	
}
