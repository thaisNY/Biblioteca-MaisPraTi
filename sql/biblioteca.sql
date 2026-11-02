-- =====================================================================
-- Biblioteca de bairro - Exercicio 2 (PostgreSQL)
-- Mesmo cenario do Exercicio 1, agora em tabelas relacionais.
-- Sem heranca: o "tipo" de item e de usuario fica numa coluna com CHECK.
--
-- Como usar no pgAdmin:
--   1) Crie o banco (uma vez):  CREATE DATABASE biblioteca;
--   2) Abra o Query Tool no banco "biblioteca" e execute este arquivo.
-- O script pode ser executado de novo: ele apaga e recria tudo.
-- =====================================================================

-- Apaga na ordem inversa da criacao (emprestimo depende das outras duas).
DROP TABLE IF EXISTS emprestimo;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS item;

-- ---------------------------------------------------------------------
-- 11. item
-- autor vale para livro e edicao para revista; por isso as duas aceitam NULL.
-- 'dvd' entra no CHECK por causa do teste de categoria nova (item 10).
-- ---------------------------------------------------------------------
CREATE TABLE item (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo      VARCHAR(10)  NOT NULL UNIQUE,
    titulo      VARCHAR(150) NOT NULL,
    tipo        VARCHAR(10)  NOT NULL CHECK (tipo IN ('livro', 'revista', 'dvd')),
    autor       VARCHAR(120),
    edicao      VARCHAR(40),
    disponivel  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- 12. usuario
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    tipo          VARCHAR(10)  NOT NULL CHECK (tipo IN ('aluno', 'professor')),
    limite_itens  INTEGER      NOT NULL CHECK (limite_itens > 0)
);

-- ---------------------------------------------------------------------
-- 13. emprestimo  (criada por ultimo: depende de item e usuario - item 20)
-- data_devolucao fica NULL enquanto o item nao voltar (item 19).
-- ---------------------------------------------------------------------
CREATE TABLE emprestimo (
    id                       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id                  INTEGER      NOT NULL REFERENCES item(id),
    usuario_id               INTEGER      NOT NULL REFERENCES usuario(id),
    data_retirada            DATE         NOT NULL DEFAULT CURRENT_DATE,
    data_devolucao_prevista  DATE         NOT NULL,
    data_devolucao           DATE,
    valor_multa              NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (valor_multa >= 0),
    CHECK (data_devolucao_prevista >= data_retirada),
    CHECK (data_devolucao IS NULL OR data_devolucao >= data_retirada)
);

-- Um mesmo item nao pode ter dois emprestimos em aberto ao mesmo tempo.
CREATE UNIQUE INDEX uq_emprestimo_item_aberto
    ON emprestimo (item_id)
    WHERE data_devolucao IS NULL;

-- =====================================================================
-- 15. Dados de teste
-- =====================================================================
INSERT INTO item (codigo, titulo, tipo, autor, edicao, disponivel) VALUES
    ('L01', 'Dom Casmurro',      'livro',   'Machado de Assis', NULL,       FALSE), -- emprestado (em aberto)
    ('L02', 'O Cortiço',         'livro',   'Aluísio Azevedo',  NULL,       TRUE),
    ('L03', 'Vidas Secas',       'livro',   'Graciliano Ramos', NULL,       TRUE),
    ('R01', 'Superinteressante', 'revista', NULL,               'Out/2026', TRUE),  -- ja devolvida
    ('R02', 'Ciência Hoje',      'revista', NULL,               'Set/2026', TRUE),
    ('D01', 'Central do Brasil', 'dvd',     NULL,               NULL,       TRUE);

INSERT INTO usuario (nome, tipo, limite_itens) VALUES
    ('Ana',    'aluno',     3),
    ('Carlos', 'professor', 5);

-- Emprestimo EM ABERTO: Ana com Dom Casmurro (livro: 14 dias de prazo).
INSERT INTO emprestimo (item_id, usuario_id, data_retirada, data_devolucao_prevista)
VALUES (
    (SELECT id FROM item    WHERE codigo = 'L01'),
    (SELECT id FROM usuario WHERE nome   = 'Ana'),
    DATE '2026-10-25',
    DATE '2026-10-25' + 14
);

-- Emprestimo JA DEVOLVIDO: Carlos com Superinteressante (revista: 7 dias),
-- devolvida 3 dias depois do prazo -> 3 x R$ 1,00 = R$ 3,00.
INSERT INTO emprestimo (item_id, usuario_id, data_retirada, data_devolucao_prevista,
                        data_devolucao, valor_multa)
VALUES (
    (SELECT id FROM item    WHERE codigo = 'R01'),
    (SELECT id FROM usuario WHERE nome   = 'Carlos'),
    DATE '2026-10-10',
    DATE '2026-10-10' + 7,
    DATE '2026-10-20',
    3.00
);

-- =====================================================================
-- Consultas para conferir
-- =====================================================================
-- SELECT * FROM item ORDER BY codigo;
-- SELECT * FROM usuario;
-- SELECT u.nome, i.titulo, e.data_retirada, e.data_devolucao_prevista,
--        e.data_devolucao, e.valor_multa,
--        CASE WHEN e.data_devolucao IS NULL THEN 'em aberto' ELSE 'devolvido' END AS situacao
--   FROM emprestimo e
--   JOIN item    i ON i.id = e.item_id
--   JOIN usuario u ON u.id = e.usuario_id
--  ORDER BY e.data_retirada;
