# Guia de estudo: Voluntários, Ministérios, Eventos e Escalas

Este guia acompanha os módulos do MVP e mostra como uma ação na tela passa pelo frontend, pela API e chega ao MySQL. A meta é ajudar você a navegar pelo código e entender o motivo de cada camada.

## O caminho de uma requisição

```text
Pessoa usuária
  → componente React
  → serviço TypeScript (fetch)
  → Controller REST
  → Service (regras do módulo)
  → Repository (Spring Data JPA)
  → Entity JPA
  → MySQL
```

O caminho de volta usa a mesma sequência ao contrário. O Controller recebe entidades de transporte (DTOs) do frontend; o Service transforma essas entradas em entidades; o Repository grava ou consulta; o Service monta DTOs de resposta, e o Controller os devolve como JSON.

## O que cada camada faz

| Camada | Responsabilidade | O que deve evitar |
|---|---|---|
| React | Exibir dados, controlar formulários e apresentar erros/carregamento | Decidir regras que também precisam valer para Insomnia ou outros clientes |
| API TypeScript | Conhecer as rotas HTTP e converter JSON em tipos do frontend | Guardar regra de negócio ou SQL |
| Controller | Definir rotas, validar o formato da entrada e códigos HTTP | Acessar o Repository diretamente |
| DTO | Definir o formato de entrada e saída da API | Ser usado como tabela do banco |
| Service | Aplicar validações e regras entre entidades | Renderizar HTML ou conhecer componentes React |
| Repository | Executar consultas e persistência por meio do Spring Data JPA | Decidir regras de negócio |
| Entity | Representar os dados persistidos e seus relacionamentos | Ser enviada diretamente como resposta HTTP |
| Flyway / MySQL | Versionar e guardar a estrutura e os dados relacionais | Ser alterado manualmente sem registrar a mudança no projeto |

## Módulo de Voluntários

### Arquivos principais

- `frontend/src/features/volunteers/volunteerApi.ts`: tipos TypeScript e operações HTTP de voluntários.
- `frontend/src/services/apiClient.ts`: endereço base, `fetch`, leitura de respostas e mensagens de erro compartilhadas.
- `frontend/src/App.tsx`: tela de voluntários, formulário, pesquisa e ações de editar/ativar/inativar.
- `backend/src/main/java/br/com/igreja/voluntarios/volunteer/VolunteerController.java`: rotas em `/api/v1/volunteers`.
- `backend/src/main/java/br/com/igreja/voluntarios/volunteer/VolunteerRequest.java`: campos recebidos e validação do nome/e-mail.
- `backend/src/main/java/br/com/igreja/voluntarios/volunteer/VolunteerResponse.java`: formato enviado ao frontend.
- `backend/src/main/java/br/com/igreja/voluntarios/volunteer/VolunteerService.java`: criar, buscar, editar e mudar status; também converte Entity para Response.
- `backend/src/main/java/br/com/igreja/voluntarios/volunteer/VolunteerRepository.java`: consultas de voluntários e carregamento de disponibilidade.
- `backend/src/main/java/br/com/igreja/voluntarios/volunteer/Volunteer.java`: Entity e tabela `volunteers`.
- `backend/src/main/resources/db/migration/V1__create_volunteers.sql`: tabelas de voluntários e códigos de disponibilidade.

### Exemplo: cadastrar voluntário

1. O formulário de `App.tsx` monta um `VolunteerPayload` e chama `volunteerApi.create`.
2. `apiClient.ts` envia `POST /api/v1/volunteers` com JSON.
3. `VolunteerController.create` valida o corpo com `@Valid` e encaminha o DTO ao Service.
4. `VolunteerService.create` cria uma Entity `Volunteer` e chama `VolunteerRepository.save`.
5. O JPA converte a Entity e sua coleção de disponibilidade em instruções SQL. O MySQL grava a pessoa e os períodos.
6. O Service cria um `VolunteerResponse`; o Controller responde `201 Created` com os dados e o cabeçalho `Location`.
7. A tela recarrega a lista e mostra o voluntário persistido.

`ativo` começa como `true` na Entity e no banco. O formulário não altera esse campo; ativar/inativar é uma operação separada em `PATCH /api/v1/volunteers/{id}/status`.

