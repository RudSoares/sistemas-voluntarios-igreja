# Sistema de voluntários e escalas

MVP web para cadastro de voluntários, ministérios, agenda e escalas da igreja.

## Estado atual

Os módulos de voluntários, ministérios, cultos/eventos e escalas estão implementados na API e na interface. A aba inicial mostra uma visão geral conectada aos dados reais. As regras e perguntas ainda pendentes estão em [`docs/PENDENCIAS.md`](docs/PENDENCIAS.md), a arquitetura em [`docs/ARQUITETURA.md`](docs/ARQUITETURA.md) e o guia didático dos fluxos em [`docs/GUIA_ESTUDO_MODULOS.md`](docs/GUIA_ESTUDO_MODULOS.md).

## Ferramentas

- Java 21 ou superior (o workspace tem JDK 22; o projeto compila para Java 21).
- Node.js 22.12 ou superior na linha 22, conforme `.nvmrc` e `frontend/package.json`.
- MySQL 8.4 ou compatível, no banco `voluntarios_db`.
- Docker Desktop opcional, caso queira subir um MySQL local pelo Compose.
- Git. O Maven Wrapper já está incluído; Maven não precisa ser instalado globalmente.

O Node global encontrado neste ambiente é 20.12, abaixo do mínimo do Vite atual. Há um Node 22 portátil em `work/.tools` para este workspace; essa pasta é local e ignorada pelo Git. Para outro computador, instale Node 22 antes de executar o frontend.

## Banco de dados

O backend preserva os padrões locais em `backend/src/main/resources/application.yaml`:

- URL: `jdbc:mysql://localhost:3306/voluntarios_db`
- Usuário: `root`
- Senha: `123456`

Também é possível substituir os valores com `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. Flyway aplica as migrações versionadas e `ddl-auto: update` permanece habilitado localmente, conforme solicitado.

Para iniciar um MySQL local com Docker, a partir da raiz do repositório:

```powershell
if (-not (Test-Path infra/docker/.env)) { Copy-Item infra/docker/.env.example infra/docker/.env }
docker compose --env-file infra/docker/.env -f infra/docker/compose.yaml up -d
```

Essa configuração é apenas para desenvolvimento e usa a porta local 3306. Se já houver um MySQL usando essa porta, use esse banco local ou altere `MYSQL_PORT` e `DB_URL` juntos.

## Executar o backend

Na pasta `backend/`, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Com os valores padrão, o backend conecta em `localhost:3306`. O Maven Wrapper baixa Maven 3.10.0 no primeiro uso.

## Executar o frontend

Na pasta `frontend/`, com Node 22.12 ou superior:

```powershell
npm install
npm run dev
```

Neste workspace, se o Node 22 ainda não estiver no `PATH`, pode usar a cópia portátil já baixada (execute a partir da raiz):

```powershell
$node = (Resolve-Path .\work\.tools\node-v22.23.5-win-x64\node.exe).Path
$npmCli = (Resolve-Path .\work\.tools\node-v22.23.5-win-x64\node_modules\npm\bin\npm-cli.js).Path
Push-Location frontend
try { & $node $npmCli run dev } finally { Pop-Location }
```

O Vite abre a aplicação em `http://localhost:5173`. Ela chama a API em `http://localhost:8080`; configure `VITE_API_BASE_URL` se a API estiver em outro endereço.

## API de voluntários

| Método | Rota | Uso |
|---|---|---|
| `GET` | `/api/v1/volunteers` | Listar voluntários |
| `GET` | `/api/v1/volunteers/{id}` | Buscar um voluntário |
| `POST` | `/api/v1/volunteers` | Cadastrar |
| `PUT` | `/api/v1/volunteers/{id}` | Atualizar dados |
| `PATCH` | `/api/v1/volunteers/{id}/status` | Ativar ou inativar |

Exemplo de corpo para `POST` e `PUT`:

```json
{
  "nomeCompleto": "Nome do voluntário",
  "telefone": "+5511999999999",
  "email": "pessoa@example.com",
  "disponibilidadeDias": ["DOMINGO_MANHA", "QUARTA_NOITE"],
  "observacoes": ""
}
```

