# Pendências de requisitos da igreja

Estas perguntas não bloqueiam a organização do repositório e a documentação. As respostas são necessárias antes de consolidar o modelo de dados e regras correspondentes.

## Pessoas e acesso

- [x] Campos do MVP definidos: nome completo obrigatório; telefone, e-mail, disponibilidade e observações opcionais; status ativo começa como verdadeiro.
- [x] Perfis definidos: SUPER_ADMIN, CHURCH_ADMIN e VOLUNTEER; autenticação por JWT e senhas com BCrypt.
- [x] Voluntário pode receber conta de acesso no cadastro; essa conta usa o e-mail e senha inicial informados pelo administrador.
- [x] Cada conta operacional pertence a uma igreja, e as consultas/escritas operacionais são limitadas à igreja autenticada.
- [ ] Há menores de idade ou dados sensíveis que exijam regras adicionais de privacidade?
- [ ] Quem cria contas e associa voluntários a ministérios? Haverá auto cadastro?
- [ ] O voluntário pode editar seus próprios dados? Quais campos?
- [ ] Um responsável pode gerenciar mais de um ministério? Pode consultar outros ministérios?
- [ ] Quais dados pessoais cada perfil pode consultar?

## Ministérios e funções

- [x] Ministério tem nome obrigatório e único, descrição opcional e status ativo inicialmente verdadeiro.
- [x] Cada função pertence a um ministério. O catálogo inicial é definido pela própria igreja (por exemplo, Violão, Recepção e Mesa de Som).
- [x] O vínculo associa voluntário, ministério e função; repetir a mesma combinação não é permitido.
- [x] Desvincular remove o vínculo atual. Histórico de vínculos não faz parte deste MVP.
- [ ] O nome da função deve ser único dentro do mesmo ministério? Atualmente é possível repetir nomes.
- [ ] Voluntários inativos podem ser vinculados? Ministérios inativos podem receber novas funções e vínculos? O sistema ainda permite ambas as ações porque não foi definida uma restrição.

## Disponibilidade e agenda

- [x] O evento do MVP tem título, data, horário, turno/dia, observações e status AGENDADO, CONCLUIDO ou CANCELADO.
- [x] A disponibilidade incompatível com o turno do evento deve gerar aviso, sem impedir a escala automaticamente.
- [ ] O MVP aceita códigos livres de disponibilidade, com exemplos DOMINGO_MANHA, DOMINGO_NOITE e QUARTA_NOITE. A igreja precisa definir o catálogo oficial e se a disponibilidade será recorrente, específica por data ou ambas.
- [ ] Cultos são recorrentes? Quais dados os distinguem de eventos?

## Escalas e presença

- [x] O MVP permite montagem manual de escala a partir de vínculos voluntário/ministério/função já cadastrados.
- [x] O mesmo voluntário não pode aparecer mais de uma vez no mesmo evento, ainda que em outra função.
- [x] Para criar uma escala, voluntário precisa estar ativo e vinculado à função e ministério selecionados.
- [x] Confirmação de presença no MVP é representada por PENDENTE, CONFIRMADO ou RECUSADO. Não há notificação automática neste módulo.
- [ ] Escalas terão sugestões automáticas em uma etapa futura?
- [ ] Existem limites de frequência, descanso ou antecedência?
- [ ] Quais respostas de confirmação existem e qual é o prazo para responder?
- [ ] Quem registra check-in e o que esse registro significa?
- [ ] Como escolher suplentes? Substituições precisam de aprovação e confirmação?

## Operação

- [x] Organização inicial: igreja matriz com zero ou mais filiais/congregações; filial aponta para a igreja e pode ser opcional no cadastro operacional.
- [x] Contas seed para desenvolvimento criadas somente quando não existem usuários. Senhas de seed são configuráveis por ambiente e devem ser alteradas antes de implantação.

- [ ] Notificações por e-mail, WhatsApp ou push entram no MVP?
- [ ] Existe preferência de hospedagem e orçamento?
- [ ] Qual versão de Java deve ser padronizada nas máquinas de desenvolvimento e produção?

Decisões serão registradas aqui conforme forem confirmadas. Regras de negócio não serão inferidas a partir de respostas incompletas.
