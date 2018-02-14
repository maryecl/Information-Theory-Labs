package ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import org.lemurproject.kstem.KrovetzStemmer;

/**
 * A processor to extract terms from HTML documents.
 */
public class HtmlProcessor implements DocumentProcessor
{

	// P3
	public HashSet<String> stopwords;
	/**
	 * Creates a new HTML processor.
	 *
	 * @param pathToStopWords the path to the file with stopwords, or {@code null} if stopwords are not filtered.
	 * @throws IOException if an error occurs while reading stopwords.
	 */
	public HtmlProcessor(File pathToStopWords) throws IOException
	{
		// P3
		// cargar stopwords
		if(pathToStopWords == null){
			stopwords = null;
		}else{
			stopwords = new HashSet<>();
			try {
				FileReader r = new FileReader(pathToStopWords.getPath());
				BufferedReader b = new BufferedReader(r);
				String line;
				while((line = b.readLine()) != null){
					line = this.normalize(line);
					stopwords.add(line);
				}
				b.close();
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Tuple<String, String> parse(String html)
	{
		// P3
		// parsear documento
		Document doc = Jsoup.parse(html); 
		String title = doc.title();
		String body = doc.select("body").text();
		return new Tuple<>(title, body);
	}

	/**
	 * Process the given text (tokenize, normalize, filter stopwords and stemize) and return the list of terms to index.
	 *
	 * @param text the text to process.
	 * @return the list of index terms.
	 */
	public ArrayList<String> processText(String text)
	{
		// P3
		// tokenizar, normalizar, stopword, stem, etc.
		Tuple<String,String> content = this.parse(text);
		String normalizedContent = normalize(content.item1 + " " + content.item2);
		ArrayList<String> tokens = tokenize(normalizedContent);
		ArrayList<String> terms = new ArrayList<>();
		for(int i=0; i<tokens.size(); i++){
			try {
				if(!this.isStopWord(tokens.get(i))){
					terms.add(stem(tokens.get(i)));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return terms;
	}

	/**
	 * Tokenize the given text.
	 *
	 * @param text the text to tokenize.
	 * @return the list of tokens.
	 */
	protected ArrayList<String> tokenize(String text)
	{
		ArrayList<String> tokens = new ArrayList<>();

		// P3
		String textTemp = text;
		String[] words = textTemp.split("\\s+");
		for(String s: words){
			tokens.add(s);
		}
		return tokens;
	}

	/**
	 * Normalize the given term.
	 *
	 * @param text the term to normalize.
	 * @return the normalized term.
	 */
	protected String normalize(String text)
	{
		//P3
		return text.toLowerCase().replaceAll("[/(){}¡!Â¿?â€¦.,:;+*=&|-â€“_'â€œâ€�\\[\\]\\-]", "");
	}

	/**
	 * Checks whether the given term is a stopword.
	 *
	 * @param term the term to check.
	 * @return {@code true} if the term is a stopword and {@code false} otherwise.
	 * @throws IOException 
	 */
	protected boolean isStopWord(String term) throws IOException
	{
		// P3
		if(stopwords == null) return false;
		else return stopwords.contains(term);
	}

	/**
	 * Stem the given term.
	 *
	 * @param term the term to stem.
	 * @return the stem of the term.
	 */
	protected String stem(String term)
	{
		// P3
		return term;
		/*
		KrovetzStemmer stemmer = new KrovetzStemmer();
		return stemmer.stem(term);
		*/
	}
}
