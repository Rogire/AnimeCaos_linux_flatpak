import sys
import os
from pathlib import Path


def get_base_path() -> Path:
    """Returns the base path, either the PyInstaller extracted folder or the current script directory."""
    if hasattr(sys, "_MEIPASS"):
        return Path(sys._MEIPASS)
    return Path(os.path.abspath("."))


def get_bin_path(binary_name: str) -> str:
    """Returns the path to a bundled binary in the 'bin' folder."""
    base = get_base_path()
    bin_path = base / "bin" / (binary_name + getattr(sys, "executable_ext", ""))
    
    # Simple win32 check for .exe
    if sys.platform == "win32" and not binary_name.endswith(".exe"):
        bin_path = base / "bin" / f"{binary_name}.exe"

    if bin_path.exists():
        return str(bin_path)
    
    # Fallback to system path (global PATH) if not bundled
    return binary_name
