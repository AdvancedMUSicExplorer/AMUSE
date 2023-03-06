import pytest
import pandas as pd
import numpy as np
import src.data.commands.make_crossera as make_crossera
from src.data.constants.feature_groups import CHROMA_FEATS
from pytest_mock import MockerFixture


class TestMakeCrossera:
    @pytest.fixture
    def df1(scope="class"):
        data = pd.DataFrame(
            [
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
                    "c11": 0.80,
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
            ]
        )

        return pd.DataFrame(data)

    @pytest.fixture
    def df2(scope="class"):
        data = pd.DataFrame(
            [
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0,
                    "c1": 0.00,
                    "c2": 0.00,
                    "c3": 0.00,
                    "c4": 0.00,
                    "c5": 0.00,
                    "c6": 0.00,
                    "c7": 0.00,
                    "c8": 0.00,
                    "c9": 0.00,
                    "c10": 0.00,
                    "c11": 0.00,
                    "c12": 0.00,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0.1,
                    "c1": 0.00,
                    "c2": 0.12,
                    "c3": 0.70,
                    "c4": 0.39,
                    "c5": 0.22,
                    "c6": 1.28,
                    "c7": 0.37,
                    "c8": 0.60,
                    "c9": 1.11,
                    "c10": -0.13,
                    "c11": 0.02,
                    "c12": -0.05,
                },
            ]
        )

        return pd.DataFrame(data)

    @pytest.fixture
    def df3(scope="class"):
        data = pd.DataFrame(
            [
                {
                    "piece": "chopin_3.mp3",
                    "time": 0,
                    "c1": 0.02,
                    "c2": 0.14,
                    "c3": 0.07,
                    "c4": 0.91,
                    "c5": 0.23,
                    "c6": 0.69,
                    "c7": 1.11,
                    "c8": 2.12,
                    "c9": 0.03,
                    "c10": 0.04,
                    "c11": 0.81,
                    "c12": 0.07,
                },
                {
                    "piece": "chopin_3.mp3",
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
            ]
        )

        return pd.DataFrame(data)

    @pytest.fixture
    def df4(scope="class"):
        data = pd.DataFrame(
            [
                {
                    "piece": "grieg_4.mp3",
                    "time": 0,
                    "c1": 0.99,
                    "c2": 0.11,
                    "c3": 1.29,
                    "c4": 1.86,
                    "c5": 0.43,
                    "c6": 0.54,
                    "c7": 1.92,
                    "c8": 2.31,
                    "c9": 0.08,
                    "c10": 0.07,
                    "c11": 0.23,
                    "c12": 0.02,
                }
            ]
        )

        return pd.DataFrame(data)

    @pytest.fixture
    def full(scope="class"):
        data = pd.DataFrame(
            [
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
                    "c11": 0.80,
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
                    "c1": 0.00,
                    "c2": 0.00,
                    "c3": 0.00,
                    "c4": 0.00,
                    "c5": 0.00,
                    "c6": 0.00,
                    "c7": 0.00,
                    "c8": 0.00,
                    "c9": 0.00,
                    "c10": 0.00,
                    "c11": 0.00,
                    "c12": 0.00,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0.1,
                    "c1": 0.00,
                    "c2": 0.12,
                    "c3": 0.70,
                    "c4": 0.39,
                    "c5": 0.22,
                    "c6": 1.28,
                    "c7": 0.37,
                    "c8": 0.60,
                    "c9": 1.11,
                    "c10": -0.13,
                    "c11": 0.02,
                    "c12": -0.05,
                },
                {
                    "piece": "chopin_3.mp3",
                    "time": 0,
                    "c1": 0.02,
                    "c2": 0.14,
                    "c3": 0.07,
                    "c4": 0.91,
                    "c5": 0.23,
                    "c6": 0.69,
                    "c7": 1.11,
                    "c8": 2.12,
                    "c9": 0.03,
                    "c10": 0.04,
                    "c11": 0.81,
                    "c12": 0.07,
                },
                {
                    "piece": "chopin_3.mp3",
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
                    "piece": "grieg_4.mp3",
                    "time": 0,
                    "c1": 0.99,
                    "c2": 0.11,
                    "c3": 1.29,
                    "c4": 1.86,
                    "c5": 0.43,
                    "c6": 0.54,
                    "c7": 1.92,
                    "c8": 2.31,
                    "c9": 0.08,
                    "c10": 0.07,
                    "c11": 0.23,
                    "c12": 0.02,
                },
            ]
        )

        return pd.DataFrame(data)

    def test_join_datasets(self, df1, df2, mocker: MockerFixture):
        expected = np.array(
            [
                [
                    "bach_2.mp3",
                    0,
                    0.22,
                    0.34,
                    1.09,
                    1.21,
                    0.22,
                    0.96,
                    1.91,
                    2.85,
                    0.02,
                    0.03,
                    0.80,
                    0.04,
                ],
                [
                    "bach_2.mp3",
                    0.1,
                    0.11,
                    2.33,
                    1.01,
                    0.99,
                    0.09,
                    0.48,
                    1.49,
                    1.77,
                    2.41,
                    4.27,
                    3.38,
                    2.73,
                ],
                [
                    "vivaldi_1.mp3",
                    0,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                    0.00,
                ],
                [
                    "vivaldi_1.mp3",
                    0.1,
                    0.00,
                    0.12,
                    0.70,
                    0.39,
                    0.22,
                    1.28,
                    0.37,
                    0.60,
                    1.11,
                    -0.13,
                    0.02,
                    -0.05,
                ],
            ],
            dtype="object",
        )

        COL_NAMES = ["piece", "time"]
        COL_NAMES.extend(CHROMA_FEATS)

        mocker.patch("pandas.read_csv", side_effect=[df1, df2])

        result = make_crossera.join_datasets(["df1.csv", "df2.csv"], COL_NAMES)

        assert np.array_equal(result, expected)

    def test_make_datasets(
        self,
        df1: pd.DataFrame,
        df2: pd.DataFrame,
        df3: pd.DataFrame,
        df4: pd.DataFrame,
        full: pd.DataFrame,
        mocker: MockerFixture,
    ):
        df12 = df1.append(df2, ignore_index=True)
        df34 = df3.append(df4, ignore_index=True)
        full = df12.append(df34, ignore_index=True)

        expected = [("df12.csv", df12), ("df34.csv", df34), ("full.csv", full)]

        COL_NAMES = ["piece", "time"]
        COL_NAMES.extend(CHROMA_FEATS)

        mocker.patch(
            "pathlib.Path.glob",
            side_effect=[["df1.csv", "df2.csv"], ["df3.csv", "df4.csv"]],
        )
        mocker.patch("pandas.read_csv", side_effect=[df1, df2, df3, df4])

        result = make_crossera.make_datasets(
            ["df1_2", "df3_4"], ["df12.csv", "df34.csv", "full.csv"],
            "path/", COL_NAMES
        )

        for i, res in enumerate(result):
            exp_filename, exp_df = expected[i]
            res_filename, res_df = res

            assert exp_filename == res_filename

            exp_df = exp_df.set_index(["piece", "time"])
            res_df = res_df.set_index(["piece", "time"])

            compare = pd.DataFrame(
                np.isclose(exp_df, res_df), columns=exp_df.columns)

            assert compare.all(axis=None)
