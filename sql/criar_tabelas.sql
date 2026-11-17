-- =====================================================================
-- ZelaCidade - Script do banco Oracle (GS 2026/2 - CitizenTech - INOVEX)
--
-- Como rodar: abra no SQL Developer, conectado ao Oracle da FIAP, e
-- execute como script (F5). Pode rodar de novo quantas vezes quiser:
-- o bloco do início apaga tudo antes de criar.
--
-- Tabelas:
--   tb_zc_cidadao     (1) ─< tb_zc_ocorrencia   (quem registrou)
--   tb_zc_ocorrencia  (1) ─< tb_zc_atualizacao  (histórico de status: o 1:N principal)
--   tb_zc_ocorrencia  (N) >─< (N) tb_zc_cidadao  pela associativa tb_zc_apoio
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. DROPs seguros (ignora o erro se a tabela/sequence ainda não existe)
-- ---------------------------------------------------------------------
BEGIN
    FOR obj IN (SELECT 'TABLE' tipo, 'TB_ZC_APOIO' nome FROM dual UNION ALL
                SELECT 'TABLE', 'TB_ZC_ATUALIZACAO' FROM dual UNION ALL
                SELECT 'TABLE', 'TB_ZC_OCORRENCIA' FROM dual UNION ALL
                SELECT 'TABLE', 'TB_ZC_CIDADAO' FROM dual UNION ALL
                SELECT 'SEQUENCE', 'SQ_TB_ZC_APOIO' FROM dual UNION ALL
                SELECT 'SEQUENCE', 'SQ_TB_ZC_ATUALIZACAO' FROM dual UNION ALL
                SELECT 'SEQUENCE', 'SQ_TB_ZC_OCORRENCIA' FROM dual UNION ALL
                SELECT 'SEQUENCE', 'SQ_TB_ZC_CIDADAO' FROM dual) LOOP
        BEGIN
            IF obj.tipo = 'TABLE' THEN
                EXECUTE IMMEDIATE 'DROP TABLE ' || obj.nome || ' CASCADE CONSTRAINTS';
            ELSE
                EXECUTE IMMEDIATE 'DROP SEQUENCE ' || obj.nome;
            END IF;
        EXCEPTION
            WHEN OTHERS THEN NULL;  -- não existia: segue em frente
        END;
    END LOOP;
END;
/

-- ---------------------------------------------------------------------
-- 2. Sequences (uma por tabela; o id nunca é reaproveitado - RN07)
-- ---------------------------------------------------------------------
CREATE SEQUENCE sq_tb_zc_cidadao START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_zc_ocorrencia START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_zc_atualizacao START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_zc_apoio START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- 3. Tabelas
-- ---------------------------------------------------------------------

-- Cidadão: guardamos o nome já padronizado (minúsculas, sem espaços extras),
-- por isso o UNIQUE garante uma linha por pessoa.
CREATE TABLE tb_zc_cidadao (
    cd_cidadao   NUMBER(10)    CONSTRAINT pk_zc_cidadao PRIMARY KEY,
    nm_cidadao   VARCHAR2(80)  CONSTRAINT nn_zc_cidadao_nome NOT NULL,
    dt_cadastro  DATE          DEFAULT SYSDATE CONSTRAINT nn_zc_cidadao_dt NOT NULL,
    CONSTRAINT uk_zc_cidadao_nome UNIQUE (nm_cidadao)
);

CREATE TABLE tb_zc_ocorrencia (
    cd_ocorrencia        NUMBER(10)     CONSTRAINT pk_zc_ocorrencia PRIMARY KEY,
    cd_cidadao           NUMBER(10)     CONSTRAINT nn_zc_oco_cidadao NOT NULL,
    ds_categoria         VARCHAR2(30)   CONSTRAINT nn_zc_oco_categoria NOT NULL,
    ds_ocorrencia        VARCHAR2(300)  CONSTRAINT nn_zc_oco_descricao NOT NULL,
    nm_bairro            VARCHAR2(60)   CONSTRAINT nn_zc_oco_bairro NOT NULL,
    nr_gravidade         NUMBER(1)      CONSTRAINT nn_zc_oco_gravidade NOT NULL,
    nr_pessoas_afetadas  NUMBER(6)      CONSTRAINT nn_zc_oco_pessoas NOT NULL,
    st_perto_servico     CHAR(1)        CONSTRAINT nn_zc_oco_perto NOT NULL,
    st_acessibilidade    CHAR(1)        CONSTRAINT nn_zc_oco_acess NOT NULL,
    st_ocorrencia        VARCHAR2(15)   DEFAULT 'ABERTA' CONSTRAINT nn_zc_oco_status NOT NULL,
    dt_abertura          DATE           DEFAULT TRUNC(SYSDATE) CONSTRAINT nn_zc_oco_abertura NOT NULL,
    dt_resolucao         DATE,
    CONSTRAINT fk_zc_oco_cidadao FOREIGN KEY (cd_cidadao) REFERENCES tb_zc_cidadao (cd_cidadao),
    CONSTRAINT ck_zc_oco_categoria CHECK (ds_categoria IN ('BURACO_NA_VIA', 'ILUMINACAO_PUBLICA', 'LIXO_ENTULHO',
        'CALCADA_ACESSIBILIDADE', 'ARVORE_PODA', 'ALAGAMENTO_BUEIRO', 'OUTRO')),
    CONSTRAINT ck_zc_oco_gravidade CHECK (nr_gravidade BETWEEN 1 AND 5),
    CONSTRAINT ck_zc_oco_pessoas CHECK (nr_pessoas_afetadas BETWEEN 1 AND 100000),
    CONSTRAINT ck_zc_oco_perto CHECK (st_perto_servico IN ('S', 'N')),
    CONSTRAINT ck_zc_oco_acess CHECK (st_acessibilidade IN ('S', 'N')),
    CONSTRAINT ck_zc_oco_status CHECK (st_ocorrencia IN ('ABERTA', 'EM_ANDAMENTO', 'RESOLVIDA')),
    -- RN04: calçada sempre conta como acessibilidade
    CONSTRAINT ck_zc_oco_calcada CHECK (ds_categoria <> 'CALCADA_ACESSIBILIDADE' OR st_acessibilidade = 'S'),
    -- RN06: só resolvida tem data de resolução
    CONSTRAINT ck_zc_oco_resolucao CHECK ((st_ocorrencia = 'RESOLVIDA' AND dt_resolucao IS NOT NULL)
        OR (st_ocorrencia <> 'RESOLVIDA' AND dt_resolucao IS NULL))
);

