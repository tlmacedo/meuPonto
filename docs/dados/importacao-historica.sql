-- ============================================================
-- SCRIPT DE IMPORTAÇÃO DE DADOS HISTÓRICOS
-- Projeto: MeuPonto
-- Autor: Thiago Macedo
-- Data: 02/03/2026 17:12:14 (INSERT OR REPLACE para atualizar dados existentes)
-- ============================================================
--
-- ATENÇÃO: Execute os comandos nesta ordem:
-- 1. Emprego
-- 2. Versões de Jornada
-- 3. Horários por Dia da Semana
-- 4. Feriados
-- 5. Ausências
-- 6. Pontos
--
-- ============================================================


-- ===== EMPREGO =====
INSERT OR REPLACE INTO empregos (
    id, nome, descricao, dataInicioTrabalho, ativo, arquivado, ordem, criadoEm, atualizadoEm
) VALUES (
    1,
    'SIDIA INSTITUTO DE CIENCIA E TECNOLOGIA',
    'DESENVOLVEDOR DE SW III',
    '2025-08-11',
    1,
    0,
    0,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);



-- ===== VERSÃO JORNADA 1: Jornada 2025 =====
INSERT OR REPLACE INTO versoes_jornada (
    id, empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente,
    jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos,
    toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm
) VALUES (
    1,
    1,
    '2025-08-11',
    '2025-12-31',
    'Jornada 2025',
    1,
    0,
    600,
    660,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- ===== VERSÃO JORNADA 2: Jornada 2026 =====
INSERT OR REPLACE INTO versoes_jornada (
    id, empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente,
    jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos,
    toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm
) VALUES (
    2,
    1,
    '2026-01-01',
    NULL,
    'Jornada 2026',
    2,
    1,
    600,
    660,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- ===== HORÁRIOS JORNADA 2025 =====

INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    100,
    1,
    1,
    1,
    1,
    490,
    '08:00',
    '12:30',
    '13:30',
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    101,
    1,
    1,
    2,
    1,
    490,
    '08:00',
    '12:30',
    '13:30',
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    102,
    1,
    1,
    3,
    1,
    490,
    '08:00',
    '12:30',
    '13:30',
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    103,
    1,
    1,
    4,
    1,
    490,
    '08:00',
    '12:30',
    '13:30',
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    104,
    1,
    1,
    5,
    1,
    490,
    '08:00',
    '12:30',
    '13:30',
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    105,
    1,
    1,
    6,
    0,
    0,
    '08:00',
    NULL,
    NULL,
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    106,
    1,
    1,
    7,
    0,
    0,
    '08:00',
    NULL,
    NULL,
    '17:10',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- ===== HORÁRIOS JORNADA 2026 =====

INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    107,
    1,
    2,
    1,
    1,
    492,
    '08:00',
    '12:30',
    '13:30',
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    108,
    1,
    2,
    2,
    1,
    492,
    '08:00',
    '12:30',
    '13:30',
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    109,
    1,
    2,
    3,
    1,
    492,
    '08:00',
    '12:30',
    '13:30',
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    110,
    1,
    2,
    4,
    1,
    492,
    '08:00',
    '12:30',
    '13:30',
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    111,
    1,
    2,
    5,
    1,
    492,
    '08:00',
    '12:30',
    '13:30',
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    112,
    1,
    2,
    6,
    0,
    0,
    '08:00',
    NULL,
    NULL,
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    113,
    1,
    2,
    7,
    0,
    0,
    '08:00',
    NULL,
    NULL,
    '17:12',
    60,
    0,
    0,
    20,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- ===== FERIADOS =====

-- FERIADO: DIA NOSSA SRA. APARECIDA - 2025-10-12
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6000,
    'DIA NOSSA SRA. APARECIDA',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-10-12',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: PROCLAMACAO DA REPUBLICA - 2025-10-15
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6001,
    'PROCLAMACAO DA REPUBLICA',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-10-15',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: ANIVERSARIO DE MANAUS - 2025-10-24
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6002,
    'ANIVERSARIO DE MANAUS',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-10-24',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: FINADOS - 2025-11-02
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6003,
    'FINADOS',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-11-02',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: DIA DA CONSCIENCIA NEGRA - 2025-11-15
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6004,
    'DIA DA CONSCIENCIA NEGRA',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-11-15',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: N. SRA CONC. - 2025-12-08
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6005,
    'N. SRA CONC.',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-12-08',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: NATAL - 2025-12-24
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6006,
    'NATAL',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-12-24',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- FERIADO: ANO NOVO - 2025-12-31
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    6007,
    'ANO NOVO',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '2025-12-31',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- ===== AUSÊNCIAS =====

-- AUSÊNCIA: FERIAS - 2025-09-22 a 2025-09-26
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5000,
    1,
    'FERIAS',
    NULL,
    '2025-09-22',
    '2025-09-26',
    NULL,
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-11 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5001,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-11',
    '2026-02-11',
    'D.S.R.',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-12 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5002,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-12',
    '2026-02-12',
    'D.S.R.',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-14 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5003,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-14',
    '2026-02-14',
    'Compensado',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-15 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5004,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-15',
    '2026-02-15',
    'D.S.R.',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-16 a 2026-02-18
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5005,
    1,
    'FOLGA',
    NULL,
    '2026-02-16',
    '2026-02-18',
    'CARNAVAL',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-21 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5006,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-21',
    '2026-02-21',
    'Compensado',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-22 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5007,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-22',
    '2026-02-22',
    'D.S.R.',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-02-28 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5008,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-02-28',
    '2026-02-28',
    'Compensado',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- AUSÊNCIA: FOLGA - 2026-03-01 
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    5009,
    1,
    'FOLGA',
    'COMPENSACAO',
    '2026-03-01',
    '2026-03-01',
    'D.S.R.',
    NULL,
    1,
    '2026-03-02 17:12:14',
    '2026-03-02 17:12:14'
);


-- ===== REGISTROS DE PONTO =====

INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10000,
    1,
    '2025-08-11 08:55',
    '2025-08-11',
    '08:55',
    '08:55',
    NULL,
    0,
    '2025-08-11 08:55',
    '2025-08-11 08:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10001,
    1,
    '2025-08-11 12:31',
    '2025-08-11',
    '12:31',
    '12:31',
    NULL,
    0,
    '2025-08-11 12:31',
    '2025-08-11 12:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10002,
    1,
    '2025-08-11 13:46',
    '2025-08-11',
    '13:46',
    '13:46',
    NULL,
    0,
    '2025-08-11 13:46',
    '2025-08-11 13:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10003,
    1,
    '2025-08-11 18:08',
    '2025-08-11',
    '18:08',
    '18:08',
    NULL,
    0,
    '2025-08-11 18:08',
    '2025-08-11 18:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10010,
    1,
    '2025-08-12 08:40',
    '2025-08-12',
    '08:40',
    '08:40',
    NULL,
    0,
    '2025-08-12 08:40',
    '2025-08-12 08:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10011,
    1,
    '2025-08-12 12:32',
    '2025-08-12',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-08-12 12:32',
    '2025-08-12 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10012,
    1,
    '2025-08-12 13:50',
    '2025-08-12',
    '13:50',
    '13:50',
    NULL,
    0,
    '2025-08-12 13:50',
    '2025-08-12 13:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10013,
    1,
    '2025-08-12 14:54',
    '2025-08-12',
    '14:54',
    '14:54',
    NULL,
    0,
    '2025-08-12 14:54',
    '2025-08-12 14:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10014,
    1,
    '2025-08-12 16:54',
    '2025-08-12',
    '16:54',
    '16:54',
    NULL,
    0,
    '2025-08-12 16:54',
    '2025-08-12 16:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10015,
    1,
    '2025-08-12 18:00',
    '2025-08-12',
    '18:00',
    '18:00',
    NULL,
    0,
    '2025-08-12 18:00',
    '2025-08-12 18:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10020,
    1,
    '2025-08-13 08:43',
    '2025-08-13',
    '08:43',
    '08:43',
    NULL,
    0,
    '2025-08-13 08:43',
    '2025-08-13 08:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10021,
    1,
    '2025-08-13 12:13',
    '2025-08-13',
    '12:13',
    '12:13',
    NULL,
    0,
    '2025-08-13 12:13',
    '2025-08-13 12:13'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10022,
    1,
    '2025-08-13 13:32',
    '2025-08-13',
    '13:32',
    '13:32',
    NULL,
    0,
    '2025-08-13 13:32',
    '2025-08-13 13:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10023,
    1,
    '2025-08-13 17:36',
    '2025-08-13',
    '17:36',
    '17:36',
    NULL,
    0,
    '2025-08-13 17:36',
    '2025-08-13 17:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10030,
    1,
    '2025-08-14 08:22',
    '2025-08-14',
    '08:22',
    '08:22',
    NULL,
    0,
    '2025-08-14 08:22',
    '2025-08-14 08:22'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10031,
    1,
    '2025-08-14 12:28',
    '2025-08-14',
    '12:28',
    '12:28',
    NULL,
    0,
    '2025-08-14 12:28',
    '2025-08-14 12:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10032,
    1,
    '2025-08-14 13:37',
    '2025-08-14',
    '13:37',
    '13:37',
    NULL,
    0,
    '2025-08-14 13:37',
    '2025-08-14 13:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10033,
    1,
    '2025-08-14 17:34',
    '2025-08-14',
    '17:34',
    '17:34',
    NULL,
    0,
    '2025-08-14 17:34',
    '2025-08-14 17:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10040,
    1,
    '2025-08-15 08:21',
    '2025-08-15',
    '08:21',
    '08:21',
    NULL,
    0,
    '2025-08-15 08:21',
    '2025-08-15 08:21'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10041,
    1,
    '2025-08-15 12:44',
    '2025-08-15',
    '12:44',
    '12:44',
    NULL,
    0,
    '2025-08-15 12:44',
    '2025-08-15 12:44'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10042,
    1,
    '2025-08-15 13:57',
    '2025-08-15',
    '13:57',
    '13:57',
    NULL,
    0,
    '2025-08-15 13:57',
    '2025-08-15 13:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10043,
    1,
    '2025-08-15 16:53',
    '2025-08-15',
    '16:53',
    '16:53',
    NULL,
    0,
    '2025-08-15 16:53',
    '2025-08-15 16:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10050,
    1,
    '2025-08-18 07:54',
    '2025-08-18',
    '07:54',
    '07:54',
    NULL,
    0,
    '2025-08-18 07:54',
    '2025-08-18 07:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10051,
    1,
    '2025-08-18 12:30',
    '2025-08-18',
    '12:30',
    '12:30',
    NULL,
    0,
    '2025-08-18 12:30',
    '2025-08-18 12:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10052,
    1,
    '2025-08-18 13:42',
    '2025-08-18',
    '13:42',
    '13:42',
    NULL,
    0,
    '2025-08-18 13:42',
    '2025-08-18 13:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10053,
    1,
    '2025-08-18 17:28',
    '2025-08-18',
    '17:28',
    '17:28',
    NULL,
    0,
    '2025-08-18 17:28',
    '2025-08-18 17:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10060,
    1,
    '2025-08-19 08:16',
    '2025-08-19',
    '08:16',
    '08:16',
    NULL,
    0,
    '2025-08-19 08:16',
    '2025-08-19 08:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10061,
    1,
    '2025-08-19 12:29',
    '2025-08-19',
    '12:29',
    '12:29',
    NULL,
    0,
    '2025-08-19 12:29',
    '2025-08-19 12:29'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10062,
    1,
    '2025-08-19 13:45',
    '2025-08-19',
    '13:45',
    '13:45',
    NULL,
    0,
    '2025-08-19 13:45',
    '2025-08-19 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10063,
    1,
    '2025-08-19 18:11',
    '2025-08-19',
    '18:11',
    '18:11',
    NULL,
    0,
    '2025-08-19 18:11',
    '2025-08-19 18:11'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10070,
    1,
    '2025-08-20 09:00',
    '2025-08-20',
    '09:00',
    '09:00',
    NULL,
    0,
    '2025-08-20 09:00',
    '2025-08-20 09:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10071,
    1,
    '2025-08-20 12:30',
    '2025-08-20',
    '12:30',
    '12:30',
    NULL,
    0,
    '2025-08-20 12:30',
    '2025-08-20 12:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10072,
    1,
    '2025-08-20 13:47',
    '2025-08-20',
    '13:47',
    '13:47',
    NULL,
    0,
    '2025-08-20 13:47',
    '2025-08-20 13:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10073,
    1,
    '2025-08-20 17:42',
    '2025-08-20',
    '17:42',
    '17:42',
    NULL,
    0,
    '2025-08-20 17:42',
    '2025-08-20 17:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10080,
    1,
    '2025-08-21 08:16',
    '2025-08-21',
    '08:16',
    '08:16',
    NULL,
    0,
    '2025-08-21 08:16',
    '2025-08-21 08:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10081,
    1,
    '2025-08-21 12:40',
    '2025-08-21',
    '12:40',
    '12:40',
    NULL,
    0,
    '2025-08-21 12:40',
    '2025-08-21 12:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10082,
    1,
    '2025-08-21 13:58',
    '2025-08-21',
    '13:58',
    '13:58',
    NULL,
    0,
    '2025-08-21 13:58',
    '2025-08-21 13:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10083,
    1,
    '2025-08-21 18:10',
    '2025-08-21',
    '18:10',
    '18:10',
    NULL,
    0,
    '2025-08-21 18:10',
    '2025-08-21 18:10'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10090,
    1,
    '2025-08-22 08:22',
    '2025-08-22',
    '08:22',
    '08:22',
    NULL,
    0,
    '2025-08-22 08:22',
    '2025-08-22 08:22'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10091,
    1,
    '2025-08-22 12:16',
    '2025-08-22',
    '12:16',
    '12:16',
    NULL,
    0,
    '2025-08-22 12:16',
    '2025-08-22 12:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10092,
    1,
    '2025-08-22 13:31',
    '2025-08-22',
    '13:31',
    '13:31',
    NULL,
    0,
    '2025-08-22 13:31',
    '2025-08-22 13:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10093,
    1,
    '2025-08-22 17:12',
    '2025-08-22',
    '17:12',
    '17:12',
    NULL,
    0,
    '2025-08-22 17:12',
    '2025-08-22 17:12'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10100,
    1,
    '2025-08-25 08:10',
    '2025-08-25',
    '08:10',
    '08:10',
    NULL,
    0,
    '2025-08-25 08:10',
    '2025-08-25 08:10'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10101,
    1,
    '2025-08-25 12:37',
    '2025-08-25',
    '12:37',
    '12:37',
    NULL,
    0,
    '2025-08-25 12:37',
    '2025-08-25 12:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10102,
    1,
    '2025-08-25 13:49',
    '2025-08-25',
    '13:49',
    '13:49',
    NULL,
    0,
    '2025-08-25 13:49',
    '2025-08-25 13:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10103,
    1,
    '2025-08-25 18:03',
    '2025-08-25',
    '18:03',
    '18:03',
    NULL,
    0,
    '2025-08-25 18:03',
    '2025-08-25 18:03'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10110,
    1,
    '2025-08-26 08:26',
    '2025-08-26',
    '08:26',
    '08:26',
    NULL,
    0,
    '2025-08-26 08:26',
    '2025-08-26 08:26'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10111,
    1,
    '2025-08-26 12:31',
    '2025-08-26',
    '12:31',
    '12:31',
    NULL,
    0,
    '2025-08-26 12:31',
    '2025-08-26 12:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10112,
    1,
    '2025-08-26 13:48',
    '2025-08-26',
    '13:48',
    '13:48',
    NULL,
    0,
    '2025-08-26 13:48',
    '2025-08-26 13:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10113,
    1,
    '2025-08-26 17:43',
    '2025-08-26',
    '17:43',
    '17:43',
    NULL,
    0,
    '2025-08-26 17:43',
    '2025-08-26 17:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10120,
    1,
    '2025-08-27 08:35',
    '2025-08-27',
    '08:35',
    '08:35',
    NULL,
    0,
    '2025-08-27 08:35',
    '2025-08-27 08:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10121,
    1,
    '2025-08-27 12:32',
    '2025-08-27',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-08-27 12:32',
    '2025-08-27 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10122,
    1,
    '2025-08-27 13:48',
    '2025-08-27',
    '13:48',
    '13:48',
    NULL,
    0,
    '2025-08-27 13:48',
    '2025-08-27 13:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10123,
    1,
    '2025-08-27 18:36',
    '2025-08-27',
    '18:36',
    '18:36',
    NULL,
    0,
    '2025-08-27 18:36',
    '2025-08-27 18:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10130,
    1,
    '2025-08-28 08:44',
    '2025-08-28',
    '08:44',
    '08:44',
    NULL,
    0,
    '2025-08-28 08:44',
    '2025-08-28 08:44'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10131,
    1,
    '2025-08-28 12:38',
    '2025-08-28',
    '12:38',
    '12:38',
    NULL,
    0,
    '2025-08-28 12:38',
    '2025-08-28 12:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10132,
    1,
    '2025-08-28 13:54',
    '2025-08-28',
    '13:54',
    '13:54',
    NULL,
    0,
    '2025-08-28 13:54',
    '2025-08-28 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10133,
    1,
    '2025-08-28 18:01',
    '2025-08-28',
    '18:01',
    '18:01',
    NULL,
    0,
    '2025-08-28 18:01',
    '2025-08-28 18:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10140,
    1,
    '2025-08-29 08:40',
    '2025-08-29',
    '08:40',
    '08:40',
    NULL,
    0,
    '2025-08-29 08:40',
    '2025-08-29 08:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10141,
    1,
    '2025-08-29 12:39',
    '2025-08-29',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-08-29 12:39',
    '2025-08-29 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10142,
    1,
    '2025-08-29 13:58',
    '2025-08-29',
    '13:58',
    '13:58',
    NULL,
    0,
    '2025-08-29 13:58',
    '2025-08-29 13:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10143,
    1,
    '2025-08-29 18:20',
    '2025-08-29',
    '18:20',
    '18:20',
    NULL,
    0,
    '2025-08-29 18:20',
    '2025-08-29 18:20'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10150,
    1,
    '2025-09-01 08:14',
    '2025-09-01',
    '08:14',
    '08:14',
    NULL,
    0,
    '2025-09-01 08:14',
    '2025-09-01 08:14'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10151,
    1,
    '2025-09-01 12:43',
    '2025-09-01',
    '12:43',
    '12:43',
    NULL,
    0,
    '2025-09-01 12:43',
    '2025-09-01 12:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10152,
    1,
    '2025-09-01 13:58',
    '2025-09-01',
    '13:58',
    '13:58',
    NULL,
    0,
    '2025-09-01 13:58',
    '2025-09-01 13:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10153,
    1,
    '2025-09-01 18:17',
    '2025-09-01',
    '18:17',
    '18:17',
    NULL,
    0,
    '2025-09-01 18:17',
    '2025-09-01 18:17'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10160,
    1,
    '2025-09-02 08:19',
    '2025-09-02',
    '08:19',
    '08:19',
    NULL,
    0,
    '2025-09-02 08:19',
    '2025-09-02 08:19'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10161,
    1,
    '2025-09-02 12:37',
    '2025-09-02',
    '12:37',
    '12:37',
    NULL,
    0,
    '2025-09-02 12:37',
    '2025-09-02 12:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10162,
    1,
    '2025-09-02 13:55',
    '2025-09-02',
    '13:55',
    '13:55',
    NULL,
    0,
    '2025-09-02 13:55',
    '2025-09-02 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10163,
    1,
    '2025-09-02 18:05',
    '2025-09-02',
    '18:05',
    '18:05',
    NULL,
    0,
    '2025-09-02 18:05',
    '2025-09-02 18:05'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10170,
    1,
    '2025-09-03 08:37',
    '2025-09-03',
    '08:37',
    '08:37',
    NULL,
    0,
    '2025-09-03 08:37',
    '2025-09-03 08:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10171,
    1,
    '2025-09-03 12:50',
    '2025-09-03',
    '12:50',
    '12:50',
    NULL,
    0,
    '2025-09-03 12:50',
    '2025-09-03 12:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10172,
    1,
    '2025-09-03 14:02',
    '2025-09-03',
    '14:02',
    '14:02',
    NULL,
    0,
    '2025-09-03 14:02',
    '2025-09-03 14:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10173,
    1,
    '2025-09-03 17:49',
    '2025-09-03',
    '17:49',
    '17:49',
    NULL,
    0,
    '2025-09-03 17:49',
    '2025-09-03 17:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10180,
    1,
    '2025-09-04 08:55',
    '2025-09-04',
    '08:55',
    '08:55',
    NULL,
    0,
    '2025-09-04 08:55',
    '2025-09-04 08:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10181,
    1,
    '2025-09-04 12:30',
    '2025-09-04',
    '12:30',
    '12:30',
    NULL,
    0,
    '2025-09-04 12:30',
    '2025-09-04 12:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10182,
    1,
    '2025-09-04 13:48',
    '2025-09-04',
    '13:48',
    '13:48',
    NULL,
    0,
    '2025-09-04 13:48',
    '2025-09-04 13:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10183,
    1,
    '2025-09-04 18:54',
    '2025-09-04',
    '18:54',
    '18:54',
    NULL,
    0,
    '2025-09-04 18:54',
    '2025-09-04 18:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10190,
    1,
    '2025-09-08 08:27',
    '2025-09-08',
    '08:27',
    '08:27',
    NULL,
    0,
    '2025-09-08 08:27',
    '2025-09-08 08:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10191,
    1,
    '2025-09-08 12:32',
    '2025-09-08',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-09-08 12:32',
    '2025-09-08 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10192,
    1,
    '2025-09-08 13:52',
    '2025-09-08',
    '13:52',
    '13:52',
    NULL,
    0,
    '2025-09-08 13:52',
    '2025-09-08 13:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10193,
    1,
    '2025-09-08 14:25',
    '2025-09-08',
    '14:25',
    '14:25',
    NULL,
    0,
    '2025-09-08 14:25',
    '2025-09-08 14:25'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10194,
    1,
    '2025-09-08 15:11',
    '2025-09-08',
    '15:11',
    '15:11',
    NULL,
    0,
    '2025-09-08 15:11',
    '2025-09-08 15:11'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10195,
    1,
    '2025-09-08 18:20',
    '2025-09-08',
    '18:20',
    '18:20',
    NULL,
    0,
    '2025-09-08 18:20',
    '2025-09-08 18:20'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10200,
    1,
    '2025-09-09 08:32',
    '2025-09-09',
    '08:32',
    '08:32',
    NULL,
    0,
    '2025-09-09 08:32',
    '2025-09-09 08:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10201,
    1,
    '2025-09-09 12:40',
    '2025-09-09',
    '12:40',
    '12:40',
    NULL,
    0,
    '2025-09-09 12:40',
    '2025-09-09 12:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10202,
    1,
    '2025-09-09 13:59',
    '2025-09-09',
    '13:59',
    '13:59',
    NULL,
    0,
    '2025-09-09 13:59',
    '2025-09-09 13:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10203,
    1,
    '2025-09-09 18:21',
    '2025-09-09',
    '18:21',
    '18:21',
    NULL,
    0,
    '2025-09-09 18:21',
    '2025-09-09 18:21'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10210,
    1,
    '2025-09-10 08:32',
    '2025-09-10',
    '08:32',
    '08:32',
    NULL,
    0,
    '2025-09-10 08:32',
    '2025-09-10 08:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10211,
    1,
    '2025-09-10 12:39',
    '2025-09-10',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-09-10 12:39',
    '2025-09-10 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10212,
    1,
    '2025-09-10 13:51',
    '2025-09-10',
    '13:51',
    '13:51',
    NULL,
    0,
    '2025-09-10 13:51',
    '2025-09-10 13:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10213,
    1,
    '2025-09-10 18:23',
    '2025-09-10',
    '18:23',
    '18:23',
    NULL,
    0,
    '2025-09-10 18:23',
    '2025-09-10 18:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10220,
    1,
    '2025-09-11 08:41',
    '2025-09-11',
    '08:41',
    '08:41',
    NULL,
    0,
    '2025-09-11 08:41',
    '2025-09-11 08:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10221,
    1,
    '2025-09-11 12:27',
    '2025-09-11',
    '12:27',
    '12:27',
    NULL,
    0,
    '2025-09-11 12:27',
    '2025-09-11 12:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10222,
    1,
    '2025-09-11 13:39',
    '2025-09-11',
    '13:39',
    '13:39',
    NULL,
    0,
    '2025-09-11 13:39',
    '2025-09-11 13:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10223,
    1,
    '2025-09-11 18:15',
    '2025-09-11',
    '18:15',
    '18:15',
    NULL,
    0,
    '2025-09-11 18:15',
    '2025-09-11 18:15'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10230,
    1,
    '2025-09-12 08:11',
    '2025-09-12',
    '08:11',
    '08:11',
    NULL,
    0,
    '2025-09-12 08:11',
    '2025-09-12 08:11'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10231,
    1,
    '2025-09-12 12:23',
    '2025-09-12',
    '12:23',
    '12:23',
    NULL,
    0,
    '2025-09-12 12:23',
    '2025-09-12 12:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10232,
    1,
    '2025-09-12 13:43',
    '2025-09-12',
    '13:43',
    '13:43',
    NULL,
    0,
    '2025-09-12 13:43',
    '2025-09-12 13:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10233,
    1,
    '2025-09-12 14:05',
    '2025-09-12',
    '14:05',
    '14:05',
    NULL,
    0,
    '2025-09-12 14:05',
    '2025-09-12 14:05'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10234,
    1,
    '2025-09-12 15:27',
    '2025-09-12',
    '15:27',
    '15:27',
    NULL,
    0,
    '2025-09-12 15:27',
    '2025-09-12 15:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10235,
    1,
    '2025-09-12 18:16',
    '2025-09-12',
    '18:16',
    '18:16',
    NULL,
    0,
    '2025-09-12 18:16',
    '2025-09-12 18:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10240,
    1,
    '2025-09-15 08:20',
    '2025-09-15',
    '08:20',
    '08:20',
    NULL,
    0,
    '2025-09-15 08:20',
    '2025-09-15 08:20'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10241,
    1,
    '2025-09-15 12:36',
    '2025-09-15',
    '12:36',
    '12:36',
    NULL,
    0,
    '2025-09-15 12:36',
    '2025-09-15 12:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10242,
    1,
    '2025-09-15 13:55',
    '2025-09-15',
    '13:55',
    '13:55',
    NULL,
    0,
    '2025-09-15 13:55',
    '2025-09-15 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10243,
    1,
    '2025-09-15 14:08',
    '2025-09-15',
    '14:08',
    '14:08',
    NULL,
    0,
    '2025-09-15 14:08',
    '2025-09-15 14:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10244,
    1,
    '2025-09-15 15:04',
    '2025-09-15',
    '15:04',
    '15:04',
    NULL,
    0,
    '2025-09-15 15:04',
    '2025-09-15 15:04'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10245,
    1,
    '2025-09-15 18:43',
    '2025-09-15',
    '18:43',
    '18:43',
    NULL,
    0,
    '2025-09-15 18:43',
    '2025-09-15 18:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10250,
    1,
    '2025-09-16 08:19',
    '2025-09-16',
    '08:19',
    '08:19',
    NULL,
    0,
    '2025-09-16 08:19',
    '2025-09-16 08:19'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10251,
    1,
    '2025-09-16 12:23',
    '2025-09-16',
    '12:23',
    '12:23',
    NULL,
    0,
    '2025-09-16 12:23',
    '2025-09-16 12:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10252,
    1,
    '2025-09-16 13:38',
    '2025-09-16',
    '13:38',
    '13:38',
    NULL,
    0,
    '2025-09-16 13:38',
    '2025-09-16 13:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10253,
    1,
    '2025-09-16 17:43',
    '2025-09-16',
    '17:43',
    '17:43',
    NULL,
    0,
    '2025-09-16 17:43',
    '2025-09-16 17:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10260,
    1,
    '2025-09-17 08:13',
    '2025-09-17',
    '08:13',
    '08:13',
    NULL,
    0,
    '2025-09-17 08:13',
    '2025-09-17 08:13'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10261,
    1,
    '2025-09-17 12:39',
    '2025-09-17',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-09-17 12:39',
    '2025-09-17 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10262,
    1,
    '2025-09-17 13:54',
    '2025-09-17',
    '13:54',
    '13:54',
    NULL,
    0,
    '2025-09-17 13:54',
    '2025-09-17 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10263,
    1,
    '2025-09-17 17:55',
    '2025-09-17',
    '17:55',
    '17:55',
    NULL,
    0,
    '2025-09-17 17:55',
    '2025-09-17 17:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10270,
    1,
    '2025-09-18 08:12',
    '2025-09-18',
    '08:12',
    '08:12',
    NULL,
    0,
    '2025-09-18 08:12',
    '2025-09-18 08:12'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10271,
    1,
    '2025-09-18 12:30',
    '2025-09-18',
    '12:30',
    '12:30',
    NULL,
    0,
    '2025-09-18 12:30',
    '2025-09-18 12:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10272,
    1,
    '2025-09-18 13:49',
    '2025-09-18',
    '13:49',
    '13:49',
    NULL,
    0,
    '2025-09-18 13:49',
    '2025-09-18 13:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10273,
    1,
    '2025-09-18 16:26',
    '2025-09-18',
    '16:26',
    '16:26',
    NULL,
    0,
    '2025-09-18 16:26',
    '2025-09-18 16:26'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10280,
    1,
    '2025-09-19 07:58',
    '2025-09-19',
    '07:58',
    '07:58',
    NULL,
    0,
    '2025-09-19 07:58',
    '2025-09-19 07:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10281,
    1,
    '2025-09-19 12:27',
    '2025-09-19',
    '12:27',
    '12:27',
    NULL,
    0,
    '2025-09-19 12:27',
    '2025-09-19 12:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10282,
    1,
    '2025-09-19 13:45',
    '2025-09-19',
    '13:45',
    '13:45',
    NULL,
    0,
    '2025-09-19 13:45',
    '2025-09-19 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10283,
    1,
    '2025-09-19 16:35',
    '2025-09-19',
    '16:35',
    '16:35',
    NULL,
    0,
    '2025-09-19 16:35',
    '2025-09-19 16:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10290,
    1,
    '2025-09-29 07:37',
    '2025-09-29',
    '07:37',
    '07:37',
    NULL,
    0,
    '2025-09-29 07:37',
    '2025-09-29 07:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10291,
    1,
    '2025-09-29 12:38',
    '2025-09-29',
    '12:38',
    '12:38',
    NULL,
    0,
    '2025-09-29 12:38',
    '2025-09-29 12:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10292,
    1,
    '2025-09-29 13:55',
    '2025-09-29',
    '13:55',
    '13:55',
    NULL,
    0,
    '2025-09-29 13:55',
    '2025-09-29 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10293,
    1,
    '2025-09-29 16:40',
    '2025-09-29',
    '16:40',
    '16:40',
    NULL,
    0,
    '2025-09-29 16:40',
    '2025-09-29 16:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10300,
    1,
    '2025-09-30 07:36',
    '2025-09-30',
    '07:36',
    '07:36',
    NULL,
    0,
    '2025-09-30 07:36',
    '2025-09-30 07:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10301,
    1,
    '2025-09-30 12:39',
    '2025-09-30',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-09-30 12:39',
    '2025-09-30 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10302,
    1,
    '2025-09-30 13:57',
    '2025-09-30',
    '13:57',
    '13:57',
    NULL,
    0,
    '2025-09-30 13:57',
    '2025-09-30 13:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10303,
    1,
    '2025-09-30 16:38',
    '2025-09-30',
    '16:38',
    '16:38',
    NULL,
    0,
    '2025-09-30 16:38',
    '2025-09-30 16:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10310,
    1,
    '2025-10-01 07:25',
    '2025-10-01',
    '07:25',
    '07:25',
    NULL,
    0,
    '2025-10-01 07:25',
    '2025-10-01 07:25'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10311,
    1,
    '2025-10-01 12:31',
    '2025-10-01',
    '12:31',
    '12:31',
    NULL,
    0,
    '2025-10-01 12:31',
    '2025-10-01 12:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10312,
    1,
    '2025-10-01 13:48',
    '2025-10-01',
    '13:48',
    '13:48',
    NULL,
    0,
    '2025-10-01 13:48',
    '2025-10-01 13:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10313,
    1,
    '2025-10-01 16:43',
    '2025-10-01',
    '16:43',
    '16:43',
    NULL,
    0,
    '2025-10-01 16:43',
    '2025-10-01 16:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10320,
    1,
    '2025-10-02 08:06',
    '2025-10-02',
    '08:06',
    '08:06',
    NULL,
    0,
    '2025-10-02 08:06',
    '2025-10-02 08:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10321,
    1,
    '2025-10-02 12:45',
    '2025-10-02',
    '12:45',
    '12:45',
    NULL,
    0,
    '2025-10-02 12:45',
    '2025-10-02 12:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10322,
    1,
    '2025-10-02 13:56',
    '2025-10-02',
    '13:56',
    '13:56',
    NULL,
    0,
    '2025-10-02 13:56',
    '2025-10-02 13:56'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10323,
    1,
    '2025-10-02 18:12',
    '2025-10-02',
    '18:12',
    '18:12',
    NULL,
    0,
    '2025-10-02 18:12',
    '2025-10-02 18:12'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10330,
    1,
    '2025-10-03 07:47',
    '2025-10-03',
    '07:47',
    '07:47',
    NULL,
    0,
    '2025-10-03 07:47',
    '2025-10-03 07:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10331,
    1,
    '2025-10-03 10:45',
    '2025-10-03',
    '10:45',
    '10:45',
    NULL,
    0,
    '2025-10-03 10:45',
    '2025-10-03 10:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10332,
    1,
    '2025-10-03 11:52',
    '2025-10-03',
    '11:52',
    '11:52',
    NULL,
    0,
    '2025-10-03 11:52',
    '2025-10-03 11:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10333,
    1,
    '2025-10-03 12:32',
    '2025-10-03',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-10-03 12:32',
    '2025-10-03 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10334,
    1,
    '2025-10-03 13:45',
    '2025-10-03',
    '13:45',
    '13:45',
    NULL,
    0,
    '2025-10-03 13:45',
    '2025-10-03 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10335,
    1,
    '2025-10-03 16:34',
    '2025-10-03',
    '16:34',
    '16:34',
    NULL,
    0,
    '2025-10-03 16:34',
    '2025-10-03 16:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10340,
    1,
    '2025-10-06 07:32',
    '2025-10-06',
    '07:32',
    '07:32',
    NULL,
    0,
    '2025-10-06 07:32',
    '2025-10-06 07:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10341,
    1,
    '2025-10-06 09:59',
    '2025-10-06',
    '09:59',
    '09:59',
    NULL,
    0,
    '2025-10-06 09:59',
    '2025-10-06 09:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10342,
    1,
    '2025-10-06 11:35',
    '2025-10-06',
    '11:35',
    '11:35',
    NULL,
    0,
    '2025-10-06 11:35',
    '2025-10-06 11:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10343,
    1,
    '2025-10-06 12:24',
    '2025-10-06',
    '12:24',
    '12:24',
    NULL,
    0,
    '2025-10-06 12:24',
    '2025-10-06 12:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10344,
    1,
    '2025-10-06 13:36',
    '2025-10-06',
    '13:36',
    '13:36',
    NULL,
    0,
    '2025-10-06 13:36',
    '2025-10-06 13:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10345,
    1,
    '2025-10-06 16:30',
    '2025-10-06',
    '16:30',
    '16:30',
    NULL,
    0,
    '2025-10-06 16:30',
    '2025-10-06 16:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10350,
    1,
    '2025-10-07 07:15',
    '2025-10-07',
    '07:15',
    '07:15',
    NULL,
    0,
    '2025-10-07 07:15',
    '2025-10-07 07:15'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10351,
    1,
    '2025-10-07 12:29',
    '2025-10-07',
    '12:29',
    '12:29',
    NULL,
    0,
    '2025-10-07 12:29',
    '2025-10-07 12:29'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10352,
    1,
    '2025-10-07 13:45',
    '2025-10-07',
    '13:45',
    '13:45',
    NULL,
    0,
    '2025-10-07 13:45',
    '2025-10-07 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10353,
    1,
    '2025-10-07 16:37',
    '2025-10-07',
    '16:37',
    '16:37',
    NULL,
    0,
    '2025-10-07 16:37',
    '2025-10-07 16:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10360,
    1,
    '2025-10-08 07:42',
    '2025-10-08',
    '07:42',
    '07:42',
    NULL,
    0,
    '2025-10-08 07:42',
    '2025-10-08 07:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10361,
    1,
    '2025-10-08 12:45',
    '2025-10-08',
    '12:45',
    '12:45',
    NULL,
    0,
    '2025-10-08 12:45',
    '2025-10-08 12:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10362,
    1,
    '2025-10-08 14:13',
    '2025-10-08',
    '14:13',
    '14:13',
    NULL,
    0,
    '2025-10-08 14:13',
    '2025-10-08 14:13'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10363,
    1,
    '2025-10-08 18:06',
    '2025-10-08',
    '18:06',
    '18:06',
    NULL,
    0,
    '2025-10-08 18:06',
    '2025-10-08 18:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10370,
    1,
    '2025-10-09 08:30',
    '2025-10-09',
    '08:30',
    '08:30',
    NULL,
    0,
    '2025-10-09 08:30',
    '2025-10-09 08:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10371,
    1,
    '2025-10-09 12:39',
    '2025-10-09',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-10-09 12:39',
    '2025-10-09 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10372,
    1,
    '2025-10-09 13:53',
    '2025-10-09',
    '13:53',
    '13:53',
    NULL,
    0,
    '2025-10-09 13:53',
    '2025-10-09 13:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10373,
    1,
    '2025-10-09 17:47',
    '2025-10-09',
    '17:47',
    '17:47',
    NULL,
    0,
    '2025-10-09 17:47',
    '2025-10-09 17:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10380,
    1,
    '2025-10-10 07:38',
    '2025-10-10',
    '07:38',
    '07:38',
    NULL,
    0,
    '2025-10-10 07:38',
    '2025-10-10 07:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10381,
    1,
    '2025-10-10 12:32',
    '2025-10-10',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-10-10 12:32',
    '2025-10-10 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10382,
    1,
    '2025-10-10 13:47',
    '2025-10-10',
    '13:47',
    '13:47',
    NULL,
    0,
    '2025-10-10 13:47',
    '2025-10-10 13:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10383,
    1,
    '2025-10-10 17:04',
    '2025-10-10',
    '17:04',
    '17:04',
    NULL,
    0,
    '2025-10-10 17:04',
    '2025-10-10 17:04'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10390,
    1,
    '2025-10-13 07:42',
    '2025-10-13',
    '07:42',
    '07:42',
    NULL,
    0,
    '2025-10-13 07:42',
    '2025-10-13 07:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10391,
    1,
    '2025-10-13 12:35',
    '2025-10-13',
    '12:35',
    '12:35',
    NULL,
    0,
    '2025-10-13 12:35',
    '2025-10-13 12:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10392,
    1,
    '2025-10-13 13:52',
    '2025-10-13',
    '13:52',
    '13:52',
    NULL,
    0,
    '2025-10-13 13:52',
    '2025-10-13 13:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10393,
    1,
    '2025-10-13 17:17',
    '2025-10-13',
    '17:17',
    '17:17',
    NULL,
    0,
    '2025-10-13 17:17',
    '2025-10-13 17:17'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10400,
    1,
    '2025-10-14 07:31',
    '2025-10-14',
    '07:31',
    '07:31',
    NULL,
    0,
    '2025-10-14 07:31',
    '2025-10-14 07:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10401,
    1,
    '2025-10-14 12:30',
    '2025-10-14',
    '12:30',
    '12:30',
    NULL,
    0,
    '2025-10-14 12:30',
    '2025-10-14 12:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10402,
    1,
    '2025-10-14 13:46',
    '2025-10-14',
    '13:46',
    '13:46',
    NULL,
    0,
    '2025-10-14 13:46',
    '2025-10-14 13:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10403,
    1,
    '2025-10-14 16:38',
    '2025-10-14',
    '16:38',
    '16:38',
    NULL,
    0,
    '2025-10-14 16:38',
    '2025-10-14 16:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10410,
    1,
    '2025-10-15 07:40',
    '2025-10-15',
    '07:40',
    '07:40',
    NULL,
    0,
    '2025-10-15 07:40',
    '2025-10-15 07:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10411,
    1,
    '2025-10-15 13:01',
    '2025-10-15',
    '13:01',
    '13:01',
    NULL,
    0,
    '2025-10-15 13:01',
    '2025-10-15 13:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10412,
    1,
    '2025-10-15 14:18',
    '2025-10-15',
    '14:18',
    '14:18',
    NULL,
    0,
    '2025-10-15 14:18',
    '2025-10-15 14:18'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10413,
    1,
    '2025-10-15 16:36',
    '2025-10-15',
    '16:36',
    '16:36',
    NULL,
    0,
    '2025-10-15 16:36',
    '2025-10-15 16:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10420,
    1,
    '2025-10-16 07:37',
    '2025-10-16',
    '07:37',
    '07:37',
    NULL,
    0,
    '2025-10-16 07:37',
    '2025-10-16 07:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10421,
    1,
    '2025-10-16 12:42',
    '2025-10-16',
    '12:42',
    '12:42',
    NULL,
    0,
    '2025-10-16 12:42',
    '2025-10-16 12:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10422,
    1,
    '2025-10-16 13:53',
    '2025-10-16',
    '13:53',
    '13:53',
    NULL,
    0,
    '2025-10-16 13:53',
    '2025-10-16 13:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10423,
    1,
    '2025-10-16 16:45',
    '2025-10-16',
    '16:45',
    '16:45',
    NULL,
    0,
    '2025-10-16 16:45',
    '2025-10-16 16:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10430,
    1,
    '2025-10-17 07:38',
    '2025-10-17',
    '07:38',
    '07:38',
    NULL,
    0,
    '2025-10-17 07:38',
    '2025-10-17 07:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10431,
    1,
    '2025-10-17 12:42',
    '2025-10-17',
    '12:42',
    '12:42',
    NULL,
    0,
    '2025-10-17 12:42',
    '2025-10-17 12:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10432,
    1,
    '2025-10-17 13:59',
    '2025-10-17',
    '13:59',
    '13:59',
    NULL,
    0,
    '2025-10-17 13:59',
    '2025-10-17 13:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10433,
    1,
    '2025-10-17 16:46',
    '2025-10-17',
    '16:46',
    '16:46',
    NULL,
    0,
    '2025-10-17 16:46',
    '2025-10-17 16:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10440,
    1,
    '2025-10-20 07:52',
    '2025-10-20',
    '07:52',
    '07:52',
    NULL,
    0,
    '2025-10-20 07:52',
    '2025-10-20 07:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10441,
    1,
    '2025-10-20 12:53',
    '2025-10-20',
    '12:53',
    '12:53',
    NULL,
    0,
    '2025-10-20 12:53',
    '2025-10-20 12:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10442,
    1,
    '2025-10-20 14:07',
    '2025-10-20',
    '14:07',
    '14:07',
    NULL,
    0,
    '2025-10-20 14:07',
    '2025-10-20 14:07'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10443,
    1,
    '2025-10-20 16:41',
    '2025-10-20',
    '16:41',
    '16:41',
    NULL,
    0,
    '2025-10-20 16:41',
    '2025-10-20 16:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10450,
    1,
    '2025-10-21 07:42',
    '2025-10-21',
    '07:42',
    '07:42',
    NULL,
    0,
    '2025-10-21 07:42',
    '2025-10-21 07:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10451,
    1,
    '2025-10-21 12:49',
    '2025-10-21',
    '12:49',
    '12:49',
    NULL,
    0,
    '2025-10-21 12:49',
    '2025-10-21 12:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10452,
    1,
    '2025-10-21 14:16',
    '2025-10-21',
    '14:16',
    '14:16',
    NULL,
    0,
    '2025-10-21 14:16',
    '2025-10-21 14:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10453,
    1,
    '2025-10-21 18:08',
    '2025-10-21',
    '18:08',
    '18:08',
    NULL,
    0,
    '2025-10-21 18:08',
    '2025-10-21 18:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10460,
    1,
    '2025-10-22 07:46',
    '2025-10-22',
    '07:46',
    '07:46',
    NULL,
    0,
    '2025-10-22 07:46',
    '2025-10-22 07:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10461,
    1,
    '2025-10-22 13:05',
    '2025-10-22',
    '13:05',
    '13:05',
    NULL,
    0,
    '2025-10-22 13:05',
    '2025-10-22 13:05'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10462,
    1,
    '2025-10-22 14:18',
    '2025-10-22',
    '14:18',
    '14:18',
    NULL,
    0,
    '2025-10-22 14:18',
    '2025-10-22 14:18'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10463,
    1,
    '2025-10-22 14:35',
    '2025-10-22',
    '14:35',
    '14:35',
    NULL,
    0,
    '2025-10-22 14:35',
    '2025-10-22 14:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10470,
    1,
    '2025-10-23 06:29',
    '2025-10-23',
    '06:29',
    '06:29',
    NULL,
    0,
    '2025-10-23 06:29',
    '2025-10-23 06:29'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10471,
    1,
    '2025-10-23 12:28',
    '2025-10-23',
    '12:28',
    '12:28',
    NULL,
    0,
    '2025-10-23 12:28',
    '2025-10-23 12:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10472,
    1,
    '2025-10-23 13:45',
    '2025-10-23',
    '13:45',
    '13:45',
    NULL,
    0,
    '2025-10-23 13:45',
    '2025-10-23 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10473,
    1,
    '2025-10-23 15:46',
    '2025-10-23',
    '15:46',
    '15:46',
    NULL,
    0,
    '2025-10-23 15:46',
    '2025-10-23 15:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10480,
    1,
    '2025-10-27 07:29',
    '2025-10-27',
    '07:29',
    '07:29',
    NULL,
    0,
    '2025-10-27 07:29',
    '2025-10-27 07:29'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10481,
    1,
    '2025-10-27 12:33',
    '2025-10-27',
    '12:33',
    '12:33',
    NULL,
    0,
    '2025-10-27 12:33',
    '2025-10-27 12:33'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10482,
    1,
    '2025-10-27 13:47',
    '2025-10-27',
    '13:47',
    '13:47',
    NULL,
    0,
    '2025-10-27 13:47',
    '2025-10-27 13:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10483,
    1,
    '2025-10-27 16:41',
    '2025-10-27',
    '16:41',
    '16:41',
    NULL,
    0,
    '2025-10-27 16:41',
    '2025-10-27 16:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10490,
    1,
    '2025-10-28 07:38',
    '2025-10-28',
    '07:38',
    '07:38',
    NULL,
    0,
    '2025-10-28 07:38',
    '2025-10-28 07:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10491,
    1,
    '2025-10-28 12:48',
    '2025-10-28',
    '12:48',
    '12:48',
    NULL,
    0,
    '2025-10-28 12:48',
    '2025-10-28 12:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10492,
    1,
    '2025-10-28 14:00',
    '2025-10-28',
    '14:00',
    '14:00',
    NULL,
    0,
    '2025-10-28 14:00',
    '2025-10-28 14:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10493,
    1,
    '2025-10-28 16:52',
    '2025-10-28',
    '16:52',
    '16:52',
    NULL,
    0,
    '2025-10-28 16:52',
    '2025-10-28 16:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10500,
    1,
    '2025-10-29 07:23',
    '2025-10-29',
    '07:23',
    '07:23',
    NULL,
    0,
    '2025-10-29 07:23',
    '2025-10-29 07:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10501,
    1,
    '2025-10-29 08:52',
    '2025-10-29',
    '08:52',
    '08:52',
    NULL,
    0,
    '2025-10-29 08:52',
    '2025-10-29 08:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10502,
    1,
    '2025-10-29 10:07',
    '2025-10-29',
    '10:07',
    '10:07',
    NULL,
    0,
    '2025-10-29 10:07',
    '2025-10-29 10:07'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10503,
    1,
    '2025-10-29 12:32',
    '2025-10-29',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-10-29 12:32',
    '2025-10-29 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10504,
    1,
    '2025-10-29 13:40',
    '2025-10-29',
    '13:40',
    '13:40',
    NULL,
    0,
    '2025-10-29 13:40',
    '2025-10-29 13:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10505,
    1,
    '2025-10-29 16:52',
    '2025-10-29',
    '16:52',
    '16:52',
    NULL,
    0,
    '2025-10-29 16:52',
    '2025-10-29 16:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10510,
    1,
    '2025-10-30 07:31',
    '2025-10-30',
    '07:31',
    '07:31',
    NULL,
    0,
    '2025-10-30 07:31',
    '2025-10-30 07:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10511,
    1,
    '2025-10-30 12:50',
    '2025-10-30',
    '12:50',
    '12:50',
    NULL,
    0,
    '2025-10-30 12:50',
    '2025-10-30 12:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10512,
    1,
    '2025-10-30 14:08',
    '2025-10-30',
    '14:08',
    '14:08',
    NULL,
    0,
    '2025-10-30 14:08',
    '2025-10-30 14:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10513,
    1,
    '2025-10-30 16:47',
    '2025-10-30',
    '16:47',
    '16:47',
    NULL,
    0,
    '2025-10-30 16:47',
    '2025-10-30 16:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10520,
    1,
    '2025-10-31 07:36',
    '2025-10-31',
    '07:36',
    '07:36',
    NULL,
    0,
    '2025-10-31 07:36',
    '2025-10-31 07:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10521,
    1,
    '2025-10-31 12:32',
    '2025-10-31',
    '12:32',
    '12:32',
    NULL,
    0,
    '2025-10-31 12:32',
    '2025-10-31 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10522,
    1,
    '2025-10-31 13:47',
    '2025-10-31',
    '13:47',
    '13:47',
    NULL,
    0,
    '2025-10-31 13:47',
    '2025-10-31 13:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10523,
    1,
    '2025-10-31 16:47',
    '2025-10-31',
    '16:47',
    '16:47',
    NULL,
    0,
    '2025-10-31 16:47',
    '2025-10-31 16:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10530,
    1,
    '2025-11-03 07:47',
    '2025-11-03',
    '07:47',
    '07:47',
    NULL,
    0,
    '2025-11-03 07:47',
    '2025-11-03 07:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10531,
    1,
    '2025-11-03 12:42',
    '2025-11-03',
    '12:42',
    '12:42',
    NULL,
    0,
    '2025-11-03 12:42',
    '2025-11-03 12:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10532,
    1,
    '2025-11-03 13:58',
    '2025-11-03',
    '13:58',
    '13:58',
    NULL,
    0,
    '2025-11-03 13:58',
    '2025-11-03 13:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10533,
    1,
    '2025-11-03 16:06',
    '2025-11-03',
    '16:06',
    '16:06',
    NULL,
    0,
    '2025-11-03 16:06',
    '2025-11-03 16:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10540,
    1,
    '2025-11-04 07:32',
    '2025-11-04',
    '07:32',
    '07:32',
    NULL,
    0,
    '2025-11-04 07:32',
    '2025-11-04 07:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10541,
    1,
    '2025-11-04 12:43',
    '2025-11-04',
    '12:43',
    '12:43',
    NULL,
    0,
    '2025-11-04 12:43',
    '2025-11-04 12:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10542,
    1,
    '2025-11-04 14:01',
    '2025-11-04',
    '14:01',
    '14:01',
    NULL,
    0,
    '2025-11-04 14:01',
    '2025-11-04 14:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10543,
    1,
    '2025-11-04 16:43',
    '2025-11-04',
    '16:43',
    '16:43',
    NULL,
    0,
    '2025-11-04 16:43',
    '2025-11-04 16:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10550,
    1,
    '2025-11-05 07:34',
    '2025-11-05',
    '07:34',
    '07:34',
    NULL,
    0,
    '2025-11-05 07:34',
    '2025-11-05 07:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10551,
    1,
    '2025-11-05 12:41',
    '2025-11-05',
    '12:41',
    '12:41',
    NULL,
    0,
    '2025-11-05 12:41',
    '2025-11-05 12:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10552,
    1,
    '2025-11-05 13:59',
    '2025-11-05',
    '13:59',
    '13:59',
    NULL,
    0,
    '2025-11-05 13:59',
    '2025-11-05 13:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10553,
    1,
    '2025-11-05 16:51',
    '2025-11-05',
    '16:51',
    '16:51',
    NULL,
    0,
    '2025-11-05 16:51',
    '2025-11-05 16:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10560,
    1,
    '2025-11-06 06:55',
    '2025-11-06',
    '06:55',
    '06:55',
    NULL,
    0,
    '2025-11-06 06:55',
    '2025-11-06 06:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10561,
    1,
    '2025-11-06 12:23',
    '2025-11-06',
    '12:23',
    '12:23',
    NULL,
    0,
    '2025-11-06 12:23',
    '2025-11-06 12:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10562,
    1,
    '2025-11-06 13:41',
    '2025-11-06',
    '13:41',
    '13:41',
    NULL,
    0,
    '2025-11-06 13:41',
    '2025-11-06 13:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10563,
    1,
    '2025-11-06 16:08',
    '2025-11-06',
    '16:08',
    '16:08',
    NULL,
    0,
    '2025-11-06 16:08',
    '2025-11-06 16:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10570,
    1,
    '2025-11-07 06:39',
    '2025-11-07',
    '06:39',
    '06:39',
    NULL,
    0,
    '2025-11-07 06:39',
    '2025-11-07 06:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10571,
    1,
    '2025-11-07 08:40',
    '2025-11-07',
    '08:40',
    '08:40',
    NULL,
    0,
    '2025-11-07 08:40',
    '2025-11-07 08:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10572,
    1,
    '2025-11-07 10:18',
    '2025-11-07',
    '10:18',
    '10:18',
    NULL,
    0,
    '2025-11-07 10:18',
    '2025-11-07 10:18'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10573,
    1,
    '2025-11-07 11:56',
    '2025-11-07',
    '11:56',
    '11:56',
    NULL,
    0,
    '2025-11-07 11:56',
    '2025-11-07 11:56'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10574,
    1,
    '2025-11-07 13:13',
    '2025-11-07',
    '13:13',
    '13:13',
    NULL,
    0,
    '2025-11-07 13:13',
    '2025-11-07 13:13'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10575,
    1,
    '2025-11-07 15:24',
    '2025-11-07',
    '15:24',
    '15:24',
    NULL,
    0,
    '2025-11-07 15:24',
    '2025-11-07 15:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10580,
    1,
    '2025-11-10 06:28',
    '2025-11-10',
    '06:28',
    '06:28',
    NULL,
    0,
    '2025-11-10 06:28',
    '2025-11-10 06:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10581,
    1,
    '2025-11-10 12:28',
    '2025-11-10',
    '12:28',
    '12:28',
    NULL,
    0,
    '2025-11-10 12:28',
    '2025-11-10 12:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10582,
    1,
    '2025-11-10 13:42',
    '2025-11-10',
    '13:42',
    '13:42',
    NULL,
    0,
    '2025-11-10 13:42',
    '2025-11-10 13:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10583,
    1,
    '2025-11-10 15:47',
    '2025-11-10',
    '15:47',
    '15:47',
    NULL,
    0,
    '2025-11-10 15:47',
    '2025-11-10 15:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10590,
    1,
    '2025-11-11 07:00',
    '2025-11-11',
    '07:00',
    '07:00',
    NULL,
    0,
    '2025-11-11 07:00',
    '2025-11-11 07:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10591,
    1,
    '2025-11-11 12:37',
    '2025-11-11',
    '12:37',
    '12:37',
    NULL,
    0,
    '2025-11-11 12:37',
    '2025-11-11 12:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10592,
    1,
    '2025-11-11 13:54',
    '2025-11-11',
    '13:54',
    '13:54',
    NULL,
    0,
    '2025-11-11 13:54',
    '2025-11-11 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10593,
    1,
    '2025-11-11 16:34',
    '2025-11-11',
    '16:34',
    '16:34',
    NULL,
    0,
    '2025-11-11 16:34',
    '2025-11-11 16:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10600,
    1,
    '2025-11-12 06:39',
    '2025-11-12',
    '06:39',
    '06:39',
    NULL,
    0,
    '2025-11-12 06:39',
    '2025-11-12 06:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10601,
    1,
    '2025-11-12 12:39',
    '2025-11-12',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-11-12 12:39',
    '2025-11-12 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10602,
    1,
    '2025-11-12 13:54',
    '2025-11-12',
    '13:54',
    '13:54',
    NULL,
    0,
    '2025-11-12 13:54',
    '2025-11-12 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10603,
    1,
    '2025-11-12 15:53',
    '2025-11-12',
    '15:53',
    '15:53',
    NULL,
    0,
    '2025-11-12 15:53',
    '2025-11-12 15:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10610,
    1,
    '2025-11-13 06:53',
    '2025-11-13',
    '06:53',
    '06:53',
    NULL,
    0,
    '2025-11-13 06:53',
    '2025-11-13 06:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10611,
    1,
    '2025-11-13 12:35',
    '2025-11-13',
    '12:35',
    '12:35',
    NULL,
    0,
    '2025-11-13 12:35',
    '2025-11-13 12:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10612,
    1,
    '2025-11-13 13:49',
    '2025-11-13',
    '13:49',
    '13:49',
    NULL,
    0,
    '2025-11-13 13:49',
    '2025-11-13 13:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10613,
    1,
    '2025-11-13 16:06',
    '2025-11-13',
    '16:06',
    '16:06',
    NULL,
    0,
    '2025-11-13 16:06',
    '2025-11-13 16:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10620,
    1,
    '2025-11-14 06:36',
    '2025-11-14',
    '06:36',
    '06:36',
    NULL,
    0,
    '2025-11-14 06:36',
    '2025-11-14 06:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10621,
    1,
    '2025-11-14 12:02',
    '2025-11-14',
    '12:02',
    '12:02',
    NULL,
    0,
    '2025-11-14 12:02',
    '2025-11-14 12:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10622,
    1,
    '2025-11-14 13:18',
    '2025-11-14',
    '13:18',
    '13:18',
    NULL,
    0,
    '2025-11-14 13:18',
    '2025-11-14 13:18'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10623,
    1,
    '2025-11-14 14:33',
    '2025-11-14',
    '14:33',
    '14:33',
    NULL,
    0,
    '2025-11-14 14:33',
    '2025-11-14 14:33'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10630,
    1,
    '2025-11-17 07:04',
    '2025-11-17',
    '07:04',
    '07:04',
    NULL,
    0,
    '2025-11-17 07:04',
    '2025-11-17 07:04'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10631,
    1,
    '2025-11-17 12:36',
    '2025-11-17',
    '12:36',
    '12:36',
    NULL,
    0,
    '2025-11-17 12:36',
    '2025-11-17 12:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10632,
    1,
    '2025-11-17 13:52',
    '2025-11-17',
    '13:52',
    '13:52',
    NULL,
    0,
    '2025-11-17 13:52',
    '2025-11-17 13:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10633,
    1,
    '2025-11-17 16:31',
    '2025-11-17',
    '16:31',
    '16:31',
    NULL,
    0,
    '2025-11-17 16:31',
    '2025-11-17 16:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10640,
    1,
    '2025-11-18 08:02',
    '2025-11-18',
    '08:02',
    '08:02',
    NULL,
    0,
    '2025-11-18 08:02',
    '2025-11-18 08:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10641,
    1,
    '2025-11-18 12:34',
    '2025-11-18',
    '12:34',
    '12:34',
    NULL,
    0,
    '2025-11-18 12:34',
    '2025-11-18 12:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10642,
    1,
    '2025-11-18 13:54',
    '2025-11-18',
    '13:54',
    '13:54',
    NULL,
    0,
    '2025-11-18 13:54',
    '2025-11-18 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10643,
    1,
    '2025-11-18 16:37',
    '2025-11-18',
    '16:37',
    '16:37',
    NULL,
    0,
    '2025-11-18 16:37',
    '2025-11-18 16:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10650,
    1,
    '2025-11-19 13:15',
    '2025-11-19',
    '13:15',
    '13:15',
    NULL,
    0,
    '2025-11-19 13:15',
    '2025-11-19 13:15'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10651,
    1,
    '2025-11-19 17:28',
    '2025-11-19',
    '17:28',
    '17:28',
    NULL,
    0,
    '2025-11-19 17:28',
    '2025-11-19 17:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10660,
    1,
    '2025-11-20 06:33',
    '2025-11-20',
    '06:33',
    '06:33',
    NULL,
    0,
    '2025-11-20 06:33',
    '2025-11-20 06:33'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10661,
    1,
    '2025-11-20 12:31',
    '2025-11-20',
    '12:31',
    '12:31',
    NULL,
    0,
    '2025-11-20 12:31',
    '2025-11-20 12:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10662,
    1,
    '2025-11-20 13:46',
    '2025-11-20',
    '13:46',
    '13:46',
    NULL,
    0,
    '2025-11-20 13:46',
    '2025-11-20 13:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10663,
    1,
    '2025-11-20 16:27',
    '2025-11-20',
    '16:27',
    '16:27',
    NULL,
    0,
    '2025-11-20 16:27',
    '2025-11-20 16:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10670,
    1,
    '2025-11-24 07:01',
    '2025-11-24',
    '07:01',
    '07:01',
    NULL,
    0,
    '2025-11-24 07:01',
    '2025-11-24 07:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10671,
    1,
    '2025-11-24 12:37',
    '2025-11-24',
    '12:37',
    '12:37',
    NULL,
    0,
    '2025-11-24 12:37',
    '2025-11-24 12:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10672,
    1,
    '2025-11-24 13:54',
    '2025-11-24',
    '13:54',
    '13:54',
    NULL,
    0,
    '2025-11-24 13:54',
    '2025-11-24 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10673,
    1,
    '2025-11-24 16:45',
    '2025-11-24',
    '16:45',
    '16:45',
    NULL,
    0,
    '2025-11-24 16:45',
    '2025-11-24 16:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10680,
    1,
    '2025-11-25 07:29',
    '2025-11-25',
    '07:29',
    '07:29',
    NULL,
    0,
    '2025-11-25 07:29',
    '2025-11-25 07:29'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10681,
    1,
    '2025-11-25 12:38',
    '2025-11-25',
    '12:38',
    '12:38',
    NULL,
    0,
    '2025-11-25 12:38',
    '2025-11-25 12:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10682,
    1,
    '2025-11-25 13:53',
    '2025-11-25',
    '13:53',
    '13:53',
    NULL,
    0,
    '2025-11-25 13:53',
    '2025-11-25 13:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10683,
    1,
    '2025-11-25 14:50',
    '2025-11-25',
    '14:50',
    '14:50',
    NULL,
    0,
    '2025-11-25 14:50',
    '2025-11-25 14:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10690,
    1,
    '2025-11-26 07:24',
    '2025-11-26',
    '07:24',
    '07:24',
    NULL,
    0,
    '2025-11-26 07:24',
    '2025-11-26 07:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10691,
    1,
    '2025-11-26 12:23',
    '2025-11-26',
    '12:23',
    '12:23',
    NULL,
    0,
    '2025-11-26 12:23',
    '2025-11-26 12:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10692,
    1,
    '2025-11-26 13:39',
    '2025-11-26',
    '13:39',
    '13:39',
    NULL,
    0,
    '2025-11-26 13:39',
    '2025-11-26 13:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10693,
    1,
    '2025-11-26 16:41',
    '2025-11-26',
    '16:41',
    '16:41',
    NULL,
    0,
    '2025-11-26 16:41',
    '2025-11-26 16:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10700,
    1,
    '2025-11-27 07:21',
    '2025-11-27',
    '07:21',
    '07:21',
    NULL,
    0,
    '2025-11-27 07:21',
    '2025-11-27 07:21'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10701,
    1,
    '2025-11-27 12:43',
    '2025-11-27',
    '12:43',
    '12:43',
    NULL,
    0,
    '2025-11-27 12:43',
    '2025-11-27 12:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10702,
    1,
    '2025-11-27 13:59',
    '2025-11-27',
    '13:59',
    '13:59',
    NULL,
    0,
    '2025-11-27 13:59',
    '2025-11-27 13:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10703,
    1,
    '2025-11-27 16:43',
    '2025-11-27',
    '16:43',
    '16:43',
    NULL,
    0,
    '2025-11-27 16:43',
    '2025-11-27 16:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10710,
    1,
    '2025-11-28 07:23',
    '2025-11-28',
    '07:23',
    '07:23',
    NULL,
    0,
    '2025-11-28 07:23',
    '2025-11-28 07:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10711,
    1,
    '2025-11-28 12:09',
    '2025-11-28',
    '12:09',
    '12:09',
    NULL,
    0,
    '2025-11-28 12:09',
    '2025-11-28 12:09'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10712,
    1,
    '2025-11-28 13:28',
    '2025-11-28',
    '13:28',
    '13:28',
    NULL,
    0,
    '2025-11-28 13:28',
    '2025-11-28 13:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10713,
    1,
    '2025-11-28 16:42',
    '2025-11-28',
    '16:42',
    '16:42',
    NULL,
    0,
    '2025-11-28 16:42',
    '2025-11-28 16:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10720,
    1,
    '2025-12-01 07:17',
    '2025-12-01',
    '07:17',
    '07:17',
    NULL,
    0,
    '2025-12-01 07:17',
    '2025-12-01 07:17'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10721,
    1,
    '2025-12-01 12:35',
    '2025-12-01',
    '12:35',
    '12:35',
    NULL,
    0,
    '2025-12-01 12:35',
    '2025-12-01 12:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10722,
    1,
    '2025-12-01 13:55',
    '2025-12-01',
    '13:55',
    '13:55',
    NULL,
    0,
    '2025-12-01 13:55',
    '2025-12-01 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10723,
    1,
    '2025-12-01 15:59',
    '2025-12-01',
    '15:59',
    '15:59',
    NULL,
    0,
    '2025-12-01 15:59',
    '2025-12-01 15:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10730,
    1,
    '2025-12-02 07:37',
    '2025-12-02',
    '07:37',
    '07:37',
    NULL,
    0,
    '2025-12-02 07:37',
    '2025-12-02 07:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10731,
    1,
    '2025-12-02 12:48',
    '2025-12-02',
    '12:48',
    '12:48',
    NULL,
    0,
    '2025-12-02 12:48',
    '2025-12-02 12:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10732,
    1,
    '2025-12-02 14:06',
    '2025-12-02',
    '14:06',
    '14:06',
    NULL,
    0,
    '2025-12-02 14:06',
    '2025-12-02 14:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10733,
    1,
    '2025-12-02 17:01',
    '2025-12-02',
    '17:01',
    '17:01',
    NULL,
    0,
    '2025-12-02 17:01',
    '2025-12-02 17:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10740,
    1,
    '2025-12-03 07:36',
    '2025-12-03',
    '07:36',
    '07:36',
    NULL,
    0,
    '2025-12-03 07:36',
    '2025-12-03 07:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10741,
    1,
    '2025-12-03 12:35',
    '2025-12-03',
    '12:35',
    '12:35',
    NULL,
    0,
    '2025-12-03 12:35',
    '2025-12-03 12:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10742,
    1,
    '2025-12-03 13:55',
    '2025-12-03',
    '13:55',
    '13:55',
    NULL,
    0,
    '2025-12-03 13:55',
    '2025-12-03 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10743,
    1,
    '2025-12-03 17:06',
    '2025-12-03',
    '17:06',
    '17:06',
    NULL,
    0,
    '2025-12-03 17:06',
    '2025-12-03 17:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10750,
    1,
    '2025-12-04 07:23',
    '2025-12-04',
    '07:23',
    '07:23',
    NULL,
    0,
    '2025-12-04 07:23',
    '2025-12-04 07:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10751,
    1,
    '2025-12-04 12:42',
    '2025-12-04',
    '12:42',
    '12:42',
    NULL,
    0,
    '2025-12-04 12:42',
    '2025-12-04 12:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10752,
    1,
    '2025-12-04 13:58',
    '2025-12-04',
    '13:58',
    '13:58',
    NULL,
    0,
    '2025-12-04 13:58',
    '2025-12-04 13:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10753,
    1,
    '2025-12-04 16:26',
    '2025-12-04',
    '16:26',
    '16:26',
    NULL,
    0,
    '2025-12-04 16:26',
    '2025-12-04 16:26'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10760,
    1,
    '2025-12-05 07:04',
    '2025-12-05',
    '07:04',
    '07:04',
    NULL,
    0,
    '2025-12-05 07:04',
    '2025-12-05 07:04'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10761,
    1,
    '2025-12-05 12:41',
    '2025-12-05',
    '12:41',
    '12:41',
    NULL,
    0,
    '2025-12-05 12:41',
    '2025-12-05 12:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10762,
    1,
    '2025-12-05 13:57',
    '2025-12-05',
    '13:57',
    '13:57',
    NULL,
    0,
    '2025-12-05 13:57',
    '2025-12-05 13:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10763,
    1,
    '2025-12-05 16:33',
    '2025-12-05',
    '16:33',
    '16:33',
    NULL,
    0,
    '2025-12-05 16:33',
    '2025-12-05 16:33'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10770,
    1,
    '2025-12-09 07:14',
    '2025-12-09',
    '07:14',
    '07:14',
    NULL,
    0,
    '2025-12-09 07:14',
    '2025-12-09 07:14'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10771,
    1,
    '2025-12-09 12:44',
    '2025-12-09',
    '12:44',
    '12:44',
    NULL,
    0,
    '2025-12-09 12:44',
    '2025-12-09 12:44'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10772,
    1,
    '2025-12-09 14:00',
    '2025-12-09',
    '14:00',
    '14:00',
    NULL,
    0,
    '2025-12-09 14:00',
    '2025-12-09 14:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10773,
    1,
    '2025-12-09 17:02',
    '2025-12-09',
    '17:02',
    '17:02',
    NULL,
    0,
    '2025-12-09 17:02',
    '2025-12-09 17:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10780,
    1,
    '2025-12-10 07:21',
    '2025-12-10',
    '07:21',
    '07:21',
    NULL,
    0,
    '2025-12-10 07:21',
    '2025-12-10 07:21'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10781,
    1,
    '2025-12-10 12:39',
    '2025-12-10',
    '12:39',
    '12:39',
    NULL,
    0,
    '2025-12-10 12:39',
    '2025-12-10 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10782,
    1,
    '2025-12-10 13:58',
    '2025-12-10',
    '13:58',
    '13:58',
    NULL,
    0,
    '2025-12-10 13:58',
    '2025-12-10 13:58'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10783,
    1,
    '2025-12-10 16:56',
    '2025-12-10',
    '16:56',
    '16:56',
    NULL,
    0,
    '2025-12-10 16:56',
    '2025-12-10 16:56'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10790,
    1,
    '2025-12-11 07:23',
    '2025-12-11',
    '07:23',
    '07:23',
    NULL,
    0,
    '2025-12-11 07:23',
    '2025-12-11 07:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10791,
    1,
    '2025-12-11 12:36',
    '2025-12-11',
    '12:36',
    '12:36',
    NULL,
    0,
    '2025-12-11 12:36',
    '2025-12-11 12:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10792,
    1,
    '2025-12-11 13:55',
    '2025-12-11',
    '13:55',
    '13:55',
    NULL,
    0,
    '2025-12-11 13:55',
    '2025-12-11 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10793,
    1,
    '2025-12-11 16:50',
    '2025-12-11',
    '16:50',
    '16:50',
    NULL,
    0,
    '2025-12-11 16:50',
    '2025-12-11 16:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10800,
    1,
    '2025-12-12 07:39',
    '2025-12-12',
    '07:39',
    '07:39',
    NULL,
    0,
    '2025-12-12 07:39',
    '2025-12-12 07:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10801,
    1,
    '2025-12-12 12:30',
    '2025-12-12',
    '12:30',
    '12:30',
    NULL,
    0,
    '2025-12-12 12:30',
    '2025-12-12 12:30'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10802,
    1,
    '2025-12-12 13:47',
    '2025-12-12',
    '13:47',
    '13:47',
    NULL,
    0,
    '2025-12-12 13:47',
    '2025-12-12 13:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10803,
    1,
    '2025-12-12 16:09',
    '2025-12-12',
    '16:09',
    '16:09',
    NULL,
    0,
    '2025-12-12 16:09',
    '2025-12-12 16:09'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10810,
    1,
    '2025-12-15 07:14',
    '2025-12-15',
    '07:14',
    '07:14',
    NULL,
    0,
    '2025-12-15 07:14',
    '2025-12-15 07:14'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10811,
    1,
    '2025-12-15 12:40',
    '2025-12-15',
    '12:40',
    '12:40',
    NULL,
    0,
    '2025-12-15 12:40',
    '2025-12-15 12:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10812,
    1,
    '2025-12-15 13:56',
    '2025-12-15',
    '13:56',
    '13:56',
    NULL,
    0,
    '2025-12-15 13:56',
    '2025-12-15 13:56'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10813,
    1,
    '2025-12-15 16:34',
    '2025-12-15',
    '16:34',
    '16:34',
    NULL,
    0,
    '2025-12-15 16:34',
    '2025-12-15 16:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10820,
    1,
    '2025-12-16 07:28',
    '2025-12-16',
    '07:28',
    '07:28',
    NULL,
    0,
    '2025-12-16 07:28',
    '2025-12-16 07:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10821,
    1,
    '2025-12-16 11:50',
    '2025-12-16',
    '11:50',
    '11:50',
    NULL,
    0,
    '2025-12-16 11:50',
    '2025-12-16 11:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10822,
    1,
    '2025-12-16 13:03',
    '2025-12-16',
    '13:03',
    '13:03',
    NULL,
    0,
    '2025-12-16 13:03',
    '2025-12-16 13:03'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10823,
    1,
    '2025-12-16 15:23',
    '2025-12-16',
    '15:23',
    '15:23',
    NULL,
    0,
    '2025-12-16 15:23',
    '2025-12-16 15:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10830,
    1,
    '2025-12-17 07:31',
    '2025-12-17',
    '07:31',
    '07:31',
    NULL,
    0,
    '2025-12-17 07:31',
    '2025-12-17 07:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10831,
    1,
    '2025-12-17 12:57',
    '2025-12-17',
    '12:57',
    '12:57',
    NULL,
    0,
    '2025-12-17 12:57',
    '2025-12-17 12:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10832,
    1,
    '2025-12-17 14:16',
    '2025-12-17',
    '14:16',
    '14:16',
    NULL,
    0,
    '2025-12-17 14:16',
    '2025-12-17 14:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10833,
    1,
    '2025-12-17 16:54',
    '2025-12-17',
    '16:54',
    '16:54',
    NULL,
    0,
    '2025-12-17 16:54',
    '2025-12-17 16:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10840,
    1,
    '2025-12-18 06:14',
    '2025-12-18',
    '06:14',
    '06:14',
    NULL,
    0,
    '2025-12-18 06:14',
    '2025-12-18 06:14'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10841,
    1,
    '2025-12-18 12:10',
    '2025-12-18',
    '12:10',
    '12:10',
    NULL,
    0,
    '2025-12-18 12:10',
    '2025-12-18 12:10'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10842,
    1,
    '2025-12-18 13:28',
    '2025-12-18',
    '13:28',
    '13:28',
    NULL,
    0,
    '2025-12-18 13:28',
    '2025-12-18 13:28'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10843,
    1,
    '2025-12-18 15:57',
    '2025-12-18',
    '15:57',
    '15:57',
    NULL,
    0,
    '2025-12-18 15:57',
    '2025-12-18 15:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10850,
    1,
    '2025-12-19 07:33',
    '2025-12-19',
    '07:33',
    '07:33',
    NULL,
    0,
    '2025-12-19 07:33',
    '2025-12-19 07:33'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10851,
    1,
    '2025-12-19 12:47',
    '2025-12-19',
    '12:47',
    '12:47',
    NULL,
    0,
    '2025-12-19 12:47',
    '2025-12-19 12:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10852,
    1,
    '2025-12-19 14:10',
    '2025-12-19',
    '14:10',
    '14:10',
    NULL,
    0,
    '2025-12-19 14:10',
    '2025-12-19 14:10'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10853,
    1,
    '2025-12-19 17:19',
    '2025-12-19',
    '17:19',
    '17:19',
    NULL,
    0,
    '2025-12-19 17:19',
    '2025-12-19 17:19'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10860,
    1,
    '2025-12-22 07:42',
    '2025-12-22',
    '07:42',
    '07:42',
    NULL,
    0,
    '2025-12-22 07:42',
    '2025-12-22 07:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10861,
    1,
    '2025-12-22 12:41',
    '2025-12-22',
    '12:41',
    '12:41',
    NULL,
    0,
    '2025-12-22 12:41',
    '2025-12-22 12:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10862,
    1,
    '2025-12-22 14:03',
    '2025-12-22',
    '14:03',
    '14:03',
    NULL,
    0,
    '2025-12-22 14:03',
    '2025-12-22 14:03'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10863,
    1,
    '2025-12-22 17:27',
    '2025-12-22',
    '17:27',
    '17:27',
    NULL,
    0,
    '2025-12-22 17:27',
    '2025-12-22 17:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10870,
    1,
    '2025-12-23 07:23',
    '2025-12-23',
    '07:23',
    '07:23',
    NULL,
    0,
    '2025-12-23 07:23',
    '2025-12-23 07:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10871,
    1,
    '2025-12-23 12:08',
    '2025-12-23',
    '12:08',
    '12:08',
    NULL,
    0,
    '2025-12-23 12:08',
    '2025-12-23 12:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10872,
    1,
    '2025-12-23 13:34',
    '2025-12-23',
    '13:34',
    '13:34',
    NULL,
    0,
    '2025-12-23 13:34',
    '2025-12-23 13:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10873,
    1,
    '2025-12-23 16:48',
    '2025-12-23',
    '16:48',
    '16:48',
    NULL,
    0,
    '2025-12-23 16:48',
    '2025-12-23 16:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10880,
    1,
    '2025-12-29 07:11',
    '2025-12-29',
    '07:11',
    '07:11',
    NULL,
    0,
    '2025-12-29 07:11',
    '2025-12-29 07:11'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10881,
    1,
    '2025-12-29 12:50',
    '2025-12-29',
    '12:50',
    '12:50',
    NULL,
    0,
    '2025-12-29 12:50',
    '2025-12-29 12:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10882,
    1,
    '2025-12-29 13:59',
    '2025-12-29',
    '13:59',
    '13:59',
    NULL,
    0,
    '2025-12-29 13:59',
    '2025-12-29 13:59'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10883,
    1,
    '2025-12-29 16:49',
    '2025-12-29',
    '16:49',
    '16:49',
    NULL,
    0,
    '2025-12-29 16:49',
    '2025-12-29 16:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10890,
    1,
    '2025-12-30 07:15',
    '2025-12-30',
    '07:15',
    '07:15',
    NULL,
    0,
    '2025-12-30 07:15',
    '2025-12-30 07:15'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10891,
    1,
    '2025-12-30 12:33',
    '2025-12-30',
    '12:33',
    '12:33',
    NULL,
    0,
    '2025-12-30 12:33',
    '2025-12-30 12:33'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10892,
    1,
    '2025-12-30 13:51',
    '2025-12-30',
    '13:51',
    '13:51',
    NULL,
    0,
    '2025-12-30 13:51',
    '2025-12-30 13:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10893,
    1,
    '2025-12-30 18:15',
    '2025-12-30',
    '18:15',
    '18:15',
    NULL,
    0,
    '2025-12-30 18:15',
    '2025-12-30 18:15'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10900,
    1,
    '2026-01-05 06:45',
    '2026-01-05',
    '06:45',
    '06:45',
    NULL,
    0,
    '2026-01-05 06:45',
    '2026-01-05 06:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10901,
    1,
    '2026-01-05 12:37',
    '2026-01-05',
    '12:37',
    '12:37',
    NULL,
    0,
    '2026-01-05 12:37',
    '2026-01-05 12:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10902,
    1,
    '2026-01-05 13:54',
    '2026-01-05',
    '13:54',
    '13:54',
    NULL,
    0,
    '2026-01-05 13:54',
    '2026-01-05 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10903,
    1,
    '2026-01-05 17:46',
    '2026-01-05',
    '17:46',
    '17:46',
    NULL,
    0,
    '2026-01-05 17:46',
    '2026-01-05 17:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10910,
    1,
    '2026-01-06 06:56',
    '2026-01-06',
    '06:56',
    '06:56',
    NULL,
    0,
    '2026-01-06 06:56',
    '2026-01-06 06:56'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10911,
    1,
    '2026-01-06 12:42',
    '2026-01-06',
    '12:42',
    '12:42',
    NULL,
    0,
    '2026-01-06 12:42',
    '2026-01-06 12:42'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10912,
    1,
    '2026-01-06 14:10',
    '2026-01-06',
    '14:10',
    '14:10',
    NULL,
    0,
    '2026-01-06 14:10',
    '2026-01-06 14:10'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10913,
    1,
    '2026-01-06 18:21',
    '2026-01-06',
    '18:21',
    '18:21',
    NULL,
    0,
    '2026-01-06 18:21',
    '2026-01-06 18:21'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10920,
    1,
    '2026-01-07 07:50',
    '2026-01-07',
    '07:50',
    '07:50',
    NULL,
    0,
    '2026-01-07 07:50',
    '2026-01-07 07:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10921,
    1,
    '2026-01-07 12:54',
    '2026-01-07',
    '12:54',
    '12:54',
    NULL,
    0,
    '2026-01-07 12:54',
    '2026-01-07 12:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10922,
    1,
    '2026-01-07 14:11',
    '2026-01-07',
    '14:11',
    '14:11',
    NULL,
    0,
    '2026-01-07 14:11',
    '2026-01-07 14:11'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10923,
    1,
    '2026-01-07 17:56',
    '2026-01-07',
    '17:56',
    '17:56',
    NULL,
    0,
    '2026-01-07 17:56',
    '2026-01-07 17:56'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10930,
    1,
    '2026-01-08 12:40',
    '2026-01-08',
    '12:40',
    '12:40',
    NULL,
    0,
    '2026-01-08 12:40',
    '2026-01-08 12:40'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10931,
    1,
    '2026-01-08 13:11',
    '2026-01-08',
    '13:11',
    '13:11',
    NULL,
    0,
    '2026-01-08 13:11',
    '2026-01-08 13:11'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10932,
    1,
    '2026-01-08 14:32',
    '2026-01-08',
    '14:32',
    '14:32',
    NULL,
    0,
    '2026-01-08 14:32',
    '2026-01-08 14:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10933,
    1,
    '2026-01-08 20:08',
    '2026-01-08',
    '20:08',
    '20:08',
    NULL,
    0,
    '2026-01-08 20:08',
    '2026-01-08 20:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10940,
    1,
    '2026-01-09 07:24',
    '2026-01-09',
    '07:24',
    '07:24',
    NULL,
    0,
    '2026-01-09 07:24',
    '2026-01-09 07:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10941,
    1,
    '2026-01-09 13:09',
    '2026-01-09',
    '13:09',
    '13:09',
    NULL,
    0,
    '2026-01-09 13:09',
    '2026-01-09 13:09'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10942,
    1,
    '2026-01-09 14:24',
    '2026-01-09',
    '14:24',
    '14:24',
    NULL,
    0,
    '2026-01-09 14:24',
    '2026-01-09 14:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10943,
    1,
    '2026-01-09 17:34',
    '2026-01-09',
    '17:34',
    '17:34',
    NULL,
    0,
    '2026-01-09 17:34',
    '2026-01-09 17:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10950,
    1,
    '2026-01-12 08:22',
    '2026-01-12',
    '08:22',
    '08:22',
    NULL,
    0,
    '2026-01-12 08:22',
    '2026-01-12 08:22'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10951,
    1,
    '2026-01-12 13:01',
    '2026-01-12',
    '13:01',
    '13:01',
    NULL,
    0,
    '2026-01-12 13:01',
    '2026-01-12 13:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10952,
    1,
    '2026-01-12 14:19',
    '2026-01-12',
    '14:19',
    '14:19',
    NULL,
    0,
    '2026-01-12 14:19',
    '2026-01-12 14:19'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10953,
    1,
    '2026-01-12 17:51',
    '2026-01-12',
    '17:51',
    '17:51',
    NULL,
    0,
    '2026-01-12 17:51',
    '2026-01-12 17:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10960,
    1,
    '2026-01-13 08:23',
    '2026-01-13',
    '08:23',
    '08:23',
    NULL,
    0,
    '2026-01-13 08:23',
    '2026-01-13 08:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10961,
    1,
    '2026-01-13 12:49',
    '2026-01-13',
    '12:49',
    '12:49',
    NULL,
    0,
    '2026-01-13 12:49',
    '2026-01-13 12:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10962,
    1,
    '2026-01-13 14:07',
    '2026-01-13',
    '14:07',
    '14:07',
    NULL,
    0,
    '2026-01-13 14:07',
    '2026-01-13 14:07'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10963,
    1,
    '2026-01-13 18:08',
    '2026-01-13',
    '18:08',
    '18:08',
    NULL,
    0,
    '2026-01-13 18:08',
    '2026-01-13 18:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10970,
    1,
    '2026-01-14 08:21',
    '2026-01-14',
    '08:21',
    '08:21',
    NULL,
    0,
    '2026-01-14 08:21',
    '2026-01-14 08:21'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10971,
    1,
    '2026-01-14 12:55',
    '2026-01-14',
    '12:55',
    '12:55',
    NULL,
    0,
    '2026-01-14 12:55',
    '2026-01-14 12:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10972,
    1,
    '2026-01-14 14:12',
    '2026-01-14',
    '14:12',
    '14:12',
    NULL,
    0,
    '2026-01-14 14:12',
    '2026-01-14 14:12'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10973,
    1,
    '2026-01-14 18:57',
    '2026-01-14',
    '18:57',
    '18:57',
    NULL,
    0,
    '2026-01-14 18:57',
    '2026-01-14 18:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10980,
    1,
    '2026-01-15 08:00',
    '2026-01-15',
    '08:00',
    '08:00',
    NULL,
    0,
    '2026-01-15 08:00',
    '2026-01-15 08:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10981,
    1,
    '2026-01-15 12:44',
    '2026-01-15',
    '12:44',
    '12:44',
    NULL,
    0,
    '2026-01-15 12:44',
    '2026-01-15 12:44'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10982,
    1,
    '2026-01-15 14:03',
    '2026-01-15',
    '14:03',
    '14:03',
    NULL,
    0,
    '2026-01-15 14:03',
    '2026-01-15 14:03'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10983,
    1,
    '2026-01-15 17:20',
    '2026-01-15',
    '17:20',
    '17:20',
    NULL,
    0,
    '2026-01-15 17:20',
    '2026-01-15 17:20'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10990,
    1,
    '2026-01-16 08:29',
    '2026-01-16',
    '08:29',
    '08:29',
    NULL,
    0,
    '2026-01-16 08:29',
    '2026-01-16 08:29'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10991,
    1,
    '2026-01-16 12:47',
    '2026-01-16',
    '12:47',
    '12:47',
    NULL,
    0,
    '2026-01-16 12:47',
    '2026-01-16 12:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10992,
    1,
    '2026-01-16 14:02',
    '2026-01-16',
    '14:02',
    '14:02',
    NULL,
    0,
    '2026-01-16 14:02',
    '2026-01-16 14:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    10993,
    1,
    '2026-01-16 16:34',
    '2026-01-16',
    '16:34',
    '16:34',
    NULL,
    0,
    '2026-01-16 16:34',
    '2026-01-16 16:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11000,
    1,
    '2026-01-19 09:01',
    '2026-01-19',
    '09:01',
    '09:01',
    NULL,
    0,
    '2026-01-19 09:01',
    '2026-01-19 09:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11001,
    1,
    '2026-01-19 12:38',
    '2026-01-19',
    '12:38',
    '12:38',
    NULL,
    0,
    '2026-01-19 12:38',
    '2026-01-19 12:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11002,
    1,
    '2026-01-19 13:55',
    '2026-01-19',
    '13:55',
    '13:55',
    NULL,
    0,
    '2026-01-19 13:55',
    '2026-01-19 13:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11003,
    1,
    '2026-01-19 18:03',
    '2026-01-19',
    '18:03',
    '18:03',
    NULL,
    0,
    '2026-01-19 18:03',
    '2026-01-19 18:03'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11010,
    1,
    '2026-01-20 08:35',
    '2026-01-20',
    '08:35',
    '08:35',
    NULL,
    0,
    '2026-01-20 08:35',
    '2026-01-20 08:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11011,
    1,
    '2026-01-20 12:43',
    '2026-01-20',
    '12:43',
    '12:43',
    NULL,
    0,
    '2026-01-20 12:43',
    '2026-01-20 12:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11012,
    1,
    '2026-01-20 14:01',
    '2026-01-20',
    '14:01',
    '14:01',
    NULL,
    0,
    '2026-01-20 14:01',
    '2026-01-20 14:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11013,
    1,
    '2026-01-20 17:48',
    '2026-01-20',
    '17:48',
    '17:48',
    NULL,
    0,
    '2026-01-20 17:48',
    '2026-01-20 17:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11014,
    1,
    '2026-01-20 18:26',
    '2026-01-20',
    '18:26',
    '18:26',
    NULL,
    0,
    '2026-01-20 18:26',
    '2026-01-20 18:26'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11015,
    1,
    '2026-01-20 19:16',
    '2026-01-20',
    '19:16',
    '19:16',
    NULL,
    0,
    '2026-01-20 19:16',
    '2026-01-20 19:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11020,
    1,
    '2026-01-21 08:34',
    '2026-01-21',
    '08:34',
    '08:34',
    NULL,
    0,
    '2026-01-21 08:34',
    '2026-01-21 08:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11021,
    1,
    '2026-01-21 12:51',
    '2026-01-21',
    '12:51',
    '12:51',
    NULL,
    0,
    '2026-01-21 12:51',
    '2026-01-21 12:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11022,
    1,
    '2026-01-21 14:09',
    '2026-01-21',
    '14:09',
    '14:09',
    NULL,
    0,
    '2026-01-21 14:09',
    '2026-01-21 14:09'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11023,
    1,
    '2026-01-21 16:48',
    '2026-01-21',
    '16:48',
    '16:48',
    NULL,
    0,
    '2026-01-21 16:48',
    '2026-01-21 16:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11030,
    1,
    '2026-01-22 08:55',
    '2026-01-22',
    '08:55',
    '08:55',
    NULL,
    0,
    '2026-01-22 08:55',
    '2026-01-22 08:55'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11031,
    1,
    '2026-01-22 12:43',
    '2026-01-22',
    '12:43',
    '12:43',
    NULL,
    0,
    '2026-01-22 12:43',
    '2026-01-22 12:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11032,
    1,
    '2026-01-22 14:00',
    '2026-01-22',
    '14:00',
    '14:00',
    NULL,
    0,
    '2026-01-22 14:00',
    '2026-01-22 14:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11033,
    1,
    '2026-01-22 17:53',
    '2026-01-22',
    '17:53',
    '17:53',
    NULL,
    0,
    '2026-01-22 17:53',
    '2026-01-22 17:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11040,
    1,
    '2026-01-23 08:23',
    '2026-01-23',
    '08:23',
    '08:23',
    NULL,
    0,
    '2026-01-23 08:23',
    '2026-01-23 08:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11041,
    1,
    '2026-01-23 12:52',
    '2026-01-23',
    '12:52',
    '12:52',
    NULL,
    0,
    '2026-01-23 12:52',
    '2026-01-23 12:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11042,
    1,
    '2026-01-23 14:10',
    '2026-01-23',
    '14:10',
    '14:10',
    NULL,
    0,
    '2026-01-23 14:10',
    '2026-01-23 14:10'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11043,
    1,
    '2026-01-23 17:36',
    '2026-01-23',
    '17:36',
    '17:36',
    NULL,
    0,
    '2026-01-23 17:36',
    '2026-01-23 17:36'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11050,
    1,
    '2026-01-26 08:37',
    '2026-01-26',
    '08:37',
    '08:37',
    NULL,
    0,
    '2026-01-26 08:37',
    '2026-01-26 08:37'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11051,
    1,
    '2026-01-26 12:50',
    '2026-01-26',
    '12:50',
    '12:50',
    NULL,
    0,
    '2026-01-26 12:50',
    '2026-01-26 12:50'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11052,
    1,
    '2026-01-26 14:07',
    '2026-01-26',
    '14:07',
    '14:07',
    NULL,
    0,
    '2026-01-26 14:07',
    '2026-01-26 14:07'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11053,
    1,
    '2026-01-26 17:23',
    '2026-01-26',
    '17:23',
    '17:23',
    NULL,
    0,
    '2026-01-26 17:23',
    '2026-01-26 17:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11060,
    1,
    '2026-01-27 09:06',
    '2026-01-27',
    '09:06',
    '09:06',
    NULL,
    0,
    '2026-01-27 09:06',
    '2026-01-27 09:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11061,
    1,
    '2026-01-27 12:48',
    '2026-01-27',
    '12:48',
    '12:48',
    NULL,
    0,
    '2026-01-27 12:48',
    '2026-01-27 12:48'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11062,
    1,
    '2026-01-27 14:06',
    '2026-01-27',
    '14:06',
    '14:06',
    NULL,
    0,
    '2026-01-27 14:06',
    '2026-01-27 14:06'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11063,
    1,
    '2026-01-27 17:16',
    '2026-01-27',
    '17:16',
    '17:16',
    NULL,
    0,
    '2026-01-27 17:16',
    '2026-01-27 17:16'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11070,
    1,
    '2026-01-28 08:07',
    '2026-01-28',
    '08:07',
    '08:07',
    NULL,
    0,
    '2026-01-28 08:07',
    '2026-01-28 08:07'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11071,
    1,
    '2026-01-28 12:51',
    '2026-01-28',
    '12:51',
    '12:51',
    NULL,
    0,
    '2026-01-28 12:51',
    '2026-01-28 12:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11072,
    1,
    '2026-01-28 14:08',
    '2026-01-28',
    '14:08',
    '14:08',
    NULL,
    0,
    '2026-01-28 14:08',
    '2026-01-28 14:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11073,
    1,
    '2026-01-28 17:02',
    '2026-01-28',
    '17:02',
    '17:02',
    NULL,
    0,
    '2026-01-28 17:02',
    '2026-01-28 17:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11080,
    1,
    '2026-01-29 08:09',
    '2026-01-29',
    '08:09',
    '08:09',
    NULL,
    0,
    '2026-01-29 08:09',
    '2026-01-29 08:09'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11081,
    1,
    '2026-01-29 12:35',
    '2026-01-29',
    '12:35',
    '12:35',
    NULL,
    0,
    '2026-01-29 12:35',
    '2026-01-29 12:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11082,
    1,
    '2026-01-29 13:46',
    '2026-01-29',
    '13:46',
    '13:46',
    NULL,
    0,
    '2026-01-29 13:46',
    '2026-01-29 13:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11083,
    1,
    '2026-01-29 16:15',
    '2026-01-29',
    '16:15',
    '16:15',
    NULL,
    0,
    '2026-01-29 16:15',
    '2026-01-29 16:15'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11090,
    1,
    '2026-01-30 07:53',
    '2026-01-30',
    '07:53',
    '07:53',
    NULL,
    0,
    '2026-01-30 07:53',
    '2026-01-30 07:53'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11091,
    1,
    '2026-01-30 12:43',
    '2026-01-30',
    '12:43',
    '12:43',
    NULL,
    0,
    '2026-01-30 12:43',
    '2026-01-30 12:43'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11092,
    1,
    '2026-01-30 14:01',
    '2026-01-30',
    '14:01',
    '14:01',
    NULL,
    0,
    '2026-01-30 14:01',
    '2026-01-30 14:01'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11093,
    1,
    '2026-01-30 17:00',
    '2026-01-30',
    '17:00',
    '17:00',
    NULL,
    0,
    '2026-01-30 17:00',
    '2026-01-30 17:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11100,
    1,
    '2026-02-02 08:34',
    '2026-02-02',
    '08:34',
    '08:34',
    NULL,
    0,
    '2026-02-02 08:34',
    '2026-02-02 08:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11101,
    1,
    '2026-02-02 12:38',
    '2026-02-02',
    '12:38',
    '12:38',
    NULL,
    0,
    '2026-02-02 12:38',
    '2026-02-02 12:38'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11102,
    1,
    '2026-02-02 13:54',
    '2026-02-02',
    '13:54',
    '13:54',
    NULL,
    0,
    '2026-02-02 13:54',
    '2026-02-02 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11103,
    1,
    '2026-02-02 17:32',
    '2026-02-02',
    '17:32',
    '17:32',
    NULL,
    0,
    '2026-02-02 17:32',
    '2026-02-02 17:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11110,
    1,
    '2026-02-03 07:47',
    '2026-02-03',
    '07:47',
    '07:47',
    NULL,
    0,
    '2026-02-03 07:47',
    '2026-02-03 07:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11111,
    1,
    '2026-02-03 12:51',
    '2026-02-03',
    '12:51',
    '12:51',
    NULL,
    0,
    '2026-02-03 12:51',
    '2026-02-03 12:51'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11112,
    1,
    '2026-02-03 14:08',
    '2026-02-03',
    '14:08',
    '14:08',
    NULL,
    0,
    '2026-02-03 14:08',
    '2026-02-03 14:08'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11113,
    1,
    '2026-02-03 16:45',
    '2026-02-03',
    '16:45',
    '16:45',
    NULL,
    0,
    '2026-02-03 16:45',
    '2026-02-03 16:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11120,
    1,
    '2026-02-04 08:05',
    '2026-02-04',
    '08:05',
    '08:05',
    NULL,
    0,
    '2026-02-04 08:05',
    '2026-02-04 08:05'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11121,
    1,
    '2026-02-04 12:39',
    '2026-02-04',
    '12:39',
    '12:39',
    NULL,
    0,
    '2026-02-04 12:39',
    '2026-02-04 12:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11122,
    1,
    '2026-02-04 13:54',
    '2026-02-04',
    '13:54',
    '13:54',
    NULL,
    0,
    '2026-02-04 13:54',
    '2026-02-04 13:54'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11123,
    1,
    '2026-02-04 17:18',
    '2026-02-04',
    '17:18',
    '17:18',
    NULL,
    0,
    '2026-02-04 17:18',
    '2026-02-04 17:18'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11130,
    1,
    '2026-02-05 07:41',
    '2026-02-05',
    '07:41',
    '07:41',
    NULL,
    0,
    '2026-02-05 07:41',
    '2026-02-05 07:41'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11131,
    1,
    '2026-02-05 12:46',
    '2026-02-05',
    '12:46',
    '12:46',
    NULL,
    0,
    '2026-02-05 12:46',
    '2026-02-05 12:46'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11132,
    1,
    '2026-02-05 14:00',
    '2026-02-05',
    '14:00',
    '14:00',
    NULL,
    0,
    '2026-02-05 14:00',
    '2026-02-05 14:00'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11133,
    1,
    '2026-02-05 16:52',
    '2026-02-05',
    '16:52',
    '16:52',
    NULL,
    0,
    '2026-02-05 16:52',
    '2026-02-05 16:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11140,
    1,
    '2026-02-08 08:12',
    '2026-02-08',
    '08:12',
    '08:12',
    NULL,
    0,
    '2026-02-08 08:12',
    '2026-02-08 08:12'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11150,
    1,
    '2026-02-19 08:02',
    '2026-02-19',
    '08:02',
    '08:02',
    NULL,
    0,
    '2026-02-19 08:02',
    '2026-02-19 08:02'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11151,
    1,
    '2026-02-19 12:32',
    '2026-02-19',
    '12:32',
    '12:32',
    NULL,
    0,
    '2026-02-19 12:32',
    '2026-02-19 12:32'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11152,
    1,
    '2026-02-19 13:52',
    '2026-02-19',
    '13:52',
    '13:52',
    NULL,
    0,
    '2026-02-19 13:52',
    '2026-02-19 13:52'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11153,
    1,
    '2026-02-19 17:17',
    '2026-02-19',
    '17:17',
    '17:17',
    NULL,
    0,
    '2026-02-19 17:17',
    '2026-02-19 17:17'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11160,
    1,
    '2026-02-20 08:09',
    '2026-02-20',
    '08:09',
    '08:09',
    NULL,
    0,
    '2026-02-20 08:09',
    '2026-02-20 08:09'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11161,
    1,
    '2026-02-20 12:31',
    '2026-02-20',
    '12:31',
    '12:31',
    NULL,
    0,
    '2026-02-20 12:31',
    '2026-02-20 12:31'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11162,
    1,
    '2026-02-20 13:45',
    '2026-02-20',
    '13:45',
    '13:45',
    NULL,
    0,
    '2026-02-20 13:45',
    '2026-02-20 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11163,
    1,
    '2026-02-20 17:26',
    '2026-02-20',
    '17:26',
    '17:26',
    NULL,
    0,
    '2026-02-20 17:26',
    '2026-02-20 17:26'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11170,
    1,
    '2026-02-23 08:39',
    '2026-02-23',
    '08:39',
    '08:39',
    NULL,
    0,
    '2026-02-23 08:39',
    '2026-02-23 08:39'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11171,
    1,
    '2026-02-23 12:27',
    '2026-02-23',
    '12:27',
    '12:27',
    NULL,
    0,
    '2026-02-23 12:27',
    '2026-02-23 12:27'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11172,
    1,
    '2026-02-23 13:45',
    '2026-02-23',
    '13:45',
    '13:45',
    NULL,
    0,
    '2026-02-23 13:45',
    '2026-02-23 13:45'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11173,
    1,
    '2026-02-23 17:57',
    '2026-02-23',
    '17:57',
    '17:57',
    NULL,
    0,
    '2026-02-23 17:57',
    '2026-02-23 17:57'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11180,
    1,
    '2026-02-24 09:26',
    '2026-02-24',
    '09:26',
    '09:26',
    NULL,
    0,
    '2026-02-24 09:26',
    '2026-02-24 09:26'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11181,
    1,
    '2026-02-24 13:05',
    '2026-02-24',
    '13:05',
    '13:05',
    NULL,
    0,
    '2026-02-24 13:05',
    '2026-02-24 13:05'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11182,
    1,
    '2026-02-24 14:23',
    '2026-02-24',
    '14:23',
    '14:23',
    NULL,
    0,
    '2026-02-24 14:23',
    '2026-02-24 14:23'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11183,
    1,
    '2026-02-24 17:35',
    '2026-02-24',
    '17:35',
    '17:35',
    NULL,
    0,
    '2026-02-24 17:35',
    '2026-02-24 17:35'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11190,
    1,
    '2026-02-25 07:49',
    '2026-02-25',
    '07:49',
    '07:49',
    NULL,
    0,
    '2026-02-25 07:49',
    '2026-02-25 07:49'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11191,
    1,
    '2026-02-25 12:05',
    '2026-02-25',
    '12:05',
    '12:05',
    NULL,
    0,
    '2026-02-25 12:05',
    '2026-02-25 12:05'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11192,
    1,
    '2026-02-25 13:24',
    '2026-02-25',
    '13:24',
    '13:24',
    NULL,
    0,
    '2026-02-25 13:24',
    '2026-02-25 13:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11193,
    1,
    '2026-02-25 17:22',
    '2026-02-25',
    '17:22',
    '17:22',
    NULL,
    0,
    '2026-02-25 17:22',
    '2026-02-25 17:22'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11200,
    1,
    '2026-02-26 08:34',
    '2026-02-26',
    '08:34',
    '08:34',
    NULL,
    0,
    '2026-02-26 08:34',
    '2026-02-26 08:34'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11201,
    1,
    '2026-02-26 12:47',
    '2026-02-26',
    '12:47',
    '12:47',
    NULL,
    0,
    '2026-02-26 12:47',
    '2026-02-26 12:47'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11202,
    1,
    '2026-02-26 14:07',
    '2026-02-26',
    '14:07',
    '14:07',
    NULL,
    0,
    '2026-02-26 14:07',
    '2026-02-26 14:07'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11203,
    1,
    '2026-02-26 17:24',
    '2026-02-26',
    '17:24',
    '17:24',
    NULL,
    0,
    '2026-02-26 17:24',
    '2026-02-26 17:24'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11210,
    1,
    '2026-02-27 08:14',
    '2026-02-27',
    '08:14',
    '08:14',
    NULL,
    0,
    '2026-02-27 08:14',
    '2026-02-27 08:14'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11211,
    1,
    '2026-02-27 12:03',
    '2026-02-27',
    '12:03',
    '12:03',
    NULL,
    0,
    '2026-02-27 12:03',
    '2026-02-27 12:03'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11212,
    1,
    '2026-02-27 13:20',
    '2026-02-27',
    '13:20',
    '13:20',
    NULL,
    0,
    '2026-02-27 13:20',
    '2026-02-27 13:20'
);


INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    11213,
    1,
    '2026-02-27 17:46',
    '2026-02-27',
    '17:46',
    '17:46',
    NULL,
    0,
    '2026-02-27 17:46',
    '2026-02-27 17:46'
);


-- ============================================================
-- FIM DO SCRIPT DE IMPORTAÇÃO
-- ============================================================

-- RESUMO DA IMPORTAÇÃO:
-- Empregos: 1
-- Versões de Jornada: 2
-- Horários por Dia: 14
-- Feriados: 8
-- Ausências: 10
-- Registros de Ponto: 122 dias, 501 batidas
-- ============================================================