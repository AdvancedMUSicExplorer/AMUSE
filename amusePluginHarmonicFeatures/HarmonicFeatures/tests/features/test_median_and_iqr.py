import pytest
import pandas as pd
import numpy as np
from src.features.median_and_iqr import MedianAndIQR


class TestMeanAndStd:
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
                    "time": 0.2,
                    "ic1": 0.1,
                    "ic2": 0.28,
                    "ic3": 0
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.3,
                    "ic1": 0.09,
                    "ic2": 0.06,
                    "ic3": 0,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.4,
                    "ic1": 0,
                    "ic2": 0.69,
                    "ic3": 0
                },
            ]
        )

        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return df

    def test_median_and_iqr(self, data: pd.DataFrame):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    ("ic1", "median"): 0.065,
                    ("ic1", "iqr"): 0.0625,
                    ("ic2", "median"): 0.305,
                    ("ic2", "iqr"): 0.19499999999999995,
                    ("ic3", "median"): 0,
                    ("ic3", "iqr"): 0,
                }
            ]
        )

        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = MedianAndIQR().run(data)

        print(result)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        print(compare)

        assert compare.all(axis=None)