Para alterar status, envie `{"ativo": false}` ou `{"ativo": true}` ao endpoint `PATCH`.

## API de ministérios

| Método | Rota | Uso |
|---|---|---|
| `GET` | `/api/v1/ministries` | Listar ministérios |
| `GET` | `/api/v1/ministries/{id}` | Buscar ministério |
| `POST` | `/api/v1/ministries` | Cadastrar ministério |
| `PUT` | `/api/v1/ministries/{id}` | Atualizar ministério |
| `PATCH` | `/api/v1/ministries/{id}/status` | Ativar ou inativar |
| `GET`, `POST` | `/api/v1/ministries/{id}/roles` | Listar ou criar funções |
| `PUT`, `DELETE` | `/api/v1/ministries/{id}/roles/{roleId}` | Editar ou remover função |
| `GET`, `POST` | `/api/v1/ministries/{id}/memberships` | Listar vínculos ou vincular voluntário |
| `DELETE` | `/api/v1/ministries/{id}/memberships/{membershipId}` | Desvincular voluntário |

Para vincular, envie `{"volunteerId": 1, "ministryRoleId": 2}`. O servidor responde `409 Conflict` quando a mesma pessoa já está vinculada à mesma função do mesmo ministério. Uma função que ainda possui vínculos também não pode ser removida.

## API de cultos, eventos e escalas

| Método | Rota | Uso |
|---|---|---|
| `GET`, `POST` | `/api/v1/events` | Listar ou criar eventos |
| `GET`, `PUT`, `DELETE` | `/api/v1/events/{id}` | Consultar, editar ou excluir (se não houver escalas) |
| `GET` | `/api/v1/events/upcoming` | Próximos eventos agendados |
| `PATCH` | `/api/v1/events/{id}/status` | Alterar para `AGENDADO`, `CONCLUIDO` ou `CANCELADO` |
| `GET` | `/api/v1/schedules` | Listar escalas; aceita `?eventId={id}` |
| `POST` | `/api/v1/schedules/events/{eventId}/assignments` | Escalar voluntário vinculado |
| `PATCH` | `/api/v1/schedules/assignments/{id}/confirmation` | Alterar confirmação |
| `DELETE` | `/api/v1/schedules/assignments/{id}` | Remover pessoa da escala |

O corpo da escala é `{"ministryId":1,"ministryRoleId":2,"volunteerId":3}`. A API impede escalar a mesma pessoa duas vezes no evento e exige que ela esteja ativa e vinculada à função. Divergência de disponibilidade retorna um alerta sem bloquear a escala. Veja o detalhe no [guia de estudo](docs/GUIA_ESTUDO_MODULOS.md).

## Acesso temporário para desenvolvimento

As rotas `/api/v1/volunteers/**`, `/api/v1/ministries/**`, `/api/v1/events/**` e `/api/v1/schedules/**` estão temporariamente abertas para permitir testar a interface. CORS aceita apenas `http://localhost:5173`. As demais rotas continuam protegidas. Antes de publicar o sistema, o acesso aberto e a configuração de CORS de desenvolvimento devem ser substituídos pela autenticação e autorização definitivas.

## Verificação manual

1. Inicie o MySQL e a API.
2. No Insomnia, envie um `POST /api/v1/volunteers` com o exemplo acima e um `POST /api/v1/ministries` com `{"nome":"Louvor","descricao":"Música e adoração"}`.
3. Crie uma função com `POST /api/v1/ministries/{id}/roles` e cadastre um vínculo em `/api/v1/ministries/{id}/memberships`.
4. Crie um evento futuro em `POST /api/v1/events` e escale em `POST /api/v1/schedules/events/{eventId}/assignments`.
5. Abra o frontend para usar a programação, as escalas e a visão geral.

Não rodei testes automatizados nem build nesta etapa. As rotas e o fluxo manual acima servem como verificação inicial depois que os serviços estiverem em execução.
