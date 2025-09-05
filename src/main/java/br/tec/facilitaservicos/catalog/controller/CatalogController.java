package br.tec.facilitaservicos.catalog.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Controlador de Catálogo - Gerenciamento de tipos de itens e transações.
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    @GetMapping("/tipos-item")
    @PreAuthorize("hasAuthority('SCOPE_catalog.read')")
    public Mono<Map<String, Object>> getTiposItem() {
        return Mono.just(Map.of(
            "tipos", List.of(
                Map.of("id", 1, "nome", "Loteria Federal", "ativo", true),
                Map.of("id", 2, "nome", "Mega Sena", "ativo", true),
                Map.of("id", 3, "nome", "Quina", "ativo", true),
                Map.of("id", 4, "nome", "Lotomania", "ativo", true)
            ),
            "total", 4
        ));
    }

    @PostMapping("/tipos-item")
    @PreAuthorize("hasAuthority('SCOPE_catalog.write')")
    public Mono<Map<String, Object>> criarTipoItem(@RequestBody Map<String, Object> tipoItem) {
        return Mono.just(Map.of(
            "status", "tipo de item criado",
            "id", "tipo_" + System.currentTimeMillis(),
            "tipo", tipoItem
        ));
    }

    @GetMapping("/transacoes")
    @PreAuthorize("hasAuthority('SCOPE_catalog.read')")
    public Mono<Map<String, Object>> getTransacoes() {
        return Mono.just(Map.of(
            "transacoes", List.of(
                Map.of("id", 1, "tipo", "compra", "status", "concluida"),
                Map.of("id", 2, "tipo", "premio", "status", "pendente"),
                Map.of("id", 3, "tipo", "reembolso", "status", "processando")
            ),
            "total", 3
        ));
    }

    @PostMapping("/transacoes")
    @PreAuthorize("hasAuthority('SCOPE_catalog.write')")
    public Mono<Map<String, Object>> criarTransacao(@RequestBody Map<String, Object> transacao) {
        return Mono.just(Map.of(
            "status", "transação criada",
            "id", "trans_" + System.currentTimeMillis(),
            "transacao", transacao
        ));
    }

    @GetMapping("/transacoes/{id}")
    @PreAuthorize("hasAuthority('SCOPE_catalog.read')")
    public Mono<Map<String, Object>> getTransacao(@PathVariable String id) {
        return Mono.just(Map.of(
            "id", id,
            "tipo", "compra",
            "status", "concluida",
            "valor", 10.00,
            "data", "2024-01-01T10:00:00Z"
        ));
    }
}