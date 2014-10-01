package nlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import model.TripletRelation;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class TripletExtraction {
	private final String TEST_FILE = "books/itaTranslated.txt";
	private final String[] TEST_DOCS = {"Articles.txt", "Articles2.txt"};
	private final String TEST_FOLDER = "src/main/resources/books/etext00/";
	private final String TEST_FOLDER_NAME = "books/etext00/";
	
	private ParserExtractor pExt;
	private TripletExtractor tExt;
	
	@Before
	public void initExtractors() throws IOException {
		pExt = new ParserExtractor();
		tExt = new TripletExtractor();
	}
	
	@Test
	public void singleExtraction() throws IOException{
		experimentExtraction(TEST_FILE, false);
	}
	
	@Test
	public void iterateExtraction() throws IOException{
		for (int i=0; i<TEST_DOCS.length; i++){
			experimentExtraction(TEST_DOCS[i], false);
		}
	}
	
	@Test
	public void iterateFolderExtraction() throws IOException{	
		List<String> docs = getAllFilenames(TEST_FOLDER);
		long startTime = System.currentTimeMillis();
		System.out.println("Start iterateFolderExtraction");
		for (String doc : docs){
			experimentExtraction(TEST_FOLDER_NAME + doc, false);
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("iterateFolderExtraction extraction ended in " 
				+ duration + "millis");
	}
	
	private void experimentExtraction(String filename, boolean compare){
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
		if (compare){
			long startTime = System.currentTimeMillis();
			System.out.println("Start extraction with OpenNLP");
			rels = pExt.extractRelationsFromText(text);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.println("Extraction ended in " + duration + "millis");
			System.out.println(rels.size() + "triplets founded");
			System.out.println(countValid(rels) + "valid triplets");
		}
		
		//Using triplet extractor (Reverb)
		long startTime = System.currentTimeMillis();
		//System.out.println("Start extraction with Reverb");
		rels = tExt.extractRelationsFromText(text);
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.println("Extraction ended in " + duration + "millis");
		System.out.println(rels.size() + "triplets founded");
		System.out.println(countValid(rels) + "valid triplets \n");		
	}
	
	private int countValid(List<TripletRelation> tList) {
		int count = 0; 
		for (TripletRelation rel : tList){
			if (rel.isComplete())
				count++;
		}
		return count;
	}
	
	private List<String> getAllFilenames(String folderPath){
		File[] listOfFiles = new File(folderPath).listFiles();
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
