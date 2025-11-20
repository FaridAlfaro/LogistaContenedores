package com.logistica.repository;

import com.logistica.model.Tramo;
import com.logistica.model.EstadoTramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByRutaId(Long rutaId);

    List<Tramo> findByEstado(EstadoTramo estado);

    List<Tramo> findByRutaIdAndEstado(Long rutaId, EstadoTramo estado);

    @Query("SELECT t FROM Tramo t WHERE t.ruta.nroSolicitudRef = :nroSolicitud")
    List<Tramo> findByNroSolicitud(@Param("nroSolicitud") String nroSolicitud);
}