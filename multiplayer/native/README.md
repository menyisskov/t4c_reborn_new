# Online Windows GLFW correction

Only `OnlineClient.main` selects this library. The single-player launcher and Maven
LWJGL dependencies continue to use their original native libraries. Other operating
systems retain the normal LWJGL selection. The online Windows build supports x86-64.

The bundled DLL is an **altered GLFW 3.4**, built from upstream commit
`7b6aead9fb88b3623e3b3725ebb42670cbe4c579` (annotated tag `3.4`, tag object
`a74efa0d5628b74adc0426af4c5710e287fa7c2c`) with only
`glfw-3.4-owned-window.patch`. GLFW's license is preserved here and beside the DLL.
Source: <https://github.com/glfw/glfw/tree/7b6aead9fb88b3623e3b3725ebb42670cbe4c579>.

## Correction

The observed Windows crash at `glfw.dll+0x101ab` maps to the modifier-key repair
loop reading `window->keys` after `GetActiveWindow` / `GetPropW(handle, L"GLFW")`.
Windows input queues can be attached across processes, sharing active-window state:
<https://learn.microsoft.com/windows/win32/api/winuser/nf-winuser-attachthreadinput>.
The property on another process's window is not a pointer valid in this process.

The patch finds the active handle in this library instance's own window list.
Unrecognized handles are ignored; owned windows retain the same key-release logic.
It does not hook Windows APIs, modify another process, or suppress focus events.
This also avoids trusting a different GLFW instance's property in the same process.

## Rebuild

Requirements: Git, Visual Studio 2022 C++ tools with Windows SDK, CMake, PowerShell.
From the repository root:

```powershell
./multiplayer/native/build-windows.ps1
mvn -Dtest=OnlineNativeLibraryTest package
```

The script pins the source revision, refuses unexpected source edits, applies the
reviewed patch, and builds Release x64 with static C runtime and `/Brepro`. Build
products live under ignored `target/`; the DLL, SHA-256 manifest and license are
packaged from `src/main/resources/online-native/windows-x86_64/`. The checked-in
build used MSVC 19.41.34123 (toolset 14.41.34120) and Windows SDK 10.0.26100.0.
Different compiler/SDK versions may produce a different hash; review and test any
binary change rather than assuming bit identity across toolchains.

At online startup Java verifies the packaged SHA-256, extracts into a fresh
per-process temporary directory, and sets LWJGL's GLFW library path before
initialization. Missing/corrupt Windows resources fail closed instead of silently
falling back to the known crashing build. Temporary files are scheduled for JVM
exit cleanup (a native crash may leave its small temporary directory behind).

## Native acceptance

Launch two independent real online clients with separate QA profiles. Switch focus
directly between them repeatedly, including while moving or attacking; confirm both
processes remain alive, movement stops on focus loss, rendering continues, and both
receive shared state. Repeat ordinary Alt-Tab as well as automation activation.
The checksum test checks packaging integrity; it is not a native focus regression
test, and no passing native claim should be made without the rendered check.
