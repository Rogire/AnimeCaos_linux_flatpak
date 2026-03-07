from __future__ import annotations

from datetime import datetime
from typing import Callable
import os
import sys

from PySide6.QtCore import Qt, QThreadPool
from PySide6.QtGui import QIcon
from PySide6.QtWidgets import (
    QFrame,
    QHBoxLayout,
    QLabel,
    QLineEdit,
    QListWidget,
    QListWidgetItem,
    QMainWindow,
    QMessageBox,
    QPlainTextEdit,
    QProgressBar,
    QPushButton,
    QSplitter,
    QVBoxLayout,
    QWidget,
)

from animecaos.services.anime_service import AnimeService
from animecaos.services.history_service import HistoryEntry, HistoryService
from .workers import FunctionWorker


class MainWindow(QMainWindow):
    def __init__(self, anime_service: AnimeService, history_service: HistoryService) -> None:
        super().__init__()
        self._anime_service = anime_service
        self._history_service = history_service
        self._thread_pool = QThreadPool.globalInstance()
        self._active_workers: set[FunctionWorker] = set()
        self._busy = False
        self._current_anime: str | None = None
        self._episodes_anime: str | None = None
        self._current_episode_index = -1

        self.setWindowTitle("animecaos")
        
        try:
            base_path = sys._MEIPASS
        except AttributeError:
            base_path = os.path.abspath(".")
        icon_path = os.path.join(base_path, "icon.png")
        self.setWindowIcon(QIcon(icon_path))

        self.resize(1320, 820)
        self.setMinimumSize(1024, 640)

        self._build_ui()
        self._bind_events()
        self._reload_history()
        self._sync_controls()

    def _build_ui(self) -> None:
        root = QWidget(self)
        root.setObjectName("RootContainer")
        self.setCentralWidget(root)

        main_layout = QVBoxLayout(root)
        main_layout.setContentsMargins(16, 16, 16, 16)
        main_layout.setSpacing(12)

        header = self._create_panel()
        header_layout = QHBoxLayout(header)
        header_layout.setContentsMargins(16, 14, 16, 14)
        header_layout.setSpacing(12)

        branding_layout = QVBoxLayout()
        branding_layout.setSpacing(2)

        title = QLabel("animecaos")
        title.setObjectName("AppTitle")
        branding_layout.addWidget(title)
        header_layout.addLayout(branding_layout, 1)

        controls_layout = QHBoxLayout()
        controls_layout.setSpacing(8)
        self.search_input = QLineEdit()
        self.search_input.setPlaceholderText("Pesquisar anime...")
        self.search_input.setMinimumWidth(320)

        self.search_button = QPushButton("Buscar")
        self.search_button.setObjectName("PrimaryButton")
        self.reload_history_button = QPushButton("Atualizar historico")

        controls_layout.addWidget(self.search_input, 1)
        controls_layout.addWidget(self.search_button)
        controls_layout.addWidget(self.reload_history_button)
        header_layout.addLayout(controls_layout, 2)
        main_layout.addWidget(header)

        body_splitter = QSplitter(Qt.Horizontal)
        body_splitter.setChildrenCollapsible(False)
        main_layout.addWidget(body_splitter, 1)

        left_panel = self._build_history_panel()
        center_panel = self._build_library_panel()
        right_panel = self._build_details_panel()

        body_splitter.addWidget(left_panel)
        body_splitter.addWidget(center_panel)
        body_splitter.addWidget(right_panel)
        body_splitter.setStretchFactor(0, 28)
        body_splitter.setStretchFactor(1, 44)
        body_splitter.setStretchFactor(2, 28)

        self.status_label = QLabel("Pronto.")
        self.status_label.setObjectName("MutedText")
        main_layout.addWidget(self.status_label)

    def _build_history_panel(self) -> QWidget:
        panel = self._create_panel()
        layout = QVBoxLayout(panel)
        layout.setContentsMargins(12, 12, 12, 12)
        layout.setSpacing(8)

        title = QLabel("Continue Assistindo")
        title.setObjectName("SectionTitle")
        hint = QLabel("Selecione um item do historico salvo")
        hint.setObjectName("MutedText")
        layout.addWidget(title)
        layout.addWidget(hint)

        self.history_list = QListWidget()
        self.history_list.setUniformItemSizes(True)
        layout.addWidget(self.history_list, 1)

        self.resume_history_button = QPushButton("Carregar historico selecionado")
        layout.addWidget(self.resume_history_button)

        return panel

    def _build_library_panel(self) -> QWidget:
        panel = QWidget()
        layout = QVBoxLayout(panel)
        layout.setContentsMargins(0, 0, 0, 0)
        layout.setSpacing(10)

        splitter = QSplitter(Qt.Vertical)
        splitter.setChildrenCollapsible(False)
        layout.addWidget(splitter)

        anime_panel = self._create_panel()
        anime_layout = QVBoxLayout(anime_panel)
        anime_layout.setContentsMargins(12, 12, 12, 12)
        anime_layout.setSpacing(8)

        anime_header = QHBoxLayout()
        anime_title = QLabel("Resultados da Busca")
        anime_title.setObjectName("SectionTitle")
        self.load_episodes_button = QPushButton("Carregar episodios")
        anime_header.addWidget(anime_title)
        anime_header.addStretch(1)
        anime_header.addWidget(self.load_episodes_button)
        anime_layout.addLayout(anime_header)

        self.anime_list = QListWidget()
        self.anime_list.setUniformItemSizes(True)
        anime_layout.addWidget(self.anime_list, 1)

        episodes_panel = self._create_panel()
        episodes_layout = QVBoxLayout(episodes_panel)
        episodes_layout.setContentsMargins(12, 12, 12, 12)
        episodes_layout.setSpacing(8)

        episodes_header = QHBoxLayout()
        episodes_title = QLabel("Episodios")
        episodes_title.setObjectName("SectionTitle")
        self.play_button = QPushButton("Reproduzir selecionado")
        self.play_button.setObjectName("PrimaryButton")
        episodes_header.addWidget(episodes_title)
        episodes_header.addStretch(1)
        episodes_header.addWidget(self.play_button)
        episodes_layout.addLayout(episodes_header)

        self.episode_list = QListWidget()
        self.episode_list.setUniformItemSizes(True)
        episodes_layout.addWidget(self.episode_list, 1)

        splitter.addWidget(anime_panel)
        splitter.addWidget(episodes_panel)
        splitter.setStretchFactor(0, 55)
        splitter.setStretchFactor(1, 45)

        return panel

    def _build_details_panel(self) -> QWidget:
        panel = self._create_panel()
        layout = QVBoxLayout(panel)
        layout.setContentsMargins(12, 12, 12, 12)
        layout.setSpacing(8)

        title = QLabel("Painel de Controle")
        title.setObjectName("SectionTitle")
        layout.addWidget(title)

        self.selected_anime_label = QLabel("Anime: -")
        self.selected_anime_label.setWordWrap(True)
        self.selected_episode_label = QLabel("Episodio: -")
        self.selected_episode_label.setWordWrap(True)
        layout.addWidget(self.selected_anime_label)
        layout.addWidget(self.selected_episode_label)

        playback_controls = QHBoxLayout()
        self.prev_button = QPushButton("Anterior")
        self.next_button = QPushButton("Proximo")
        playback_controls.addWidget(self.prev_button)
        playback_controls.addWidget(self.next_button)
        layout.addLayout(playback_controls)

        url_title = QLabel("URL de reproducao")
        url_title.setObjectName("MutedText")
        self.url_output = QLineEdit()
        self.url_output.setReadOnly(True)
        layout.addWidget(url_title)
        layout.addWidget(self.url_output)

        self.progress_bar = QProgressBar()
        self.progress_bar.setTextVisible(False)
        self.progress_bar.setMaximumHeight(10)
        self.progress_bar.setVisible(False)
        layout.addWidget(self.progress_bar)

        logs_title = QLabel("Eventos")
        logs_title.setObjectName("MutedText")
        self.log_output = QPlainTextEdit()
        self.log_output.setReadOnly(True)
        self.log_output.setMaximumBlockCount(400)
        layout.addWidget(logs_title)
        layout.addWidget(self.log_output, 1)

        return panel

    def _bind_events(self) -> None:
        self.search_input.returnPressed.connect(self._on_search_clicked)
        self.search_input.textChanged.connect(self._sync_controls)
        self.search_button.clicked.connect(self._on_search_clicked)
        self.reload_history_button.clicked.connect(self._reload_history)
        self.resume_history_button.clicked.connect(self._on_resume_history_clicked)
        self.history_list.itemSelectionChanged.connect(self._sync_controls)
        self.history_list.itemDoubleClicked.connect(lambda _: self._on_resume_history_clicked())

        self.anime_list.itemSelectionChanged.connect(self._on_anime_selection_changed)
        self.anime_list.itemDoubleClicked.connect(lambda _: self._on_load_episodes_clicked())
        self.load_episodes_button.clicked.connect(self._on_load_episodes_clicked)

        self.episode_list.itemSelectionChanged.connect(self._on_episode_selection_changed)
        self.episode_list.itemDoubleClicked.connect(lambda _: self._on_play_clicked())
        self.play_button.clicked.connect(self._on_play_clicked)
        self.prev_button.clicked.connect(self._on_previous_clicked)
        self.next_button.clicked.connect(self._on_next_clicked)

    def _create_panel(self) -> QFrame:
        panel = QFrame()
        panel.setObjectName("GlassPanel")
        return panel

    def _run_task(self, status_message: str, task: Callable[[], object], on_success: Callable[[object], None]) -> None:
        if self._busy:
            self._set_status("Aguarde a tarefa atual finalizar.")
            return

        self._set_busy(True, status_message)
        worker = FunctionWorker(task)
        self._active_workers.add(worker)
        worker.signals.succeeded.connect(on_success)
        worker.signals.failed.connect(self._on_task_failed)
        worker.signals.finished.connect(lambda current=worker: self._on_task_finished(current))
        self._thread_pool.start(worker)

    def _on_task_failed(self, error_text: str) -> None:
        self._append_log(f"Erro: {error_text}")
        self._set_status("Falha na operacao.")
        summary = error_text.splitlines()[0] if error_text else "Erro inesperado."
        QMessageBox.critical(self, "Erro", summary)

    def _on_task_finished(self, worker: FunctionWorker) -> None:
        self._active_workers.discard(worker)
        self._set_busy(False)
        self._sync_controls()

    def _set_busy(self, busy: bool, status_message: str = "") -> None:
        self._busy = busy
        self.progress_bar.setVisible(busy)
        if busy:
            self.progress_bar.setRange(0, 0)
            self._set_status(status_message)
        else:
            self.progress_bar.setRange(0, 1)
            self.progress_bar.setValue(0)

        self._sync_controls()

    def _set_status(self, text: str) -> None:
        self.status_label.setText(text)

    def _append_log(self, text: str) -> None:
        stamp = datetime.now().strftime("%H:%M:%S")
        self.log_output.appendPlainText(f"[{stamp}] {text}")

    def _selected_anime(self) -> str:
        item = self.anime_list.currentItem()
        return item.text() if item else ""

    def _selected_episode_index(self) -> int:
        return self.episode_list.currentRow()

    def _on_search_clicked(self) -> None:
        query = self.search_input.text().strip()
        if not query:
            self._set_status("Digite um termo para buscar.")
            return

        self._run_task(
            status_message=f"Buscando '{query}'...",
            task=lambda: self._anime_service.search_animes(query),
            on_success=self._on_search_finished,
        )

    def _on_search_finished(self, anime_titles: object) -> None:
        if not isinstance(anime_titles, list):
            self._set_status("Resposta invalida da busca.")
            return

        self._populate_list(self.anime_list, [str(title) for title in anime_titles])
        self._populate_list(self.episode_list, [])
        self._current_anime = None
        self._episodes_anime = None
        self._current_episode_index = -1
        self.url_output.clear()
        self.selected_anime_label.setText("Anime: -")
        self.selected_episode_label.setText("Episodio: -")

        if not anime_titles:
            self._set_status("Nenhum anime encontrado.")
            self._append_log("Busca sem resultados.")
            return

        self.anime_list.setCurrentRow(0)
        self._set_status(f"{len(anime_titles)} animes encontrados.")
        self._append_log(f"Busca concluida com {len(anime_titles)} resultado(s).")

    def _on_anime_selection_changed(self) -> None:
        anime = self._selected_anime()
        if anime:
            self.selected_anime_label.setText(f"Anime: {anime}")
            self._current_anime = anime
            if anime != self._episodes_anime:
                self._populate_list(self.episode_list, [])
                self._current_episode_index = -1
                self.url_output.clear()
                self.selected_episode_label.setText("Episodio: -")
            self._set_status("Anime selecionado. Carregue os episodios.")
        self._sync_controls()

    def _on_load_episodes_clicked(self) -> None:
        anime = self._selected_anime()
        if not anime:
            self._set_status("Selecione um anime primeiro.")
            return

        self._run_task(
            status_message=f"Carregando episodios de '{anime}'...",
            task=lambda: (anime, self._anime_service.fetch_episode_titles(anime)),
            on_success=self._on_episodes_finished,
        )

    def _on_episodes_finished(self, payload: object) -> None:
        if not isinstance(payload, tuple) or len(payload) != 2:
            self._set_status("Falha ao receber episodios.")
            return

        anime, episode_titles = payload
        if not isinstance(anime, str) or not isinstance(episode_titles, list):
            self._set_status("Dados de episodios invalidos.")
            return

        self._current_anime = anime
        self._episodes_anime = anime
        episodes = [str(title) for title in episode_titles]
        self._populate_list(self.episode_list, episodes)
        self.url_output.clear()

        if not episodes:
            self._set_status("Nenhum episodio encontrado para o anime.")
            self.selected_episode_label.setText("Episodio: -")
            self._append_log(f"Nenhum episodio encontrado para '{anime}'.")
            return

        self.episode_list.setCurrentRow(0)
        self._set_status(f"{len(episodes)} episodios carregados.")
        self._append_log(f"Episodios de '{anime}' carregados.")

    def _on_episode_selection_changed(self) -> None:
        index = self._selected_episode_index()
        if index >= 0:
            self.selected_episode_label.setText(f"Episodio: {index + 1}")
        else:
            self.selected_episode_label.setText("Episodio: -")
        self._sync_controls()

    def _on_play_clicked(self) -> None:
        anime = self._current_anime or self._selected_anime()
        episode_index = self._selected_episode_index()
        if not anime:
            self._set_status("Selecione um anime.")
            return
        if episode_index < 0:
            self._set_status("Selecione um episodio.")
            return

        self._run_task(
            status_message=f"Reproduzindo '{anime}' episodio {episode_index + 1}...",
            task=lambda: self._play_episode(anime, episode_index),
            on_success=self._on_play_finished,
        )

    def _play_episode(self, anime: str, episode_index: int) -> dict[str, object]:
        player_url = self._anime_service.resolve_player_url(anime, episode_index)
        self._anime_service.play_url(player_url)
        return {
            "anime": anime,
            "episode_index": episode_index,
            "player_url": player_url,
            "episode_sources": self._anime_service.get_episode_sources(anime),
        }

    def _on_play_finished(self, payload: object) -> None:
        if not isinstance(payload, dict):
            self._set_status("Resposta invalida apos reproducao.")
            return

        anime_obj = payload.get("anime", "")
        episode_index_obj = payload.get("episode_index", -1)
        player_url_obj = payload.get("player_url", "")
        episode_sources = payload.get("episode_sources", [])

        if not isinstance(anime_obj, str) or not isinstance(episode_index_obj, int):
            self._set_status("Falha ao consolidar reproducao.")
            return
        if not isinstance(player_url_obj, str):
            self._set_status("Falha ao consolidar reproducao.")
            return

        anime = anime_obj
        episode_index = episode_index_obj
        player_url = player_url_obj

        if not anime or episode_index < 0 or not player_url or not isinstance(episode_sources, list):
            self._set_status("Falha ao consolidar reproducao.")
            return

        self._current_anime = anime
        self._episodes_anime = anime
        self._current_episode_index = episode_index
        self.url_output.setText(player_url)
        self.selected_episode_label.setText(f"Episodio: {episode_index + 1}")

        try:
            self._history_service.save_entry(anime, episode_index, episode_sources)
        except Exception as exc:  # pylint: disable=broad-except
            self._append_log(f"Historico nao salvo: {exc}")
        else:
            self._reload_history(silent=True)

        self._set_status(f"Episodio {episode_index + 1} finalizado.")
        self._append_log(f"Reproducao concluida: '{anime}' episodio {episode_index + 1}.")
        self._sync_controls()

    def _on_previous_clicked(self) -> None:
        index = self._selected_episode_index()
        target = index - 1
        if target < 0:
            self._set_status("Nao existe episodio anterior.")
            return

        self.episode_list.setCurrentRow(target)
        self._on_play_clicked()

    def _on_next_clicked(self) -> None:
        index = self._selected_episode_index()
        target = index + 1
        if target >= self.episode_list.count():
            self._set_status("Nao existe proximo episodio.")
            return

        self.episode_list.setCurrentRow(target)
        self._on_play_clicked()

    def _reload_history(self, silent: bool = False) -> None:
        try:
            entries = self._history_service.load_entries()
        except Exception as exc:  # pylint: disable=broad-except
            self._append_log(f"Falha ao carregar historico: {exc}")
            self._set_status("Nao foi possivel ler historico.")
            return

        self.history_list.setUpdatesEnabled(False)
        try:
            self.history_list.clear()
            for entry in entries:
                item = QListWidgetItem(entry.label)
                item.setData(Qt.ItemDataRole.UserRole, entry)
                self.history_list.addItem(item)
        finally:
            self.history_list.setUpdatesEnabled(True)

        if not silent:
            if entries:
                self._set_status(f"Historico carregado: {len(entries)} item(ns).")
            else:
                self._set_status("Sem historico salvo.")
        self._sync_controls()

    def _on_resume_history_clicked(self) -> None:
        item = self.history_list.currentItem()
        if not item:
            self._set_status("Selecione um item do historico.")
            return

        entry = item.data(Qt.ItemDataRole.UserRole)
        if not isinstance(entry, HistoryEntry):
            self._set_status("Entrada de historico invalida.")
            return

        self._run_task(
            status_message=f"Preparando historico de '{entry.anime}'...",
            task=lambda: self._resume_history_entry(entry),
            on_success=self._on_resume_history_finished,
        )

    def _resume_history_entry(self, entry: HistoryEntry) -> dict[str, object]:
        episode_count = self._anime_service.load_history_sources(entry.anime, entry.episode_sources)
        if episode_count <= 0:
            raise ValueError("Historico sem episodios validos.")

        episode_titles = self._anime_service.synthetic_episode_titles(entry.anime)
        return {
            "entry": entry,
            "episode_titles": episode_titles,
        }

    def _on_resume_history_finished(self, payload: object) -> None:
        if not isinstance(payload, dict):
            self._set_status("Falha ao preparar historico.")
            return

        entry = payload.get("entry")
        episode_titles = payload.get("episode_titles")
        if not isinstance(entry, HistoryEntry) or not isinstance(episode_titles, list):
            self._set_status("Dados invalidos de historico.")
            return

        self._ensure_anime_visible(entry.anime)
        self._current_anime = entry.anime
        self._episodes_anime = entry.anime
        self.selected_anime_label.setText(f"Anime: {entry.anime}")

        episodes = [str(title) for title in episode_titles]
        self._populate_list(self.episode_list, episodes)

        if not episodes:
            self._set_status("Historico carregado, mas sem episodios.")
            return

        safe_index = min(entry.episode_index, len(episodes) - 1)
        self.episode_list.setCurrentRow(safe_index)
        self._current_episode_index = safe_index
        self.selected_episode_label.setText(f"Episodio: {safe_index + 1}")
        self.url_output.clear()
        self._set_status("Historico aplicado. Selecione reproduzir para continuar.")
        self._append_log(f"Historico carregado para '{entry.anime}' no episodio {safe_index + 1}.")
        self._sync_controls()

    def _ensure_anime_visible(self, anime: str) -> None:
        if not anime:
            return

        matches = self.anime_list.findItems(anime, Qt.MatchFlag.MatchExactly)
        if matches:
            self.anime_list.setCurrentItem(matches[0])
            return

        self.anime_list.addItem(anime)
        matches = self.anime_list.findItems(anime, Qt.MatchFlag.MatchExactly)
        if matches:
            self.anime_list.setCurrentItem(matches[0])

    def _populate_list(self, target: QListWidget, values: list[str]) -> None:
        target.setUpdatesEnabled(False)
        try:
            target.clear()
            if values:
                target.addItems(values)
        finally:
            target.setUpdatesEnabled(True)

    def _sync_controls(self) -> None:
        has_query = bool(self.search_input.text().strip())
        selected_anime = self._selected_anime()
        has_anime = bool(selected_anime)
        has_episode = self._selected_episode_index() >= 0 and selected_anime == self._episodes_anime
        has_history_item = self.history_list.currentItem() is not None
        episode_count = self.episode_list.count()
        episode_index = self._selected_episode_index()

        can_previous = has_episode and episode_index > 0
        can_next = has_episode and episode_index < episode_count - 1

        self.search_button.setEnabled(not self._busy and has_query)
        self.reload_history_button.setEnabled(not self._busy)
        self.resume_history_button.setEnabled(not self._busy and has_history_item)

        self.load_episodes_button.setEnabled(not self._busy and has_anime)
        self.play_button.setEnabled(not self._busy and has_anime and has_episode)
        self.prev_button.setEnabled(not self._busy and can_previous)
        self.next_button.setEnabled(not self._busy and can_next)
