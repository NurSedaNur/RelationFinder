package in.relationfinder;

import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPSyntaxErrorException;
import com.ugos.jiprolog.engine.JIPTerm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Handles all the stuff related to Relationships. Couldn't think of another name :/.
 * Using JIProlog to query Prolog.
 *
 * @author Raghav
 */
public class RelationsHandler {

    /**
     * Gets relation by running the Prolog query using JIProlog.
     *
     * @param raw_query The query input by user as it is. Ex. father's mother's sister
     * @return An Object array which contains gender of the relation and A list of relation(s).
     *         There can be more than one relation sometimes.
     *         Ex. grandparent's son = father and uncle.
     */
    public static Object[] getRelation(String raw_query) {
        String knowledgeBase = "familyrelationships.pl";
        String[] relations = processQuery(raw_query);
        String prolog_query = generatePrologQuery(relations);
        List<String> results = null;

        JIPEngine jipEngine = new JIPEngine();
        try {
            jipEngine.setSearchPath(Constants.RESOURCES_PATH);
            jipEngine.consultFile(knowledgeBase);
            JIPQuery jipQuery = jipEngine.openSynchronousQuery(prolog_query);
            if (jipQuery.hasMoreChoicePoints())
                results = new ArrayList<>();
            while (jipQuery.hasMoreChoicePoints()) {
                JIPTerm jipTerm = jipQuery.nextSolution();
                if (jipTerm == null)
                    break;
                Hashtable hashtable = jipTerm.getVariablesTable();
                if (!hashtable.containsKey("Result"))
                    break;
                String result = hashtable.get("Result").toString();
                results.add(result);
            }
        } catch (JIPSyntaxErrorException e) {
            //TODO Logging
        }

        // We get the last element (The relation user wanted to know) from the query.
        String gender = getGender(relations[relations.length - 1]);

        return new Object[]{ gender, results };
    }

    /**
     * Processes user query.
     * Removes redundant characters and changes other relation names to actual name used in Prolog rules.
     * For ex, if there's daddy in query it gets changed to father, because that's what we use in our Prolog knowledge base.
     *
     * @param raw_query The query input by user as it is.
     * @return String array of relations. Basically query.split(" ").
     */
    private static String[] processQuery(String raw_query) {
        String query;
        query = raw_query.replaceAll("'s", ""); //Replace all apostrophe s
        String[] relations = query.split(" ");

        for (int i = 0; i < relations.length; i++) //Whoa
            if (Constants.OTHER_RELATION_NAMES.contains(relations[i]))
                for (Entry<String, ArrayList<String>> entry : Constants.RELATION_NAMES_MAP.entrySet())
                    for (String otherName : entry.getValue())
                        if (Objects.equals(relations[i], otherName))
                            relations[i] = entry.getKey();
        return relations;
    }

    /**
     * TODO Document this in detail
     * Generates a Prolog query string to query family relations in Prolog using JIProlog.
     *
     * @param relations A String array of relations on the basis of which we return the query.
     *                  Ex. ['father', 'mother'] means relation to my father's mother.
     * @return Prolog query string
     */
    private static String generatePrologQuery(String[] relations) {
        StringBuilder builder = new StringBuilder();

        char alphabet = 'A';
        for (int i = 0; i < relations.length; i++) {
            builder.append("query_family(").append(alphabet).append(", ");
            if (i == 0)
                builder.append("raghav, ");
            else
                builder.append((char) (alphabet - 1)).append(", ");
            builder.append(relations[i]).append("), ");
            if (i == relations.length - 1)
                builder.append("query_family(").append(alphabet).append(", raghav, Result).");
            alphabet++;
        }
        return builder.toString();
    }

    /**
     * Returns other names of a relation.
     *
     * @param relation Relation
     */
    public static String[] getOtherNames(String relation) {
        List<String> otherNames = Constants.RELATION_NAMES_MAP.get(relation);
        if (otherNames != null) {
            String[] result = new String[otherNames.size()];
            otherNames.toArray(result);
            return result;
        } else
            return new String[]{relation};
    }

    /**
     * Processes raw relationship names.
     * Replaces underscore with space or dash if it contains '_law'.
     * So, paternal_grandfather becomes paternal grandfather and brother_in_law becomes brother-in-law.
     *
     * @param results List of relations.
     * @return Processed relations.
     */
    public static List<String[]> processRelations(List<String[]> results) {
        for (String[] result : results) {
            for (int i = 0; i < result.length; i++) {
                String relation = result[i];
                String replaceBy = relation.contains("_law") ? "-" : " ";
                result[i] = relation.replaceAll("_", replaceBy);
            }
        }
        return results;
    }

    private static String getGender(String relation) {
        return Constants.GENDER.get(relation);
    }
}
