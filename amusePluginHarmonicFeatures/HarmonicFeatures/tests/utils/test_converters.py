from src.utils.converters import sec_to_ms


class TestConverters:
    def test_sec_to_ms(self):
        assert sec_to_ms(0.1) == 100
        assert sec_to_ms(2) == 2000
        assert sec_to_ms(0.567) == 567
        assert sec_to_ms(0.025) == 25
