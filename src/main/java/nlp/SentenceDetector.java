package nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceDetector {
	private SentenceDetectorME sentenceDetector;
	
	public SentenceDetector(String modelPath) {		
		try (InputStream modelIn = SentenceDetector.class.getClassLoader()
						.getResourceAsStream(modelPath);){
			SentenceModel model = new SentenceModel(modelIn);
			sentenceDetector = new SentenceDetectorME(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> detectSentencesIn(String text) {
		return Arrays.asList(sentenceDetector.sentDetect(text));
	}
}
