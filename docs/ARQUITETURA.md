# Arquitetura inicial

## Visão

Monólito modular com dois aplicativos no mesmo repositório: API REST Spring Boot e interface React/TypeScript. O MySQL será o banco relacional. Docker Compose poderá orquestrar os serviços locais quando Docker estiver disponível.

## Organização pretendida

```text
backend/src/main/java/br/com/igreja/voluntarios/
  config/       configurações gerais
  security/     autenticação e autorização
  volunteer/    voluntários e vínculos
  ministry/     ministérios e funções
  schedule/     agenda, escalas e presença
  shared/       componentes realmente compartilhados
frontend/src/
  app/          inicialização e rotas
  features/     funcionalidades por domínio
  components/   componentes reutilizáveis
  services/     comunicação com a API
docs/           arquitetura e requisitos
infra/          configuração local e implantação
```

Os pacotes e tabelas seguem os domínios de voluntários, ministérios, agenda e escalas. A associação voluntário-ministério é muitos-para-muitos. Dados e regras de autorização são verificados no backend.

Os módulos usam as migrações Flyway V1 a V3. O `ddl-auto: update` local foi mantido conforme solicitado. As APIs de voluntários, ministérios, eventos e escalas ficam temporariamente abertas para desenvolvimento local; CORS permite somente `http://localhost:5173`. Essa exceção precisa ser removida ao implementar a autenticação definitiva.

## Segurança desde o início

- Senhas armazenadas somente como hashes apropriados; nunca em texto puro.
- Segredos e credenciais por variáveis de ambiente, fora do Git.
- Autorização no servidor e escopo por ministério quando aplicável.
- Validação de entrada e registro de ações administrativas relevantes.
- Coletar apenas dados pessoais necessários, após confirmação da igreja.

## IDE

Recomendação: IntelliJ IDEA para Java/Spring e VS Code para React/TypeScript. É possível abrir o repositório inteiro em uma IDE e usar o terminal integrado. Para Spring Boot, qualquer editor compatível com Java funciona; os plugins podem melhorar navegação e depuração.

## Ferramentas observadas

- Java encontrado: 22; o projeto compila para Java 21 LTS, que também pode ser compilado pelo JDK 22 instalado.
- Node.js: 20.12.0 (abaixo do mínimo exigido pela versão atual do Vite)
- npm: 10.5.0
- Git: 2.54.0
- Maven, Docker e MySQL: não encontrados no PATH

As ferramentas necessárias serão verificadas novamente antes de depender delas. A equipe poderá optar por Maven Wrapper e Docker para reduzir diferenças entre máquinas.
