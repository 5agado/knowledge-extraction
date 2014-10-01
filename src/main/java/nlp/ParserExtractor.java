package nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import model.TripletRelation;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.Span;
import util.Consts;

public class ParserExtractor {
	private static final String LABEL_TOP = "TOP";
	private static final String LABEL_SENTENCE = "S";
	private static final String LABEL_NOUN_PHRASE = "NP";
	private static final String LABEL_VERBAL_PHRASE = "VP";

	private static final String LABEL_NAME_PREFIX = "NN";
	private static final String LABEL_VERB_PREFIX = "VB";
	
	private SentenceDetector sentenceDetector;
	private Parser parser;

	public ParserExtractor() {
		sentenceDetector = new SentenceDetector(Consts.EN_SENT_MODEL);
		
		try (InputStream modelIn = ParserExtractor.class.getClassLoader()
				.getResourceAsStream(Consts.EN_PARSER_MODEL);){
			ParserModel model = new ParserModel(modelIn);
			parser = ParserFactory.create(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	
	public List<TripletRelation> extractRelationsFromText(String text){
		List<TripletRelation> relations = new ArrayList<TripletRelation>();
		List<String> sentences = sentenceDetector.detectSentencesIn(text);

		for (String sentence : sentences) {
			relations.add(extractRelationFromSentence(sentence));
		}
		
		return relations;
	}
	
	public TripletRelation extractRelationFromSentence(String sentence){
		TripletRelation rel = new TripletRelation();
		
		Parse p = parseSentence(sentence);
		if (p != null){
			rel = new TripletRelation(ParserExtractor.getSubject(p),
					ParserExtractor.getPredicate(p),
					ParserExtractor.getObject(p) );
		}
		else {
			System.out.println("no valid parse from parseSentence");
		}
		
		return rel;
	}
	
	public Parse parseSentence(String sentence){
		Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);
		if (topParses.length == 0)
			return null;
		else 
			return topParses[0];
	}

	// TODO add possibility of multiple Ss and PP
	public static String getSubject(final Parse parse) {
		if (parse.getType().equals(LABEL_TOP)) {
			return getSubject(parse.getChildren()[0]);
		}

		if (parse.getType().equals(LABEL_SENTENCE)) {
			for (Parse child : parse.getChildren()) {
				if (child.getType().equals(LABEL_NOUN_PHRASE)) {
					return getSubject(child);
				}
			}
		}
		if (parse.getType().equals(LABEL_NOUN_PHRASE)) {
			return getFirstOccurenceForType(parse, LABEL_NAME_PREFIX);
		}

		return "";
	}

	public static String getPredicate(final Parse parse) {
		if (parse.getType().equals(LABEL_TOP)) {
			return getPredicate(parse.getChildren()[0]);
		}

		if (parse.getType().equals(LABEL_SENTENCE)) {
			for (Parse child : parse.getChildren()) {
				if (child.getType().equals(LABEL_VERBAL_PHRASE)) {
					return getPredicate(child);
				}
			}
			return "";
		}
		if (parse.getType().equals(LABEL_VERBAL_PHRASE)) {
			return getFirstOccurenceForType(parse, LABEL_VERB_PREFIX);
		}

		return "";
	}

	public static String getObject(final Parse parse) {
		String object = "";
		if (parse.getType().equals(LABEL_TOP)) {
			return getObject(parse.getChildren()[0]);
		}

		if (parse.getType().equals(LABEL_SENTENCE)) {
			for (Parse child : parse.getChildren()) {
				if (child.getType().equals(LABEL_VERBAL_PHRASE)) {
					object = getObject(child); 
					if (!object.isEmpty()){
						return object;
					}
				}
			}
			return object;
		}
		if (parse.getType().equals(LABEL_VERBAL_PHRASE)) {
			return getFirstOccurenceForType(parse, LABEL_NAME_PREFIX);
		}

		return object;
	}
	
	public static String getConstituent(final Parse parse, final String syntactic_cat,
			String lexical_cat) {
		String object = "";
		if (parse.getType().equals(LABEL_TOP)) {
			return getConstituent(parse.getChildren()[0], syntactic_cat, lexical_cat);
		}

		if (parse.getType().equals(LABEL_SENTENCE)) {
			for (Parse child : parse.getChildren()) {
				if (child.getType().equals(syntactic_cat)) {
					object = getConstituent(child, syntactic_cat, lexical_cat); 
					if (!object.isEmpty()){
						return object;
					}
				}
			}
			return object;
		}
		if (parse.getType().equals(syntactic_cat)) {
			return getFirstOccurenceForType(parse, lexical_cat);
		}

		return object;
	}

	// public static String getObject(Parse parse){}

	private static String getFirstOccurenceForType(final Parse parse,
			final String typePrefix) {
		
		//TODO ADD PRP 
		// For now we are only checking the prefix

		// check current
		if (parse.getType().length() > 1
				&& parse.getType().substring(0, 2).equals(typePrefix)) {
			Span span = parse.getSpan();
			String text = parse.getText().substring(span.getStart(),
					span.getEnd());
			return text;
		}

		// check children (breadth)
		for (Parse child : parse.getChildren()) {
			if (child.getType().length() > 1
					&& child.getType().substring(0, 2).equals(typePrefix)) {
				Span span = child.getSpan();
				String text = child.getText().substring(span.getStart(),
						span.getEnd());
				if (!text.isEmpty())
					return text;
			}
		}

		// recursively check for children (deep)
		for (Parse child : parse.getChildren()) {
			String text = getFirstOccurenceForType(child, typePrefix);
			if (!text.isEmpty())
				return text;
		}
		
		return "";
	}
}
