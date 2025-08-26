package br.tec.facilitaservicos.resultados.apresentacao.controlador;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;

/**
 * Manipulador global de exceções reativo
 * Trata erros de forma consistente em toda a aplicação
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@RestControllerAdvice
@Order(-2)
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata erros de validação
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(WebExchangeBindException ex) {
        logger.warn("Erro de validação: {}", ex.getMessage());
        
        Map<String, String> erros = ex.getBindingResult().getFieldErrors()
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Valor inválido"
            ));

        ErrorResponse response = ErrorResponse.validacao("Dados inválidos", erros);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Trata violações de restrição de parâmetros
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConstraintViolation(ConstraintViolationException ex) {
        logger.warn("Violação de restrição: {}", ex.getMessage());

        Map<String, String> erros = ex.getConstraintViolations().stream()
            .collect(java.util.stream.Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));

        ErrorResponse response = ErrorResponse.validacao("Parâmetros inválidos", erros);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Trata erros de entrada de dados
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInputErrors(ServerWebInputException ex) {
        logger.warn("Erro de entrada: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.simples(
            HttpStatus.BAD_REQUEST.value(),
            "Parâmetro inválido",
            "Verifique os parâmetros enviados"
        );
        
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Trata ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatus(ResponseStatusException ex) {
        logger.warn("Erro de status: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.simples(
            ex.getStatusCode().value(),
            ex.getReason() != null ? ex.getReason() : "Erro na requisição",
            ex.getMessage()
        );
        
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(response));
    }

    /**
     * Trata argumentos ilegais
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.simples(
            HttpStatus.BAD_REQUEST.value(),
            "Parâmetro inválido",
            ex.getMessage()
        );
        
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Trata erros gerais
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneral(Exception ex) {
        logger.error("Erro interno: ", ex);
        
        ErrorResponse response = ErrorResponse.simples(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro interno do servidor",
            "Ocorreu um erro inesperado. Tente novamente."
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    /**
     * Resposta padronizada de erro
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp,
        Map<String, String> detalhes
    ) {
        
        public static ErrorResponse simples(int status, String erro, String mensagem) {
            return new ErrorResponse(status, erro, mensagem, LocalDateTime.now(), null);
        }
        
        public static ErrorResponse validacao(String mensagem, Map<String, String> detalhes) {
            return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                mensagem,
                LocalDateTime.now(),
                detalhes
            );
        }
    }
}