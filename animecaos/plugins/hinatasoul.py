from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, WebDriverException
from selenium.webdriver.firefox.service import Service as FirefoxService
from urllib.parse import quote
import time

from animecaos.core.paths import get_bin_path
from animecaos.core.repository import rep
from animecaos.core.loader import PluginInterface
from .utils import build_firefox_options, is_firefox_installed_as_snap


def _make_driver() -> webdriver.Firefox:
    options = build_firefox_options()
    try:
        if is_firefox_installed_as_snap():
            service = FirefoxService(executable_path="/snap/bin/geckodriver")
            return webdriver.Firefox(options=options, service=service)
            
        gd_path = get_bin_path("geckodriver")
        if gd_path != "geckodriver":
            service = FirefoxService(executable_path=gd_path)
            return webdriver.Firefox(options=options, service=service)
            
        return webdriver.Firefox(options=options)
    except WebDriverException as exc:
        raise RuntimeError("Firefox/geckodriver nao encontrado.") from exc


class HinataSoul(PluginInterface):
    """Integracao com Hinata Soul."""

    name = "hinatasoul"
    languages = ["pt-br"]

    @staticmethod
    def search_anime(query: str) -> None:
        q = quote(query)
        url = f"https://hinatasoul.com/busca?busca={q}"
        driver = _make_driver()
        try:
            driver.get(url)
            time.sleep(3)

            cards = driver.find_elements(By.CSS_SELECTOR, "div.item, div.post")
            for card in cards:
                try:
                    a = card.find_element(By.CSS_SELECTOR, "a")
                    href = a.get_attribute("href")
                    title = a.get_attribute("title") or a.text.strip()
                    if title and href:
                        rep.add_anime(title, href, HinataSoul.name)
                except Exception:
                    continue
        except Exception as e:
            print(f"[{HinataSoul.name}] search_anime erro: {e}")
        finally:
            driver.quit()

    @staticmethod
    def search_episodes(anime: str, anime_url: str, params: object = None) -> None:
        driver = _make_driver()
        try:
            driver.get(anime_url)
            time.sleep(3)

            ep_links = []
            title_list = []

            for a in driver.find_elements(By.CSS_SELECTOR, "a[href*='/episodio'], a.btn-ep"):
                href = a.get_attribute("href") or ""
                name = a.get_attribute("title") or a.text.strip()
                if href and href not in ep_links:
                    ep_links.append(href)
                    title_list.append(name if name else f"Episódio {len(ep_links)}")

            if ep_links:
                ep_links.reverse()
                title_list.reverse()
                rep.add_episode_list(anime, title_list, ep_links, HinataSoul.name)
        except Exception as e:
            print(f"[{HinataSoul.name}] search_episodes erro: {e}")
        finally:
            driver.quit()

    @staticmethod
    def search_player_src(episode_url: str) -> str:
        driver = _make_driver()
        try:
            driver.get(episode_url)
            try:
                iframe = WebDriverWait(driver, 12).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "iframe, video"))
                )
            except TimeoutException as exc:
                raise RuntimeError("Player nao encontrado.") from exc

            src = iframe.get_attribute("src") or ""
            if not src:
                src = iframe.find_element(By.CSS_SELECTOR, "source").get_attribute("src")
                if not src:
                    raise RuntimeError("Sem src no HinataSoul.")

            if "blogger.com" in src:
                raise RuntimeError("Blogger não é mais suportado (fonte instavel).")

            return src
        finally:
            driver.quit()


def load(languages_dict):
    if any(lang in languages_dict for lang in HinataSoul.languages):
        rep.register(HinataSoul)
