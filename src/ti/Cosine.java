package ti;

import java.util.*;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation.
 */
public class Cosine implements RetrievalModel
{
	public Cosine()
	{
		// vacío
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
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
		Double qNorm = 0.0;
		// P1
		for(Tuple<Integer, Double> term : queryVector){
			qNorm += term.item2;
			ArrayList<Tuple<Integer, Double>> docs = index.invertedIndex.get(term.item1);
			for(Tuple<Integer, Double> d : docs){
				int pos = -1;
				for(int i=0; i<results.size(); i++){
					if(results.get(i).item1 == term.item1){
						pos = i;
						break;
					}
				}
				if(pos < 0){
					results.add(new Tuple<>(d.item1, d.item2 * term.item2));
				} else{
					results.get(pos).item2 += d.item2 * term.item2;
				}
			}
		}
		qNorm = Math.sqrt(qNorm);
		for(Tuple<Integer, Double> res : results){
			res.item2 /= (qNorm * index.documents.get(res.item1).item2);
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
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the weight as second one.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeVector(ArrayList<String> terms, Index index)
	{
		ArrayList<Tuple<Integer, Double>> vector = new ArrayList<>();
		ArrayList<Double> idf = new ArrayList<>();

		// P1
		// Rellenar vector de términos con Ftq en el segundo valor de la tupla.
		for (String s: terms){
			if(index.vocabulary.containsKey(s)){
				Tuple<Integer,Double> term = index.vocabulary.get(s);
				int pos = -1;
				for(int i=0; i<vector.size(); i++){
					if(vector.get(i).item1 == term.item1){
						pos = i;
						break;
					}
				}
				if (pos < 0){
					vector.add(new Tuple<>(term.item1, 1.0));
					idf.add(term.item2);
				} else{
					vector.get(pos).item2++;
				}
			}
		}

		// Calcular peso para cada término en la query.
		for(int i=0; i<vector.size(); i++){
			Double tftd = 1 + Math.log(vector.get(i).item2);
			vector.get(i).item2 = tftd * idf.get(i);
		}

		return vector;
	}
}
