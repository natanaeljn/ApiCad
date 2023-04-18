package curso.api.rest.security;

import java.io.IOException;
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
import javassist.bytecode.stackmap.BasicBlock.Catch;

@Service
@Component
public class JWTTokenAutenticacaoService {

	private static final long EXPERATION_TIME = 172800000;
	private static final String SECRET = "senha";
	private static final String TOKEN_PREFIX = "Bearer";
	private static final String HEADER_STRING = "Authorization";

	public void addAuthentication(HttpServletResponse response, String username) throws IOException {
		/* monatgem do token */
		String JWT = Jwts.builder()/* chama o gerador de token */
				.setSubject(username)/* adiciona o usuario */
				.setExpiration(new Date(System.currentTimeMillis() + EXPERATION_TIME))/* tempo de expiraçao */
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();/* compactaçao e algoritimos de geraçao */

		/* junta o token com o prefixo */
		String token = TOKEN_PREFIX + " " + JWT;

		/* adiciona no cabeçalho HTTP */
		response.setHeader(HEADER_STRING, token);

		ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class).atualizaTokenUsuario(username,
				JWT);
		
		response.addHeader("Access-Control-Allow-Origin", "*");

		/* liberando portas diferentes que acessao a api, ou clientes web */
		liberacaoCors(response);
		
		

		/* escreve token como resposta no corpo do http */
		response.getWriter().write("{\"Authorization\": \"" + token + "\"}");

	}

	/*
	 * Agora teremos outro metodo que retorna o usuario validado com token ou caso
	 * não seja valido retorna um null
	 */
	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
		/* pega o token enviado no cabeçalho http */
		String token = request.getHeader(HEADER_STRING);
		try {
			if (token != null) {

				String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();

				/* faz a validaçao do token do usuario */
				String user = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(tokenLimpo).getBody().getSubject();
				if (user != null) {

					Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
							.findUserByLogin(user);

					if (usuario != null) {
						if (tokenLimpo.equalsIgnoreCase(usuario.getToken())) {
							return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
									usuario.getAuthorities());

						}
					}

				}

			}
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			try {
				response.getOutputStream().println("seu token expirou, faça login ou gere outro para a autenticação");
			} catch (IOException e1) {

			}
		}
		liberacaoCors(response);
		response.addHeader("Access-Control-Allow-Origin", "*");
        return null;/* nao autorizado */

	}

	private void liberacaoCors(HttpServletResponse response) {
		if (response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}

		if (response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}

		if (response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}

		if (response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}

	}

}
