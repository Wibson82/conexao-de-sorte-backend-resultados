package br.tec.facilitaservicos.catalog.apresentacao.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/tipos-item")
@Tag(name = "Tipos de Item", description = "Gestão de tipos de item")
public class TipoItemController {

    private final ConcurrentHashMap<String, Map<String,Object>> store = new ConcurrentHashMap<>();

    @PostMapping
    @Operation(summary = "Criar tipo de item")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Map<String,Object>>> criar(@RequestBody Map<String,Object> req) {
        String id = UUID.randomUUID().toString();
        req.put("id", id);
        req.putIfAbsent("ativo", true);
        store.put(id, req);
        return Mono.just(ResponseEntity.ok(req));
    }

    @PutMapping
    @Operation(summary = "Atualizar tipo de item")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Map<String,Object>>> atualizar(@RequestBody Map<String,Object> req) {
        String id = (String) req.get("id");
        if (id == null || !store.containsKey(id)) return Mono.just(ResponseEntity.notFound().build());
        store.put(id, req);
        return Mono.just(ResponseEntity.ok(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter tipo por id")
    public Mono<ResponseEntity<Map<String,Object>>> obter(@PathVariable String id) {
        Map<String,Object> it = store.get(id);
        return Mono.just(it != null ? ResponseEntity.ok(it) : ResponseEntity.notFound().build());
    }

    @GetMapping("/por-cliente/{clienteId}")
    @Operation(summary = "Listar por cliente")
    public Flux<Map<String,Object>> porCliente(@PathVariable String clienteId) {
        return Flux.fromIterable(store.values());
    }

    @PutMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> ativar(@PathVariable String id) {
        store.computeIfPresent(id, (k,v) -> { v.put("ativo", true); return v;});
        return Mono.just(ResponseEntity.ok().build());
    }

    @PutMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> desativar(@PathVariable String id) {
        store.computeIfPresent(id, (k,v) -> { v.put("ativo", false); return v;});
        return Mono.just(ResponseEntity.ok().build());
    }
}

