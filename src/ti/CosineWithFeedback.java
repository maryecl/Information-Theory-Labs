package ti;

import java.util.ArrayList;

/**
 * Implements retrieval in a vector space with the cosine similarity function and a TFxIDF weight formulation,
 * plus pseudorelevance feedback.
 */
public class CosineWithFeedback extends Cosine
{
	protected int feedbackDepth;
	protected int feedbackNDepth;
	protected double feedbackAlpha;
	protected double feedbackBeta;
	//protected double feedbackGamma;


	/**
	 * Creates a new retriver with the specified pseudorelevance feedback parameters.
	 *
	 * @param feedbackDepth number of documents to consider relevant.
	 * @param feedbackAlpha relative weight of the original query terms
	 * @param feedbackBeta  relative weight of the expanded terms.
	 */
	public CosineWithFeedback(int feedbackDepth, double feedbackAlpha, double feedbackBeta)//,double feedbackGamma)
	{
		super();
		this.feedbackDepth = feedbackDepth;
		this.feedbackAlpha = feedbackAlpha;
		this.feedbackBeta = feedbackBeta;
		//this.feedbackGamma = feedbackGamma; 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList<Tuple<Integer, Double>> runQuery(String queryText, Index index, DocumentProcessor docProcessor)
	{
		// P4
		// extraer términos de la consulta
		ArrayList<String> terms = docProcessor.processText(queryText);

		// calcular el vector consulta
		ArrayList<Tuple<Integer, Double>> queryVector = computeVector(terms, index);
		
		// calcular resultados iniciales
		ArrayList<Tuple<Integer, Double>> scores = computeScores(queryVector, index);
		
		ArrayList<Tuple<Integer, Double>> kscores;
		if(scores.size() <= feedbackDepth)
			kscores = scores;
		else{
			kscores = new ArrayList<>();
			for(int i=0; i<feedbackDepth; i++){
				kscores.add(scores.get(i));
			}
		}
		
		/**
		 * Calcular el vector qm a partir de q0 y los documentos, segÃºn el algoritmo de Rocchio 
		 * Calcular la similitud de qm con todos los documentos.
		 * Generar la salida con los primeros 500 resultados.
		 */
		
		ArrayList<Tuple<Integer, Double>> list = computeFeedbackVector(queryVector, kscores, index);
		return computeScores(list, index); // y devolver resultados
	}
	
	/**
	 * Computes the modified query vector for relevance feedback.
	 *
	 * @param queryVector the original query vector.
	 * @param results     the results with the original query.
	 * @param index       the index to search in.
	 * @return a list of {@code Tuple}s with the {@code termID} as first item and the weight as second one.
	 */
	protected ArrayList<Tuple<Integer, Double>> computeFeedbackVector(ArrayList<Tuple<Integer, Double>> queryVector,
	                                                                  ArrayList<Tuple<Integer, Double>> results,
	                                                                  Index index)
	{
		// P4
		ArrayList<Tuple<Integer,Double>> newQueryVector = new ArrayList<>();
		for(Tuple<Integer,Double> t : queryVector) {
			Tuple<Integer,Double> newTuple = new Tuple<>(t.item1,t.item2*this.feedbackAlpha);
			newQueryVector.add(newTuple);
		}
		
		/*
		ArrayList<Tuple<Integer,Double>> gammaPart = new ArrayList<>();
		
		// Empezamos en la posición máxima y volvemos hacia la posición K.
		for (int i = results.size(); i > this.feedbackDepth; i--) {
			gammaPart.add(results.get(i));
		}
		*/
		
		
		ArrayList<Tuple<Integer,Double>> gammaPart = new ArrayList<>();
		
		ArrayList<Tuple<Integer,Double>> betaPart = new ArrayList<>();
		for(Tuple<Integer,Double> doc : results){
			int i;
			for(i=0; i<betaPart.size(); i++){
				if(doc.item1 == betaPart.get(i).item1){
					betaPart.get(i).item2 += doc.item2;
					break;
				}
			}
			if(i == betaPart.size()){
				betaPart.add(doc);
			}
		}
		for(Tuple<Integer,Double> doc : betaPart){
			doc.item2 *= feedbackBeta;
		}
		
		// Actualizar qm
		for(Tuple<Integer,Double> betaDoc : betaPart){
			int i;
			for(i=0; i<newQueryVector.size(); i++){
				if(betaDoc.item1 == newQueryVector.get(i).item1){
					newQueryVector.get(i).item2 += betaDoc.item2;
					break;
				}
			}
			if(i == newQueryVector.size()){
				newQueryVector.add(betaDoc);
			}
		}	
		
		
	/*	for(Tuple<Integer,Double> t : gammaPart){ 
			for (int i = 0; i < newQueryVector.size(); i++) {
				if (t.item1 == newQueryVector.get(i).item1) {
					newQueryVector.get(i).item2 -= t.item2;
				}
			}
		}
		*/
		return newQueryVector;
	}
}
