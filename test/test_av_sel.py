from animecaos.core.repository import rep
from animecaos.core.loader import PluginInterface
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, WebDriverException

# Test if selenium + geckodriver works fine with AnimesVision
options = webdriver.FirefoxOptions()
options.add_argument("--headless")
driver = webdriver.Firefox(options=options)
url = "https://animesvision.biz/search?nome=Devil+May+Cry"
print("Acessando:", url)
driver.get(url)
import time
time.sleep(3)

# print page source snippet and links
from bs4 import BeautifulSoup
soup = BeautifulSoup(driver.page_source, "html.parser")

# AnimesvIsion result cards
for a in soup.find_all("a", href=True)[:30]:
    if "anime" in a['href']:
        print("LINK:", a['href'], "TEXT:", a.text.strip()[:50])

driver.quit()
