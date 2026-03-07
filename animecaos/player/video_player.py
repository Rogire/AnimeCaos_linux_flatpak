import subprocess


def play_video(url: str, debug: bool = False) -> dict[str, bool]:
    if debug:
        return {"eof": False}

    try:
        result = subprocess.run(
            [
                "mpv",
                url,
                "--fullscreen",
                "--cursor-autohide-fs-only",
                "--log-file=log.txt",
                "--term-status-msg=PlaybackStatus: ${=eof-reached}",
            ],
            capture_output=True,
            text=True,
            check=False,
        )
    except FileNotFoundError as exc:
        raise EnvironmentError("Erro: 'mpv' nao esta instalado ou nao esta no PATH.") from exc

    if result.returncode != 0 and result.returncode != 4:
        # mpv return code 4 can occasionally happen on network aborts, but others are errors.
        raise RuntimeError(f"mpv encerrou com codigo {result.returncode}.")

    out = result.stdout or ""
    # Se o player chegou no fim natural do stream (eof-reached = yes)
    eof = "PlaybackStatus: yes" in out

    return {"eof": eof}
