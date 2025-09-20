package br.tec.facilitaservicos.catalog.aplicacao.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço reativo para geração de números de conta únicos.
 * Migrado de backend-original-2 com melhorias para Java 25 LTS + WebFlux.
 * 
 * Algoritmo:
 * - Prefixo baseado em modalidade
 * - Timestamp codificado
 * - Contador sequencial
 * - Dígito verificador
 */
@Service
public class GeradorNumeroConta {
    private static final Logger logger = LoggerFactory.getLogger(GeradorNumeroConta.class);
    
    private static final SecureRandom random = new SecureRandom();
    private static final AtomicLong contador = new AtomicLong(1);
    
    // Prefixos por tipo de conta/modalidade
    private static final String PREFIXO_USUARIO = "USR";
    private static final String PREFIXO_APOSTA = "APT";
    private static final String PREFIXO_TRANSACAO = "TXN";
    private static final String PREFIXO_BOLAO = "BOL";

    /**
     * Gera número de conta para usuário.
     */
    public Mono<String> gerarNumeroContaUsuario() {
        return gerarNumeroConta(PREFIXO_USUARIO);
    }

    /**
     * Gera número de conta para aposta.
     */
    public Mono<String> gerarNumeroContaAposta() {
        return gerarNumeroConta(PREFIXO_APOSTA);
    }

    /**
     * Gera número de conta para transação.
     */
    public Mono<String> gerarNumeroContaTransacao() {
        return gerarNumeroConta(PREFIXO_TRANSACAO);
    }

    /**
     * Gera número de conta para bolão.
     */
    public Mono<String> gerarNumeroContaBolao() {
        return gerarNumeroConta(PREFIXO_BOLAO);
    }

    /**
     * Gera um número de conta genérico com prefixo customizado.
     */
    public Mono<String> gerarNumeroContaCustom(String prefixo) {
        return Mono.fromCallable(() -> {
            if (prefixo == null || prefixo.trim().isEmpty() || prefixo.length() > 5) {
                throw new IllegalArgumentException("Prefixo deve ter entre 1 e 5 caracteres");
            }
            return gerarNumeroContaInterno(prefixo.toUpperCase());
        });
    }

    private Mono<String> gerarNumeroConta(String prefixo) {
        return Mono.fromCallable(() -> gerarNumeroContaInterno(prefixo));
    }

    private String gerarNumeroContaInterno(String prefixo) {
        try {
            // 1. Timestamp compactado (últimos 8 dígitos do timestamp em milissegundos)
            long timestamp = System.currentTimeMillis();
            String timestampStr = String.valueOf(timestamp).substring(5); // Últimos 8 dígitos

            // 2. Contador sequencial (4 dígitos)
            long contadorAtual = contador.getAndIncrement();
            if (contadorAtual > 9999) {
                contador.set(1); // Reset após 9999
                contadorAtual = 1;
            }
            String contadorStr = String.format("%04d", contadorAtual);

            // 3. Componente aleatório (3 dígitos)
            int aleatorio = random.nextInt(1000);
            String aleatorioStr = String.format("%03d", aleatorio);

            // 4. Construir número base
            String numeroBase = prefixo + timestampStr + contadorStr + aleatorioStr;

            // 5. Calcular dígito verificador
            int digitoVerificador = calcularDigitoVerificador(numeroBase);

            // 6. Número final
            String numeroFinal = numeroBase + digitoVerificador;

            logger.debug("Número de conta gerado: {} (prefixo: {})", numeroFinal, prefixo);
            return numeroFinal;

        } catch (Exception e) {
            logger.error("Erro ao gerar número de conta com prefixo {}: {}", prefixo, e.getMessage());
            throw new RuntimeException("Falha na geração do número de conta", e);
        }
    }

    /**
     * Calcula dígito verificador usando algoritmo módulo 11.
     */
    private int calcularDigitoVerificador(String numero) {
        int soma = 0;
        int peso = 2;

        // Processar string da direita para esquerda
        for (int i = numero.length() - 1; i >= 0; i--) {
            char c = numero.charAt(i);
            
            // Converter letra para valor numérico
            int valor;
            if (Character.isDigit(c)) {
                valor = Character.getNumericValue(c);
            } else {
                // A=1, B=2, ..., Z=26
                valor = c - 'A' + 1;
            }

            soma += valor * peso;
            peso++;
            
            if (peso > 9) {
                peso = 2; // Reset peso
            }
        }

        int resto = soma % 11;
        int digito = 11 - resto;

        // Ajustar dígito conforme regra
        if (digito >= 10) {
            digito = digito - 10;
        }

        return digito;
    }

    /**
     * Gera lote de números de conta de forma eficiente.
     */
    public Mono<java.util.List<String>> gerarLoteNumerosContaUsuario(int quantidade) {
        return Mono.fromCallable(() -> {
            if (quantidade <= 0 || quantidade > 1000) {
                throw new IllegalArgumentException("Quantidade deve estar entre 1 e 1000");
            }

            var numeros = new java.util.ArrayList<String>(quantidade);
            for (int i = 0; i < quantidade; i++) {
                numeros.add(gerarNumeroContaInterno(PREFIXO_USUARIO));
            }

            logger.info("Lote de {} números de conta gerado", quantidade);
            return numeros;
        });
    }

    /**
     * Gera número de conta com informações detalhadas.
     */
    public Mono<NumeroContaDetalhado> gerarNumeroContaDetalhado(String prefixo) {
        return Mono.fromCallable(() -> {
            String numero = gerarNumeroContaInterno(prefixo);
            
            return NumeroContaDetalhado.builder()
                    .numero(numero)
                    .prefixo(prefixo)
                    .timestampGeracao(LocalDateTime.now())
                    .algoritmo("ModulO11-SecureRandom")
                    .versao("2.0")
                    .build();
        });
    }

    /**
     * DTO para número de conta com detalhes.
     */
    public static class NumeroContaDetalhado {
        private String numero;
        private String prefixo;
        private LocalDateTime timestampGeracao;
        private String algoritmo;
        private String versao;

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getNumero() { return numero; }
        public String getPrefixo() { return prefixo; }
        public LocalDateTime getTimestampGeracao() { return timestampGeracao; }
        public String getAlgoritmo() { return algoritmo; }
        public String getVersao() { return versao; }

        public String getTimestampGeracaoFormatado() {
            return timestampGeracao != null ? 
                    timestampGeracao.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
        }

        public static class Builder {
            private final NumeroContaDetalhado objeto = new NumeroContaDetalhado();

            public Builder numero(String numero) {
                objeto.numero = numero;
                return this;
            }

            public Builder prefixo(String prefixo) {
                objeto.prefixo = prefixo;
                return this;
            }

            public Builder timestampGeracao(LocalDateTime timestamp) {
                objeto.timestampGeracao = timestamp;
                return this;
            }

            public Builder algoritmo(String algoritmo) {
                objeto.algoritmo = algoritmo;
                return this;
            }

            public Builder versao(String versao) {
                objeto.versao = versao;
                return this;
            }

            public NumeroContaDetalhado build() {
                return objeto;
            }
        }
    }
}