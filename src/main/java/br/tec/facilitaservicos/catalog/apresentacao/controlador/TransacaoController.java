package br.tec.facilitaservicos.catalog.apresentacao.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping("/api/transacoes")
@Tag(name = "Transações", description = "Transações comerciais")
public class TransacaoController {

    private final ConcurrentLinkedQueue<Map<String,Object>> store = new ConcurrentLinkedQueue<>();

    @PostMapping
    @Operation(summary = "Criar transação")
    public Mono<ResponseEntity<Map<String,Object>>> criar(@RequestBody Map<String,Object> req) {
        String id = UUID.randomUUID().toString();
        Map<String,Object> tx = Map.of(
                "id", id,
                "valor", req.getOrDefault("valor", 0),
                "tipo", req.getOrDefault("tipo", "GENERICA"),
                "data", LocalDateTime.now().toString()
        );
        store.add(tx);
        return Mono.just(ResponseEntity.ok(tx));
    }

    @GetMapping
    @Operation(summary = "Listar transações")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Map<String,Object>> listar() {
        return Flux.fromIterable(store);
    }
}

