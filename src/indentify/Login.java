package indentify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Login() {
	        super();
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		String id = request.getParameter("id");
		String pw = request.getParameter("pw");

		try {
			PrintWriter out = response.getWriter();
			if(isUser(id) == 1) {
				if(connect(id, pw) == 1) {
					HttpSession session = request.getSession(true);
					session.setAttribute("userID", id);
					session.setAttribute("userPW", pw);
				}
				else
					out.print("loginFail");
			}
			else
				out.print("NoID");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int connect(String userID, String userPW) {
		try {
			Socket soc = new Socket("localhost", 5935);

			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			PrintWriter pw = new PrintWriter(soc.getOutputStream());

			System.out.println("[LOGIN] Accept to Server Success...");
			pw.println("login");
			pw.println(userID);
			pw.println(userPW);
			pw.flush();

			int result = -1;
			if (br.readLine().equals("complete"))
				result = 1;
			soc.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -2;
	}

	public int isUser(String userID) {
		/*String sql = "SELECT userID FROM VIEW_USER WHERE userID = ?;";
		try {
			String dbURL = "jdbc:mysql://localhost:3306/server?";
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(dbURL, "root", "Benel&Bende1");
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return 1;
			}
			return -1;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return -2; // db error*/
		String[] user = {"user1", "user2"};
		if(user[0].equals(userID) || user[1].equals(userID))
			return 1;
		else
			return -1;
	}
}
