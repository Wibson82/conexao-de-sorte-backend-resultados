package br.tec.facilitaservicos.resultados.dominio.entidade;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * Entidade R2DBC reativa para Resultado
 * Representa um resultado genérico no sistema com suporte a operações reativas.
 * 
 * Inclui o horário, data e valores sorteados com validação defensiva.
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Table("resultados")
public class ResultadoR2dbc extends ReactiveAuditableEntity {

    @Id
    private Long id;

    @NotBlank(message = "Horário é obrigatório")
    @Column("horario")
    private String horario;

    @NotBlank(message = "Primeiro número é obrigatório")
    @Column("primeiro")
    private String primeiro;

    @NotBlank(message = "Segundo número é obrigatório")
    @Column("segundo")
    private String segundo;

    @NotBlank(message = "Terceiro número é obrigatório")
    @Column("terceiro")
    private String terceiro;

    @NotBlank(message = "Quarto número é obrigatório")
    @Column("quarto")
    private String quarto;

    @NotBlank(message = "Quinto número é obrigatório")
    @Column("quinto")
    private String quinto;

    @NotBlank(message = "Sexto número é obrigatório")
    @Column("sexto")
    private String sexto;

    @NotBlank(message = "Sétimo número é obrigatório")
    @Column("setimo")
    private String setimo;

    @Column("soma")
    private String soma;

    @NotNull(message = "Data do resultado é obrigatória")
    @PastOrPresent(message = "Data do resultado não pode ser futura")
    @Column("data_resultado")
    private LocalDate dataResultado;

    // Construtores
    public ResultadoR2dbc() {
        super();
    }

    public ResultadoR2dbc(String horario, String primeiro, String segundo, String terceiro,
                         String quarto, String quinto, String sexto, String setimo, LocalDate dataResultado) {
        this();
        this.horario = validarCampoObrigatorio(horario, "Horário");
        this.primeiro = validarCampoObrigatorio(primeiro, "Primeiro número");
        this.segundo = validarCampoObrigatorio(segundo, "Segundo número");
        this.terceiro = validarCampoObrigatorio(terceiro, "Terceiro número");
        this.quarto = validarCampoObrigatorio(quarto, "Quarto número");
        this.quinto = validarCampoObrigatorio(quinto, "Quinto número");
        this.sexto = validarCampoObrigatorio(sexto, "Sexto número");
        this.setimo = validarCampoObrigatorio(setimo, "Sétimo número");
        this.dataResultado = Objects.requireNonNull(dataResultado, "Data do resultado é obrigatória");
        calcularSoma();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = validarCampoObrigatorio(horario, "Horário");
    }

    public String getPrimeiro() {
        return primeiro;
    }

    public void setPrimeiro(String primeiro) {
        this.primeiro = validarCampoObrigatorio(primeiro, "Primeiro número");
    }

    public String getSegundo() {
        return segundo;
    }

    public void setSegundo(String segundo) {
        this.segundo = validarCampoObrigatorio(segundo, "Segundo número");
    }

    public String getTerceiro() {
        return terceiro;
    }

    public void setTerceiro(String terceiro) {
        this.terceiro = validarCampoObrigatorio(terceiro, "Terceiro número");
    }

    public String getQuarto() {
        return quarto;
    }

    public void setQuarto(String quarto) {
        this.quarto = validarCampoObrigatorio(quarto, "Quarto número");
    }

    public String getQuinto() {
        return quinto;
    }

    public void setQuinto(String quinto) {
        this.quinto = validarCampoObrigatorio(quinto, "Quinto número");
    }

    public String getSexto() {
        return sexto;
    }

    public void setSexto(String sexto) {
        this.sexto = validarCampoObrigatorio(sexto, "Sexto número");
    }

    public String getSetimo() {
        return setimo;
    }

    public void setSetimo(String setimo) {
        this.setimo = validarCampoObrigatorio(setimo, "Sétimo número");
    }

    public String getSoma() {
        return soma;
    }

    public void setSoma(String soma) {
        this.soma = soma;
    }

    public LocalDate getDataResultado() {
        return dataResultado;
    }

    public void setDataResultado(LocalDate dataResultado) {
        this.dataResultado = Objects.requireNonNull(dataResultado, "Data do resultado é obrigatória");
    }

    // Métodos de negócio

