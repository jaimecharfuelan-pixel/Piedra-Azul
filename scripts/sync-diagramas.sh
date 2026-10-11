#!/usr/bin/env bash
# Sincroniza PlantUML de implementación hacia MkDocs y la copia en .kilo.
set -euo pipefail
ROOT="/home/jacsj/proyectos/Piedra Azul Ing Soft 3"
SRC="$ROOT/diagramas-por-modulo"
DOCS_PUML="$ROOT/backend/docs/diagramas/puml"
DOCS_MD="$ROOT/backend/docs/diagramas"
KILO_MOD="$ROOT/.kilo/worktrees/selective-armadillo/diagramas-por-modulo"
KILO_PUML="$ROOT/.kilo/worktrees/selective-armadillo/backend/docs/diagramas/puml"

mkdir -p "$DOCS_PUML"
cp -f "$SRC"/*.puml "$DOCS_PUML/"

if [ -d "$KILO_MOD" ]; then
  cp -f "$SRC"/*.puml "$KILO_MOD/"
fi
if [ -d "$KILO_PUML" ]; then
  mkdir -p "$KILO_PUML"
  cp -f "$SRC"/*.puml "$KILO_PUML/"
fi

# Regenera los .md de MkDocs que embeben el PlantUML
write_md() {
  local num="$1"
  local title="$2"
  local blurb="$3"
  local file="$4"
  local out="$DOCS_MD/${num}.md"
  {
    echo "# ${title}"
    echo
    echo "${blurb}"
    echo
    echo "Fuente: [\`puml/${file}\`](puml/${file})"
    echo
    echo '```plantuml'
    cat "$SRC/$file"
    echo '```'
  } > "$out"
}

write_md "02-identidad" "Identidad" \
  "Módulo \`com.piedraazul.identidad\`." \
  "02_modulo_identidad.puml"

write_md "03-personas" "Personas" \
  "Módulo \`com.piedraazul.personas\`." \
  "03_modulo_personas.puml"

write_md "05-citas" "Citas" \
  "Módulo \`com.piedraazul.citas\`." \
  "05_modulo_citas.puml"

echo "Diagramas sincronizados."
