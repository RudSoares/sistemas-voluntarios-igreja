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

## Autenticação e administração

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Público; recebe e-mail/senha e retorna JWT |
| `GET`, `POST` | `/api/v1/churches` | SUPER_ADMIN lista e cadastra igrejas |
| `PUT`, `PATCH` | `/api/v1/churches/{id}` e `/status` | SUPER_ADMIN edita ou ativa/inativa |
| `POST` | `/api/v1/churches/{id}/admins` | SUPER_ADMIN cria CHURCH_ADMIN |
| `GET`, `POST` | `/api/v1/branches` | CHURCH_ADMIN lista/cria filiais da sua igreja |
| `PUT`, `PATCH` | `/api/v1/branches/{id}` e `/status` | CHURCH_ADMIN edita/ativa filial da própria igreja |
| `GET`, `PATCH` | `/api/v1/volunteers/me` e `/me/availability` | VOLUNTEER consulta perfil e edita disponibilidade |
| `GET` | `/api/v1/schedules/mine` | VOLUNTEER consulta suas escalas |

Use o header `Authorization: Bearer <token>` nas rotas protegidas. O frontend o inclui automaticamente após login. As senhas do seed local são `admin123` para `admin@sistema.com` e `poiema123` para `lider@poiema.com`. Configure senhas próprias com `SEED_SUPER_ADMIN_PASSWORD` e `SEED_CHURCH_ADMIN_PASSWORD`; defina também `JWT_SECRET` forte e diferente antes de publicar. O seed só cria as contas quando `app_users` está vazio; `SEED_ENABLED=false` desativa o processo.

CORS aceita somente `http://localhost:5173`. Só login e health check são públicos. Os demais endpoints exigem JWT e as regras de acesso por perfil também são aplicadas no backend. Dados antigos são migrados para a igreja inicial Igreja Poiema; a configuração MySQL local permanece intacta.

## Verificação manual

1. Inicie o MySQL e a API.
2. A primeira execução aplica as migrações e cria os usuários iniciais se a tabela estiver vazia. Faça login via `POST /api/v1/auth/login` com `lider@poiema.com` / `poiema123`.
3. Configure `Authorization: Bearer <token>` no Insomnia; então crie um voluntário, ministério, função e vínculo.
4. Crie um evento futuro em `POST /api/v1/events` e escale em `POST /api/v1/schedules/events/{eventId}/assignments`.
5. Faça login como o voluntário, se criou uma senha inicial, e consulte `GET /api/v1/schedules/mine`.
6. Abra o frontend e entre com uma das contas iniciais para usar as telas correspondentes ao perfil.

O guia de estudo documenta a matriz de papéis e um roteiro manual para verificar o escopo de igreja e as permissões de cada perfil.
