import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.common.exceptions import TimeoutException, WebDriverException
from selenium.webdriver.common.by import By
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from animecaos.core.loader import PluginInterface
from animecaos.core.repository import rep

from .utils import is_firefox_installed_as_snap


REQUEST_TIMEOUT_SECONDS = 15
HEADERS = {"User-Agent": "Mozilla/5.0 (animecaos)"}
_BLOGGER_MARKER = "blogger.com/video.g"


def _uses_blogger(episode_url: str) -> bool:
    """Return True if the episode page statically embeds a Blogger video link."""
    try:
        r = requests.get(episode_url, timeout=REQUEST_TIMEOUT_SECONDS, headers=HEADERS)
        return _BLOGGER_MARKER in r.text
    except Exception:
        return False  # assume OK if unreachable


class AnimeFire(PluginInterface):
    languages = ["pt-br"]
    name = "animefire"

    @staticmethod
    def search_anime(query: str):
        url = "https://animefire.io/pesquisar/" + "-".join(query.split())
        response = requests.get(url, timeout=REQUEST_TIMEOUT_SECONDS, headers=HEADERS)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, "html.parser")
        target_class = "col-6 col-sm-4 col-md-3 col-lg-2 mb-1 minWDanime divCardUltimosEps"
        cards = soup.find_all("div", class_=target_class)

        titles_urls: list[tuple[str, str]] = []
        for card in cards:
            link_tag = card.find("a", href=True)
            title_tag = card.find("h3", class_="animeTitle")
            if not link_tag or not title_tag:
                continue
            titles_urls.append((title_tag.get_text(strip=True), link_tag["href"]))

        if not titles_urls:
            # Fallback parser for minor HTML layout changes.
            fallback_urls = []
            for div in cards:
                article = getattr(div, "article", None)
                anchor = getattr(article, "a", None) if article else None
                if anchor and anchor.get("href"):
                    fallback_urls.append(anchor["href"])
            titles = [h3.get_text(strip=True) for h3 in soup.find_all("h3", class_="animeTitle")]
            titles_urls = list(zip(titles, fallback_urls))

        if not titles_urls:
            return

        def get_first_episode_url(anime_url: str) -> str:
            """Return the first episode URL from the anime page, or empty string."""
            try:
                r = requests.get(anime_url, timeout=REQUEST_TIMEOUT_SECONDS, headers=HEADERS)
                r.raise_for_status()
                s = BeautifulSoup(r.text, "html.parser")
                link = s.find("a", class_=lambda c: c and "lEp" in c)
                return link["href"] if link and link.get("href") else ""
            except Exception:
                return ""

        from concurrent.futures import ThreadPoolExecutor, as_completed
        from os import cpu_count
        workers = max(1, min(len(titles_urls), cpu_count() or 1))
        with ThreadPoolExecutor(max_workers=workers) as executor:
            future_to_item = {
                executor.submit(get_first_episode_url, anime_url): (title, anime_url)
                for title, anime_url in titles_urls
            }
            for future in as_completed(future_to_item):
                title, anime_url = future_to_item[future]
                first_ep_url = future.result()
                if first_ep_url and _uses_blogger(first_ep_url):
                    continue  # skip – blogger hosting
                rep.add_anime(title, anime_url, AnimeFire.name)

    @staticmethod
    def search_episodes(anime: str, url: str, params):
        response = requests.get(url, timeout=REQUEST_TIMEOUT_SECONDS, headers=HEADERS)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, "html.parser")
        links = soup.find_all("a", class_="lEp epT divNumEp smallbox px-2 mx-1 text-left d-flex")
        episode_links = [link["href"] for link in links if link.get("href")]
        episode_titles = [link.get_text(strip=True) for link in links]
        if not episode_links:
            return

        # Reject this source entirely if the first episode uses Blogger hosting.
        if _uses_blogger(episode_links[0]):
            return

        rep.add_episode_list(anime, episode_titles, episode_links, AnimeFire.name)

    @staticmethod
    def search_player_src(url_episode: str) -> str:
        options = webdriver.FirefoxOptions()
        options.add_argument("--headless")

        try:
            if is_firefox_installed_as_snap():
                service = FirefoxService(executable_path="/snap/bin/geckodriver")
                driver = webdriver.Firefox(options=options, service=service)
            else:
                driver = webdriver.Firefox(options=options)
        except WebDriverException as exc:
            raise RuntimeError("Firefox/geckodriver nao encontrado.") from exc

        try:
            driver.get(url_episode)

            try:
                video = WebDriverWait(driver, 10).until(
                    EC.visibility_of_element_located((By.ID, "my-video_html5_api"))
                )
                src = video.get_property("src") or video.get_attribute("src")
                if src:
                    return src
            except TimeoutException:
                pass

            try:
                iframe = WebDriverWait(driver, 10).until(
                    EC.visibility_of_element_located(
                        (By.XPATH, "/html/body/div[2]/div[2]/div/div[1]/div[1]/div/div/div[2]/div[4]/iframe")
                    )
                )
                src = iframe.get_property("src") or iframe.get_attribute("src")
                if src:
                    if "blogger.com/video.g" in src:
                        raise RuntimeError("Hospedagem de video nao disponivel para este episodio.")
                    return src
            except TimeoutException as exc:
                raise RuntimeError("Iframe/video nao encontrado no AnimeFire.") from exc

            raise RuntimeError("Fonte de video nao encontrada no AnimeFire.")
        finally:
            driver.quit()


def load(languages_dict):
    if any(language in languages_dict for language in AnimeFire.languages):
        rep.register(AnimeFire)
