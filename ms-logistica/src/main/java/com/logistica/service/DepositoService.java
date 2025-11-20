package com.logistica.service;

import com.logistica.model.Deposito;
import com.logistica.repository.DepositoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositoService {

    private final DepositoRepository depositoRepository;

    public Deposito crearDeposito(Deposito deposito) {
        log.info("Creando depósito: {}", deposito.getNombre());
        return depositoRepository.save(deposito);
    }

    public List<Deposito> listarDepositos() {
        return depositoRepository.findAll();
    }

    public Deposito obtenerDeposito(Long id) {
        return depositoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado: " + id));
    }

    public Deposito actualizarDeposito(Long id, Deposito depositoActualizado) {
        Deposito deposito = obtenerDeposito(id);
        deposito.setNombre(depositoActualizado.getNombre());
        deposito.setDireccion(depositoActualizado.getDireccion());
        deposito.setLatitud(depositoActualizado.getLatitud());
        deposito.setLongitud(depositoActualizado.getLongitud());
        deposito.setCostoEstadiaDiario(depositoActualizado.getCostoEstadiaDiario());
        return depositoRepository.save(deposito);
    }
}