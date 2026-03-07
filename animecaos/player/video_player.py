import subprocess


def play_video(url: str, debug: bool = False) -> None:
    if debug:
        return

    try:
        result = subprocess.run(
            [
                "mpv",
                url,
                "--fullscreen",
                "--cursor-autohide-fs-only",
                "--log-file=log.txt",
            ],
            check=False,
        )
    except FileNotFoundError as exc:
        raise EnvironmentError("Erro: 'mpv' nao esta instalado ou nao esta no PATH.") from exc

    if result.returncode != 0:
        raise RuntimeError(f"mpv encerrou com codigo {result.returncode}.")
