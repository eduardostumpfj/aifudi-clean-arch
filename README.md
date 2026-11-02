# aifudi-clean-arch

## Banco de dados

O arquivo `postgres-db/init.sql` cria as tabelas e os dados iniciais na ordem
das dependências, seguindo o padrão de inicialização do projeto aifudi.
A aplicação não usa Flyway. O Hibernate somente valida o esquema
(`spring.jpa.hibernate.ddl-auto=validate`).

Com o `.env` preenchido, execute na raiz do projeto:

```sh
docker --context desktop-linux compose up -d
```

Em volumes novos, a imagem do PostgreSQL executa `init.sql` automaticamente.
Em volumes já inicializados, o script não é executado novamente ao reiniciar
ou recriar o contêiner. Para aplicar o script no banco existente sem apagar
o volume, execute na raiz do projeto:

```sh
docker --context desktop-linux compose exec -T postgres-db sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < postgres-db/init.sql
```

O script usa uma transação, preserva os registros existentes e configura
`DEFAULT gen_random_uuid()` para os IDs das sete tabelas. Novos registros
recebem seus IDs do banco. Reexecutar o script não duplica os dados iniciais;
ele não corrige diferenças arbitrárias na estrutura de tabelas existentes.
Não é necessário excluir volumes para aplicá-lo.

Depois, inicie o backend pela IDE ou execute em `backend`:

```sh
./mvnw spring-boot:run
```

Rodando o backend no computador, use `localhost` e a porta publicada no Compose.
Entre contêineres do Compose, o endereço é `postgres-db:5432`.
O pgAdmin está disponível em http://localhost:5050 e usa as credenciais
`PGADMIN_EMAIL` e `PGADMIN_PASSWORD` do `.env`.

## Dados iniciais para desenvolvimento

Roles e subroles são resolvidos por `value`, preservando IDs existentes.
Os subroles `admin`, `main` e `user` têm UUIDs fixos terminados em `011`, `012`
e `013`. O script cria a conta ADMIN com login `admin` e senha `admin123`.

| Role | ID |
| --- | --- |
| `ADMIN` | `00000000-0000-4000-8000-000000000001` |
| `OWNER` | `00000000-0000-4000-8000-000000000002` |
| `USER` | `00000000-0000-4000-8000-000000000003` |


As senhas das contas de teste são armazenadas com BCrypt via `pgcrypto`.

| Nome | Login | Senha | Role | ID |
| --- | --- | --- | --- | --- |
| Ana Silva | `ana.user` | `ana123` | USER | `00000000-0000-4000-8000-000000000101` |
| Bruno Santos | `bruno.owner` | `bruno123` | OWNER | `00000000-0000-4000-8000-000000000102` |
| Carla Oliveira | `carla.owner` | `carla123` | OWNER | `00000000-0000-4000-8000-000000000103` |
| Diego Souza | `diego.user` | `diego123` | USER | `00000000-0000-4000-8000-000000000104` |
| Elisa Costa | `elisa.user` | `elisa123` | USER | `00000000-0000-4000-8000-000000000105` |


Os e-mails seguem `<login>@example.com`.

| Value | Cozinha | ID |
| --- | --- | --- |
| `ITALIAN` | Italiana | `00000000-0000-4000-8000-000000000201` |
| `ARABIC` | Árabe | `00000000-0000-4000-8000-000000000202` |
| `BRAZILIAN` | Brasileira | `00000000-0000-4000-8000-000000000203` |
| `JAPANESE` | Japonesa | `00000000-0000-4000-8000-000000000204` |
| `MEXICAN` | Mexicana | `00000000-0000-4000-8000-000000000205` |
| `CHINESE` | Chinesa | `00000000-0000-4000-8000-000000000206` |
| `INDIAN` | Indiana | `00000000-0000-4000-8000-000000000207` |
| `FRENCH` | Francesa | `00000000-0000-4000-8000-000000000208` |

## Geração de IDs

As entidades JPA usam `@Generated` do Hibernate para recuperar IDs produzidos
pelo banco. Os casos de uso enviam objetos novos sem ID e usam os objetos
retornados pela persistência. Vínculos existentes preservam seu ID.
A criação do restaurante e do vínculo do proprietário ocorre na mesma transação:
o restaurante é salvo primeiro e seu ID retornado é utilizado no vínculo.

## Vínculos iniciais

O `init.sql` inclui dois restaurantes de exemplo com IDs terminados em `301`
(Restaurante do Bruno, cozinha brasileira) e `302` (Restaurante da Carla,
cozinha italiana). Bruno e Carla recebem `mainOwner` nos respectivos restaurantes;
Ana e Diego recebem `mainUser` no primeiro e Elisa no segundo. O usuário `admin`
recebe `admin` nos dois. Cada usuário tem `users.client_subrole_id` obrigatório para seu papel global.
Os IDs dos memberships são gerados pelo PostgreSQL e a reexecução preserva
os vínculos existentes para o mesmo usuário e restaurante.

## Perfil obrigatório do usuário

`users.client_subrole_id` é obrigatório: identifica o papel global do usuário
(inclusive ADMIN). Memberships identificam o papel dentro de cada restaurante.
O assign de subroles permite ADMIN em qualquer restaurante e mainOwner somente
no próprio restaurante. mainOwner não pode atribuir nem substituir vínculos
ADMIN/mainOwner, incluindo o próprio vínculo. ADMIN pode realizar essas
atribuições e substituições. O perfil global do usuário não é alterado;
o CRUD de subroles continua exclusivo de ADMIN.

