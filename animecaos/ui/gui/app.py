from __future__ import annotations

import sys

from PySide6.QtWidgets import QApplication

from animecaos.services.anime_service import AnimeService
from animecaos.services.history_service import HistoryService
from animecaos.services.watchlist_service import WatchlistService
from animecaos.services.anilist_service import AniListService
from .main_window import MainWindow
from .theme import build_stylesheet

# Correcao para o icone da barra de tarefas no Windows
if sys.platform == "win32":
    import ctypes
    try:
        myappid = 'mycompany.myproduct.subproduct.version' # arbitrary string
        ctypes.windll.shell32.SetCurrentProcessExplicitAppUserModelID(myappid)
    except Exception:
        pass

def run_gui(debug: bool = False) -> int:
    app = QApplication.instance() or QApplication(sys.argv)
    app.setStyle("Fusion")
    app.setStyleSheet(build_stylesheet())

    anime_service = AnimeService(debug=debug)
    history_service = HistoryService()
    watchlist_service = WatchlistService()
    anilist_service = AniListService()
    window = MainWindow(
        anime_service=anime_service,
        history_service=history_service,
        watchlist_service=watchlist_service,
        anilist_service=anilist_service
    )
    window.show()

    return app.exec()
