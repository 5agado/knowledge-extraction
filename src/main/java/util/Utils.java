package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.TripletRelation;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.chunking.Parser;
import opennlp.tools.util.Span;

public class Utils {
	private Utils(){}
	
	public static void printParseTree(Parse p, int deep) {
		if (p.getType().length() > 1 && p.getType().substring(0, 2).equals(Parser.TOK_NODE))
			return;
		
		char[] spaces = new char[deep*2];
		Arrays.fill(spaces, ' ');
		Span span = p.getSpan();
        System.out.println(new String(spaces) + p.getType() + " -- " + p.getText().substring(span.getStart(),
				span.getEnd()));
        for (Parse child : p.getChildren()) {
        	printParseTree(child, new Integer(deep + 1));
        }
    }
	
	public static <T> void printList(List<T> list) {
		for (T s : list){
			System.out.println(s);
			System.out.println("--------------------");
		}
	}
	
	public static long countValidTriplets(List<TripletRelation> tList) {
		return tList.stream().filter(rel -> rel.isComplete()).count();
	}
	
	public static List<String> getAllFilenames(String folderPath){
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
