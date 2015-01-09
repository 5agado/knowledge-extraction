package nlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.TripletRelation;
import util.Consts;
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class TripletExtractor {
	private OpenNlpSentenceChunker chunker;
	private SentenceDetector detector;
	private ReVerbExtractor reverb;
	
	public TripletExtractor() {
		try {
			chunker = new OpenNlpSentenceChunker();
		} catch (IOException e) {
			e.printStackTrace();
		}
		detector = new SentenceDetector(Consts.EN_SENT_MODEL);
		reverb = new ReVerbExtractor();
	}

	public List<TripletRelation> extractRelationsFromText(String text){
		List<TripletRelation> relations = new ArrayList<TripletRelation>();
		List<String> sentences = detector.detectSentencesIn(text);

		for (String sentence : sentences) {
			relations.add(extractRelationFromSentence(sentence));
		}
		
		return relations;
	}
	
	public TripletRelation extractRelationFromSentence(String sentence){
		TripletRelation rel = new TripletRelation();
		ChunkedSentence chunkedSent = chunker.chunkSentence(sentence);
		ConfidenceFunction confFunc = null;
		try {
			confFunc = new ReVerbOpenNlpConfFunction();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (ChunkedBinaryExtraction extr : reverb.extract(chunkedSent)) {
			double conf = confFunc.getConf(extr);
			rel.setConfidence(conf);
			
			rel.setArg1(extr.getArgument1().getText());
			rel.setRelation(extr.getRelation().getText());
			rel.setArg2(extr.getArgument2().getText());
		}
		
		return rel;
	}
}
