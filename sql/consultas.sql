-- =====================================================================
-- Biblioteca de bairro - Exercicio 3 (consultas SQL)
-- Execute no banco "biblioteca" depois de rodar biblioteca.sql (Exercicio 2).
-- No pgAdmin: selecione uma consulta e aperte F5 para executar so ela.
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. Todo o acervo: codigo, titulo, tipo e disponibilidade.
--    LEFT JOIN com o emprestimo EM ABERTO (e com o usuario) para mostrar,
--    na mesma linha, com quem o item esta. LEFT para nao perder os itens
--    disponiveis, que nao tem emprestimo em aberto.
-- ---------------------------------------------------------------------
SELECT i.codigo,
       i.titulo,
       i.tipo,
       i.disponivel,
       CASE WHEN i.disponivel THEN 'disponível'
            ELSE 'emprestado para ' || u.nome
       END AS situacao
  FROM item i
  LEFT JOIN emprestimo e ON e.item_id = i.id
                        AND e.data_devolucao IS NULL
  LEFT JOIN usuario u    ON u.id = e.usuario_id
 ORDER BY i.codigo;


-- ---------------------------------------------------------------------
-- 2. Emprestimos em aberto, com o nome do usuario e o titulo do item.
--    Em aberto = data_devolucao IS NULL (nunca "= NULL", que nunca e verdadeiro).
-- ---------------------------------------------------------------------
SELECT e.id                      AS emprestimo,
       u.nome                    AS usuario,
       i.titulo                  AS item,
       e.data_retirada,
       e.data_devolucao_prevista
  FROM emprestimo e
  JOIN usuario u ON u.id = e.usuario_id
  JOIN item    i ON i.id = e.item_id
 WHERE e.data_devolucao IS NULL
 ORDER BY e.data_devolucao_prevista;


-- ---------------------------------------------------------------------
-- 3. Total de multas acumuladas por usuario.
--    GROUP BY por usuario + SUM(valor_multa).
--    LEFT JOIN + COALESCE para quem nunca pegou nada aparecer com R$ 0,00.
-- ---------------------------------------------------------------------
SELECT u.nome,
       u.tipo,
       COALESCE(SUM(e.valor_multa), 0) AS total_multas
  FROM usuario u
  LEFT JOIN emprestimo e ON e.usuario_id = u.id
 GROUP BY u.id, u.nome, u.tipo
 ORDER BY total_multas DESC, u.nome;


-- ---------------------------------------------------------------------
-- 4. Itens que nunca foram emprestados.
--    O LEFT JOIN mantem todos os itens; quem nunca apareceu em emprestimo
--    fica com as colunas de "e" nulas. Testamos IS NULL na chave de emprestimo.
-- ---------------------------------------------------------------------
SELECT i.codigo,
       i.titulo,
       i.tipo
  FROM item i
  LEFT JOIN emprestimo e ON e.item_id = i.id
 WHERE e.id IS NULL
 ORDER BY i.codigo;
