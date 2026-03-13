from __future__ import annotations

import os
from json import JSONDecodeError, dump, load
from pathlib import Path

APP_NAME = "animecaos"


def _watchlist_dir(app_name: str) -> Path:
    if os.name == "nt":
        appdata = os.getenv("APPDATA")
        if appdata:
            return Path(appdata) / app_name
        return Path.home() / "AppData" / "Roaming" / app_name
    return Path.home() / ".local" / "state" / app_name


class WatchlistService:
    """Persistence service for favorited animes (watchlist)."""

    def __init__(self, app_name: str = APP_NAME) -> None:
        self._watchlist_file = _watchlist_dir(app_name) / "watchlist.json"

    def load_watchlist(self) -> list[str]:
        data = self._read_data()
        if not isinstance(data, list):
            return []
        
        animes = [str(item) for item in data if isinstance(item, str)]
        animes.sort(key=lambda a: a.lower())
        return animes

    def add_anime(self, anime: str) -> None:
        if not anime:
            return

        animes = set(self.load_watchlist())
        animes.add(anime)

        self._save_data(list(animes))

    def remove_anime(self, anime: str) -> None:
        if not anime:
            return

        animes = set(self.load_watchlist())
        animes.discard(anime)

        self._save_data(list(animes))

    def is_favorited(self, anime: str) -> bool:
        return anime in self.load_watchlist()

    def _read_data(self, ignore_errors: bool = False) -> list[str]:
        if not self._watchlist_file.exists():
            return []

        try:
            with self._watchlist_file.open("r", encoding="utf-8") as file:
                data = load(file)
        except (PermissionError, JSONDecodeError):
            if ignore_errors:
                return []
            raise

        return data if isinstance(data, list) else []

    def _save_data(self, animes: list[str]) -> None:
        self._watchlist_file.parent.mkdir(parents=True, exist_ok=True)
        with self._watchlist_file.open("w", encoding="utf-8") as file:
            dump(animes, file, ensure_ascii=False, indent=2)
