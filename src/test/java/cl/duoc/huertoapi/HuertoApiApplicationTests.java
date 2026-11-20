package cl.duoc.huertoapi;

import cl.duoc.huertoapi.repository.ProblemaRepository;
import cl.duoc.huertoapi.repository.SectorRepository;
import cl.duoc.huertoapi.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifica que la aplicación arranca y que DataInitializer carga los datos de ejemplo. */
@SpringBootTest
class HuertoApiApplicationTests {

	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private SectorRepository sectorRepository;
	@Autowired
	private ProblemaRepository problemaRepository;

	@Test
	void contextLoadsYCargaDatosDemo() {
		assertEquals(4, usuarioRepository.count());
		assertEquals(4, sectorRepository.count());
		assertEquals(3, problemaRepository.count());
	}

}
