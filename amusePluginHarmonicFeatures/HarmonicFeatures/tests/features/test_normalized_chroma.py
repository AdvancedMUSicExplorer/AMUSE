import pytest
import pandas as pd
import numpy as np
from src.features.normalized_chroma import NormalizedChroma


class TestNormalizedChroma:
    @pytest.fixture
    def data(scope="class"):
        data = [
            {
                "piece": "bach_2.mp3",
                "time": 0,
                "c1": 0.22,
                "c2": 0.34,
                "c3": 1.09,
                "c4": 1.21,
                "c5": 0.22,
                "c6": 0.96,
                "c7": 1.91,
                "c8": 2.85,
                "c9": 0.02,
                "c10": 0.03,
                "c11": 0.8,
                "c12": 0.04,
            },
            {
                "piece": "bach_2.mp3",
                "time": 0.1,
                "c1": 0.11,
                "c2": 2.33,
                "c3": 1.01,
                "c4": 0.99,
                "c5": 0.09,
                "c6": 0.48,
                "c7": 1.49,
                "c8": 1.77,
                "c9": 2.41,
                "c10": 4.27,
                "c11": 3.38,
                "c12": 2.73,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0,
                "c1": 0,
                "c2": 0,
                "c3": 0,
                "c4": 0,
                "c5": 0,
                "c6": 0,
                "c7": 0,
                "c8": 0,
                "c9": 0,
                "c10": 0,
                "c11": 0,
                "c12": 0,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.1,
                "c1": 0,
                "c2": 0.12,
                "c3": 0.7,
                "c4": 0.39,
                "c5": 0.22,
                "c6": 1.28,
                "c7": 0.37,
                "c8": 0.6,
                "c9": 1.11,
                "c10": -0.13,
                "c11": 0.02,
                "c12": -0.05,
            },
        ]

        df = pd.DataFrame(data)
        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return pd.DataFrame(df)

    def test_extraction(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 0.02270381837,
                    "c2": 0.0350877193,
                    "c3": 0.1124871001,
                    "c4": 0.124871001,
                    "c5": 0.02270381837,
                    "c6": 0.09907120743,
                    "c7": 0.1971104231,
                    "c8": 0.2941176471,
                    "c9": 0.002063983488,
                    "c10": 0.003095975232,
                    "c11": 0.08255933953,
                    "c12": 0.004127966976,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "c1": 0.00522317189,
                    "c2": 0.1106362773,
                    "c3": 0.04795821462,
                    "c4": 0.04700854701,
                    "c5": 0.004273504274,
                    "c6": 0.02279202279,
                    "c7": 0.07075023742,
                    "c8": 0.08404558405,
                    "c9": 0.1144349478,
                    "c10": 0.2027540361,
                    "c11": 0.1604938272,
                    "c12": 0.1296296296,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0,
                    "c1": 0,
                    "c2": 0,
                    "c3": 0,
                    "c4": 0,
                    "c5": 0,
                    "c6": 0,
                    "c7": 0,
                    "c8": 0,
                    "c9": 0,
                    "c10": 0,
                    "c11": 0,
                    "c12": 0,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0.1,
                    "c1": 0,
                    "c2": 0.02404809619,
                    "c3": 0.1402805611,
                    "c4": 0.07815631263,
                    "c5": 0.04408817635,
                    "c6": 0.2565130261,
                    "c7": 0.07414829659,
                    "c8": 0.120240481,
                    "c9": 0.2224448898,
                    "c10": -0.02605210421,
                    "c11": 0.004008016032,
                    "c12": -0.01002004008,
                },
            ]
        )
        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = NormalizedChroma().run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
