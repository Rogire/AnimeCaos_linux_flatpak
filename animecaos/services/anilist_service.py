from __future__ import annotations

import os
from pathlib import Path

import requests

from animecaos.services.watchlist_service import _watchlist_dir

APP_NAME = "animecaos"


class AniListService:
    """Service to fetch anime metadata (covers, synopsis) from AniList GraphQL API."""

    def __init__(self) -> None:
        self._url = "https://graphql.anilist.co"
        self._cache_dir = _watchlist_dir(APP_NAME) / "cache" / "covers"
        self._cache_dir.mkdir(parents=True, exist_ok=True)
        
        self._query_template = """
        query ($search: String) {
          Media (search: $search, type: ANIME) {
            id
            title {
              romaji
              english
            }
            description
            coverImage {
              large
            }
          }
        }
        """

    def fetch_anime_info(self, query: str) -> dict[str, str | None]:
        """Fetches metadata for a given anime title."""
        if not query:
            return {"description": None, "cover_path": None}

        # Light sanitation for better search hits
        clean_query = query.replace("(Dublado)", "").replace("(Legendado)", "").strip()

        variables = {"search": clean_query}

        try:
            response = requests.post(
                self._url,
                json={"query": self._query_template, "variables": variables},
                timeout=10,
            )
            response.raise_for_status()
            data = response.json()
        except Exception:
            return {"description": None, "cover_path": None}

        media = data.get("data", {}).get("Media")
        if not media:
            return {"description": None, "cover_path": None}

        description = media.get("description", "")
        if description:
            description = (
                description.replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<i>", "")
                .replace("</i>", "")
                .replace("<b>", "")
                .replace("</b>", "")
                .replace("\n\n", "\n")
            )
            description = self._translate_to_ptbr(description)

        cover_url = media.get("coverImage", {}).get("large")
        cover_path = None

        if cover_url:
            media_id = media.get("id", "unknown")
            ext = cover_url.split(".")[-1]
            if len(ext) > 4:
                ext = "jpg"
            
            cover_path = self._cache_dir / f"{media_id}.{ext}"
            if not cover_path.exists():
                try:
                    img_resp = requests.get(cover_url, timeout=10)
                    img_resp.raise_for_status()
                    with cover_path.open("wb") as f:
                        f.write(img_resp.content)
                except Exception:
                    cover_path = None

        return {
            "description": description,
            "cover_path": str(cover_path) if cover_path else None
        }

    def _translate_to_ptbr(self, text: str) -> str:
        """Translates the given text to Portuguese (pt-br) using the free Google Translate API endpoint."""
        if not text:
            return ""
        try:
            from urllib.parse import quote
            url = f"https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=pt&dt=t&q={quote(text)}"
            resp = requests.get(url, timeout=5)
            if resp.status_code == 200:
                data = resp.json()
                translated = "".join(sentence[0] for sentence in data[0] if sentence[0])
                return translated
        except Exception:
            pass
        return text
