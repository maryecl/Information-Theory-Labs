package ti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * This class contains the logic to run the indexing process of the search engine.
 */
public class Indexer
{
	protected File pathToIndex;
	protected File pathToCollection;
	protected DocumentProcessor docProcessor;

	/**
	 * Creates a new indexer with the given paths and document processor.
	 * @param pathToIndex path to the index directory.
	 * @param pathToCollection path to the original documents directory.
	 * @param docProcessor document processor to extract terms.
	 */
	public Indexer(File pathToIndex, File pathToCollection, DocumentProcessor docProcessor)
	{
		this.pathToIndex = pathToIndex;
		this.pathToCollection = pathToCollection;
		this.docProcessor = docProcessor;
	}

	/**
	 * Run the indexing process in two passes and save the index to disk.
	 * @throws IOException if an error occurs while indexing.
	 */
	public void run() throws IOException
	{
		Index ind = new Index(this.pathToIndex.getPath());
		File di = new File(this.pathToIndex.getPath());
        if (!di.exists())
            di.mkdir();
        
		this.firstPass(ind);
		//System.out.println("\nFirst pass status:");
		//ind.printIndex();
		
		this.secondPass(ind);
		//System.out.println("\nSecond pass status:");
		//ind.printIndex();
		
		// Save index
		System.err.print("Saving index...");
		ind.save();
		System.err.println("done.");
		System.err.println("Index statistics:");
		ind.printStatistics();
	}
	
