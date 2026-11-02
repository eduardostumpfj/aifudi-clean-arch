# Subroles

Gerencia subroles associados a um papel (`role`) e atribui subroles a usuários em restaurantes por meio de vínculos (`memberships`). Todas as operações exigem autenticação HTTP Basic. O CRUD de subroles exige papel global **ADMIN**; o assign também aceita o **mainOwner do restaurante**.

## Configuração no Postman

Configure a autenticação da pasta como **Basic Auth**, usando `{{adminLogin}}` e `{{adminPassword}}`. Nas requisições, selecione **Inherit auth from parent**.

Defina as variáveis na coleção ou no ambiente:

| Variável | Valor |
| --- | --- |
| `baseUrl` | URL da aplicação, sem barra final; exemplo: `http://localhost:8080` |
| `adminLogin` | Login de um usuário ADMIN existente |
| `adminPassword` | Senha desse usuário |
| `roleId` | UUID de um papel existente |
| `subRoleId` | UUID do subrole criado ou consultado |
| `userId` | UUID do usuário que receberá o vínculo |
| `restaurantId` | UUID de um restaurante existente |

Para requisições com corpo, use **Body → raw → JSON** e o cabeçalho `Content-Type: application/json`.

## Criar subrole

**POST** `{{baseUrl}}/v1/subroles`

```json
{
  "value": "manager",
  "label": "Gerente",
  "roleId": "{{roleId}}"
}
```

| Campo | Obrigatório | Regra |
| --- | --- | --- |
| `value` | Sim | Identificador textual, não vazio, com até 50 caracteres |
| `label` | Sim | Nome de exibição, não vazio, com até 50 caracteres |
| `roleId` | Sim | UUID de um papel existente |

**Resposta: `201 Created`**. Retorna `id`, `value`, `label` e `roleId`. O cabeçalho `Location` indica `/v1/subroles/{id}`.

Exemplo ilustrativo de resposta:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "value": "manager",
  "label": "Gerente",
  "roleId": "550e8400-e29b-41d4-a716-446655440001"
}
```

Para salvar automaticamente o ID, adicione em **Scripts → Post-response**:

```javascript
pm.test("Subrole criado", function () {
  pm.response.to.have.status(201);
});

if (pm.response.code === 201) {
  pm.collectionVariables.set("subRoleId", pm.response.json().id);
}
```

Se usar esse script, evite definir `subRoleId` também no ambiente, pois o valor do ambiente tem prioridade sobre o da coleção.

O valor `mainOwner` é reservado e sua criação retorna `403 Forbidden`. Um papel inexistente retorna `404 Not Found`. Conflitos com restrições do banco retornam `409 Conflict`.

## Consultar subrole

**GET** `{{baseUrl}}/v1/subroles/{{subRoleId}}`

Não possui corpo.

**Resposta: `200 OK`**. Retorna `id`, `value`, `label` e `roleId`, no mesmo formato da criação. Se o subrole não existir, retorna `404 Not Found`.

Não há endpoint de listagem de subroles nesta API.

## Atualizar subrole

**PUT** `{{baseUrl}}/v1/subroles/{{subRoleId}}`

```json
{
  "value": "supervisor",
  "label": "Supervisor",
  "roleId": "{{roleId}}"
}
```

Apesar de usar PUT, esta operação atualiza somente os campos informados. Campos omitidos ou enviados como `null` preservam os valores atuais. Para alterar apenas o nome de exibição:

```json
{
  "label": "Gerente de operações"
}
```

`value` e `label`, quando enviados, devem conter texto não vazio e ter até 50 caracteres. `roleId`, quando informado, deve apontar para um papel existente.

**Resposta: `200 OK`**. Retorna `id`, `value`, `label` e `roleId` atualizados.

- Subrole ou papel inexistente: `404 Not Found`.
- Alteração do subrole proprietário principal (`mainOwner` associado ao papel `OWNER`): `403 Forbidden`.
- Alteração de `value` para `mainOwner`: `403 Forbidden`.
- Conflito com restrições do banco: `409 Conflict`.

## Excluir subrole

**DELETE** `{{baseUrl}}/v1/subroles/{{subRoleId}}`

Não possui corpo.

**Resposta: `204 No Content`**, sem corpo.

- Subrole inexistente: `404 Not Found`.
- Exclusão do proprietário principal (`mainOwner` associado a `OWNER`): `403 Forbidden`.
- Subrole ainda referenciado por registros relacionados: `409 Conflict`.

## Atribuir subrole a um usuário

**POST** `{{baseUrl}}/v1/subroles/assign`

```json
{
  "userId": "{{userId}}",
  "subRoleId": "{{subRoleId}}",
  "restaurantId": "{{restaurantId}}"
}
```

Os três campos são obrigatórios e devem conter UUIDs de registros existentes.

A operação cria um vínculo entre usuário e restaurante. Se esse vínculo já existir, atualiza seu subrole e preserva o ID do vínculo. O perfil global do usuário não é alterado.

**Resposta: `200 OK`**, tanto na criação quanto na atualização do vínculo.

Exemplo ilustrativo de resposta:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "userId": "550e8400-e29b-41d4-a716-446655440003",
  "subRoleId": "550e8400-e29b-41d4-a716-446655440000",
  "restaurantId": "550e8400-e29b-41d4-a716-446655440004"
}
```

- Usuário, restaurante ou subrole inexistente: `404 Not Found`.
- ADMIN pode atribuir qualquer subrole e substituir qualquer vínculo, incluindo `mainOwner`.
- mainOwner pode atribuir subroles comuns somente no restaurante em que possui esse vínculo.
- mainOwner não pode atribuir nem substituir vínculos `mainOwner` ou associados ao papel `ADMIN`; essas tentativas retornam `403 Forbidden`. Isso também impede mudar sua própria função.
- Outros usuários, inclusive OWNER sem vínculo mainOwner no restaurante solicitado, recebem `403 Forbidden`.

## Respostas de erro

Os erros são retornados no formato Problem Detail (`application/problem+json`).

| Status | Significado |
| --- | --- |
| `400 Bad Request` | JSON inválido, UUID inválido ou campos que não atendem às validações |
| `401 Unauthorized` | Credenciais ausentes ou inválidas |
| `403 Forbidden` | Usuário sem autorização para a operação ou operação sobre um subrole protegido |
| `404 Not Found` | Recurso necessário à operação não encontrado |
| `409 Conflict` | Conflito com dados existentes ou referências no banco |
| `500 Internal Server Error` | Falha interna inesperada |

Exemplo de erro de permissão:

```json
{
  "type": "about:blank",
  "title": "Not allowed",
  "status": 403,
  "detail": "Mensagem que descreve a restrição de acesso",
  "instance": "/v1/subroles"
}
```

Erros de validação de campos também podem incluir uma lista `errors` com os detalhes das violações.

## Sequência sugerida

1. Configure as variáveis e a autenticação ADMIN.
2. Crie um subrole e salve o ID em `subRoleId`.
3. Consulte o subrole pelo ID.
4. Atualize seu nome de exibição e consulte novamente.
5. Teste a exclusão antes de criar vínculos, para evitar conflito por referências.
6. Crie outro subrole para testar a atribuição a um usuário e restaurante existentes.
