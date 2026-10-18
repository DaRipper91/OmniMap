# Linux packaging

Desktop builds bundle their own trimmed Java runtime, so users don't need Java installed.
jpackage only builds for the CPU it runs on: build x86_64 packages on x86_64 and aarch64
packages (Asahi, ARM laptops) on aarch64. CI does both (`.github/workflows/release.yml`).

| Distro | Package | How |
| --- | --- | --- |
| CachyOS / Arch (x86_64) | `omnimap-<ver>-linux-x86_64.tar.gz` or `packaging/arch/PKGBUILD` | see below |
| Asahi Fedora (aarch64) | `omnimap-<ver>-1.aarch64.rpm` | `sudo dnf install ./omnimap-*.aarch64.rpm` |
| Asahi ALARM / other ARM | `omnimap-<ver>-linux-aarch64.tar.gz` | tarball install below |
| Fedora (x86_64) | `.x86_64.rpm` | `sudo dnf install ./omnimap-*.rpm` |
| Debian / Ubuntu | `.deb` | `sudo apt install ./omnimap_*.deb` |

## Tarball (any distro, no root)

```sh
tar xzf omnimap-1.0.0-linux-x86_64.tar.gz
./omnimap-1.0.0/install.sh          # installs to ~/.local, adds a menu entry
./omnimap-1.0.0/install.sh --uninstall
```

Build it yourself with `./gradlew :desktop:packageLinuxTarball`; the output is in
`desktop/build/compose/binaries/main/tarball/`.

## Arch / CachyOS from source

```sh
cd packaging/arch
makepkg -si
```

`PKGBUILD` builds the `main` branch with `jdk21-openjdk` and installs to `/opt/omnimap`,
with `omnimap` on the PATH and a menu entry. It works on `x86_64` and `aarch64`.

## Where OmniMap keeps your data

- Graph: `~/.local/share/omnimap/graph.json` (`$XDG_DATA_HOME`)
- Settings and API key: `~/.config/omnimap/settings.properties` (`$XDG_CONFIG_HOME`, mode 600)

Uninstalling never removes these.

## Troubleshooting

- **Blank or flickering window** (possible on Asahi's GPU drivers or in VMs): force
  software rendering with `SKIKO_RENDER_API=SOFTWARE omnimap`.