	/**
	 * Runs the first pass of the indexer.
	 * It builds the inverted index by iterating all original document files and calling {@link #processDocument}.
	 * @param ind the index.
	 * @throws IOException if an error occurs while processing a document.
	 */
	protected void firstPass(Index ind) throws IOException
	{
		DecimalFormat df = new DecimalFormat("#.##");
		long startTime = System.currentTimeMillis();
		int totalDocuments = 0;
		long totalBytesDocuments = 0;

		System.err.println("Running first pass...");
		for (File subDir : this.pathToCollection.listFiles()) {
			if (!subDir.getName().startsWith(".")) {
				for (File docFile : subDir.listFiles()) {
					if (docFile.getPath().endsWith(".html")) {
						try {
							System.err.print("  Indexing file " + docFile.getName() + "...");
							this.processDocument(docFile, ind);
							System.err.print("done.");
						} catch (IOException ex) {
							System.err.println("exception!");
							System.err.print(ex.getMessage());
						} finally {
							System.err.println();
						}
						totalDocuments++;
						totalBytesDocuments += docFile.length();
					}
				}
			}
		}

		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime) / 1000d;
		double totalMegabytes = totalBytesDocuments / 1024d / 1024d;
		System.err.println("...done:");
		System.err.println("  - Documents: " + totalDocuments + " (" + df.format(totalMegabytes) + " MB).");
		System.err.println("  - Time: " + df.format(totalTime) + " seconds.");
		System.err.println("  - Throughput: " + df.format(totalMegabytes / totalTime) + " MB/s.");
	}
	/**
	 * Runs the second pass of the indexer.
	 * Here it traverses the inverted index to compute and store IDF, update weights in the postings,
	 * build the direct index, and compute document norms.
	 * @param ind the index.
	 */
	protected void secondPass(Index ind)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		long startTime = System.currentTimeMillis();

		System.err.println("Running second pass...");
		System.err.print("  Updating term weights and direct index...");

		// P2
		// recorrer el índice para calcular IDF y actualizar pesos
		/*
		 * Iteramos sobre los términos del índice y miramos los documentos que lo
		 * contienen. Para cada documentos calculamos su IDF y lo multiplicamos
		 * por el TF calculado en la primera pasada.
		 *
		 * A parte, vamos sumando el cuadrado del peso que hemos calculado en el
		 * item2 del documento para luego hacer la norma.
		 */
		Double nd = (double) ind.documents.size();
		for(String term : ind.vocabulary.keySet()){
			Tuple<Integer,Double> t = ind.vocabulary.get(term);
			Double ct = (double) ind.invertedIndex.get(t.item1).size();
			Double idf = Math.log(1.0 + (nd/ct));
			t.item2 = idf;
			for(Tuple<Integer, Double> d : ind.invertedIndex.get(t.item1)){
				d.item2 *= idf;
				ind.documents.get(d.item1).item2 += Math.pow(d.item2,2);
			}
		}

		// P4
		// actualizar directIndex
		// Traverse all terms to compute IDF, direct postings, and norm summations
		
		for(int i=0; i<ind.invertedIndex.size(); i++){
			for(Tuple<Integer, Double> posting : ind.invertedIndex.get(i)){
				ind.directIndex.get(posting.item1).add(new Tuple<>(i,posting.item2));
			}
		}
		
		System.err.println("done.");
		System.err.print("  Updating document norms...");

		// P2
		// actualizar normas de documentos
		/*
		 * Iteramos sobre los documentos y hacemos la raíz cuadrada de los
		 * sumatorios de pesos calculados antes.
		 */
		for(Tuple<String, Double> d : ind.documents){
			d.item2 = Math.sqrt(d.item2);
		}

		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime) / 1000d;
		System.err.println("done.");
		System.err.println("...done");
		System.err.println("  - Time: " + df.format(totalTime) + " seconds.");
	}
	/**
	 * Process the original document in the specified path and add it to the given index.
	 * <p>
	 * After extracting the document terms, it populates the vocabulary and document structures,
	 * and adds the corresponding postings to the inverted index.
	 * @param docFile the path to the original document file.
	 * @param ind the index to add the document to.
	 * @throws IOException if an error occurrs while processing this document.
	 */
	protected void processDocument(File docFile, Index ind) throws IOException
	{
		// P2
		// leer documento desde disc
		FileReader r = new FileReader(docFile.getPath());
		BufferedReader b = new BufferedReader(r);
		String text = "";
		String line;
		while((line = b.readLine()) != null){
			text += " " + line;
		}
		b.close();
		r.close();
		
		//procesarlo para obtener los términos
		Tuple<String, String> document = docProcessor.parse(text);
		ArrayList<String> title = docProcessor.processText(document.item1);
		ArrayList<String> body = docProcessor.processText(document.item2);
		
		ArrayList<Tuple<String, Double>> vector = new ArrayList<>();
		
		// calcular pesos
		for (String s: title){
			int pos = -1;
			for(int i=0; i<vector.size(); i++){
				if(vector.get(i).item1.equals(s)){
					pos = i;
					break;
				}
			}
			if (pos < 0){
				vector.add(new Tuple<>(s, 2.0));
			} else{
				vector.get(pos).item2 += 2;
			}
		}
		
		for (String s: body){
			int pos = -1;
			for(int i=0; i<vector.size(); i++){
				if(vector.get(i).item1.equals(s)){
					pos = i;
					break;
				}
			}
			if (pos < 0){
				vector.add(new Tuple<>(s, 1.0));
			} else{
				vector.get(pos).item2++;
			}
		}
		
		// actualizar estructuras del índice: vocabulary, documents e invertedIndex
		String namedoc = docFile.getName();
		String[] namedocsplit = namedoc.split("\\.");
		namedoc = namedocsplit[0];

		ind.documents.add(new Tuple<>(namedoc,0.0));
		ind.directIndex.add(new ArrayList<>());
		int docId = ind.documents.size() - 1;
		ind.setCachedDocument(docId, new Tuple<>(document.item1, document.item2));
		for(int i=0; i<vector.size(); i++){
			if (!ind.vocabulary.containsKey(vector.get(i).item1)) {
				ind.invertedIndex.add(new ArrayList<>());
				ind.vocabulary.put(vector.get(i).item1,new Tuple<>(ind.invertedIndex.size() - 1, 0.0));
			}
			int termId = ind.vocabulary.get(vector.get(i).item1).item1;
			ind.invertedIndex.get(termId).add(new Tuple<>(docId,1 + Math.log(vector.get(i).item2)));
		}
	}
}
