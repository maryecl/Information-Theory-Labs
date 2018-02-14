package ti;

import java.util.ArrayList;

public class SnippetProcessor{

		public SnippetProcessor(){
		}

		public String description(String q, String b){
			ArrayList<String> sentence = tokenizeSentence(b);
			ArrayList<ArrayList<String>> body = new ArrayList<>();
			for(String s : sentence){
				body.add(this.tokenize(s));
			}
			ArrayList<String> query = this.tokenize(this.normalize(q));
			ArrayList<ArrayList<Integer>> hits = new ArrayList<>();
			
			for(int i=0; i<body.size(); i++){
				hits.add(new ArrayList<>());
				for(int j=0; j<body.get(i).size(); j++){
					for(String s : query){
						if(this.isSimilar(s, normalize(body.get(i).get(j)))){
							hits.get(i).add(j);
						}
					}
				}
			}
			
			int best=0;
			for(int i=1; i<body.size(); i++){
				if(hits.get(i).size() > hits.get(best).size()){
					best = i;
				}
			}
			
			int last = best+1;
			if(last >= body.size()){
				last = body.size()-1;
			}	
			
			boolean pretc = true, posetc = true;
			if(best == 0) pretc = false;
			String descr = "";
			int n = 0;
			for(int i=best; i<=last; i++){
				int j;
				for(j=0; j<body.get(i).size() && n < 305; j++){
					if(hits.get(i).contains(j)){
						descr += "*"+body.get(i).get(j)+"* ";
						n += ("*"+body.get(i).get(j)+"* ").length();
					}else{
						descr += body.get(i).get(j)+" ";
						n += (body.get(i).get(j)+" ").length();
					}
				}
				if(last == body.size() && j == body.get(i).size())
					posetc = false;
			}
			String pre = "";
			for(int i=best-1; i>=0; i--){
				int j;
				for(j=body.get(i).size()-1; j>=0 && n < 305; j--){
					if(hits.get(i).contains(j)){
						pre = "*" + body.get(i).get(j) + "* " + pre;
						n += ("*"+body.get(i).get(j)+"* ").length();
					}else{
						pre = body.get(i).get(j) + " " + pre;
						n += (body.get(i).get(j)+" ").length();
					}
				}
				if(i == 0 && j == 0)
					pretc = false;
			}
			descr = pre + descr;
			if(pretc) descr = "[...] " + descr;
			else{
				for(int i=last+1; i<body.size(); i++){
					int j;
					for(j=0; j<body.get(i).size() && n < 305; j++){
						if(hits.get(i).contains(j)){
							descr += "*"+body.get(i).get(j)+"* ";
							n += ("*"+body.get(i).get(j)+"* ").length();
						}else{
							descr += body.get(i).get(j)+" ";
							n += (body.get(i).get(j)+" ").length();
						}
					}
					if(i == body.size() && j == body.get(i).size())
						posetc = false;
				}
			}
			if(posetc) descr += "[...]";
			return descr;
		}
		
		protected boolean isSimilar(String s1, String s2){
			return s1.equals(s2);
		}
		
		protected ArrayList<String> tokenizeSentence(String text)
		{
			ArrayList<String> sentence = new ArrayList<>();

			String textTemp = text;
			textTemp = textTemp.replaceAll("^\\p{ASCII}*$", " ");
			textTemp = textTemp.replaceAll("â€™", "'");
			textTemp = textTemp.replaceAll("  ", " ");
			String[] sentences = textTemp.split("[,\\.\\?\\!]");
			for(String s: sentences){
				if(!s.isEmpty())
					sentence.add(s+".");
			}
			return sentence;
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

			String textTemp = text;
			String[] words = textTemp.split("\\s+");
			for(String s: words){
				if(!s.isEmpty())
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
			return text.toLowerCase().replaceAll("[/(){}Â¡!Â¿?â€¦.,:;+*=&|-â€“_'â€œâ€�\\[\\]\\-]", "");
		}
}
