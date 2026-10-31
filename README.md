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
As permissões voltaram à implementação anterior à separação dos checkers.
