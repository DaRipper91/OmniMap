#!/usr/bin/env bash
# Deterministic build script for OmniMap visual assets.
# Regenerates binary assets (social-preview.png) from SVG sources
# and verifies XML integrity across all production assets.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> Building OmniMap Visual Assets..."

# 1. Render social preview card (1280x640)
if command -v resvg >/dev/null 2>&1; then
  echo "  [resvg] Rendering social-preview.png (1280x640)..."
  resvg --width 1280 --height 640 src/social-preview.svg social-preview.png
elif command -v rsvg-convert >/dev/null 2>&1; then
  echo "  [rsvg-convert] Rendering social-preview.png (1280x640)..."
  rsvg-convert -w 1280 -h 640 -f png src/social-preview.svg -o social-preview.png
else
  echo "  [warn] Neither resvg nor rsvg-convert found; skipping PNG rasterization" >&2
fi

# 2. Validate XML structure on all SVGs
echo "  [critic] Validating SVG XML syntax..."
for svg in *.svg src/*.svg; do
  [[ -f "$svg" ]] || continue
  if command -v xmllint >/dev/null 2>&1; then
    xmllint --noout "$svg"
  else
    # Python fallback validation
    python3 -c "import xml.etree.ElementTree as ET; ET.parse('$svg')"
  fi
  printf "    ✓ %s\n" "$svg"
done

# 3. Verify output
if [[ -f social-preview.png ]]; then
  size=$(wc -c < social-preview.png | tr -d ' ')
  echo "  ✓ social-preview.png generated ($size bytes)"
fi

echo "==> Asset build complete."
