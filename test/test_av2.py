from animecaos.services.anime_service import AnimeService
from animecaos.core.repository import rep
import time

s = AnimeService(debug=False)
import animecaos.plugins.animesvision as av
rep.register(av.AnimesVision())

# we bypass ensure_plugins_loaded logic directly in script
def dummy_load():
    pass
s.ensure_plugins_loaded = dummy_load

animes = s.search_animes("Devil May Cry")
print("Found:", animes)

if animes:
    episodes = s.fetch_episode_titles(animes[0])
    print("Episodes:", episodes)
    sources = s.get_episode_sources(animes[0])
    print("Sources available:", sources)
