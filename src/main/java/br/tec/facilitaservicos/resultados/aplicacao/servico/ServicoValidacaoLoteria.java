package br.tec.facilitaservicos.resultados.aplicacao.servico;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface reativa para serviços de validação de loterias.
 * Migrado de backend-original-2 para Spring WebFlux + Java 25 LTS.
 */
public interface ServicoValidacaoLoteria {

    /**
     * Valida um resultado completo de loteria de forma reativa.
     */
    Mono<Boolean> validarResultado(Long resultadoId);

    /**
     * Valida o formato de números sorteados.
     */
    Mono<String> validarNumeros(String numeros);

    /**
     * Valida uma lista de números de forma reativa.
     */
    Mono<String> validarListaNumeros(List<Integer> numeros);

    /**
     * Verifica se um número específico está dentro da faixa válida.
     */
    Mono<Boolean> isNumeroValido(int numero);

    /**
     * Obtém a modalidade atendida por este serviço.
     */
    String getModalidade();

    /**
     * Valida se a quantidade de números está correta.
     */
    Mono<Boolean> validarQuantidadeNumeros(List<Integer> numeros);

    /**
     * Verifica se há números duplicados.
     */
    Mono<Boolean> temNumerosDuplicados(List<Integer> numeros);
}

/**
 * Implementação base abstrata com lógica comum.
 */
@Component
abstract class ServicoValidacaoLoteriaBase implements ServicoValidacaoLoteria {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Mono<String> validarNumeros(String numeros) {
        return Mono.fromCallable(() -> {
            if (numeros == null || numeros.trim().isEmpty()) {
                throw new IllegalArgumentException("Números não podem ser vazios");
            }

            // Remove espaços e caracteres inválidos
            String numerosLimpos = numeros.trim().replaceAll("[^0-9,\\-\\s]", "");
            
            if (numerosLimpos.isEmpty()) {
                throw new IllegalArgumentException("Formato de números inválido");
            }

            return numerosLimpos;
        });
    }

    @Override
    public Mono<String> validarListaNumeros(List<Integer> numeros) {
        return Flux.fromIterable(numeros)
                .flatMap(this::isNumeroValido)
                .all(valido -> valido)
                .flatMap(todosValidos -> {
                    if (!todosValidos) {
                        return Mono.error(new IllegalArgumentException(
                            "Alguns números estão fora da faixa válida para " + getModalidade()));
                    }
                    
                    return validarQuantidadeNumeros(numeros)
                            .flatMap(quantidadeOk -> {
                                if (!quantidadeOk) {
                                    return Mono.error(new IllegalArgumentException(
                                        "Quantidade de números inválida para " + getModalidade()));
                                }
                                
                                return temNumerosDuplicados(numeros)
                                        .flatMap(temDuplicados -> {
                                            if (temDuplicados) {
                                                return Mono.error(new IllegalArgumentException(
                                                    "Números duplicados não são permitidos"));
                                            }
                                            
                                            return Mono.just(numeros.stream()
                                                    .sorted()
                                                    .map(String::valueOf)
                                                    .collect(Collectors.joining("-")));
                                        });
                            });
                });
    }

    @Override
    public Mono<Boolean> temNumerosDuplicados(List<Integer> numeros) {
        return Mono.fromCallable(() -> {
            Set<Integer> numerosUnicos = numeros.stream().collect(Collectors.toSet());
            return numerosUnicos.size() != numeros.size();
        });
    }

    protected Mono<List<Integer>> parseNumeros(String numerosStr) {
        return Mono.fromCallable(() -> {
            String[] partes = numerosStr.split("[,\\-\\s]+");
            return Arrays.stream(partes)
                    .filter(s -> !s.trim().isEmpty())
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }).onErrorMap(NumberFormatException.class, 
                ex -> new IllegalArgumentException("Formato de números inválido: " + numerosStr));
    }
}

/**
 * Implementação específica para Mega-Sena.
 */
@Component("megaSenaValidacao")
class ServicoValidacaoMegaSena extends ServicoValidacaoLoteriaBase {

    private static final int QUANTIDADE_NUMEROS_MINIMA = 6;
    private static final int QUANTIDADE_NUMEROS_MAXIMA = 15;
    private static final int NUMERO_MINIMO = 1;
    private static final int NUMERO_MAXIMO = 60;

    @Override
    public String getModalidade() {
        return "MEGA_SENA";
    }

    @Override
    public Mono<Boolean> validarResultado(Long resultadoId) {
        return Mono.fromCallable(() -> {
            if (resultadoId == null || resultadoId <= 0) {
                return false;
            }
            
            logger.debug("Validando resultado Mega-Sena ID: {}", resultadoId);
            return true;
        });
    }

    @Override
    public Mono<Boolean> isNumeroValido(int numero) {
        return Mono.just(numero >= NUMERO_MINIMO && numero <= NUMERO_MAXIMO);
    }

    @Override
    public Mono<Boolean> validarQuantidadeNumeros(List<Integer> numeros) {
        return Mono.just(numeros.size() >= QUANTIDADE_NUMEROS_MINIMA && 
                        numeros.size() <= QUANTIDADE_NUMEROS_MAXIMA);
    }
}

/**
 * Implementação específica para Quina.
 */
@Component("quinaValidacao")
class ServicoValidacaoQuina extends ServicoValidacaoLoteriaBase {

    private static final int QUANTIDADE_NUMEROS_MINIMA = 5;
    private static final int QUANTIDADE_NUMEROS_MAXIMA = 15;
    private static final int NUMERO_MINIMO = 1;
    private static final int NUMERO_MAXIMO = 80;

