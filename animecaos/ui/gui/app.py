from __future__ import annotations

import sys

from PySide6.QtWidgets import QApplication

from animecaos.services.anime_service import AnimeService
from animecaos.services.history_service import HistoryService
from .main_window import MainWindow
from .theme import build_stylesheet


def run_gui(debug: bool = False) -> int:
    app = QApplication.instance() or QApplication(sys.argv)
    app.setStyle("Fusion")
    app.setStyleSheet(build_stylesheet())

    anime_service = AnimeService(debug=debug)
    history_service = HistoryService()
    window = MainWindow(anime_service=anime_service, history_service=history_service)
    window.show()

    return app.exec()
