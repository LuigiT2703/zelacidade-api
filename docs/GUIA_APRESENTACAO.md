# Guia da apresentação: ZelaCidade API (Java)

O guia segue os critérios da banca. Para cada um tem:
- o que mostrar;
- o arquivo que fica aberto;
- a frase curta para falar.

**Antes de começar:** API no ar (abra a URL do Render 1 minuto antes), banco recém-criado (`sql/criar_tabelas.sql`), `requests/zelacidade.http` aberto e o front aberto.

---

## 1. Demonstração do CRUD (8 pontos)

Faça pelo **front** (mostra a integração) e confirme no `.http` ou no SQL Developer.

| Operação | Pelo front | Rota | Resposta |
|---|---|---|---|
| Create | Relatar → "Confirmar registro" | `POST /ocorrencias` | 201 + `Location: /ocorrencias/7` |
| Read | Fila e detalhe da #7 | `GET /ocorrencias` e `GET /ocorrencias/7` | 200 |
| Update | "Atualizar status" na #7 | `POST /ocorrencias/7/atualizacoes` (e `PUT /ocorrencias/{id}` no `.http`) | 201 / 200 |
| Delete | "Excluir ocorrência" → "Sim, excluir a #7" | `DELETE /ocorrencias/7` | 204 sem corpo |

**Extra:** apoiar a #3 no front mostra a pontuação subindo 3 pontos ("de 85 para 88").

## 2. Erros ao vivo (4 pontos)

Rode no `.http`, na seção **ERROS**:

| Pedido | Resposta |
|---|---|
| Gravidade 9 | **400** "A gravidade deve ser um número de 1 a 5." |
| Categoria `XYZ` | **400** com a lista de categorias válidas |
| `GET /ocorrencias/abc` | **400** "Use um número inteiro." |
| `GET /ocorrencias/9999` | **404** "Não encontramos a ocorrência #9999." |
| `PATCH /ocorrencias/1` | **405** |
| Duplicada pendente | **409** "Já existe uma ocorrência igual em aberto (#…)" |
| `"  JOAO   lima "` apoiando a #1 | **409**, porque o nome é padronizado antes de comparar |
| `DELETE /ocorrencias/5` (resolvida) | **409** |

**Frase:** "Todo erro sai no mesmo formato, `{status, mensagem}`, pelo `TratadorDeErros`, que é o nosso `@RestControllerAdvice`. Nunca aparece stack trace; o erro de banco vira 500 com mensagem genérica e o detalhe fica só no log."

## 3. Caminho de uma requisição (6 pontos)

Exemplo: `POST /ocorrencias/3/apoios` com `{ "nome": "Ana Lima" }`. Abra os arquivos nesta ordem:

1. **`controller/OcorrenciaController.apoiar`:** recebe o JSON no `ApoioRequestDTO`. O `@Valid` roda o Bean Validation (`@NotBlank`, `@Size(min=2)`); se falhar, vira 400 antes de chegar no service.
2. **`service/ApoioService.apoiar`:**
   - busca a ocorrência (404 se não existe);
   - bloqueia se está RESOLVIDA (409);
   - `CidadaoService.buscarOuCriar` padroniza o nome (`Texto.padronizarNome`) e acha ou cria o cidadão;
   - bloqueia quem registrou (409) e quem já apoiou (409).
3. **`dao/ApoioDAO.inserir`:** `dataSource.getConnection()` → `PreparedStatement` com `?` → `getGeneratedKeys` pega o id da sequence. Tudo em try-with-resources, então a conexão sempre fecha.
4. **De volta no service:** `contarPorOcorrencia` monta `{ ocorrenciaId, totalApoios }` e o controller devolve **201**.
5. **Se der `SQLException`:** o DAO embrulha em `AcessoDadosException`, e o `TratadorDeErros` responde 500 com mensagem genérica.

**Frase:** "O controller só recebe e devolve, a regra fica toda no service e o SQL fica só no DAO. Cada camada tem uma responsabilidade."

## 4. Entidades, relacionamento 1:N e regra de negócio (4 pontos)

Mostre `docs/der.png` e `docs/classes.png`.

