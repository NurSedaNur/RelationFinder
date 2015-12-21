package in.relationfinder;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gets the search query, finds relation and renders it to user.
 *
 * @author Raghav
 */
@WebServlet(name = "FindRelationServlet", urlPatterns = {"/find"})
public class FindRelationServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String raw_query = request.getParameter("query"); //Ex: father's mother's daughter
        String query_without_punctuation = raw_query.replaceAll("'s", "");

        String[] query = query_without_punctuation.split(" ");

        String contextPath = getServletContext().getResource("/WEB-INF").getPath();
        String[] relation = RelationsHandler.getRelation(contextPath, query);

        request.setAttribute("results", relation);
        request.setAttribute("query", raw_query);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/static/index.jsp");
        requestDispatcher.forward(request, response);
    }

}
