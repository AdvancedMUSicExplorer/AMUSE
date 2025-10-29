import pytest
import numpy as np
import pandas as pd
from src.data.tasks.select_columns import SelectColumns


class TestSelectColumns:
    @pytest.fixture(scope="class")
    def data(self):
        df = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 0.03,
                    "c2": 0.07,
                    "c3": 0.11
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "c1": 0.02,
                    "c2": 0.08,
                    "c3": 0.14
                },
            ]
        )

        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return df

    def test_remove_chroma(self, data):
        expected = pd.DataFrame(
            [
                {"piece": "bach_2.mp3", "time": 0, "c1": 0.03, "c3": 0.11},
                {"piece": "bach_2.mp3", "time": 0.1, "c1": 0.02, "c3": 0.14},
            ]
        )
        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = SelectColumns(['c1', 'c3']).run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
