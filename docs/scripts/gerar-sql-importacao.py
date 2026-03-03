#!/usr/bin/env python3
"""
Script para gerar comandos SQL de importação a partir do arquivo JSON.

Este script lê o arquivo importacao-historica.json e gera comandos SQL
para inserir os dados diretamente no banco de dados Room do app MeuPonto.

Autor: Thiago Macedo
Data: 02/03/2026
"""

import json
from datetime import datetime
from typing import List

# Mapeamento de dias da semana para o enum DiaSemana
DIA_SEMANA_MAP = {
    "SEGUNDA": 1,
    "TERCA": 2,
    "QUARTA": 3,
    "QUINTA": 4,
    "SEXTA": 5,
    "SABADO": 6,
    "DOMINGO": 7
}

# Mapeamento de tipos de ausência para TipoAusencia
TIPO_AUSENCIA_MAP = {
    "FERIAS": "FERIAS",
    "ATESTADO": "ATESTADO",
    "FALTA_JUSTIFICADA": "FALTA_JUSTIFICADA",
    "FOLGA": "FOLGA",
    "FALTA_INJUSTIFICADA": "FALTA_INJUSTIFICADA",
    "FALTA": "FALTA_INJUSTIFICADA"
}

# Mapeamento de tipos de folga para TipoFolga
TIPO_FOLGA_MAP = {
    "COMPENSADO": "COMPENSACAO",
    "D.S.R.": "COMPENSACAO",
    "DSR": "COMPENSACAO",
    "DAY-OFF": "DAY_OFF",
    "DAY OFF": "DAY_OFF"
}


def gerar_emprego(emprego_data, emprego_id=1):
    """Gera SQL para inserir emprego."""
    data_inicio = emprego_data.get("dataInicioTrabalho", "2025-08-11")
    data_hoje = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    return f"""
-- ===== EMPREGO =====
INSERT OR REPLACE INTO empregos (
    id, nome, descricao, dataInicioTrabalho, ativo, arquivado, ordem, criadoEm, atualizadoEm
) VALUES (
    {emprego_id},
    '{emprego_data['nome']}',
    {f"'{emprego_data['descricao']}'" if emprego_data.get('descricao') else 'NULL'},
    '{data_inicio}',
    1,
    0,
    0,
    '{data_hoje}',
    '{data_hoje}'
);
"""


def gerar_versao_jornada(versao_data, emprego_id, numero_versao):
    """Gera SQL para inserir versão de jornada."""
    data_inicio = versao_data["dataInicio"]
    data_fim_sql = f"'{versao_data['dataFim']}'" if versao_data.get("dataFim") else "NULL"
    vigente = "0" if versao_data.get("dataFim") else "1"
    data_hoje = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    return f"""
-- ===== VERSÃO JORNADA {numero_versao}: {versao_data['descricao']} =====
INSERT OR REPLACE INTO versoes_jornada (
    id, empregoId, dataInicio, dataFim, descricao, numeroVersao, vigente,
    jornadaMaximaDiariaMinutos, intervaloMinimoInterjornadaMinutos,
    toleranciaIntervaloMaisMinutos, criadoEm, atualizadoEm
) VALUES (
    {numero_versao},
    {emprego_id},
    '{data_inicio}',
    {data_fim_sql},
    '{versao_data['descricao']}',
    {numero_versao},
    {vigente},
    {versao_data['jornadaMaximaDiariaMinutos']},
    {versao_data['intervaloMinimoInterjornadaMinutos']},
    {versao_data['toleranciaIntervaloMaisMinutos']},
    '{data_hoje}',
    '{data_hoje}'
);
"""


def gerar_horarios_dia_semana(horarios_data, emprego_id, versao_jornada_id, inicio_id):
    """Gera SQL para inserir horários de cada dia da semana."""
    sqls = []
    data_hoje = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    for i, horario in enumerate(horarios_data, start=inicio_id):
        dia_semana = DIA_SEMANA_MAP[horario["diaSemana"]]
        ativo = "1" if horario["cargaHorariaMinutos"] > 0 else "0"
        
        sql = f"""
INSERT OR REPLACE INTO horarios_dia_semana (
    id, empregoId, versaoJornadaId, diaSemana, ativo, cargaHorariaMinutos,
    entradaIdeal, saidaIntervaloIdeal, voltaIntervaloIdeal, saidaIdeal,
    intervaloMinimoMinutos, toleranciaIntervaloMaisMinutos, toleranciaEntradaMinutos, toleranciaSaidaMinutos,
    criadoEm, atualizadoEm
) VALUES (
    {i},
    {emprego_id},
    {versao_jornada_id},
    {dia_semana},
    {ativo},
    {horario['cargaHorariaMinutos']},
    '{horario['entradaIdeal']}',
    {f"'{horario['saidaIntervaloIdeal']}'" if horario.get('saidaIntervaloIdeal') else 'NULL'},
    {f"'{horario['voltaIntervaloIdeal']}'" if horario.get('voltaIntervaloIdeal') else 'NULL'},
    '{horario['saidaIdeal']}',
    {horario['intervaloMinimoMinutos']},
    0,
    0,
    {horario['toleranciaSaidaMinutos']},
    '{data_hoje}',
    '{data_hoje}'
);
"""
        sqls.append(sql)
    
    return "\n".join(sqls)


