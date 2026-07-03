package com.macronnect.api_macronnect.cliente;

import com.macronnect.api_macronnect.cliente.dto.ClienteRequest;
import com.macronnect.api_macronnect.cliente.dto.ClienteResponse;
import com.macronnect.api_macronnect.common.dto.PagedResponse;
import com.macronnect.api_macronnect.common.exception.DuplicateResourceException;
import com.macronnect.api_macronnect.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClienteResponse> listar(Pageable pageable) {
        Page<Cliente> page = clienteRepository.findAll(pageable);
        List<ClienteResponse> content = page.getContent().stream()
                .map(ClienteResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        return ClienteResponse.fromEntity(buscarOFallar(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cliente", "email", request.getEmail());
        }
        Cliente cliente = new Cliente(
                request.getNombre(),
                request.getEmail(),
                request.getTelefono(),
                request.getDireccion());
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarOFallar(id);

        if (clienteRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Cliente", "email", request.getEmail());
        }

        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        // sin save(): dirty checking dentro de la transacción
        return ClienteResponse.fromEntity(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        buscarOFallar(id).setActivo(false); // soft delete
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }
}