    /**
     * Obtém todos os números como lista
     */
    public List<String> obterNumeros() {
        return Arrays.asList(primeiro, segundo, terceiro, quarto, quinto, sexto, setimo)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Calcula automaticamente a soma dos números
     */
    public void calcularSoma() {
        try {
            List<String> numeros = obterNumeros();
            if (numeros.size() == 7 && numeros.stream().allMatch(this::isNumeric)) {
                int somaTotal = numeros.stream()
                    .mapToInt(Integer::parseInt)
                    .sum();
                this.soma = String.valueOf(somaTotal);
            }
        } catch (Exception e) {
            // Se não conseguir calcular, mantém soma atual ou null
            this.soma = null;
        }
    }

    /**
     * Verifica se o resultado está completo
     */
    public boolean isCompleto() {
        return horario != null && !horario.trim().isEmpty() &&
               primeiro != null && !primeiro.trim().isEmpty() &&
               segundo != null && !segundo.trim().isEmpty() &&
               terceiro != null && !terceiro.trim().isEmpty() &&
               quarto != null && !quarto.trim().isEmpty() &&
               quinto != null && !quinto.trim().isEmpty() &&
               sexto != null && !sexto.trim().isEmpty() &&
               setimo != null && !setimo.trim().isEmpty() &&
               dataResultado != null;
    }

    /**
     * Verifica se todos os números são válidos
     */
    public boolean numerosValidos() {
        return obterNumeros().stream().allMatch(this::isNumeric);
    }

    /**
     * Obtém representação textual dos números
     */
    public String obterNumerosTexto() {
        return String.join("-", obterNumeros());
    }

    /**
     * Verifica se é resultado de hoje
     */
    public boolean isResultadoDeHoje() {
        return dataResultado != null && dataResultado.equals(LocalDate.now());
    }

    /**
     * Verifica se é resultado recente (últimos 7 dias)
     */
    public boolean isResultadoRecente() {
        return dataResultado != null && 
               dataResultado.isAfter(LocalDate.now().minusDays(7));
    }

    // Métodos auxiliares

    /**
     * Valida campo obrigatório
     */
    private String validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório e não pode ser vazio");
        }
        return valor.trim();
    }

    /**
     * Verifica se string é numérica
     */
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Builder Pattern para construção segura

    public static class Builder {
        private String horario;
        private String primeiro;
        private String segundo;
        private String terceiro;
        private String quarto;
        private String quinto;
        private String sexto;
        private String setimo;
        private LocalDate dataResultado;

        public Builder horario(String horario) {
            this.horario = horario;
            return this;
        }

        public Builder primeiro(String primeiro) {
            this.primeiro = primeiro;
            return this;
        }

        public Builder segundo(String segundo) {
            this.segundo = segundo;
            return this;
        }

        public Builder terceiro(String terceiro) {
            this.terceiro = terceiro;
            return this;
        }

        public Builder quarto(String quarto) {
            this.quarto = quarto;
            return this;
        }

        public Builder quinto(String quinto) {
            this.quinto = quinto;
            return this;
        }

        public Builder sexto(String sexto) {
            this.sexto = sexto;
            return this;
        }

        public Builder setimo(String setimo) {
            this.setimo = setimo;
            return this;
        }

        public Builder soma(String soma) {
            // Soma será calculada automaticamente no build()
            return this;
        }

        public Builder numeros(String primeiro, String segundo, String terceiro, String quarto,
                              String quinto, String sexto, String setimo) {
            this.primeiro = primeiro;
            this.segundo = segundo;
            this.terceiro = terceiro;
            this.quarto = quarto;
            this.quinto = quinto;
            this.sexto = sexto;
            this.setimo = setimo;
            return this;
        }

        public Builder dataResultado(LocalDate dataResultado) {
            this.dataResultado = dataResultado;
            return this;
        }

        public ResultadoR2dbc build() {
            return new ResultadoR2dbc(horario, primeiro, segundo, terceiro,
                                     quarto, quinto, sexto, setimo, dataResultado);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Equals, HashCode e ToString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultadoR2dbc that = (ResultadoR2dbc) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(horario, that.horario) &&
               Objects.equals(dataResultado, that.dataResultado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, horario, dataResultado);
    }

    @Override
    public String toString() {
        return "ResultadoR2dbc{" +
                "id=" + id +
                ", horario='" + horario + '\'' +
                ", numeros='" + obterNumerosTexto() + '\'' +
                ", soma='" + soma + '\'' +
                ", dataResultado=" + dataResultado +
                ", dataCriacao=" + getCriadoEm() +
                '}';
    }
}