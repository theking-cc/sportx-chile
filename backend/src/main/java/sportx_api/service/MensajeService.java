package sportx_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sportx_api.dto.MensajeRequest;
import sportx_api.dto.MensajeResponse;
import sportx_api.model.MensajeContacto;
import sportx_api.repository.MensajeContactoRepository;

@Service
public class MensajeService {

    private static final String ASUNTO_POR_DEFECTO = "Consulta desde el sitio web";

    private final MensajeContactoRepository mensajeRepository;

    public MensajeService(MensajeContactoRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Transactional
    public MensajeResponse guardar(MensajeRequest datos, Long usuarioId) {

        MensajeContacto m = new MensajeContacto();
        m.setIdUsuario(usuarioId);
        m.setNombre(datos.nombre().trim());
        m.setEmail(datos.email().trim().toLowerCase(Locale.ROOT));
        m.setAsunto(datos.asunto() == null || datos.asunto().isBlank()
            ? ASUNTO_POR_DEFECTO
            : datos.asunto().trim());
        m.setMensaje(datos.mensaje().trim());
        m.setEstado("Nuevo");
        m.setFechaCreacion(LocalDateTime.now());

        return convertir(mensajeRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MensajeResponse> listar() {
        return mensajeRepository.findAllByOrderByFechaCreacionDesc()
            .stream().map(this::convertir).toList();
    }

    private MensajeResponse convertir(MensajeContacto m) {
        return new MensajeResponse(
            m.getId(), m.getIdUsuario(), m.getNombre(), m.getEmail(),
            m.getAsunto(), m.getMensaje(), m.getEstado(), m.getFechaCreacion());
    }
}
