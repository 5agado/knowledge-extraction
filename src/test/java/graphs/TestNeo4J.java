package graphs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.TripletRelation;
import neo4JUtils.Neo4JDb;
import nlp.TripletExtractor;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestNeo4J {
	private final String DB_URL = "C:/Users/Alex/Documents/Neo4j/test"; 
	private final String TEST_TEXT = "books/itaTranslated.txt"; 
	private final String TEST_OUT = "src/main/resources/out/test.txt"; 
	
	Neo4JDb db;

	@Before
	public void prepareTestDatabase() {
		db = new Neo4JDb(DB_URL);
		//db.createIndexes();
	}

	@After
	public void destroyTestDatabase() {
		db.shutdown();
	}

	@Test
	public void testPrint() throws IOException {
		db.writeOutContent(TEST_OUT);
	}
	
	@Test
	public void basicNeo4JTest() throws IOException {
		neo4JTest(TEST_TEXT);
	}
	
	@Test
	public void extractRandomRel() throws IOException {
		Random rand = new Random();
		TripletRelation rel;
		for (int i=0; i<100; i++){
			int id = rand.nextInt(9000);
			rel = db.getRelation(Integer.toUnsignedLong(id));
			System.out.println(rel.getArg1() + "; " + rel.getRelation() + "; " 
			+ rel.getArg2() + "; " + rel.getConfidence());
		}
	}

	private void neo4JTest(String fileName) throws IOException {
		System.out.println("----------------");
		System.out.println(fileName);
		InputStream testArticle = TestNeo4J.class.getClassLoader()
				.getResourceAsStream(fileName);
		String text = IOUtils.toString(testArticle, "UTF-8");
		testArticle.close();
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
		System.out.println(countValid(rels) + "valid triplets");
		System.out.println(numValid + "valid triplets");
		System.out.println("Average conf = " + confTot/numValid);
	}
	
	@Test
	public void iterateFolderExtraction() throws IOException{	
		List<String> docs = getAllFilenames("src/main/resources/books/");
		long startTime = System.currentTimeMillis();
		System.out.println("Start iterateFolderExtraction");
		for (String doc : docs){
			neo4JTest("books/" + doc);
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("iterateFolderExtraction extraction ended in " + duration + "millis");
	}

	private int countValid(List<TripletRelation> tList) {
		int count = 0;
		for (TripletRelation rel : tList) {
			if (rel.isComplete())
				count++;
		}
		return count;
	}
	
	private List<String> getAllFilenames(String folderPath){
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		List<String> filenames = new ArrayList<String>();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
		        filenames.add(listOfFiles[i].getName());
		        //System.out.println(listOfFiles[i].getName());
		    }
		}
		
		return filenames;
	}
}
