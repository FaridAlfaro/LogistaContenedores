package com.logistica.repository;

import com.logistica.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    java.util.List<Tarifa> findByFechaVigencia(java.time.LocalDate fechaVigencia);
}