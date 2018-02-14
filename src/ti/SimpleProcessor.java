package ti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A very simple document processor.
 */
public class SimpleProcessor implements DocumentProcessor
{
    public SimpleProcessor()
    {
    }

    /**
     * {@inheritDoc}
     */
    public Tuple<String, String> parse(String docText)
    {
        return new Tuple<>("", docText);
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<String> processText(String text)
    {
        List<String> tokens = Arrays.asList(text.toLowerCase().replaceAll("[^a-z0-9']", " ").split("\\s+"));

        ArrayList<String> terms = new ArrayList<>();
        for (String token : tokens)
            if (token.length() > 4)	//4 normal, 0 test pequeño
                terms.add(token);

        return terms;
    }
}
