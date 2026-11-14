package sportx_api.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import sportx_api.dto.ComunaResponse;
import sportx_api.model.Comuna;
import sportx_api.model.Region;
import sportx_api.repository.ComunaRepository;
import sportx_api.repository.RegionRepository;

// Comunas con el nombre de su región (tablas comunas y regiones).
@Service
@Transactional(readOnly = true)
public class UbicacionService {

    private static final Comparator<ComunaResponse> POR_REGION_Y_NOMBRE =
        Comparator.comparing(
                ComunaResponse::region,
                Comparator.nullsLast(Comparator.<String>naturalOrder()))
            .thenComparing(ComunaResponse::nombre);

    private final ComunaRepository comunaRepository;
    private final RegionRepository regionRepository;

    public UbicacionService(
            ComunaRepository comunaRepository,
            RegionRepository regionRepository) {

        this.comunaRepository = comunaRepository;
        this.regionRepository = regionRepository;
    }

    public List<ComunaResponse> listarComunas() {

        Map<Long, String> regiones = regionRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Region::getId, Region::getNombre));

        return comunaRepository.findAll()
            .stream()
            .map(c -> convertir(c, regiones.get(c.getIdRegion())))
            .sorted(POR_REGION_Y_NOMBRE)
            .toList();
    }

    public ComunaResponse buscarComuna(Long idComuna) {

        if (idComuna == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "La comuna es obligatoria");
        }

        Comuna comuna = comunaRepository.findById(idComuna)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "La comuna seleccionada no existe"));

        String region = regionRepository.findById(comuna.getIdRegion())
            .map(Region::getNombre)
            .orElse("");

        return convertir(comuna, region);
    }

    public Map<Long, ComunaResponse> mapaComunas() {
        return listarComunas().stream()
            .collect(Collectors.toMap(ComunaResponse::id, Function.identity()));
    }

    private ComunaResponse convertir(Comuna c, String region) {
        return new ComunaResponse(c.getId(), c.getIdRegion(), region, c.getNombre());
    }
}
