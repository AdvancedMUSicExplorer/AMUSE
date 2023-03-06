import pytest
import pandas as pd
import numpy as np
from src.features.mean_and_std import MeanAndStd


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
                    "time": 0.1,
                    "ic1": 0.1,
                    "ic2": 0.28,
                    "ic3": 0
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "ic1": 0.09,
                    "ic2": 0.06,
                    "ic3": 0,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "ic1": 0,
                    "ic2": 0.69,
                    "ic3": 0
                },
            ]
        )

        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return df

    def test_mean_and_std(self, data: pd.DataFrame):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    ("ic1", "mean"): 0.0575,
                    ("ic1", "std"): 0.040233692348578,
                    ("ic2", "mean"): 0.34,
                    ("ic2", "std"): 0.22616365755797,
                    ("ic3", "mean"): 0,
                    ("ic3", "std"): 0,
                }
            ]
        )

        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = MeanAndStd().run(data)

        pd.set_option("display.max_columns", None)
        print(result)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)
        print(compare)

        assert compare.all(axis=None)
