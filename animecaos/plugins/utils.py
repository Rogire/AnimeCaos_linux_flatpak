import os
import subprocess
from selenium import webdriver


def is_firefox_installed_as_snap() -> bool:
    if os.name == "nt":
        return False

    try:
        result = subprocess.run(
            ["snap", "list", "firefox"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=5,
            check=False,
        )
        return result.returncode == 0
    except (FileNotFoundError, subprocess.TimeoutExpired):
        return False


def build_firefox_options() -> webdriver.FirefoxOptions:
    """
    Build shared Firefox options for Selenium, including Cloudflare DNS-over-HTTPS.
    """
    options = webdriver.FirefoxOptions()
    options.add_argument("--headless")

    # Prefer Cloudflare DNS resolution to reduce ISP/local DNS instability.
    # mode=2 => TRR first, then native resolver fallback.
    options.set_preference("network.trr.mode", 2)
    options.set_preference("network.trr.uri", "https://1.1.1.1/dns-query")
    options.set_preference("network.trr.bootstrapAddress", "1.1.1.1")

    return options