## Módulo de Ministérios

### Modelo de dados

- `Ministry` guarda nome único, descrição opcional e status ativo.
- `MinistryRole` guarda o nome de uma função e a referência ao ministério dono. O MVP não restringe nomes de função repetidos dentro do mesmo ministério; essa decisão está em `docs/PENDENCIAS.md`.
- `VolunteerMinistry` é a entidade de vínculo. Ela guarda chaves para voluntário, ministério e função. Isso permite que uma pessoa participe de mais de um ministério e tenha mais de uma função dentro do mesmo ministério.

A combinação `(volunteer_id, ministry_id, ministry_role_id)` tem uma restrição `UNIQUE` no banco. O Service também verifica a existência antes de inserir, para devolver um conflito legível. Assim a mesma associação não aparece duplicada, inclusive quando a regra é chamada fora do frontend.

O Service confirma que a função selecionada pertence ao ministério da rota. A chave estrangeira composta da migração também impede gravar uma função de outro ministério junto daquele `ministry_id`.

### Arquivos principais

- `frontend/src/features/ministries/ministryApi.ts`: tipos e rotas de ministérios, funções e vínculos.
- `frontend/src/features/ministries/MinistriesPage.tsx`: cadastro/edição de ministério, funções e vínculos na interface.
- `frontend/src/App.tsx`: seleção da aba Ministério e composição da página.
- `backend/src/main/java/br/com/igreja/voluntarios/ministry/MinistryController.java`: endpoints REST aninhados sob `/api/v1/ministries`.
- `MinistryRequest.java`, `MinistryRoleRequest.java`, `VolunteerMinistryRequest.java`: entradas validadas para cada ação.
- `MinistryResponse.java`, `MinistryRoleResponse.java`, `VolunteerMinistryResponse.java`: respostas para as telas.
- `MinistryService.java`: verifica nome duplicado, existência do ministério/voluntário/função, ministério dono da função e duplicidade do vínculo.
- `MinistryRepository.java`, `MinistryRoleRepository.java`, `VolunteerMinistryRepository.java`: consultas e persistência de cada Entity.
- `Ministry.java`, `MinistryRole.java`, `VolunteerMinistry.java`: entidades JPA e relacionamentos.
- `backend/src/main/resources/db/migration/V2__create_ministries_and_memberships.sql`: tabelas, chaves estrangeiras e restrição contra duplicidade.
- `backend/src/main/java/br/com/igreja/voluntarios/security/SecurityConfiguration.java`: libera temporariamente os endpoints e configura CORS local.

### Exemplo: vincular voluntário a uma função

1. `MinistriesPage.tsx` busca ministérios, voluntários e, ao selecionar um ministério, carrega suas funções e vínculos.
2. A pessoa escolhe um voluntário existente e uma função daquele ministério. `ministryApi.linkVolunteer` envia `POST /api/v1/ministries/{ministryId}/memberships`.
3. `MinistryController.linkVolunteer` valida os dois IDs e chama `MinistryService.linkVolunteer`.
4. O Service carrega o ministério, o voluntário e a função. A busca da função usa também o ID do ministério, então uma função de outro ministério não serve.
5. O Repository confere a combinação existente e grava `VolunteerMinistry`.
6. O banco aplica chaves estrangeiras e a restrição única. A resposta identifica o vínculo, a pessoa e a função.
7. A tela recarrega os vínculos e mostra a pessoa dentro do ministério.

Se a mesma combinação for enviada de novo, a API responde `409 Conflict`. Um voluntário pode, porém, receber outra função do mesmo ministério, porque isso gera outra combinação. Desvincular remove somente aquele registro de `VolunteerMinistry`; o histórico de vínculos não faz parte deste MVP.

Uma função que tenha voluntários vinculados não pode ser removida. Primeiro desvincule as pessoas; então o `DELETE /api/v1/ministries/{ministryId}/roles/{roleId}` pode removê-la. Isso evita apagar associações sem uma ação explícita.

## Módulo de Cultos e Eventos

### Modelo e arquivos