def gerar_pontos(pontos_data, emprego_id, inicio_id=1000):
    """Gera SQL para inserir registros de ponto."""
    sqls = []
    
    for i, ponto_dia in enumerate(pontos_data, start=inicio_id):
        data = ponto_dia["data"]
        horarios = ponto_dia["horarios"]
        
        for j, horario in enumerate(horarios):
            ponto_id = i * 10 + j  # Ex: 1000, 1001, 1002, 1003 para primeiro dia
            
            # Formatar data e hora
            data_hora = f"{data} {horario}"
            
            sql = f"""
INSERT OR REPLACE INTO pontos (
    id, empregoId, dataHora, data, hora, horaConsiderada, observacao,
    isEditadoManualmente, criadoEm, atualizadoEm
) VALUES (
    {ponto_id},
    {emprego_id},
    '{data_hora}',
    '{data}',
    '{horario}',
    '{horario}',
    NULL,
    0,
    '{data_hora}',
    '{data_hora}'
);
"""
            sqls.append(sql)
    
    return "\n".join(sqls)


def gerar_ausencias(ausencias_data, emprego_id, inicio_id=5000):
    """Gera SQL para inserir ausências (Férias, Folgas, etc.)."""
    sqls = []
    data_hoje = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    for i, ausencia in enumerate(ausencias_data, start=inicio_id):
        tipo = ausencia["tipo"]
        data_inicio = ausencia["dataInicio"]
        data_fim = ausencia.get("dataFim") or data_inicio
        
        tipo_folga = None
        if tipo == "FOLGA" and ausencia.get("descricao"):
            descricao_upper = ausencia["descricao"].upper()
            for chave, valor in TIPO_FOLGA_MAP.items():
                if chave in descricao_upper:
                    tipo_folga = valor
                    break
        
        tipo_folga_sql = f"'{tipo_folga}'" if tipo_folga else "NULL"
        
        sql = f"""
-- AUSÊNCIA: {tipo} - {data_inicio} {'a ' + data_fim if data_fim != data_inicio else ''}
INSERT OR REPLACE INTO ausencias (
    id, empregoId, tipo, tipoFolga, dataInicio, dataFim, descricao, observacao,
    ativo, criadoEm, atualizadoEm
) VALUES (
    {i},
    {emprego_id},
    '{TIPO_AUSENCIA_MAP.get(tipo, tipo)}',
    {tipo_folga_sql},
    '{data_inicio}',
    '{data_fim}',
    {f"'{ausencia['descricao']}'" if ausencia.get('descricao') else 'NULL'},
    {f"'{ausencia['observacao']}'" if ausencia.get('observacao') else 'NULL'},
    1,
    '{data_hoje}',
    '{data_hoje}'
);
"""
        sqls.append(sql)
    
    return "\n".join(sqls)


def gerar_feriados(feriados_data, inicio_id=6000):
    """Gera SQL para inserir feriados."""
    sqls = []
    data_hoje = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    for i, feriado in enumerate(feriados_data, start=inicio_id):
        data = feriado["data"]
        
        sql = f"""
-- FERIADO: {feriado['descricao']} - {data}
INSERT OR REPLACE INTO feriados (
    id, nome, tipo, recorrencia, abrangencia, diaMes, dataEspecifica, anoReferencia,
    uf, municipio, empregoId, ativo, observacao, criadoEm, atualizadoEm
) VALUES (
    {i},
    '{feriado['descricao']}',
    'ESPECIFICO',
    'ANUAL',
    'NACIONAL',
    NULL,
    '{data}',
    NULL,
    'AM',
    'Manaus',
    NULL,
    1,
    NULL,
    '{data_hoje}',
    '{data_hoje}'
);
"""
        sqls.append(sql)
    
    return "\n".join(sqls)


