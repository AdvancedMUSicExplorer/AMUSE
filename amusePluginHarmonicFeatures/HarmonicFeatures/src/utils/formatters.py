from src.utils.converters import sec_to_ms
from src.features.chroma_resolution import ChromaResolution


def format_chroma_resolution(resolution: float):
    """Formats a chroma resolution value into a string.

    Args:
        resolution: Chroma resolution.

    Returns:
        Formatted chroma resolution string.
    """
    if resolution == ChromaResolution.GLOBAL:
        return "global"

    if resolution < 1:
        return f"{sec_to_ms(resolution)}ms"
    else:
        return f"{resolution}s"