- `ServiceEvent` guarda título, data, horário, turno/dia, observações e status (`AGENDADO`, `CONCLUIDO`, `CANCELADO`). A data e o horário usam `LocalDate` e `LocalTime`, sem fuso horário porque representam o horário local definido pela igreja.
- `ServiceEventRequest` valida que título, data, horário e turno/dia sejam informados. O status começa como `AGENDADO`.
- `ServiceEventController` atende `/api/v1/events`; `ScheduleService` concentra a persistência e as regras compartilhadas com escalas.
- `ServiceEventRepository` consulta a programação ordenada e os próximos eventos agendados.
- `frontend/src/features/schedule/scheduleApi.ts` define tipos e chamadas HTTP; `EventsPage.tsx` implementa formulário e lista responsiva.
- `V3__create_events_and_schedules.sql` cria a tabela `service_events` e define status `AGENDADO` como padrão.

Um evento agendado pode ser editado ou cancelado. O sistema impede sua exclusão enquanto existirem escalas para ele, para preservar as referências; a interface explica esse conflito. Depois de cancelar, as escalas permanecem registradas para consulta. O estado concluído também está disponível pela API.

### Rotas de eventos

| Método | Rota | Ação |
|---|---|---|
| `GET` | `/api/v1/events` | Listar eventos |
| `GET` | `/api/v1/events/upcoming` | Listar eventos agendados a partir de hoje |
| `GET` | `/api/v1/events/{id}` | Buscar evento |
| `POST` | `/api/v1/events` | Criar evento |
| `PUT` | `/api/v1/events/{id}` | Editar evento agendado |
| `PATCH` | `/api/v1/events/{id}/status` | Atualizar status |
| `DELETE` | `/api/v1/events/{id}` | Excluir se não houver escalas associadas |

Exemplo de entrada: `{"titulo":"Culto de Celebração","dataEvento":"2026-12-06","horario":"10:00:00","turnoOuDiaSemana":"DOMINGO_MANHA","observacoes":""}`.

## Módulo de Escalas

### Modelo e validações

`ScheduleAssignment` relaciona um evento, ministério, função e voluntário, com status de confirmação inicialmente `PENDENTE`. Cada `@ManyToOne` escreve em sua própria coluna: `service_event_id`, `ministry_id`, `ministry_role_id` ou `volunteer_id`. A associação da função não reutiliza a coluna `ministry_id`; o Service confirma que a função pertence ao ministério selecionado antes de salvar. Isso evita o conflito JPA de misturar `insertable=true` e `insertable=false` no mesmo mapeamento.

As regras implementadas em `ScheduleService.createAssignment` são:

- O evento precisa estar `AGENDADO`.
- O voluntário precisa existir e estar ativo.
- O ministério precisa existir e estar ativo.
- A função precisa pertencer ao ministério escolhido.
- O voluntário precisa ter um vínculo cadastrado com aquela função e ministério.
- O mesmo voluntário só pode aparecer uma vez no mesmo evento, mesmo em outra função. O Service responde `409 Conflict`, e a constraint `uk_schedule_event_volunteer` reforça a regra no MySQL.
- Uma disponibilidade que não inclui o turno/dia do evento gera `alertaDisponibilidade` e mensagem na resposta; não bloqueia a escala. Essa decisão segue a solicitação do MVP de permitir que a liderança confirme mesmo com o aviso.

O status de confirmação pode ser `PENDENTE`, `CONFIRMADO` ou `RECUSADO`. A interface permite atualizar o status ou remover a escala. O módulo não envia convites nem registra check-in; esses fluxos continuam fora do escopo implementado.

### Arquivos e rotas

- `ServiceEvent.java`, `ScheduleAssignment.java`: entidades JPA. Cada chave estrangeira da escala possui associação independente.
- `ServiceEventRepository.java`, `ScheduleAssignmentRepository.java`: consultas de programação e escala.
- `ServiceEventRequest.java`, `ScheduleAssignmentRequest.java`, `EventStatusRequest.java`, `ConfirmationStatusRequest.java`: DTOs de entrada.
- `ServiceEventResponse.java`, `ScheduleAssignmentResponse.java`: DTOs de saída; a resposta da escala inclui o alerta de disponibilidade.
- `ScheduleService.java`: valida vínculo, status ativo/agendado, duplicidade e disponibilidade.
- `ServiceEventController.java`, `ScheduleController.java`: endpoints REST.
- `frontend/src/features/schedule/EventsPage.tsx`: CRUD de programação.
- `frontend/src/features/schedule/SchedulesPage.tsx`: escolha de evento, seleção somente entre vínculos existentes, estado de confirmação e alerta.
- `frontend/src/features/schedule/scheduleApi.ts`: contratos e chamadas HTTP.
- `V3__create_events_and_schedules.sql`: tabelas, chaves estrangeiras e unicidade por evento/voluntário.

