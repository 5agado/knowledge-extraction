package nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import model.TripletRelation;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import util.Utils;

public class TripletExtraction {
	private final String TEST_FILE = "books/myConv/myRisp.txt";
	private final String TEST_FOLDER = "src/main/resources/books/myConv/";
	private final String TEST_FOLDER_NAME = "books/myConv/";
	private final Boolean COMPARE = false;
	
	private ParserExtractor pExt;
	private TripletExtractor tExt;
	
	@BeforeClass
	public void initExtractors() throws IOException {
		pExt = new ParserExtractor();
		tExt = new TripletExtractor();
	}
	
	@Test
	public void singleExtraction() throws IOException{
		experimentExtraction(TEST_FILE);
	}
	
	@Test
	public void iterateFolderExtraction() throws IOException{	
		List<String> docs = Utils.getAllFilenames(TEST_FOLDER);
		long startTime = System.currentTimeMillis();
		System.out.println("Start iterateFolderExtraction");
		for (String doc : docs){
			experimentExtraction(TEST_FOLDER_NAME + doc);
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("iterateFolderExtraction extraction ended in " 
				+ duration + " millis");
	}
	
	private void experimentExtraction(String filename){
		String text = null;
		try (InputStream testArticle = TripletExtraction.class.getClassLoader()
				.getResourceAsStream(filename);) {
			text = IOUtils.toString(testArticle, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Current text article length: " + text.length());
		List<TripletRelation> rels;
		
		//Using parser extractor
		if (COMPARE){
			long startTime = System.currentTimeMillis();
			System.out.println("Start extraction with OpenNLP");
			rels = pExt.extractRelationsFromText(text);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.println("Extraction ended in " + duration + " millis");
			System.out.println(rels.size() + " triplets founded");
			System.out.println(Utils.countValidTriplets(rels) + " valid triplets");
		}
		
		//Using triplet extractor (Reverb)
		long startTime = System.currentTimeMillis();
		System.out.println("----------------------------");
		System.out.println("Start extraction with Reverb");
		rels = tExt.extractRelationsFromText(text);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("----------------------------");
		Utils.printList(rels);
		System.out.println("----------------------------");
		System.out.println("Extraction ended in " + duration + " millis");
		System.out.println(rels.size() + " triplets founded");
		System.out.println(Utils.countValidTriplets(rels) + " valid triplets \n");
	}
}
