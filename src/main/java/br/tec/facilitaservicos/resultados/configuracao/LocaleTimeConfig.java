package br.tec.facilitaservicos.resultados.configuracao;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.TimeZone;

@Configuration
public class LocaleTimeConfig {

    @PostConstruct
    public void init() {
        // Garantir padrões do Brasil/São Paulo na JVM
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }
}