| Método | Rota | Ação |
|---|---|---|
| `GET` | `/api/v1/schedules` | Listar todas as atribuições |
| `GET` | `/api/v1/schedules?eventId={id}` | Listar a equipe de um evento |
| `POST` | `/api/v1/schedules/events/{eventId}/assignments` | Escalar um voluntário |
| `PATCH` | `/api/v1/schedules/assignments/{id}/confirmation` | Atualizar confirmação |
| `DELETE` | `/api/v1/schedules/assignments/{id}` | Remover uma atribuição |

Exemplo de escala: `POST /api/v1/schedules/events/1/assignments` com `{"ministryId":1,"ministryRoleId":2,"volunteerId":3}`. Em conflito, a resposta HTTP `409` contém uma mensagem clara, por exemplo “Este voluntário já está escalado neste evento, mesmo que em outra função.”

## Visão geral (Dashboard)

`frontend/src/features/dashboard/DashboardPage.tsx` consulta as APIs reais de voluntários, ministérios, próximos eventos e escalas. Mostra contagens de voluntários ativos, ministérios ativos, eventos agendados futuros e atribuições de escala. A seção de agenda lista até quatro próximos eventos. Os botões levam às telas correspondentes. As contagens são calculadas no navegador a partir das listas retornadas pela API; para o tamanho do MVP isso evita endpoints duplicados, e uma evolução futura pode criar um endpoint agregado paginado.

## Caminho ponta a ponta: criar uma escala

1. `SchedulesPage.tsx` carrega eventos, ministérios ativos e os vínculos de voluntários cadastrados em cada ministério.
2. A pessoa escolhe um evento agendado e um vínculo existente; assim a interface oferece voluntário, ministério e função já relacionados.
3. `scheduleApi.create` envia `POST /api/v1/schedules/events/{eventId}/assignments`.
4. `ScheduleController` valida o formato e delega a operação a `ScheduleService`.
5. `ScheduleService` confirma que o evento está agendado, voluntário e ministério estão ativos, a função é daquele ministério e o vínculo existe. Também verifica se a pessoa já está escalada naquele evento.
6. Se a disponibilidade não combina, o serviço ainda grava e inclui a mensagem de alerta na resposta. Caso haja duplicidade ou outra regra não atendida, responde com conflito e a tela mostra a mensagem.
7. `ScheduleAssignmentRepository` grava a entidade. O MySQL reforça os relacionamentos com chaves estrangeiras e impede duplicidade por evento/voluntário.
8. A tela atualiza a lista e mostra a confirmação pendente ou o alerta de disponibilidade.

## Dashboard e execução local

O Dashboard abre na aba inicial e busca dados ao entrar ou quando se seleciona “Atualizar dados”. Para exercitar o fluxo completo, crie voluntários e ministérios, adicione funções e vínculos, cadastre um evento futuro e então monte sua escala. As migrações `V1` a `V5` criam o esquema inicial, atribuem os dados antigos à igreja inicial e ajustam a unicidade de nomes de ministérios para cada igreja. `application.yaml` mantém banco `voluntarios_db`, senha local `123456` e `ddl-auto: update`.

## Identidade, autenticação e autorização

### Modelo

- `Church` representa a igreja matriz e contém nome, CNPJ opcional, cidade, estado e status ativo.
- `Branch` representa uma filial/congregação e sempre aponta para uma `Church`.
- `AppUser` (tabela `app_users`) guarda nome, e-mail único, hash de senha, perfil, status e relações opcionais com igreja, filial e voluntário.
- Os papéis são `SUPER_ADMIN`, `CHURCH_ADMIN` e `VOLUNTEER`.
- Voluntários, ministérios, eventos e escalas têm igreja obrigatória e filial opcional. O vínculo voluntário-ministério herda o escopo pela igreja do ministério e do voluntário.

