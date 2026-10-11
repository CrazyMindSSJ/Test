package cl.duoc.productos_service.service;

import cl.duoc.productos_service.model.Camion;
import cl.duoc.productos_service.model.ComunaCamion;
import cl.duoc.productos_service.model.FlotaCamion;
import cl.duoc.productos_service.model.MarcaInicializacion;
import cl.duoc.productos_service.model.TipoBasura;
import cl.duoc.productos_service.repository.CamionRepository;
import cl.duoc.productos_service.repository.MarcaInicializacionRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class CamionDataInitializer {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    ApplicationRunner inicializarCamiones(
            CamionRepository camionRepository,
            MarcaInicializacionRepository marcaRepository) {
        return args -> {
            String marca = "camiones-demo-inicializados";
            if (marcaRepository.existsById(marca)) {
                return;
            }
            if (camionRepository.count() == 0) {
                camionRepository.saveAll(List.of(
                        crear("DEMO-01", 3000, TipoBasura.ESCOMBRO, ComunaCamion.LA_FLORIDA, FlotaCamion.LA_FLORIDA),
                        crear("DEMO-02", 2500, TipoBasura.BASURA, ComunaCamion.LA_FLORIDA, FlotaCamion.LA_FLORIDA),
                        crear("DEMO-03", 4000, TipoBasura.RECICLABLE, ComunaCamion.LA_FLORIDA, FlotaCamion.LA_FLORIDA),
                        crear("DEMO-04", 3500, TipoBasura.ESCOMBRO, ComunaCamion.LA_FLORIDA, FlotaCamion.LA_FLORIDA),
                        crear("DEMO-05", 3000, TipoBasura.BASURA, ComunaCamion.PUENTE_ALTO, FlotaCamion.PUENTE_ALTO),
                        crear("DEMO-06", 4500, TipoBasura.ESCOMBRO, ComunaCamion.PUENTE_ALTO, FlotaCamion.PUENTE_ALTO),
                        crear("DEMO-07", 2500, TipoBasura.RECICLABLE, ComunaCamion.PUENTE_ALTO, FlotaCamion.PUENTE_ALTO),
                        crear("DEMO-08", 3500, TipoBasura.BASURA, ComunaCamion.LA_PINTANA, FlotaCamion.LA_PINTANA),
                        crear("DEMO-09", 4000, TipoBasura.ESCOMBRO, ComunaCamion.LA_PINTANA, FlotaCamion.LA_PINTANA),
                        crear("DEMO-10", 3000, TipoBasura.RECICLABLE, ComunaCamion.LA_PINTANA, FlotaCamion.LA_PINTANA)
                ));
            }
            marcaRepository.save(new MarcaInicializacion(marca));
        };
    }

    private Camion crear(
            String patente,
            int capacidadKg,
            TipoBasura tipoBasura,
            ComunaCamion comuna,
            FlotaCamion flota) {
        Camion camion = new Camion();
        camion.setPatente(patente);
        camion.setCapacidadKg(capacidadKg);
        camion.setTipoBasura(tipoBasura);
        camion.setComunaAsignada(comuna);
        camion.setFlota(flota);
        return camion;
    }
}
