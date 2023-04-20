package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioDTO;
import curso.api.rest.repositoy.UsuarioRepository;

@RestController /* Arquitetura REST */
@RequestMapping(value = "/usuario")
@CrossOrigin(origins = "*") 
public class IndexController {

	@Autowired /* de fosse CDI seria @Inject */
	private UsuarioRepository usuarioRepository;

	/* Serviço RESTful */
	@GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable(value = "id") Long id,
			@PathVariable(value = "venda") Long venda) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);

		/* o retorno seria um relatorio */
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}

	/* Serviço RESTful */
	@GetMapping(value = "v1/{id}", produces = "application/json")
	public ResponseEntity<UsuarioDTO> initv1(@PathVariable(value = "id") Long id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);
		System.out.println("executando versao 1");
		return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuario.get()), HttpStatus.OK);
	}

	/* Serviço RESTful */
	@GetMapping(value = "v2/{id}", produces = "application/json")
	public ResponseEntity<Usuario> initv2(@PathVariable(value = "id") Long id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);
		System.out.println("executando versao 1");

		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}

	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete(@PathVariable("id") Long id) {

		usuarioRepository.deleteById(id);

		return "ok";
	}

	@DeleteMapping(value = "/{id}/venda", produces = "application/text")
	public String deleteVenda(@PathVariable("id") Long id) {

		usuarioRepository.deleteById(id);

		return "ok";
	}

	@GetMapping(value = "/listatodos", produces = "application/json")
	public ResponseEntity<List<Usuario>> usuario() {

		List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();

		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}

	@PostMapping(value = "/cad", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) throws Exception {

		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		/* consumindo API externa */
		URL url = new URL("https://viacep.com.br/ws/" + usuario.getCep() + "/json/");
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String cep = "";
		StringBuilder jsonCep = new StringBuilder();
		while ((cep = bufferedReader.readLine()) != null) {
			jsonCep.append(cep);

		}

		Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);
		usuario.setCep(userAux.getCep());
		usuario.setLocalidade(userAux.getLocalidade());
		usuario.setBairro(userAux.getBairro());
		usuario.setComplemento(userAux.getComplemento());
		usuario.setLogradouro(userAux.getLogradouro());
		usuario.setUf(userAux.getUf());

		/* consumindo API externa */
		String senha = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senha);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}

	@PutMapping(value = "/atualizar", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {

		/* outras rotinas antes de atualizar */

		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		Usuario userTemp = usuarioRepository.findUserByLogin(usuario.getLogin());

		if (!userTemp.getSenha().equals(usuario.getSenha())) {
			String senha = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senha);
		}

		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);

	}

	@PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity updateVenda(@PathVariable Long iduser, @PathVariable Long idvenda) {
		/* outras rotinas antes de atualizar */

		// Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity("Venda atualzada", HttpStatus.OK);

	}

	@PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity cadastrarvenda(@PathVariable Long iduser, @PathVariable Long idvenda) {

		/* Aqui seria o processo de venda */
		// Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity("id user :" + iduser + " idvenda :" + idvenda, HttpStatus.OK);

	}

}
