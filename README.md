# 📊 Microserviço de Resultados

![Java](https://img.shields.io/badge/Java-24-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5+-green?style=flat-square&logo=springboot)
![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue?style=flat-square)
![R2DBC](https://img.shields.io/badge/R2DBC-Reactive-purple?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)

Microserviço **100% reativo** para consulta de resultados de loteria, construído com Spring WebFlux e R2DBC.

## 🎯 Características Principais

- **🚀 100% Reativo**: Spring WebFlux + R2DBC para máxima performance
- **🔐 Segurança JWT**: Validação via JWKS do microserviço de autenticação
- **📈 Observabilidade**: Actuator + Prometheus + Grafana
- **⚡ Cache Inteligente**: Redis distribuído + Caffeine local
- **🐳 Containerizado**: Docker + Docker Compose
- **📊 API Documentada**: OpenAPI 3 + Swagger UI
- **🧪 Testado**: Testes unitários e de integração
- **🔄 Anti-extração**: Mantém funcionalidade no monólito

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │───▶│ Microserviço     │───▶│    MySQL        │
│   (React/Vue)   │    │   Resultados     │    │   (R2DBC)       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │                         │
                              ▼                         ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │     Redis        │    │   Flyway        │
                       │    (Cache)       │    │  (Migrations)   │
                       └──────────────────┘    └─────────────────┘
```

## 🛠️ Stack Tecnológica

- **Java 24** - Linguagem principal
- **Spring Boot 3.5+** - Framework base
- **Spring WebFlux** - Programação reativa
- **Spring Security** - Segurança JWT
- **R2DBC MySQL** - Acesso reativo ao banco
- **Redis** - Cache distribuído
- **Caffeine** - Cache local
- **Flyway** - Migrations de banco
- **Docker** - Containerização
- **Testcontainers** - Testes de integração

## 🚀 Início Rápido

### Pré-requisitos

- Java 24+
- Docker e Docker Compose
- Maven 3.9+

### 1. Clone e Execute

```bash
# Clone o projeto
cd /Volumes/NVME/Projetos/conexao-de-sorte-backend-resultados

# Execute com Docker Compose
docker-compose up -d

# Ou execute localmente
mvn spring-boot:run
```

### 2. Acesse os Serviços

- **API**: http://localhost:8082/api/resultados
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **Actuator**: http://localhost:8082/actuator
- **Grafana**: http://localhost:3001 (admin:admin123!)
- **Prometheus**: http://localhost:9091

## 📋 Endpoints da API

### Consultas Públicas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/resultados` | Buscar resultados paginados |
| `GET` | `/api/resultados/{id}` | Buscar resultado específico |
| `GET` | `/api/resultados/ranking` | Ranking de números mais sorteados |
| `GET` | `/api/resultados/estatisticas` | Estatísticas agregadas |
| `GET` | `/api/resultados/hoje` | Resultados de hoje |
| `GET` | `/api/resultados/ultimo/{horario}` | Último resultado por horário |
| `GET` | `/api/resultados/horarios` | Horários disponíveis por data |

### Exemplos de Uso

```bash
# Buscar resultados paginados
curl "http://localhost:8082/api/resultados?pagina=0&tamanho=20&ordenarPor=dataResultado,desc"

# Buscar ranking dos últimos 30 dias
curl "http://localhost:8082/api/resultados/ranking?temporada=30&limite=50"

# Buscar estatísticas gerais
curl "http://localhost:8082/api/resultados/estatisticas"

# Buscar resultados de hoje
curl "http://localhost:8082/api/resultados/hoje"
```

## 🔧 Configuração

### Variáveis de Ambiente

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=conexao_sorte_resultados
DB_USERNAME=resultados_user
DB_PASSWORD=resultados_pass123!

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_pass123!

# JWT
JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json
JWT_ISSUER=https://auth.conexaodesorte.com

# Features
FEATURE_RESULTADOS_MS=true
FEATURE_ADVANCED_RANKING=true
FEATURE_STATS_CACHE=true
```

### Profiles Disponíveis

- **dev** - Desenvolvimento local
- **test** - Execução de testes
- **prod** - Produção otimizada

## 🧪 Testes

```bash
# Testes unitários
mvn test

# Testes de integração
mvn verify

# Testes com Testcontainers
mvn test -Dtest=**/*IntegrationTest
```

## 📊 Monitoramento

### Métricas Customizadas

- `resultados.consultas.total` - Total de consultas
- `ranking.consultas.total` - Total de consultas ao ranking
- `estatisticas.consultas.total` - Total de consultas às estatísticas

### Health Checks

- **Database**: Conectividade R2DBC
- **Redis**: Disponibilidade do cache
- **Application**: Status geral

## 🐳 Docker

### Build Local

```bash
# Build da imagem
docker build -t resultados-microservice .

# Executar container
docker run -p 8082:8082 -e FEATURE_RESULTADOS_MS=true resultados-microservice
```

### Docker Compose Completo

```bash
# Subir ambiente completo
docker-compose up -d

# Ver logs
docker-compose logs -f resultados-service

# Parar ambiente
docker-compose down
```

## 🔐 Segurança

- **JWT Validation**: Via JWKS endpoint
- **CORS**: Configurado para frontend
- **Rate Limiting**: Por endpoint
- **Security Headers**: CSP, HSTS, etc.
- **Public Endpoints**: Consultas não requerem autenticação

## 📈 Performance

- **Paginação Otimizada**: Para grandes volumes
- **Cache Inteligente**: Redis + Caffeine
- **Consultas Otimizadas**: Índices específicos
- **Connection Pooling**: R2DBC configurado

## 🔄 Integração com Monólito

- **Feature Flag**: `FEATURE_RESULTADOS_MS=false` por padrão
- **Anti-extração**: Funcionalidade preservada no monólito
- **Migração Gradual**: Ativação por feature flag
- **Rollback Seguro**: Desativação instantânea

## 🤝 Contribuição

1. Clone o repositório
2. Crie uma branch: `git checkout -b feature/nova-funcionalidade`
3. Faça commit: `git commit -m 'Adiciona nova funcionalidade'`
4. Push: `git push origin feature/nova-funcionalidade`
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📞 Suporte

- **Issues:** [GitHub Issues](https://github.com/Wibson82/conexao-de-sorte-backend-resultados/issues)
- **Wiki:** [Documentação Completa](https://github.com/Wibson82/conexao-de-sorte-backend-resultados/wiki)

---

**📊 Conexão de Sorte Results Microservice**  
*Sistema de Migração R2DBC v1.0 - Powered by Spring Boot 3.5+ & Java 24*  
*Built with ❤️ by the Conexão de Sorte Team*