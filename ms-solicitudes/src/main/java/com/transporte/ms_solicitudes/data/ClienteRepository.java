package com.transporte.ms_solicitudes.data;

import com.transporte.ms_solicitudes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Método para buscar usuarios por su email (username en keycloak)
    Optional<Cliente> findByEmail(String email);
}
