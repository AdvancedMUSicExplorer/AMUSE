import pytest
import pandas as pd
import numpy as np
from src.features.template_based import TemplateBased


class TestTemplateBased:
    @pytest.fixture
    def data(scope="class"):
        df = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 0.21,
                    "c2": 0.45,
                    "c3": 0.77,
                    "c4": 0.09,
                    "c5": 0.23,
                    "c6": 0.50,
                    "c7": 0.14,
                    "c8": 0.08,
                    "c9": 0.91,
                    "c10": 0.83,
                    "c11": 0.77,
                    "c12": 0.11,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "c1": 0.03,
                    "c2": 0.06,
                    "c3": 0.44,
                    "c4": 0.31,
                    "c5": 0.97,
                    "c6": 0.13,
                    "c7": 0.19,
                    "c8": 0.25,
                    "c9": 0.37,
                    "c10": 0.52,
                    "c11": 0.81,
                    "c12": 0.01,
                },
            ]
        )

        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return df

    def test_calc_template_feat(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "ic1": 2.3022,
                    "ic2": 1.6935,
                    "ic3": 1.8768,
                    "ic4": 2.2966,
                    "ic5": 2.0628,
                    "ic6": 2.1458,
                    "chord_maj": 0.870031,
                    "chord_min": 0.856451,
                    "chord_dim": 0.702883,
                    "chord_aug": 0.943503,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "ic1": 1.3781,
                    "ic2": 1.2458,
                    "ic3": 0.8478,
                    "ic4": 1.1827,
                    "ic5": 1.3709,
                    "ic6": 2.2634,
                    "chord_maj": 0.212985,
                    "chord_min": 0.223641,
                    "chord_dim": 0.334307,
                    "chord_aug": 0.249942,
                },
            ]
        )

        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = TemplateBased().run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
