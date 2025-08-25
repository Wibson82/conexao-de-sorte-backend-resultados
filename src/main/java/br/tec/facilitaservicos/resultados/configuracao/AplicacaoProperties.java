package br.tec.facilitaservicos.resultados.configuracao;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * 🔐 CONFIGURAÇÃO DE PROPRIEDADES DA APLICAÇÃO - MICROSERVIÇO RESULTADOS
 * ============================================================================
 * 
 * Configuração das propriedades específicas da aplicação para acesso
 * aos secrets do Azure Key Vault de forma tipada e validada:
 * - JWT para validação de tokens
 * - Criptografia para dados sensíveis
 * - Cache e rate limiting
 * 
 * @author Sistema de Migração R2DBC
 * @version 1.0
 * @since 2024
 */
@Configuration
@ConfigurationProperties(prefix = "aplicacao")
public class AplicacaoProperties {

    private Microservico microservico = new Microservico();
    private Jwt jwt = new Jwt();
    private Criptografia criptografia = new Criptografia();
    private Cache cache = new Cache();
    private LimiteTaxa limiteTaxa = new LimiteTaxa();

    // Getters e Setters
    public Microservico getMicroservico() { return microservico; }
    public void setMicroservico(Microservico microservico) { this.microservico = microservico; }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Criptografia getCriptografia() { return criptografia; }
    public void setCriptografia(Criptografia criptografia) { this.criptografia = criptografia; }

    public Cache getCache() { return cache; }
    public void setCache(Cache cache) { this.cache = cache; }

    public LimiteTaxa getLimiteTaxa() { return limiteTaxa; }
    public void setLimiteTaxa(LimiteTaxa limiteTaxa) { this.limiteTaxa = limiteTaxa; }

    // Classes internas para organização
    public static class Microservico {
        private String nome;
        private String versao;
        private String descricao;

        // Getters e Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getVersao() { return versao; }
        public void setVersao(String versao) { this.versao = versao; }

        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
    }

    public static class Jwt {
        private String chaveAssinatura;
        private String chaveVerificacao;
        private String idChave;
        private String segredo;
        private String emissor;
        private String audiencia;
        private String algoritmo;
        private Validacao validacao = new Validacao();

        // Getters e Setters
        public String getChaveAssinatura() { return chaveAssinatura; }
        public void setChaveAssinatura(String chaveAssinatura) { this.chaveAssinatura = chaveAssinatura; }

        public String getChaveVerificacao() { return chaveVerificacao; }
        public void setChaveVerificacao(String chaveVerificacao) { this.chaveVerificacao = chaveVerificacao; }

        public String getIdChave() { return idChave; }
        public void setIdChave(String idChave) { this.idChave = idChave; }

        public String getSegredo() { return segredo; }
        public void setSegredo(String segredo) { this.segredo = segredo; }

        public String getEmissor() { return emissor; }
        public void setEmissor(String emissor) { this.emissor = emissor; }

        public String getAudiencia() { return audiencia; }
        public void setAudiencia(String audiencia) { this.audiencia = audiencia; }

        public String getAlgoritmo() { return algoritmo; }
        public void setAlgoritmo(String algoritmo) { this.algoritmo = algoritmo; }

        public Validacao getValidacao() { return validacao; }
        public void setValidacao(Validacao validacao) { this.validacao = validacao; }

        public static class Validacao {
            private boolean habilitado;
            private boolean modoEstrito;
            private boolean requerExpiracao;
            private String desvioRelogio;

            // Getters e Setters
            public boolean isHabilitado() { return habilitado; }
            public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

            public boolean isModoEstrito() { return modoEstrito; }
            public void setModoEstrito(boolean modoEstrito) { this.modoEstrito = modoEstrito; }

            public boolean isRequerExpiracao() { return requerExpiracao; }
            public void setRequerExpiracao(boolean requerExpiracao) { this.requerExpiracao = requerExpiracao; }

            public String getDesvioRelogio() { return desvioRelogio; }
            public void setDesvioRelogio(String desvioRelogio) { this.desvioRelogio = desvioRelogio; }
        }
    }

    public static class Criptografia {
        private String chaveMestra;
        private String algoritmo;
        private String rotacaoChaves;

        // Getters e Setters
        public String getChaveMestra() { return chaveMestra; }
        public void setChaveMestra(String chaveMestra) { this.chaveMestra = chaveMestra; }

        public String getAlgoritmo() { return algoritmo; }
        public void setAlgoritmo(String algoritmo) { this.algoritmo = algoritmo; }

        public String getRotacaoChaves() { return rotacaoChaves; }
        public void setRotacaoChaves(String rotacaoChaves) { this.rotacaoChaves = rotacaoChaves; }
    }

    public static class Cache {
        private boolean habilitado;
        private Ttl ttl = new Ttl();

        // Getters e Setters
        public boolean isHabilitado() { return habilitado; }
        public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

        public Ttl getTtl() { return ttl; }
        public void setTtl(Ttl ttl) { this.ttl = ttl; }

        public static class Ttl {
            private String resultados;
            private String ranking;
            private String estatisticas;

            // Getters e Setters
            public String getResultados() { return resultados; }
            public void setResultados(String resultados) { this.resultados = resultados; }

            public String getRanking() { return ranking; }
            public void setRanking(String ranking) { this.ranking = ranking; }

            public String getEstatisticas() { return estatisticas; }
            public void setEstatisticas(String estatisticas) { this.estatisticas = estatisticas; }
        }
    }

    public static class LimiteTaxa {
        private int requisicoesporMinuto;
        private int capacidadeRajada;

        // Getters e Setters
        public int getRequisicoesporMinuto() { return requisicoesporMinuto; }
        public void setRequisicoesporMinuto(int requisicoesporMinuto) { this.requisicoesporMinuto = requisicoesporMinuto; }

        public int getCapacidadeRajada() { return capacidadeRajada; }
        public void setCapacidadeRajada(int capacidadeRajada) { this.capacidadeRajada = capacidadeRajada; }
    }
}
