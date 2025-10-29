import pytest
import pandas as pd
import numpy as np
from src.features.skewness_and_whiskers import SknewnessAndWhiskers


class TestSkewnessAndWhiskers:
    @pytest.fixture
    def data(scope="class"):
        df = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "ic1": 0.04,
                    "ic2": 0.33,
                    "ic3": 0
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "ic1": 0.1,
                    "ic2": 0.28,
                    "ic3": 0
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.2,
                    "ic1": 0.09,
                    "ic2": 0.06,
                    "ic3": 0,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.3,
                    "ic1": 0,
                    "ic2": 0.69,
                    "ic3": 0
                },
            ]
        )

        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return df

    def test_skewness_and_whiskers(self, data: pd.DataFrame):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    ("ic1", "skewness"): -0.3238799938616294,
                    ("ic1", "lower_whisker"): -0.06375,
                    ("ic1", "upper_whisker"): 0.18625,
                    ("ic2", "skewness"): 0.44747395374570403,
                    ("ic2", "lower_whisker"): -0.0674999999999999,
                    ("ic2", "upper_whisker"): 0.7124999999999999,
                    ("ic3", "skewness"): 0,
                    ("ic3", "lower_whisker"): 0,
                    ("ic3", "upper_whisker"): 0,
                }
            ]
        )

        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = SknewnessAndWhiskers().run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