## Organização dos testes

Os testes acompanham os pacotes do código de produção em
`backend/src/test/java/com/aifudi_clean_arch/backend`:

```text
core/
  controller/              # Encaminhamento para os casos de uso
  domain/                  # Validações e regras das entidades
  usecase/                 # Cenários que envolvem mais de um caso de uso
    menuItem/
    restaurant/
    subrole/
    membership/
    permission/
infra/
  database/jpa/gateway/
  web/
    handler/
    json/
```

Para executar os testes isolados e gerar uma medição nova, dentro de `backend`:

```sh
./mvnw clean test
```

Esse comando executa os testes unitários. O teste de inicialização do Spring foi
movido para `integration/BackendApplicationIT` e roda com o Failsafe.
O relatório é gerado em `target/site/jacoco/index.html`, sem excluir pacotes de
produção do cálculo. Não há exigência automática de cobertura mínima configurada.
Para executar apenas as validações de domínio:

```sh
./mvnw test '-Dtest=com.aifudi_clean_arch.backend.core.domain.*Test'
```

`MenuItem.hasPrice()` possui um caminho falso que o construtor não permite
alcançar: itens com preço nulo ou não positivo são rejeitados na criação. Os
testes preservam essa regra, sem alterar o estado interno para aumentar a cobertura.

## Testes de integração com REST Assured

Os testes em `src/test/java/com/aifudi_clean_arch/backend/integration` usam
REST Assured (`given / when / then`), Spring Boot numa porta aleatória e
PostgreSQL real em um Docker Compose separado. Não usam Testcontainers ou mocks.
O teste de inicialização também fica nessa pasta como `BackendApplicationIT`.

Na raiz do projeto, com o Docker Desktop aberto, suba o banco exclusivo de testes:

```sh
docker --context desktop-linux compose -f docker-compose.test.yml up -d --wait
```

O banco `aifudi_integration` usa a porta local `55432`, usuário e senha
`integration` e é inicializado por `postgres-db/init.sql`. O serviço de testes
não utiliza o volume do banco de desenvolvimento. Seus dados ficam em memória
no contêiner e são descartados quando ele é parado. Para outro ambiente Docker,
use o contexto correspondente no comando.

Dentro de `backend`, execute:

```sh
./mvnw clean test     # Apenas unitários; não usa banco
./mvnw clean verify   # Unitários e integração; exige o banco de testes rodando
```

O perfil `integration` usa `src/test/resources/application-integration.properties`.
Para mudar a conexão, configure `TEST_DB_URL`, `TEST_DB_USERNAME` e
`TEST_DB_PASSWORD`. O nome do banco deve continuar sendo `aifudi_integration`;
os testes verificam isso antes de limpar dados. Não use o banco de desenvolvimento.

Os cenários de menuItem limpam o cardápio antes de cada teste. Os cenários de
restaurante removem somente os restaurantes e relacionamentos criados pelo
próprio cenário. Não execute os cenários em paralelo, pois compartilham o banco.

Ao terminar, na raiz do projeto:

```sh
docker --context desktop-linux compose -f docker-compose.test.yml down
```

Os relatórios são separados:

- Unitários: `backend/target/surefire-reports` e `backend/target/site/jacoco/index.html`.
- Integração: `backend/target/failsafe-reports` e `backend/target/site/jacoco-it/index.html`.

`MenuItemApiIT` cobre o CRUD, autenticação, permissões e validações do cardápio.
`RestaurantApiIT` cobre cadastro com vínculo `mainOwner`, consulta com coleções,
atualização parcial e de cozinha, exclusão, permissões e entradas inválidas.
A exclusão de um restaurante com vínculos retorna 409, conforme as restrições
atuais do banco; a exclusão sem dependências retorna 200.

`SubRoleApiIT` cobre CRUD com acesso ADMIN, cabeçalho Location, papel inexistente,
valores duplicados, proteção de `mainOwner`, conflitos de exclusão, criação e
reatribuição de memberships com preservação do ID, dependências ausentes e
validações. Os cenários limpam somente os subroles e restaurantes criados pelo
próprio teste, sem alterar os perfis globais dos usuários iniciais.

## Swagger / OpenAPI

Com o banco e o backend em execução, acesse:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Documento OpenAPI JSON: http://localhost:8080/v3/api-docs

Se mudar `server.port`, ajuste a porta desses endereços. A documentação é
organizada em Restaurantes, Cardápio e Subroles, com campos, respostas e regras
de acesso. Clique em **Authorize**, informe o login e a senha de um usuário do
banco e use **Try it out** para chamar os endpoints. A autenticação é HTTP Basic;
não é necessário preencher o cabeçalho Authorization manualmente. As permissões
continuam sendo verificadas pela aplicação. As chamadas pelo Swagger gravam e
alteram dados no banco ao qual a aplicação estiver conectada.

A configuração segue o padrão do projeto aifudi de referência, usando Springdoc
3.1.1 para este projeto com Spring Boot 4.1 e o esquema `basicAuth`.
Os corpos de requisição exibem exemplos JSON com IDs do seed. As respostas
mostram código e descrição, sem modelos genéricos; a lista de schemas no rodapé
fica oculta. Isso altera somente a documentação, não os corpos retornados pela API.
`OpenApiIT` verifica a geração do documento, autenticação documentada, principais
endpoints e acesso à interface e sua configuração local.
