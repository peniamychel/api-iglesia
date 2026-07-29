package com.mcmm.controller;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de intentos para la verificación pública de certificados.
 *
 * El código corto impreso tiene un espacio de 32^4 combinaciones: sin límite,
 * un script podría recorrerlo entero y quedarse con los datos de todos los
 * certificados. Con este tope, barrer ese espacio pasa de minutos a meses.
 *
 * Es un contador en memoria, suficiente para un despliegue en una sola
 * instancia. Si algún día el backend corre replicado habría que moverlo a un
 * almacén compartido (Redis) o al servidor web de entrada.
 */
@Component
public class LimitadorVerificacion {

    /** Intentos permitidos por IP dentro de la ventana. */
    private static final int MAX_INTENTOS = 20;
    private static final long VENTANA_SEGUNDOS = 60;

    /** Tope de IPs vigiladas a la vez, para que el mapa no crezca sin control. */
    private static final int MAX_IPS_EN_MEMORIA = 10_000;

    private final Map<String, Deque<Long>> intentosPorIp = new ConcurrentHashMap<>();

    /**
     * Registra un intento y devuelve true si todavía está dentro del límite.
     */
    public boolean permitir(String ip) {
        if (ip == null || ip.isBlank()) {
            ip = "desconocida";
        }

        long ahora = Instant.now().getEpochSecond();
        long limiteInferior = ahora - VENTANA_SEGUNDOS;

        if (intentosPorIp.size() > MAX_IPS_EN_MEMORIA) {
            limpiarExpirados(limiteInferior);
        }

        Deque<Long> marcas = intentosPorIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (marcas) {
            // Se descartan los intentos que ya salieron de la ventana
            while (!marcas.isEmpty() && marcas.peekFirst() < limiteInferior) {
                marcas.pollFirst();
            }

            if (marcas.size() >= MAX_INTENTOS) {
                return false;
            }

            marcas.addLast(ahora);
            return true;
        }
    }

    /** Segundos que faltan para que la IP vuelva a tener cupo. */
    public long segundosDeEspera(String ip) {
        Deque<Long> marcas = intentosPorIp.get(ip);
        if (marcas == null) return VENTANA_SEGUNDOS;

        synchronized (marcas) {
            Long masAntiguo = marcas.peekFirst();
            if (masAntiguo == null) return 0;
            long espera = VENTANA_SEGUNDOS - (Instant.now().getEpochSecond() - masAntiguo);
            return Math.max(espera, 1);
        }
    }

    private void limpiarExpirados(long limiteInferior) {
        intentosPorIp.entrySet().removeIf(entrada -> {
            Deque<Long> marcas = entrada.getValue();
            synchronized (marcas) {
                return marcas.isEmpty() || marcas.peekLast() < limiteInferior;
            }
        });
    }
}
