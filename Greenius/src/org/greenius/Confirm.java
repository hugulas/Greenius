package org.greenius;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servlet implementation class Confirm
 */
@WebServlet("/Confirm")
public class Confirm extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Confirm() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String json = request.getParameter("doc");
		ObjectMapper mapper = new ObjectMapper();
		Map parsedObject = mapper.readValue(json, Map.class);
		Date date = new Date();
		parsedObject.put("docDate", (date.getYear()+1900)+"-"+date.getMonth()+"-"+date.getDate());
		CloudantConstant.getGreenDB().create(parsedObject);
		PrintWriter writer = response.getWriter();
		writer.write("{\"success\":true}");
		writer.close();
	}

}