def main():
    """Função principal."""
    import os
    import sys
    
    print("Gerador de SQL para Importacao de Dados Historicos")
    print("=" * 60)
    print()
    
    # Ler arquivo JSON
    # Obter o diretório atual do script
    script_dir = os.path.dirname(os.path.abspath(__file__))
    json_file = os.path.join(script_dir, "../dados/importacao-historica.json")
    
    try:
        with open(json_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        print(f"[OK] Arquivo JSON lido: {json_file}")
        print(f"[OK] Versao: {data['versao']}")
        print(f"[OK] Emprego: {data['emprego']['nome']}")
        print(f"[OK] Versoes de jornada: {len(data['versoesJornada'])}")
        print(f"[OK] Dias com pontos: {len(data['pontos'])}")
        print(f"[OK] Ausencias: {len(data['ausencias'])}")
        print(f"[OK] Feriados: {len(data['feriados'])}")
        print()
    except FileNotFoundError:
        print(f"[ERRO] Arquivo nao encontrado: {json_file}")
        print(f"  Diretorio atual: {os.getcwd()}")
        return
    except json.JSONDecodeError as e:
        print(f"[ERRO] Erro ao ler JSON: {e}")
        return
    
    # IDs
    emprego_id = 1
    versao_jornada_1_id = 1
    versao_jornada_2_id = 2
    horarios_inicio_id = 100
    pontos_inicio_id = 1000
    ausencias_inicio_id = 5000
    feriados_inicio_id = 6000
    
    # Gerar SQL completo
    sql_completo = []
    
    sql_completo.append("-- ============================================================")
    sql_completo.append("-- SCRIPT DE IMPORTAÇÃO DE DADOS HISTÓRICOS")
    sql_completo.append("-- Projeto: MeuPonto")
    sql_completo.append("-- Autor: Thiago Macedo")
    sql_completo.append(f"-- Data: {datetime.now().strftime('%d/%m/%Y %H:%M:%S')} (INSERT OR REPLACE para atualizar dados existentes)")
    sql_completo.append("-- ============================================================")
    sql_completo.append("--")
    sql_completo.append("-- ATENÇÃO: Execute os comandos nesta ordem:")
    sql_completo.append("-- 1. Emprego")
    sql_completo.append("-- 2. Versões de Jornada")
    sql_completo.append("-- 3. Horários por Dia da Semana")
    sql_completo.append("-- 4. Feriados")
    sql_completo.append("-- 5. Ausências")
    sql_completo.append("-- 6. Pontos")
    sql_completo.append("--")
    sql_completo.append("-- ============================================================")
    sql_completo.append("")
    
    # Emprego
    sql_completo.append(gerar_emprego(data["emprego"], emprego_id))
    sql_completo.append("")
    
    # Versões de jornada
    sql_completo.append(gerar_versao_jornada(data["versoesJornada"][0], emprego_id, versao_jornada_1_id))
    sql_completo.append(gerar_versao_jornada(data["versoesJornada"][1], emprego_id, versao_jornada_2_id))
    sql_completo.append("")
    
    # Horários por dia da semana
    sql_completo.append("-- ===== HORÁRIOS JORNADA 2025 =====")
    sql_completo.append(gerar_horarios_dia_semana(
        data["versoesJornada"][0]["horariosDiasSemana"],
        emprego_id,
        versao_jornada_1_id,
        horarios_inicio_id
    ))
    sql_completo.append("")
    
    sql_completo.append("-- ===== HORÁRIOS JORNADA 2026 =====")
    sql_completo.append(gerar_horarios_dia_semana(
        data["versoesJornada"][1]["horariosDiasSemana"],
        emprego_id,
        versao_jornada_2_id,
        horarios_inicio_id + 7
    ))
    sql_completo.append("")
    
    # Feriados
    sql_completo.append("-- ===== FERIADOS =====")
    sql_completo.append(gerar_feriados(data["feriados"], feriados_inicio_id))
    sql_completo.append("")
    
    # Ausências
    sql_completo.append("-- ===== AUSÊNCIAS =====")
    sql_completo.append(gerar_ausencias(data["ausencias"], emprego_id, ausencias_inicio_id))
    sql_completo.append("")
    
    # Pontos
    sql_completo.append("-- ===== REGISTROS DE PONTO =====")
    sql_completo.append(gerar_pontos(data["pontos"], emprego_id, pontos_inicio_id))
    sql_completo.append("")
    
    sql_completo.append("-- ============================================================")
    sql_completo.append("-- FIM DO SCRIPT DE IMPORTAÇÃO")
    sql_completo.append("-- ============================================================")
    sql_completo.append("")
    sql_completo.append("-- RESUMO DA IMPORTAÇÃO:")
    sql_completo.append(f"-- Empregos: 1")
    sql_completo.append(f"-- Versões de Jornada: 2")
    sql_completo.append(f"-- Horários por Dia: 14")
    sql_completo.append(f"-- Feriados: {len(data['feriados'])}")
    sql_completo.append(f"-- Ausências: {len(data['ausencias'])}")
    sql_completo.append(f"-- Registros de Ponto: {len(data['pontos'])} dias, {sum(len(p['horarios']) for p in data['pontos'])} batidas")
    sql_completo.append("-- ============================================================")
    
    # Salvar arquivo SQL
    sql_file = os.path.join(script_dir, "../dados/importacao-historica.sql")
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write("\n".join(sql_completo))
    
    print("[OK] Script SQL gerado com sucesso!")
    print(f"[OK] Arquivo: {sql_file}")
    print()
    print("Para executar:")
    print("1. Abra o banco de dados do app (usando DB Browser for SQLite)")
    print("2. Abra o arquivo importacao-historica.sql")
    print("3. Execute todos os comandos SQL")
    print()


if __name__ == "__main__":
    main()