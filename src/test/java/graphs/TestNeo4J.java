package graphs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import model.TripletRelation;
import neo4JUtils.Neo4JDb;
import nlp.TripletExtractor;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import util.Utils;

public class TestNeo4J {
	private final String DB_URL = "C:/Users/Alex/Documents/Neo4j/rispTest"; 
	private final String TEST_FILE = "books/myConv/sherryRisp.txt"; 
	private final String TEST_OUT = "src/main/resources/out/test.txt"; 
	private final String TEST_FOLDER = "src/main/resources/books/myConv/";
	private final String TEST_FOLDER_NAME = "books/myConv/";
	
	Neo4JDb db;

	@BeforeClass
	public void prepareTestDatabase() {
		db = new Neo4JDb(DB_URL);
		//TODO
		//db.createIndexes();
	}

	@AfterClass
	public void destroyTestDatabase() {
		db.shutdown();
	}

	@Test
	public void testPrint() {
		db.writeOutContent(TEST_OUT);
	}
	
	@Test
	public void SingleNeo4JTest() {
		neo4JTest(TEST_FILE);
	}
	
	@Test
	public void extractRandomRels() {
		int NUM_RELS = 30;
		int MAX_REL_ID = 400;
		Random rand = new Random();
		TripletRelation rel;
		for (int i=0; i<NUM_RELS; i++){
			int id = rand.nextInt(MAX_REL_ID);
			rel = db.getRelation(Integer.toUnsignedLong(id));
			System.out.println(rel.getArg1() + "; " + rel.getRelation() + "; " 
			+ rel.getArg2() + "; " + rel.getConfidence());
		}
	}
	
	@Test
	public void iterateFolderExtraction() throws IOException{	
		List<String> docs = Utils.getAllFilenames(TEST_FOLDER);
		long startTime = System.currentTimeMillis();
		System.out.println("Start iterateFolderExtraction");
		for (String doc : docs){
			neo4JTest(TEST_FOLDER_NAME + doc);
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("iterateFolderExtraction extraction ended in " + duration + "millis");
	}
	
	private void neo4JTest(String fileName) {
		System.out.println("----------------");
		System.out.println(fileName);
		
		String text = null;
		try (InputStream testArticle = TestNeo4J.class.getClassLoader()
				.getResourceAsStream(fileName);) {
			text = IOUtils.toString(testArticle, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Current text length: " + text.length());

		TripletExtractor tExt = new TripletExtractor();
		long startTime = System.currentTimeMillis();
		System.out.println("Start extraction to Neo4J");
		List<TripletRelation> rels = tExt.extractRelationsFromText(text);
		int numValid = 0;
		double confTot = 0.0;
		for (TripletRelation rel : rels) {
			if (rel.isComplete()){
				db.insertTriplet(rel, false);
				numValid++;
				confTot += rel.getConfidence();
			}
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Extraction ended in " + duration + "millis");
		System.out.println(rels.size() + "triplets founded");
		System.out.println(numValid + "valid triplets");
		System.out.println("Average conf = " + confTot/numValid);
	}
}
