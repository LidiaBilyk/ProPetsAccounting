package telran.ProPets.security.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import telran.ProPets.dao.UserAccountRepository;
import telran.ProPets.model.UserAccount;


@Service
@Order(30)
public class AdminAuthorizationFilter implements Filter {
	
	@Autowired
	UserAccountRepository repository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();		
		Principal principal = request.getUserPrincipal();		
		if (principal != null) {
			UserAccount userAccount = repository.findById(principal.getName()).get();			
			if (checkPointCut(userAccount.getRoles(),path)) {
				response.sendError(401, "Header Authorization is not valid");
				return;				
			} 
		}
		chain.doFilter(request, response);
	}
	
	private boolean checkPointCut(List<String> roles, String path) {
		boolean check = !roles.contains("Administrator") && path.matches(".+/role/.+");		
		return check;
	}

}
