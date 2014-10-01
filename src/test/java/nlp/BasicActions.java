package nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import util.Consts;
import util.Utils;

public class BasicActions {	
	private static final String TEST_TEXT = "books/itaTranslated.txt"; 
	private static final String TEST_PHRASE = "Michael McGinn is the mayor of Seattle.";
	
	@Test
	public void testSentenceDetector(){
		SentenceDetector detector = new SentenceDetector(Consts.EN_SENT_MODEL);
		try (InputStream testArticle = BasicActions.class.getClassLoader()
				.getResourceAsStream(TEST_TEXT);) {
			
			String text = IOUtils.toString(testArticle, "UTF-8");
			List<String> sentences = detector.detectSentencesIn(text);
			Utils.printList(sentences);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String[] testTokenizer(){
		String[] tokens = {};
		try (InputStream modelIn = BasicActions.class.getClassLoader()
				.getResourceAsStream(Consts.EN_TOKEN_MODEL);) {
			
			TokenizerModel tokenModel = new TokenizerModel(modelIn);
			Tokenizer tokenizer = new TokenizerME(tokenModel);
			tokens = tokenizer.tokenize(TEST_PHRASE);
			System.out.println(Arrays.toString(tokens));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tokens;
	}
	
	public String[] testTagger(){
		String[] tags = {};
		try (InputStream modelIn = BasicActions.class.getClassLoader().
					getResourceAsStream(Consts.EN_POS_MODEL);){
					
			POSModel posModel = new POSModel(modelIn);
			POSTaggerME tagger = new POSTaggerME(posModel);
			tags = tagger.tag(testTokenizer());
 			System.out.println(Arrays.toString(tags));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tags;
	}
	
	@Test
	public void testNameFinder(){
		try (InputStream modelIn = BasicActions.class.getClassLoader()
					.getResourceAsStream(Consts.EN_NER_MODEL);){
			
			TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
			NameFinderME nameFinder = new NameFinderME(model);
			Span nameSpans[] = nameFinder.find(testTokenizer());
			System.out.println(Arrays.toString(nameSpans));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testChunker(){
		try (InputStream modelIn = BasicActions.class.getClassLoader().
					getResourceAsStream(Consts.EN_CHUNK_MODEL);){
			
			String[] tokens = testTokenizer();
			String[] tags = testTagger();
			
			ChunkerModel chunkerModel = new ChunkerModel(modelIn);
			ChunkerME chunker = new ChunkerME(chunkerModel);
			String chunks[] = chunker.chunk(tokens, tags);
 			System.out.println(Arrays.toString(chunks));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}