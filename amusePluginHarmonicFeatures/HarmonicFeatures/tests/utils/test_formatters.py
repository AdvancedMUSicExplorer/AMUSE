import pytest
from src.utils.formatters import format_chroma_resolution


class TestFormatters:
    @pytest.mark.parametrize(
        "resolution, expected", [(0, "global"), (0.2, "200ms"), (1.2, "1.2s")]
    )
    def test_format_chroma_resolution(self, resolution, expected):
        result = format_chroma_resolution(resolution)

        assert expected == result
