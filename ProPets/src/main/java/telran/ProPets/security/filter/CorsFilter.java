//package telran.ProPets.security.filter;
//
//import java.io.IOException;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Service;
//
//
//@Service
//@Order(9)
//public class CorsFilter implements Filter{
//
//	@Override
//	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
//			throws IOException, ServletException {
//		HttpServletRequest request = (HttpServletRequest) servletRequest;
//		 HttpServletResponse resp = (HttpServletResponse) servletResponse;
//		        resp.addHeader("Access-Control-Allow-Origin", "*");
//		        resp.addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
//		        resp.addHeader("Access-Control-Allow-Headers","Content-Type");
//		        if (request.getMethod().equals("OPTIONS")) {
//		            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
//		            return;
//		        }
//		        chain.doFilter(request, servletResponse);
//		
//	}
//
//}
