package br.tec.facilitaservicos.resultados.dominio.entidade;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import jakarta.validation.constraints.NotNull;

/**
 * Entidade base reativa para auditoria automática.
 * Fornece campos de criação e modificação para entidades R2DBC.
 * 
 * Compatível com Spring Boot 3.5.5 e Java 25 LTS.
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
public abstract class ReactiveAuditableEntity {

    @CreatedDate
    @NotNull
    @Column("created_at")
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @NotNull
    @Column("updated_at")
    private LocalDateTime atualizadoEm;

    /**
     * Construtor padrão.
     * Inicializa os timestamps de auditoria.
     */
    protected ReactiveAuditableEntity() {
        final LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    /**
     * Obtém a data/hora de criação da entidade.
     * 
     * @return data/hora de criação
     */
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    /**
     * Define a data/hora de criação da entidade.
     * 
     * @param criadoEm data/hora de criação
     */
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação é obrigatória");
    }

    /**
     * Obtém a data/hora da última modificação da entidade.
     * 
     * @return data/hora da última modificação
     */
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    /**
     * Define a data/hora da última modificação da entidade.
     * 
     * @param atualizadoEm data/hora da última modificação
     */
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "Data de atualização é obrigatória");
    }

    /**
     * Atualiza o timestamp de modificação para o momento atual.
     * Útil para operações de update manuais.
     */
    public void marcarComoAtualizado() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Verifica se a entidade foi criada recentemente (últimas 24 horas).
     * 
     * @return true se foi criada nas últimas 24 horas
     */
    public boolean isCriadaRecentemente() {
        if (criadoEm == null) {
            return false;
        }
        return criadoEm.isAfter(LocalDateTime.now().minusDays(1));
    }

    /**
     * Verifica se a entidade foi modificada recentemente (últimas 24 horas).
     * 
     * @return true se foi modificada nas últimas 24 horas
     */
    public boolean isModificadaRecentemente() {
        if (atualizadoEm == null) {
            return false;
        }
        return atualizadoEm.isAfter(LocalDateTime.now().minusDays(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReactiveAuditableEntity that)) return false;
        return Objects.equals(criadoEm, that.criadoEm) &&
               Objects.equals(atualizadoEm, that.atualizadoEm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criadoEm, atualizadoEm);
    }

    @Override
    public String toString() {
        return "ReactiveAuditableEntity{" +
                "criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                '}';
    }
}