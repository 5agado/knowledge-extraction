package nlp;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.TreebankLinker;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.parser.Parse;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import util.Consts;

public class CoreferenceResolution {
	Linker linker = null;
	ParserExtractor parser = null;
	SentenceDetector detector = null;
	String text;

	@Before
	public void initLinker() throws IOException {
		try {
			linker = new TreebankLinker("src/main/resources/coref",
					LinkerMode.TEST);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		parser = new ParserExtractor();
		detector = new SentenceDetector(Consts.EN_SENT_MODEL);
		
		InputStream testArticle = CoreferenceResolution.class.getClassLoader().getResourceAsStream(
				"story1.txt");
		
		text = IOUtils.toString(testArticle, "UTF-8");
	}

	@Test
	public void testOpenNLPResolution() throws IOException {
		assertNotNull(linker);
		assertNotNull(parser);
		assertNotNull(detector);
		
		List<String> sentences = detector.detectSentencesIn(text);
		int sentenceNumber = 0;
		List<Mention> document = new ArrayList<Mention>();
		List<Parse> parses = new ArrayList<Parse>();
		for (String sentence : sentences) {
			Parse p = parser.parseSentence(sentence);
			if (p == null){
				System.out.println("Null parse for: " + sentence);
				continue;
			}
			parses.add(p);
			Mention[] extents = linker.getMentionFinder().getMentions(
					new DefaultParse(p, sentenceNumber));
			// construct new parses for mentions which don't have
			// constituents.
			for (int ei = 0, en = extents.length; ei < en; ei++) {
				// System.err.println("PennTreebankLiner.main: "+ei+" "+extents[ei]);

				if (extents[ei].getParse() == null) {
					// not sure how to get head index, but its not used at
					// this point.
					Parse snp = new Parse(p.getText(), extents[ei].getSpan(),
							"NML", 1.0, 0);
					p.insert(snp);
					extents[ei].setParse(new DefaultParse(snp, sentenceNumber));
				}

			}
			document.addAll(Arrays.asList(extents));
			sentenceNumber++;
		}

		DiscourseEntity[] entities = linker.getEntities(document
				.toArray(new Mention[document.size()]));
		new CorefParse(parses, entities).print();
		sentenceNumber = 0;
		document.clear();
		parses.clear();
	}
}
