package curso.api.rest.security;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {

	private static final long EXPERATION_TIME = 172800000;
	private static final String SECRET = "senha";
	private static final String TOKEN_PREFIX = "Bearer";
	private static final String HEADER_STRING = "Authorization";
	
	
	public void addAuthentication(HttpServletResponse response , String username) throws Exception {
		/*monatgem do token*/
		String JWT = Jwts.builder()/*chama o gerador de token*/
				.setSubject(username)/*adiciona o usuario*/
				.setExpiration(new Date(System.currentTimeMillis()+EXPERATION_TIME))/*tempo de expiraçao*/
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();/*compactaçao e algoritimos de geraçao*/
		
		/*junta o token com o prefixo*/
		String token= TOKEN_PREFIX + " " + JWT;
		
		/*adiciona no cabeçalho HTTP*/
		response.setHeader(HEADER_STRING, token);
		
		/*escreve token como resposta no corpo do http*/
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
		
	}
	/*Agora teremos outro metodo que retorna o usuario  validado com token ou caso não seja valido retorna um null */
	public Authentication getAuthentication(HttpServletRequest request) {
		/*pega o token enviado no cabeçalho http*/
		String token = request.getHeader(HEADER_STRING);
		if(token!=null) {
			/*faz a validaçao do token do usuario*/
			String user = Jwts.parser().setSigningKey(SECRET)
					.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
			        .getBody().getSubject();
			if(user != null) {
				Usuario usuario = ApplicationContextLoad.getApplicationContext()
						          .getBean(UsuarioRepository.class).findUserByLogin(user);
				if(usuario!=null) {
					return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(), usuario.getAuthorities());
					
				}
				
			}
		
		}
		
			return null;/*nao autorizado*/
		
		
		
		
	}
	
	
	
}