- **4 entidades:** `Cidadao`, `Ocorrencia`, `Atualizacao` e `Apoio`, mais os enums `Categoria` e `StatusOcorrencia`.
- **1:N:** uma ocorrência tem N atualizações (o histórico, `GET /ocorrencias/{id}/atualizacoes`). Cidadão 1:N ocorrência.
- **N:N:** cidadão e ocorrência pela tabela `tb_zc_apoio`, com `UNIQUE (cd_ocorrencia, cd_cidadao)`.
- **Regra principal:** a prioridade em `PrioridadeService.calcularPontos`, com a mesma fórmula do motor em Python (máx. 130).
- **Outras regras:**
  - RN04: calçada força acessibilidade;
  - RN05: regras do apoio;
  - RN06: data de resolução;
  - RN07: resolvida não exclui;
  - RN11: duplicada.
- **Transação JDBC:** `AtualizacaoDAO.inserirMudandoStatus` grava o histórico e muda o status juntos (`setAutoCommit(false)`, `commit`, `rollback`).

## 5. Perguntas da banca (8 pontos): respostas curtas

| Pergunta provável | Resposta |
|---|---|
| Por que não JPA? | A disciplina pede JDBC puro. Com DAO e PreparedStatement a gente controla o SQL e vê exatamente o que vai para o banco. |
| O que é o DAO? | A classe que isola o acesso ao banco. O service não sabe que é Oracle nem conhece SQL. |
| Por que PreparedStatement? | Evita SQL injection (os valores vão separados do comando) e trata tipos como data. |
| Para que o try-with-resources? | Fecha Connection, PreparedStatement e ResultSet sozinho, mesmo se der erro. Sem isso, o pool de conexões esgota. |
| De onde vem o DataSource? | Do Spring Boot (pool HikariCP), montado com as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD`. Ele é injetado pelo construtor de cada DAO. |
| Para que o DTO? | Separa o que a API recebe/devolve do model. Exemplo: o nome do cidadão entra no DTO, mas nunca sai na resposta (LGPD). |
| Para que o ModelMapper? | Copia os campos entre DTO e model sem escrever `set` por `set`. Usamos o modo STRICT para não ligar campos errados. |
| Onde fica a validação? | Formato no DTO (Bean Validation). Regra de negócio no service. As constraints do banco (CHECK, UNIQUE, FK) são a última barreira. |
| Como funciona o tratamento de erro? | Exceções próprias (`RecursoNaoEncontradoException` 404, `RegraNegocioException` 409, `RequisicaoInvalidaException` 400, `AcessoDadosException` 500) são capturadas pelo `@RestControllerAdvice`, que monta o `ErroDTO`. |
| Por que 201 com Location? | É o padrão REST para "criado": o Location diz onde o recurso novo pode ser lido. |
| Por que 409 e não 400? | O 400 é para pedido mal formado. O 409 é para pedido correto que conflita com o estado atual (já apoiou, já está resolvida, duplicada). |
| Como os ids são gerados? | Por sequences do Oracle (`sq_tb_zc_*`). O id volta pelo `getGeneratedKeys`. Id excluído nunca é reaproveitado. |
| E se duas pessoas apoiarem ao mesmo tempo? | O service confere antes de inserir, e o UNIQUE no banco garante no fim: se passar, o banco recusa. |
| Onde fica a senha do banco? | Só em variáveis de ambiente: `.env` local (fora do Git) e o painel do Render. O código só tem `${DB_PASSWORD}`. |
| O que o CORS libera? | Só o localhost do Vite e o endereço do front na Vercel (`FRONT_URL`), com GET/POST/PUT/DELETE. Outra origem leva 403. |
| Como funciona o deploy? | Dockerfile multi-stage: a 1ª etapa compila com Maven e a 2ª roda só o `.jar` numa imagem JRE 21, menor. O Render informa a porta por `PORT`. |
| Por que o fuso de São Paulo? | O Render roda em UTC. Sem fixar o fuso, depois das 21h a data "pularia" para o dia seguinte e os dias em aberto sairiam errados. |
| Por que a API já devolve os pontos? | Para a regra ficar num lugar só (o servidor). O front só mostra. |

**Quem fala o quê (sugestão):**
- **Luigi:** caminho da requisição, código e erros.
- **Cauã:** entidades, banco e regras de negócio.
- **Gabriel:** demonstração no front e deploy.

Nas perguntas, quem souber melhor responde.
