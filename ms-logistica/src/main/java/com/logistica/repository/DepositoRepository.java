package com.logistica.repository;

import com.logistica.model.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    java.util.List<Deposito> findByNombre(String nombre);
}