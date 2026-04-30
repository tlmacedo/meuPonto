#!/usr/bin/env python3
from pathlib import Path
import shutil
import datetime

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main/java/br/com/tlmacedo/meuponto"

BACKUP_DIR = ROOT / f".backup_refactor_{datetime.datetime.now().strftime('%Y%m%d_%H%M%S')}"

REPLACEMENTS = {
    "TipoAusencia.FERIAS": "TipoAusencia.Ferias",
    "TipoAusencia.ATESTADO": "TipoAusencia.Atestado",
    "TipoAusencia.DECLARACAO": "TipoAusencia.Declaracao",
    "TipoAusencia.FOLGA": "TipoAusencia.DayOff",
    "TipoAusencia.FALTA_JUSTIFICADA": "TipoAusencia.Justificada",
    "TipoAusencia.FALTA_INJUSTIFICADA": "TipoAusencia.Injustificada",

    "TipoDiaEspecial.NORMAL": "null",
    "TipoDiaEspecial.DESCANSO": "TipoDiaEspecial.Descanso",
    "TipoDiaEspecial.FERIAS": "TipoDiaEspecial.Ferias",
    "TipoDiaEspecial.ATESTADO": "TipoDiaEspecial.Atestado",
    "TipoDiaEspecial.DECLARACAO": "TipoDiaEspecial.Declaracao",
    "TipoDiaEspecial.FOLGA": "TipoDiaEspecial.Folga",
    "TipoDiaEspecial.FALTA_JUSTIFICADA": "TipoDiaEspecial.FaltaJustificada",
    "TipoDiaEspecial.FALTA_INJUSTIFICADA": "TipoDiaEspecial.FaltaInjustificada",
    "TipoDiaEspecial.FERIADO": "TipoDiaEspecial.Feriado",
    "TipoDiaEspecial.FERIADO_TRABALHADO": "TipoDiaEspecial.FeriadoTrabalhado",
    "TipoDiaEspecial.PONTE": "TipoDiaEspecial.Ponte",
    "TipoDiaEspecial.FACULTATIVO": "TipoDiaEspecial.Facultativo",

    "TipoFeriado.FERIADO": "TipoFeriado.Feriado",
    "TipoFeriado.PONTE": "TipoFeriado.Ponte",
    "TipoFeriado.FACULTATIVO": "TipoFeriado.Facultativo",

    ".toTipoDiaEspecial(ausencia.tipoFolga)": ".toTipoDiaEspecial()",
    ".toTipoDiaEspecial(tipoFolga)": ".toTipoDiaEspecial()",
    ".toTipoDiaEspecial()": ".toTipoDiaEspecial()",
}

IMPORT_EXTENSION = (
    "import br.com.tlmacedo.meuponto.domain.model.extensions.toTipoDiaEspecial"
)

FILES_TO_ENSURE_IMPORT = [
    "domain/usecase/ausencia/BuscarAusenciaPorDataUseCase.kt",
    "domain/usecase/ponto/ObterResumoDiaCompletoUseCase.kt",
    "domain/usecase/ponto/ObterTipoDiaEspecialUseCase.kt",
    "presentation/screen/home/HomeViewModel.kt",
]


def backup_file(file: Path):
    relative = file.relative_to(ROOT)
    target = BACKUP_DIR / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(file, target)


def apply_replacements(file: Path):
    original = file.read_text(encoding="utf-8")
    updated = original

    for old, new in REPLACEMENTS.items():
        updated = updated.replace(old, new)

    if updated != original:
        backup_file(file)
        file.write_text(updated, encoding="utf-8")
        return True

    return False


def ensure_import(file: Path):
    path = SRC / file
    if not path.exists():
        return False

    text = path.read_text(encoding="utf-8")

    if "toTipoDiaEspecial()" not in text:
        return False

    if IMPORT_EXTENSION in text:
        return False

    lines = text.splitlines()
    package_index = next(
        (i for i, line in enumerate(lines) if line.startswith("package ")),
        None
    )

    if package_index is None:
        return False

    insert_index = package_index + 1

    while insert_index < len(lines) and lines[insert_index].startswith("import "):
        insert_index += 1

    lines.insert(insert_index, IMPORT_EXTENSION)

    backup_file(path)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return True


def create_extension_file():
    path = SRC / "domain/model/extensions/TipoAusenciaExtensions.kt"

    if path.exists():
        print(f"SKIP extension já existe: {path}")
        return

    path.parent.mkdir(parents=True, exist_ok=True)

    content = """package br.com.tlmacedo.meuponto.domain.model.extensions


import br.com.tlmacedo.meuponto.domain.model.ausencia.TipoAusencia

fun TipoAusencia.toTipoDiaEspecial(): TipoAusencia? {
    return when (this) {
        TipoAusencia.Ferias -> TipoAusencia?.Ferias
        TipoAusencia.Atestado -> TipoAusencia?.Atestado
        TipoAusencia.Declaracao -> TipoAusencia?.Declaracao
        TipoAusencia.DayOff -> TipoAusencia?.Folga
        TipoAusencia.Justificada -> TipoAusencia?.FaltaJustificada
        TipoAusencia.Injustificada -> TipoAusencia?.FaltaInjustificada
        TipoAusencia.DiaPonte -> TipoAusencia?.Ponte
        TipoAusencia.Facultativo -> TipoAusencia?.Facultativo
        TipoAusencia.CompensacaoBanco -> TipoAusencia?.Folga
        TipoAusencia.DiminuirBanco -> TipoAusencia?.Folga
    }
}
"""

    path.write_text(content, encoding="utf-8")
    print(f"CREATE {path}")


def main():
    if not SRC.exists():
        raise SystemExit(f"Pasta não encontrada: {SRC}")

    changed = []

    for file in SRC.rglob("*.kt"):
        if apply_replacements(file):
            changed.append(file)

    create_extension_file()

    imports_added = []
    for file in FILES_TO_ENSURE_IMPORT:
        if ensure_import(Path(file)):
            imports_added.append(file)

    print()
    print("Refactor concluído.")
    print(f"Backup em: {BACKUP_DIR}")
    print(f"Arquivos alterados: {len(changed)}")
    print(f"Imports adicionados: {len(imports_added)}")

    if changed:
        print("\nArquivos modificados:")
        for file in changed:
            print(f"- {file.relative_to(ROOT)}")

    if imports_added:
        print("\nImports adicionados em:")
        for file in imports_added:
            print(f"- {file}")


if __name__ == "__main__":
    main()