### Fluxo JWT e BCrypt

1. O formulário `LoginPage.tsx` chama `POST /api/v1/auth/login` com e-mail e senha.
2. `AuthService` procura a conta e usa `PasswordEncoder.matches` para comparar a senha digitada ao hash BCrypt; a senha original nunca é salva.
3. Em sucesso, `JwtTokenService` assina um JWT HS256 com prazo de expiração. A resposta inclui token, nome, e-mail, papel e IDs de igreja/filial.
4. `AuthContext.tsx` mantém perfil e token no `localStorage`. `apiClient.ts` acrescenta `Authorization: Bearer <token>` em cada chamada protegida.
5. `JwtAuthenticationFilter` valida assinatura e expiração, recarrega o usuário ativo e estabelece o principal. O papel da autorização é carregado do banco, então desativação/alteração do usuário passa a valer mesmo antes de o token expirar.
6. `SecurityConfiguration` torna a API stateless, libera somente o login e health check e aplica autenticação nas demais rotas. `@PreAuthorize` limita cada área ao papel permitido.

O JWT é assinado com `JWT_SECRET` e dura por padrão oito horas (`JWT_EXPIRATION_SECONDS`). O segredo padrão existe apenas para desenvolvimento local; configure um segredo forte por variável de ambiente fora do desenvolvimento. Logout remove o token e o perfil do navegador; como o backend é stateless, o token deixa de ser aceito quando expira ou quando a conta/igreja é desativada.

### Matriz resumida de acesso

| Perfil | Acesso |
|---|---|
| `SUPER_ADMIN` | Cadastrar/editar/ativar igrejas e criar contas `CHURCH_ADMIN`; não acessa os dados operacionais das igrejas |
| `CHURCH_ADMIN` | Gerenciar filiais e os módulos operacionais da própria igreja; pode criar acesso de voluntário ao cadastrar a pessoa |
| `VOLUNTEER` | Consultar apenas suas escalas, confirmar/recusar e alterar apenas a própria disponibilidade |

O escopo de igreja é aplicado também nos Services/Repositories: IDs recebidos em rotas são buscados junto ao `church_id` do principal autenticado. A interface adaptar o menu por perfil melhora a experiência, mas não substitui essa validação no backend. O SUPER_ADMIN recebe 403 ao tentar abrir as APIs operacionais.

### Criação inicial segura

`DataSeeder` implementa `CommandLineRunner` e só cria usuários quando `app_users` está vazio (e `SEED_ENABLED` não está desligado). A migração V4 cria a igreja Poiema e a filial Sede como base para migrar registros locais antigos; o seeder reutiliza essa organização e cria:

- SUPER_ADMIN: `admin@sistema.com`
- CHURCH_ADMIN: `lider@poiema.com`

Para desenvolvimento, as senhas padrão são `admin123` e `poiema123`; ambas são imediatamente transformadas em BCrypt. Elas podem ser substituídas por `SEED_SUPER_ADMIN_PASSWORD` e `SEED_CHURCH_ADMIN_PASSWORD`. Troque as senhas e o segredo JWT antes de qualquer implantação; para impedir o seed, use `SEED_ENABLED=false`.

### Arquivos novos/alterados principais

- `identity/AppUser.java`, `UserRole.java`, `AppUserRepository.java`: conta, papéis e consulta por e-mail.
- `identity/AuthController.java`, `AuthService.java`, `LoginRequest.java`, `LoginResponse.java`: endpoint e contrato de login.
- `security/JwtTokenService.java`, `JwtAuthenticationFilter.java`, `TenantPrincipal.java`, `SecurityUser.java`: assinatura, validação do token e contexto de autorização/igreja.
- `security/SecurityConfiguration.java`: BCrypt, CORS, sessões stateless e regras por rota.
- `identity/DataSeeder.java`: criação idempotente das contas iniciais.
- `organization/Church.java`, `Branch.java`, seus Repository/Controller/Service e DTOs: matriz, filiais e administração.
- `VolunteerService.java`, `MinistryService.java`, `ScheduleService.java`: filtragem por igreja e permissões no servidor.
- `V4__churches_users_and_tenant_scope.sql`: tabelas de igreja/filial/conta e backfill do banco legado; `V5` muda nomes de ministérios para unicidade por igreja.
- `frontend/src/features/auth/AuthContext.tsx`, `LoginPage.tsx`: sessão e tela de entrada.
- `features/organization/PlatformAdminPage.tsx`, `BranchesPage.tsx`: painel de plataforma e gestão de filiais.
- `features/volunteer-portal/VolunteerPortal.tsx`: escalas próprias e disponibilidade.
- `App.tsx`: proteção visual e navegação conforme o papel.

