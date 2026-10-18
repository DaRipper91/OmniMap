#!/usr/bin/env sh
# Installs OmniMap from the extracted tarball for the current user (no root):
#   app      -> ~/.local/share/omnimap/app
#   launcher -> ~/.local/bin/omnimap
#   menu     -> ~/.local/share/applications/omnimap.desktop
# Run with --uninstall to remove it. Your graph and settings are kept.
set -eu

here=$(cd "$(dirname "$0")" && pwd)
data="${XDG_DATA_HOME:-$HOME/.local/share}"
app="$data/omnimap/app"
bin="$HOME/.local/bin"

if [ "${1:-}" = "--uninstall" ]; then
    rm -rf "$app" "$bin/omnimap" "$data/applications/omnimap.desktop" "$data/icons/hicolor/512x512/apps/omnimap.png"
    echo "OmniMap removed. Your data is still in $data/omnimap and ~/.config/omnimap."
    exit 0
fi

mkdir -p "$app" "$bin" "$data/applications" "$data/icons/hicolor/512x512/apps"
rm -rf "$app"
mkdir -p "$app"
cp -r "$here/bin" "$here/lib" "$app/"
ln -sf "$app/bin/OmniMap" "$bin/omnimap"
cp "$here/omnimap.png" "$data/icons/hicolor/512x512/apps/omnimap.png"
# Desktop Entry Exec: quote the path (it may contain spaces) and escape \ " ` $ inside it.
exec_path=$(printf '%s' "$app/bin/OmniMap" | sed 's/[\\"`$]/\\&/g')
while IFS= read -r line; do
    case "$line" in
        Exec=*) printf 'Exec="%s"\n' "$exec_path" ;;
        *) printf '%s\n' "$line" ;;
    esac
done < "$here/omnimap.desktop" > "$data/applications/omnimap.desktop"
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "$data/applications" || true

echo "OmniMap installed. Launch it from your app menu or run: omnimap"
case ":$PATH:" in *":$bin:"*) ;; *) echo "Note: $bin is not on your PATH." ;; esac
