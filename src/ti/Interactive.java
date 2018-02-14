package ti;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class contains the logic to run the retrieval process of the search engine in interactive mode.
 */
public class Interactive
{
	protected RetrievalModel model;
	protected Index index;
	protected DocumentProcessor docProcessor;
	protected SnippetProcessor snippetProcessor;

	/**
	 * Creates a new interactive retriever using the given model.
	 *
	 * @param model        the retrieval model to run queries.
	 * @param index        the index.
	 * @param docProcessor the processor to extract query terms.
	 */
	public Interactive(RetrievalModel model, Index index, DocumentProcessor docProcessor)
	{
		this.model = model;
		this.index = index;
		this.docProcessor = docProcessor;
		this.snippetProcessor = new SnippetProcessor();
	}

	/**
	 * Runs the interactive retrieval process. It asks the user for a query, and then it prints the results to
	 * {@link System#out} showing the document title and a snippet, highlighting important terms for the query.
	 *
	 * @throws Exception in an error occurs during the process.
	 */
	public void run() throws Exception
	{
		// Run prompt loop
		Scanner scan = new Scanner(System.in);
		String input, input2;
		do {
			System.out.println();
			System.out.print("Query (empty to exit): ");
			scan.reset();
			input = scan.nextLine();
			ArrayList<Tuple<Integer, Double>> results = this.model.runQuery(input, this.index, this.docProcessor);

			// P5
			// paginar resultados
			int page = 0, pages = results.size()/10;
			if(results.size() != 0){
				do{
					this.printResults(input, results,page*10,10);
					System.out.println("Results: " + results.size());
					System.out.println("Number of pages: " + (pages+1));
					System.out.println("Current page: " + (page+1));
					System.out.println("Options: Previous(p), Next(n), Finish(f)");
					scan.reset();
					input2 = scan.nextLine();
				   
					if(input2.equals("p")){
						if(page>0){
							page--;
						}
					}else if(input2.equals("n")){
						if(page<pages){
							page++;
				    	}
				   	}else if(input2.equals("f")){
				   		break;
				   	}
				}while(!input2.isEmpty());
			}else{
				if(!input.isEmpty()){
					System.out.println("The search \""+input+"\" has no results.");
				}
			}
		} while (!input.isEmpty());
		scan.close();
	}
	
	/**
	 * Print a page of results for a query, showing for each document its title and snippet, with highlighted terms.
	 *
	 * @param results the results for the query. A list of {@link Tuple}s where the first item is the {@code docID} and
	 *                the second item is the similarity score.
	 * @param from    index of the first result to print.
	 * @param count   how many results to print from the {@code from} index.
	 */
	protected void printResults(String query, ArrayList<Tuple<Integer, Double>> results, int from, int count)
	{
		// P5
		Tuple<String,String> document;
		for(int i=from; i<(from+count) && i<results.size(); i++){
	  		try {
	  			document = index.getCachedDocument(results.get(i).item1);
	  			System.out.println("-----------------------------------------------------");
	  			System.out.println((i+1)+"-[id:"+results.get(i).item1+"] "+document.item1); //Solo título hay que implementar los snippets para descripción
	  			System.out.println("\n" + snippetProcessor.description(query, document.item2));
	  		} catch (Exception e) {
	  			// TODO Auto-generated catch block
	  			e.printStackTrace();
	  		}
	  	}
		System.out.println("-----------------------------------------------------");
	}
}