package ti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BinaryCosine implements RetrievalModel {

	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
		// P1
		// extraer términos de la consulta
		ArrayList<String> terms = docProcessor.processText(queryText);

		// calcular el vector consulta
		 ArrayList<Integer> queryVector = computeVector(terms, index);

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
	protected ArrayList<Tuple<Integer, Double>> computeScores(ArrayList<Integer> queryVector, Index index)
	{
		ArrayList<Tuple<Integer, Double>> results = new ArrayList<>();

		// P1

		//Crear vector d y norma de q:
		//		id doc,				     id term, wtd
		HashMap<Integer, ArrayList<Integer>> d = new HashMap<>();
		for (Integer term : queryVector){
			ArrayList<Tuple<Integer, Double>> docs = index.invertedIndex.get(term);
			for (Tuple<Integer, Double> doc : docs){
				if (!d.containsKey(doc.item1)){
					d.put(doc.item1, new ArrayList<Integer>());
					d.get(doc.item1).add(term);
				} else{
					d.get(doc.item1).add(term);
				}
			}
		}

		//Calcular similitud para cada documento:
		for(Integer docId : d.keySet()){
			//Calcular numerador del coseno:
			ArrayList<Integer> doc = d.get(docId);
			Double sim = doc.size()/(Math.sqrt(doc.size())*Math.sqrt(queryVector.size()));
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
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the binary weight as second one.
	 */
	protected ArrayList<Integer> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Integer> vector = new ArrayList<>();

		// P1
		// Rellenar vector de términos con Ftq en el segundo valor de la tupla.
		for (String s: terms){
			Integer id = index.vocabulary.get(s).item1;
			if (!vector.contains(id)){
				vector.add(id);
			}
		}
		return vector;
	}
}
