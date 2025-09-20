package br.tec.facilitaservicos.catalog.aplicacao.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * Serviço reativo para validação de números de conta.
 * Migrado de backend-original-2 com melhorias para Java 25 LTS + WebFlux.
 * 
 * Valida:
 * - Formato do número
 * - Dígito verificador
 * - Prefixos conhecidos
 * - Integridade estrutural
 */
@Service
public class ValidadorNumeroConta {
    private static final Logger logger = LoggerFactory.getLogger(ValidadorNumeroConta.class);

    // Padrões de validação - Thread-safe
    private static final Pattern PATTERN_NUMERO_CONTA = Pattern.compile("^[A-Z]{3}\\d{15}\\d$");
    private static final Pattern PATTERN_APENAS_NUMEROS = Pattern.compile("\\d+");

    // Prefixos válidos
    private static final java.util.Set<String> PREFIXOS_VALIDOS = java.util.Set.of(
            "USR", "APT", "TXN", "BOL", "ADM", "SYS", "RPT", "LOG"
    );

    /**
     * Valida um número de conta de forma completa.
     */
    public Mono<ResultadoValidacao> validarNumeroConta(String numeroConta) {
        return Mono.fromCallable(() -> {
            if (numeroConta == null || numeroConta.trim().isEmpty()) {
                return ResultadoValidacao.invalido("Número de conta não pode ser vazio");
            }

            String numero = numeroConta.trim().toUpperCase();

            // 1. Validar formato básico
            if (!PATTERN_NUMERO_CONTA.matcher(numero).matches()) {
                return ResultadoValidacao.invalido(
                    "Formato inválido. Esperado: XXX seguido de 16 dígitos");
            }

            // 2. Extrair componentes
            String prefixo = numero.substring(0, 3);
            String numeroBase = numero.substring(0, numero.length() - 1);
            String digitoStr = numero.substring(numero.length() - 1);

            // 3. Validar prefixo
            if (!PREFIXOS_VALIDOS.contains(prefixo)) {
                return ResultadoValidacao.invalido("Prefixo desconhecido: " + prefixo);
            }

            // 4. Validar dígito verificador
            try {
                int digitoEsperado = calcularDigitoVerificador(numeroBase);
                int digitoInformado = Integer.parseInt(digitoStr);

                if (digitoEsperado != digitoInformado) {
                    return ResultadoValidacao.invalido(
                        String.format("Dígito verificador inválido. Esperado: %d, Informado: %d", 
                                     digitoEsperado, digitoInformado));
                }
            } catch (NumberFormatException e) {
                return ResultadoValidacao.invalido("Dígito verificador deve ser numérico");
            }

            // 5. Validações estruturais adicionais
            var validacaoEstrutura = validarEstrutura(numero);
            if (!validacaoEstrutura.isValido()) {
                return validacaoEstrutura;
            }

            logger.debug("Número de conta válido: {}", numero);
            return ResultadoValidacao.valido("Número de conta válido", prefixo);
        });
    }

    /**
     * Valida apenas o formato sem verificar dígito.
     */
    public Mono<Boolean> validarFormato(String numeroConta) {
        return Mono.fromCallable(() -> {
            if (numeroConta == null || numeroConta.trim().isEmpty()) {
                return false;
            }

            String numero = numeroConta.trim().toUpperCase();
            return PATTERN_NUMERO_CONTA.matcher(numero).matches();
        });
    }

    /**
     * Valida apenas o prefixo.
     */
    public Mono<Boolean> validarPrefixo(String numeroConta) {
        return Mono.fromCallable(() -> {
            if (numeroConta == null || numeroConta.length() < 3) {
                return false;
            }

            String prefixo = numeroConta.substring(0, 3).toUpperCase();
            return PREFIXOS_VALIDOS.contains(prefixo);
        });
    }

    /**
     * Extrai o prefixo de um número de conta.
     */
    public Mono<String> extrairPrefixo(String numeroConta) {
        return Mono.fromCallable(() -> {
            if (numeroConta == null || numeroConta.length() < 3) {
                throw new IllegalArgumentException("Número de conta muito curto");
            }

            return numeroConta.substring(0, 3).toUpperCase();
        });
    }

    /**
     * Extrai informações detalhadas do número de conta.
     */
    public Mono<InformacoesConta> extrairInformacoes(String numeroConta) {
        return validarNumeroConta(numeroConta)
                .flatMap(validacao -> {
                    if (!validacao.isValido()) {
                        return Mono.error(new IllegalArgumentException(validacao.getMotivo()));
                    }

                    return Mono.fromCallable(() -> {
                        String numero = numeroConta.trim().toUpperCase();
                        String prefixo = numero.substring(0, 3);
                        String parteNumerica = numero.substring(3, numero.length() - 1);
                        String digito = numero.substring(numero.length() - 1);

                        // Tentar extrair timestamp (aproximado)
                        String timestampPart = parteNumerica.substring(0, 8);
                        
                        return InformacoesConta.builder()
                                .numeroCompleto(numero)
                                .prefixo(prefixo)
                                .tipoConta(determinarTipoConta(prefixo))
                                .parteNumerica(parteNumerica)
                                .digitoVerificador(digito)
                                .timestampAproximado(timestampPart)
                                .valido(true)
                                .build();
                    });
                });
    }