    @Override
    public String getModalidade() {
        return "QUINA";
    }

    @Override
    public Mono<Boolean> validarResultado(Long resultadoId) {
        return Mono.fromCallable(() -> {
            if (resultadoId == null || resultadoId <= 0) {
                return false;
            }
            
            logger.debug("Validando resultado Quina ID: {}", resultadoId);
            return true;
        });
    }

    @Override
    public Mono<Boolean> isNumeroValido(int numero) {
        return Mono.just(numero >= NUMERO_MINIMO && numero <= NUMERO_MAXIMO);
    }

    @Override
    public Mono<Boolean> validarQuantidadeNumeros(List<Integer> numeros) {
        return Mono.just(numeros.size() >= QUANTIDADE_NUMEROS_MINIMA && 
                        numeros.size() <= QUANTIDADE_NUMEROS_MAXIMA);
    }
}

/**
 * Implementação específica para Lotofácil.
 */
@Component("lotofacilValidacao")
class ServicoValidacaoLotofacil extends ServicoValidacaoLoteriaBase {

    private static final int QUANTIDADE_NUMEROS_MINIMA = 15;
    private static final int QUANTIDADE_NUMEROS_MAXIMA = 18;
    private static final int NUMERO_MINIMO = 1;
    private static final int NUMERO_MAXIMO = 25;

    @Override
    public String getModalidade() {
        return "LOTOFACIL";
    }

    @Override
    public Mono<Boolean> validarResultado(Long resultadoId) {
        return Mono.fromCallable(() -> {
            if (resultadoId == null || resultadoId <= 0) {
                return false;
            }
            
            logger.debug("Validando resultado Lotofácil ID: {}", resultadoId);
            return true;
        });
    }

    @Override
    public Mono<Boolean> isNumeroValido(int numero) {
        return Mono.just(numero >= NUMERO_MINIMO && numero <= NUMERO_MAXIMO);
    }

    @Override
    public Mono<Boolean> validarQuantidadeNumeros(List<Integer> numeros) {
        return Mono.just(numeros.size() >= QUANTIDADE_NUMEROS_MINIMA && 
                        numeros.size() <= QUANTIDADE_NUMEROS_MAXIMA);
    }
}

/**
 * Factory para obter validadores por modalidade.
 */
@Component
class ValidadorLoteriaFactory {

    private final Map<String, ServicoValidacaoLoteria> validadores;

    public ValidadorLoteriaFactory(List<ServicoValidacaoLoteria> validadores) {
        this.validadores = validadores.stream()
                .collect(Collectors.toMap(
                    ServicoValidacaoLoteria::getModalidade,
                    v -> v
                ));
    }

    public Mono<ServicoValidacaoLoteria> getValidador(String modalidade) {
        return Mono.fromCallable(() -> {
            ServicoValidacaoLoteria validador = validadores.get(modalidade.toUpperCase());
            if (validador == null) {
                throw new IllegalArgumentException("Modalidade não suportada: " + modalidade);
            }
            return validador;
        });
    }

    public Mono<Set<String>> getModalidadesSuportadas() {
        return Mono.just(validadores.keySet());
    }
}

/**
 * Serviço agregador para todas as validações de loteria.
 */
@Component
class ServicoValidacaoLoteriaManager {
    private static final Logger logger = LoggerFactory.getLogger(ServicoValidacaoLoteriaManager.class);

    private final ValidadorLoteriaFactory validadorFactory;

    public ServicoValidacaoLoteriaManager(ValidadorLoteriaFactory validadorFactory) {
        this.validadorFactory = validadorFactory;
    }

    /**
     * Valida números para qualquer modalidade.
     */
    public Mono<String> validarNumerosParaModalidade(String modalidade, List<Integer> numeros) {
        return validadorFactory.getValidador(modalidade)
                .flatMap(validador -> validador.validarListaNumeros(numeros))
                .doOnSuccess(resultado -> logger.debug(
                    "Números validados para {}: {}", modalidade, resultado))
                .doOnError(error -> logger.warn(
                    "Erro validando números para {}: {}", modalidade, error.getMessage()));
    }

    /**
     * Valida um resultado completo.
     */
    public Mono<Boolean> validarResultadoCompleto(String modalidade, Long resultadoId, List<Integer> numeros) {
        return validadorFactory.getValidador(modalidade)
                .flatMap(validador -> 
                    Mono.zip(
                        validador.validarResultado(resultadoId),
                        validador.validarListaNumeros(numeros).map(s -> true).onErrorReturn(false)
                    ).map(tuple -> tuple.getT1() && tuple.getT2())
                )
                .doOnSuccess(resultado -> logger.info(
                    "Resultado {} para {} é válido: {}", resultadoId, modalidade, resultado));
    }

    /**
     * Obtém informações de validação para uma modalidade.
     */
    public Mono<ModalidadeInfo> getInformacoesModalidade(String modalidade) {
        return validadorFactory.getValidador(modalidade)
                .map(validador -> {
                    // Informações específicas baseadas na modalidade
                    return switch (modalidade.toUpperCase()) {
                        case "MEGA_SENA" -> new ModalidadeInfo(modalidade, 6, 15, 1, 60);
                        case "QUINA" -> new ModalidadeInfo(modalidade, 5, 15, 1, 80);
                        case "LOTOFACIL" -> new ModalidadeInfo(modalidade, 15, 18, 1, 25);
                        default -> new ModalidadeInfo(modalidade, 1, 1, 1, 1);
                    };
                });
    }

    public record ModalidadeInfo(
            String modalidade,
            int quantidadeMinima,
            int quantidadeMaxima,
            int numeroMinimo,
            int numeroMaximo
    ) {}
}