-- Histórico de status: cada ocorrência (1) tem várias atualizações (N).
CREATE TABLE tb_zc_atualizacao (
    cd_atualizacao  NUMBER(10)     CONSTRAINT pk_zc_atualizacao PRIMARY KEY,
    cd_ocorrencia   NUMBER(10)     CONSTRAINT nn_zc_atu_ocorrencia NOT NULL,
    st_atualizacao  VARCHAR2(15)   CONSTRAINT nn_zc_atu_status NOT NULL,
    ds_comentario   VARCHAR2(300)  CONSTRAINT nn_zc_atu_comentario NOT NULL,
    nm_responsavel  VARCHAR2(80)   CONSTRAINT nn_zc_atu_responsavel NOT NULL,
    dt_atualizacao  TIMESTAMP(0)   DEFAULT SYSTIMESTAMP CONSTRAINT nn_zc_atu_dt NOT NULL,
    CONSTRAINT fk_zc_atu_ocorrencia FOREIGN KEY (cd_ocorrencia) REFERENCES tb_zc_ocorrencia (cd_ocorrencia),
    CONSTRAINT ck_zc_atu_status CHECK (st_atualizacao IN ('ABERTA', 'EM_ANDAMENTO', 'RESOLVIDA'))
);

-- Apoio: associativa Ocorrência x Cidadão. O UNIQUE garante a RN05 no banco
-- (cada pessoa apoia a mesma ocorrência uma vez só).
CREATE TABLE tb_zc_apoio (
    cd_apoio       NUMBER(10)  CONSTRAINT pk_zc_apoio PRIMARY KEY,
    cd_ocorrencia  NUMBER(10)  CONSTRAINT nn_zc_apo_ocorrencia NOT NULL,
    cd_cidadao     NUMBER(10)  CONSTRAINT nn_zc_apo_cidadao NOT NULL,
    dt_apoio       DATE        DEFAULT SYSDATE CONSTRAINT nn_zc_apo_dt NOT NULL,
    CONSTRAINT fk_zc_apo_ocorrencia FOREIGN KEY (cd_ocorrencia) REFERENCES tb_zc_ocorrencia (cd_ocorrencia),
    CONSTRAINT fk_zc_apo_cidadao FOREIGN KEY (cd_cidadao) REFERENCES tb_zc_cidadao (cd_cidadao),
    CONSTRAINT uk_zc_apoio UNIQUE (cd_ocorrencia, cd_cidadao)
);

-- ---------------------------------------------------------------------
-- 4. Dados de exemplo (as mesmas 6 ocorrências do motor em Python)
-- ---------------------------------------------------------------------
-- Cidadãos 1 a 12 (nomes já padronizados)
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'maria souza');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'rafael costa');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'dona cida');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'pedro henrique');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'joana silva');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'joao lima');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'ana paula');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'carlos mendes');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'bruna alves');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'luana reis');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'marcos vinicius');
INSERT INTO tb_zc_cidadao (cd_cidadao, nm_cidadao) VALUES (sq_tb_zc_cidadao.NEXTVAL, 'fernanda rocha');

-- Ocorrências 1 a 6 (datas relativas a hoje, para os pontos de "dias em aberto" fazerem sentido)
INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, nr_gravidade,
    nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, dt_resolucao)
VALUES (sq_tb_zc_ocorrencia.NEXTVAL, 1, 'BURACO_NA_VIA',
    'Buraco grande na faixa de ônibus, em frente ao ponto da Estrada de Itapecerica', 'Capão Redondo',
    4, 400, 'S', 'N', 'ABERTA', TRUNC(SYSDATE) - 9, NULL);

INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, nr_gravidade,
    nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, dt_resolucao)
VALUES (sq_tb_zc_ocorrencia.NEXTVAL, 2, 'ILUMINACAO_PUBLICA',
    'Três postes apagados na rua que dá acesso à escola estadual', 'Jardim Ângela',
    3, 150, 'S', 'N', 'EM_ANDAMENTO', TRUNC(SYSDATE) - 6, NULL);

INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, nr_gravidade,
    nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, dt_resolucao)
VALUES (sq_tb_zc_ocorrencia.NEXTVAL, 3, 'CALCADA_ACESSIBILIDADE',
    'Calçada quebrada e sem rampa na esquina da UBS, cadeirantes vão pela rua', 'Grajaú',
    4, 120, 'S', 'S', 'ABERTA', TRUNC(SYSDATE) - 4, NULL);

INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, nr_gravidade,
    nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, dt_resolucao)
VALUES (sq_tb_zc_ocorrencia.NEXTVAL, 4, 'LIXO_ENTULHO',
    'Sacos de lixo acumulados na esquina da praça, a coleta não passou', 'Itaquera',
    2, 20, 'N', 'N', 'ABERTA', TRUNC(SYSDATE) - 2, NULL);

INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, nr_gravidade,
    nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, dt_resolucao)
VALUES (sq_tb_zc_ocorrencia.NEXTVAL, 5, 'ALAGAMENTO_BUEIRO',
    'Bueiro entupido, a rua alaga sempre que chove forte', 'Brasilândia',
    5, 300, 'N', 'N', 'RESOLVIDA', TRUNC(SYSDATE) - 15, TRUNC(SYSDATE) - 10);

INSERT INTO tb_zc_ocorrencia (cd_ocorrencia, cd_cidadao, ds_categoria, ds_ocorrencia, nm_bairro, nr_gravidade,
    nr_pessoas_afetadas, st_perto_servico, st_acessibilidade, st_ocorrencia, dt_abertura, dt_resolucao)
VALUES (sq_tb_zc_ocorrencia.NEXTVAL, 6, 'ARVORE_PODA',
    'Galho seco grande pendurado sobre a fiação elétrica', 'Capão Redondo',
    3, 40, 'N', 'N', 'ABERTA', TRUNC(SYSDATE) - 1, NULL);

-- Apoios (cidadão x ocorrência)
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 1, 6);   -- joao lima
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 1, 7);   -- ana paula
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 1, 8);   -- carlos mendes
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 2, 9);   -- bruna alves
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 4, 10);  -- luana reis
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 5, 11);  -- marcos vinicius
INSERT INTO tb_zc_apoio (cd_apoio, cd_ocorrencia, cd_cidadao) VALUES (sq_tb_zc_apoio.NEXTVAL, 5, 12);  -- fernanda rocha

-- Histórico (1:N)
INSERT INTO tb_zc_atualizacao (cd_atualizacao, cd_ocorrencia, st_atualizacao, ds_comentario, nm_responsavel, dt_atualizacao)
VALUES (sq_tb_zc_atualizacao.NEXTVAL, 2, 'EM_ANDAMENTO', 'Equipe da Ilume agendada para trocar as lâmpadas na quinta-feira.',
    'Iluminação pública (Ilume)', CAST(TRUNC(SYSDATE) - 3 + 9.5/24 AS TIMESTAMP(0)));
INSERT INTO tb_zc_atualizacao (cd_atualizacao, cd_ocorrencia, st_atualizacao, ds_comentario, nm_responsavel, dt_atualizacao)
VALUES (sq_tb_zc_atualizacao.NEXTVAL, 5, 'EM_ANDAMENTO', 'Caminhão de desobstrução enviado ao local.',
    'Subprefeitura - drenagem', CAST(TRUNC(SYSDATE) - 12 + 14/24 AS TIMESTAMP(0)));
INSERT INTO tb_zc_atualizacao (cd_atualizacao, cd_ocorrencia, st_atualizacao, ds_comentario, nm_responsavel, dt_atualizacao)
VALUES (sq_tb_zc_atualizacao.NEXTVAL, 5, 'RESOLVIDA', 'Bueiro desentupido e galeria limpa.',
    'Subprefeitura - drenagem', CAST(TRUNC(SYSDATE) - 10 + 16.75/24 AS TIMESTAMP(0)));

COMMIT;

-- ---------------------------------------------------------------------
-- 5. Conferência rápida (deve mostrar 12 cidadãos, 6 ocorrências, 3 atualizações e 7 apoios)
-- ---------------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM tb_zc_cidadao) AS cidadaos,
       (SELECT COUNT(*) FROM tb_zc_ocorrencia) AS ocorrencias,
       (SELECT COUNT(*) FROM tb_zc_atualizacao) AS atualizacoes,
       (SELECT COUNT(*) FROM tb_zc_apoio) AS apoios
FROM dual;
