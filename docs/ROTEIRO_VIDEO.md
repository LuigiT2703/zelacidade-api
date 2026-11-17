# Roteiro do vídeo: ZelaCidade API (até 5 minutos)

**Antes de gravar:**
- **Banco:** rode o `sql/criar_tabelas.sql` (F5 no SQL Developer) para começar com os 6 exemplos.
- **API:** abra a URL do Render uma vez para ela acordar. A primeira chamada pode levar uns 50 s.
- **Abas abertas:**
  - o front (Vercel ou `npm run dev`);
  - o `requests/zelacidade.http` no VS Code ou no IntelliJ;
  - o SQL Developer;
  - a IDE com o código.
- Grave em 1080p, com a fonte da IDE grande (zoom 125%).

| Tempo | O que mostrar | O que falar |
|---|---|---|
| 0:00–0:25 | Front na Home | "Somos o grupo INOVEX. O ZelaCidade é uma fila de prioridade para a zeladoria: o cidadão relata, os vizinhos apoiam e a prefeitura atende primeiro o mais urgente. Esta é a API em Java que guarda tudo no Oracle." |
| 0:25–1:05 | IDE: as pastas `controller`, `service`, `dao`, `model`, `dto`, `exception`, `handler` | "A requisição entra no controller, a regra fica no service e o banco só é acessado pelo DAO, com JDBC puro: PreparedStatement e try-with-resources, sem JPA. O DataSource chega pelo construtor. Os DTOs são validados com Bean Validation e convertidos com ModelMapper." |
| 1:05–1:30 | `docs/der.png` ou as tabelas no SQL Developer | "São 4 tabelas. Uma ocorrência tem N atualizações, que formam o histórico (1:N). Cidadão e ocorrência se ligam pela tabela de apoio, com UNIQUE para cada pessoa apoiar uma vez só." |
| 1:30–2:40 | **Front ligado na API:** Relatar → preencher → "Confirmar registro" → "Ocorrência #7 registrada!" → abrir a #7 → apoiar com um nome → pontos sobem 3 → mudar status para "Em andamento" → aparece no histórico → excluir a #7 | "Cada tela chama uma rota: POST /ocorrencias devolveu 201 com Location, o apoio é POST /ocorrencias/7/apoios, o status é POST /atualizacoes e a exclusão é DELETE com 204. Os pontos vêm calculados pela API." |
| 2:40–3:00 | SQL Developer: `SELECT * FROM tb_zc_atualizacao ORDER BY 1 DESC` | "Está gravado no Oracle de verdade, inclusive o histórico." |
| 3:00–4:10 | **Erros ao vivo** no `.http` (Send Request em cada um) | Mostrar 5 erros e ler a `mensagem` de cada um: <br>1. gravidade 9 → 400 "A gravidade deve ser um número de 1 a 5."<br>2. GET /ocorrencias/9999 → 404<br>3. registrar a mesma ocorrência de novo → 409 "Já existe uma ocorrência igual em aberto…"<br>4. apoiar duas vezes com "  JOAO   lima " → 409 (o nome é padronizado)<br>5. DELETE /ocorrencias/5 (resolvida) → 409<br>"Todos os erros saem no mesmo JSON, status e mensagem, pelo @RestControllerAdvice, sem stack trace." |
| 4:10–4:40 | `PrioridadeService` | "A regra de prioridade é a mesma do motor em Python: gravidade vezes 10, pessoas, dias em aberto, bônus de escola ou saúde, de acessibilidade e de apoios, até 130 pontos." |
| 4:40–5:00 | README com os links e o painel do Render | "A API está no Render, num Docker multi-stage, com as credenciais só em variáveis de ambiente. Obrigado!" |

**Dica:** se passar de 5 minutos, corte a parte do SQL Developer (2:40–3:00) e mostre só 4 erros.
