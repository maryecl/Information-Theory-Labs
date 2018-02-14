package ti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Okapi implements RetrievalModel {

	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor) {
		// P1
		// extraer términos de la consulta
		ArrayList<String> terms = docProcessor.processText(queryText);

		// calcular el vector consulta
		 ArrayList<Tuple<Integer, Double>> queryVector = computeVector(terms, index);

		// calcular similitud de documentos
		 return computeScores(queryVector, index); // devolver resultados
	}
	
	/**
	 * Returns the list of documents in the specified index sorted by similarity with the specified query vector.
	 *
	 * @param queryVector the vector with query term weights.
	 * @param index       the index to search in.
	 * @return a list of {@link Tuple}s where the first item is the {@code docID} and the second one the similarity score.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Tuple<Integer, Double>> queryVector, Index index)
	{
		ArrayList<Tuple<Integer, Double>> results = new ArrayList<>();

		// P1

		//Crear vector d:
		//		id doc,				     id term, wtd
		HashMap<Integer, ArrayList<Tuple<Integer, Double>>> d = new HashMap<>();
		for (Tuple<Integer, Double> term : queryVector){
			ArrayList<Tuple<Integer, Double>> docs = index.invertedIndex.get(term.item1);
			for (Tuple<Integer, Double> doc : docs){
				if (!d.containsKey(doc.item1)){
					d.put(doc.item1, new ArrayList<Tuple<Integer, Double>>());
					d.get(doc.item1).add(new Tuple<Integer, Double>(term.item1, doc.item2));
				} else{
					d.get(doc.item1).add(new Tuple<Integer, Double>(term.item1, doc.item2));
				}
			}
		}
		
		//Constantes OKAPI BM25:
		Double k = 2.0; //o 1.2
		Double b = 0.75;

		//Calcular similitud para cada documento:
		for(Integer docId : d.keySet()){
			ArrayList<Tuple<Integer, Double>> doc = d.get(docId);
			Double sim = 0.0;
			int j = 0;
			for(int i=0; i<doc.size(); i++){
				while(doc.get(i).item1 != queryVector.get(j).item1)
					j++;
			/**
			 * Nota: a falta de longitud del documento y longitud media de los documentos indexados
			 * (se deberían calcular al hacer el indice, otra práctica) ponemos valor 1 a la divisón.
			 */

			//			'			idf		  ' * '		tftd	  ' *		 / '	tftd		'			^Nota
				sim += queryVector.get(j).item2 * (doc.get(i).item2 * (k+1)) / (doc.get(i).item2 + k*(1-b + b*1));
			}
			results.add(new Tuple<Integer,Double>(docId,sim));
		}

		// Ordenar documentos por similitud y devolver
		Collections.sort(results, new Comparator<Tuple<Integer, Double>>()
		{
			@Override
			public int compare(Tuple<Integer, Double> o1, Tuple<Integer, Double> o2)
			{
				return o2.item2.compareTo(o1.item2);
			}
		});
		return results;
	}

	/**
	 * Compute the vector of weights for the specified list of terms.
	 *
	 * @param terms the list of terms.
	 * @param index the index
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and idf as second one.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();

		// P1
		// Rellenar vector de términos con Ftq en el primer valor de la tupla de valoress.
		for (String s: terms){
			Integer id = index.vocabulary.get(s).item1;
			if (!vector.contains(id)){
				Tuple<Integer,Double> newTerm = new Tuple<>(id, 0.0);
				vector.add(newTerm);
			}
		}

		// Calcular idf para cada término en la query.
		Integer nd = index.documents.size();
		for (Tuple<Integer, Double> tup : vector){
			Integer ct = index.invertedIndex.get(tup.item1).size();
			tup.item2 = Math.log10((nd-ct+0.5)/(ct+0.5));
		}

		return vector;
	}

}
