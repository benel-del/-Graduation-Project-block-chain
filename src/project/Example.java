package project;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Example
 */
// @WebServlet("/Example")
public class Example extends HttpServlet {
        private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Example() {
        super();
        // TODO Auto-generated constructor stub
    }

        /**
         * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
         */
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                // TODO Auto-generated method stub
                // response.getWriter().append("Served at: ").append(request.getContextPath());
                doPost(request, response);
        }

        /**
         * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
         */
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                // TODO Auto-generated method stub
                // doGet(request, response);
                request.setCharacterEncoding("UTF-8");

                String servLogAll =  (String) request.getParameter("servLog");
                String blockLogAll = (String) request.getParameter("blockLog");
                System.out.println(servLogAll);

                String[] splitLog1 = servLogAll.split("\\n");
                String[] splitLog2 = blockLogAll.split("\\n");
                request.setAttribute("servLog", splitLog1);
                request.setAttribute("blockLog", splitLog2);

                ServletContext context = getServletContext();
                RequestDispatcher dispatcher = context.getRequestDispatcher("/source/project/logView.jsp"); // /logView.jsp
                dispatcher.forward(request, response);
        }

}