    /**
     * Valida uma lista de números de conta.
     */
    public Mono<java.util.Map<String, ResultadoValidacao>> validarLoteNumerosConta(
            java.util.List<String> numerosConta) {
        
        return reactor.core.publisher.Flux.fromIterable(numerosConta)
                .flatMap(numero -> 
                    validarNumeroConta(numero)
                            .map(resultado -> java.util.Map.entry(numero, resultado))
                            .onErrorReturn(java.util.Map.entry(numero, 
                                ResultadoValidacao.invalido("Erro na validação")))
                )
                .collectMap(
                    java.util.Map.Entry::getKey, 
                    java.util.Map.Entry::getValue
                );
    }

    private int calcularDigitoVerificador(String numero) {
        int soma = 0;
        int peso = 2;

        for (int i = numero.length() - 1; i >= 0; i--) {
            char c = numero.charAt(i);
            
            int valor;
            if (Character.isDigit(c)) {
                valor = Character.getNumericValue(c);
            } else {
                valor = c - 'A' + 1;
            }

            soma += valor * peso;
            peso++;
            
            if (peso > 9) {
                peso = 2;
            }
        }

        int resto = soma % 11;
        int digito = 11 - resto;

        if (digito >= 10) {
            digito = digito - 10;
        }

        return digito;
    }

    private ResultadoValidacao validarEstrutura(String numero) {
        // Validar se não há sequências suspeitas
        String parteNumerica = numero.substring(3);
        
        // Verificar se não são todos zeros
        if (PATTERN_APENAS_NUMEROS.matcher(parteNumerica).matches() && 
            parteNumerica.chars().allMatch(c -> c == '0')) {
            return ResultadoValidacao.invalido("Número de conta inválido (todos zeros)");
        }

        // Verificar se não há sequência crescente óbvia (123456...)
        boolean sequenciaSimples = true;
        for (int i = 1; i < parteNumerica.length() && sequenciaSimples; i++) {
            if (Character.isDigit(parteNumerica.charAt(i)) && 
                Character.isDigit(parteNumerica.charAt(i-1))) {
                
                int atual = Character.getNumericValue(parteNumerica.charAt(i));
                int anterior = Character.getNumericValue(parteNumerica.charAt(i-1));
                
                if (atual != (anterior + 1) % 10) {
                    sequenciaSimples = false;
                }
            }
        }

        if (sequenciaSimples) {
            return ResultadoValidacao.invalido("Número de conta suspeito (sequência simples)");
        }

        return ResultadoValidacao.valido("Estrutura válida", numero.substring(0, 3));
    }

    private String determinarTipoConta(String prefixo) {
        return switch (prefixo) {
            case "USR" -> "Conta de Usuário";
            case "APT" -> "Conta de Aposta";
            case "TXN" -> "Conta de Transação";
            case "BOL" -> "Conta de Bolão";
            case "ADM" -> "Conta Administrativa";
            case "SYS" -> "Conta de Sistema";
            case "RPT" -> "Conta de Relatório";
            case "LOG" -> "Conta de Log";
            default -> "Tipo Desconhecido";
        };
    }

    // DTOs
    public static class ResultadoValidacao {
        private boolean valido;
        private String motivo;
        private String prefixo;

        public static ResultadoValidacao valido(String motivo, String prefixo) {
            var resultado = new ResultadoValidacao();
            resultado.valido = true;
            resultado.motivo = motivo;
            resultado.prefixo = prefixo;
            return resultado;
        }

        public static ResultadoValidacao invalido(String motivo) {
            var resultado = new ResultadoValidacao();
            resultado.valido = false;
            resultado.motivo = motivo;
            return resultado;
        }

        // Getters
        public boolean isValido() { return valido; }
        public String getMotivo() { return motivo; }
        public String getPrefixo() { return prefixo; }
    }

    public static class InformacoesConta {
        private String numeroCompleto;
        private String prefixo;
        private String tipoConta;
        private String parteNumerica;
        private String digitoVerificador;
        private String timestampAproximado;
        private boolean valido;

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getNumeroCompleto() { return numeroCompleto; }
        public String getPrefixo() { return prefixo; }
        public String getTipoConta() { return tipoConta; }
        public String getParteNumerica() { return parteNumerica; }
        public String getDigitoVerificador() { return digitoVerificador; }
        public String getTimestampAproximado() { return timestampAproximado; }
        public boolean isValido() { return valido; }

        public static class Builder {
            private final InformacoesConta info = new InformacoesConta();

            public Builder numeroCompleto(String numeroCompleto) {
                info.numeroCompleto = numeroCompleto;
                return this;
            }

            public Builder prefixo(String prefixo) {
                info.prefixo = prefixo;
                return this;
            }

            public Builder tipoConta(String tipoConta) {
                info.tipoConta = tipoConta;
                return this;
            }

            public Builder parteNumerica(String parteNumerica) {
                info.parteNumerica = parteNumerica;
                return this;
            }

            public Builder digitoVerificador(String digitoVerificador) {
                info.digitoVerificador = digitoVerificador;
                return this;
            }

            public Builder timestampAproximado(String timestampAproximado) {
                info.timestampAproximado = timestampAproximado;
                return this;
            }

            public Builder valido(boolean valido) {
                info.valido = valido;
                return this;
            }

            public InformacoesConta build() {
                return info;
            }
        }
    }
}