## Como validar os perfis manualmente no Insomnia

1. Faça login com `POST /api/v1/auth/login`, corpo `{"email":"lider@poiema.com","senha":"poiema123"}`, e copie `token`.
2. Em cada rota protegida, envie `Authorization: Bearer <token>`. O login e `GET /api/v1/health` são as únicas exceções públicas.
3. Como CHURCH_ADMIN, crie voluntário. Para criar também login, inclua e-mail e `senhaInicial` (mínimo 8 caracteres); a resposta nunca contém o hash.
4. Faça login como o voluntário e chame `GET /api/v1/schedules/mine`; tente confirmar uma escala de outro ID e confira que não pode.
5. Faça login como SUPER_ADMIN e use `GET/POST /api/v1/churches`; crie uma igreja e depois `POST /api/v1/churches/{id}/admins`.
6. Use um token de outra igreja para consultar um ID de dados operacionais da Poiema; a consulta deve retornar 404 porque o recurso fora do escopo não é encontrado.

## Rotas do módulo de Ministérios

| Método | Rota | Ação |
|---|---|---|
| `GET` | `/api/v1/ministries` | Listar ministérios |
| `GET` | `/api/v1/ministries/{id}` | Buscar ministério |
| `POST` | `/api/v1/ministries` | Criar ministério |
| `PUT` | `/api/v1/ministries/{id}` | Editar ministério |
| `PATCH` | `/api/v1/ministries/{id}/status` | Ativar/inativar |
| `GET`, `POST` | `/api/v1/ministries/{id}/roles` | Listar/adicionar funções |
| `PUT`, `DELETE` | `/api/v1/ministries/{id}/roles/{roleId}` | Editar/remover função |
| `GET`, `POST` | `/api/v1/ministries/{id}/memberships` | Listar vínculos/vincular pessoa |
| `GET`, `DELETE` | `/api/v1/ministries/{id}/memberships/{membershipId}` | Consultar/desvincular vínculo |

O JSON para criar ou editar um ministério é `{"nome":"Louvor","descricao":"Música e adoração"}`. Para adicionar uma função, `{"nome":"Violão"}`. Para vincular, `{"volunteerId":1,"ministryRoleId":2}`.

## Banco e configuração local

`V1__create_volunteers.sql` e `V2__create_ministries_and_memberships.sql` registram a estrutura do banco. A configuração local em `backend/src/main/resources/application.yaml` continua com `voluntarios_db`, `localhost:3306`, usuário `root`, senha `123456` e `ddl-auto: update`, conforme solicitado. Flyway é o histórico versionado; `update` permanece para o fluxo local de desenvolvimento.

## Como estudar na prática

1. Inicie MySQL.
2. Em um terminal, execute `Set-Location backend` e depois `.\mvnw.cmd spring-boot:run`.
3. Em outro terminal, execute `Set-Location frontend` e depois `npm run dev`.
4. No Insomnia, crie um ministério, depois uma função.
5. Crie ou escolha um voluntário no módulo Voluntários e use o ID da resposta para criar um vínculo.
6. Repita a combinação para ver `409`; tente uma função diferente para ver um segundo vínculo.
7. Desvincule uma função e remova o vínculo; depois remova a função.
8. Compare cada chamada no Network do navegador com os métodos Controller, Service e Repository correspondentes.

## Observações sobre o ambiente

O CORS aceita somente `http://localhost:5173`. O login e health check são públicos; as demais rotas requerem Bearer JWT. CORS não impede clientes como Insomnia ou chamadas servidor-a-servidor. O segredo de desenvolvimento e as senhas de seed devem ser trocados antes de publicar.
