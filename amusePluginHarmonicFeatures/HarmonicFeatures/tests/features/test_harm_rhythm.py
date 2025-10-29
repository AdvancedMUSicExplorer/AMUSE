import pytest
import numpy as np
import pandas as pd
from src.features.harm_rhythm import HarmRhythm


class TestHarmRhythm:
    @pytest.fixture
    def chroma_data(scope="class"):
        chroma = np.array(
            [
                [
                    1.20,
                    5.10,
                    0.90,
                    1.60,
                    2.50,
                    3.30,
                    6.00,
                    0.20,
                    0.30,
                    0.10,
                    0.02,
                    0.01,
                ],
                [
                    2.10,
                    1.50,
                    9.40,
                    6.10,
                    5.20,
                    2.20,
                    3.00,
                    0.40,
                    0.20,
                    0.50,
                    0.07,
                    0.04,
                ],
                [
                    0.30,
                    1.10,
                    4.90,
                    2.30,
                    2.40,
                    0.25,
                    5.37,
                    0.52,
                    0.13,
                    0.16,
                    2.02,
                    1.01,
                ],
                [
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
                    0.02,
                    0.01,
                    1.95,
                    1.69,
                    5.23,
                    4.35,
                    2.62,
                    0.26,
                    0.39,
                    0.10,
                    0.02,
                    0.01,
                ],
            ]
        )

        return chroma

    @pytest.fixture()
    def data(scope="class"):
        df = pd.DataFrame(
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
                    "hcdf_peak_idx": 0,
                    "hcdf_peak_mag": 0
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.7,
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
                    "hcdf_peak_idx": 10,
                    "hcdf_peak_mag": 0.8
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 1.2,
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
                    "hcdf_peak_idx": 13,
                    "hcdf_peak_mag": 1.3
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 1.6,
                    "c1": 0.00,
                    "c2": 0.12,
                    "c3": 0.70,
                    "c4": 0.39,
                    "c5": 0.22,
                    "c6": 1.28,
                    "c7": 0.37,
                    "c8": 0.60,
                    "c9": 1.11,
                    "c10": 0.13,
                    "c11": 0.02,
                    "c12": 0.05,
                    "hcdf_peak_idx": 18,
                    "hcdf_peak_mag": 0.65
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
                    "hcdf_peak_idx": 0,
                    "hcdf_peak_mag": 0
                },
                {
                    "piece": "chopin_3.mp3",
                    "time": 0.4,
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
                    "hcdf_peak_idx": 7,
                    "hcdf_peak_mag": 1.24
                },
                {
                    "piece": "chopin_3.mp3",
                    "time": 0.9,
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
                    "hcdf_peak_idx": 15,
                    "hcdf_peak_mag": 0.93
                },
                {
                    "piece": "chopin_3.mp3",
                    "time": 1.3,
                    "c1": 1.05,
                    "c2": 3.01,
                    "c3": 1.93,
                    "c4": 1.69,
                    "c5": 0.35,
                    "c6": 0.42,
                    "c7": 1.02,
                    "c8": 2.31,
                    "c9": 0.02,
                    "c10": 0.17,
                    "c11": 2.41,
                    "c12": 0.01,
                    "hcdf_peak_idx": 21,
                    "hcdf_peak_mag": 1.79
                },
            ]
        )

        df = df.set_index(['piece', 'time'])

        return df

    def test_hcdf_peak_interval(self):
        hcdf_peak_indexes = np.array([0, 7, 20, 38, 39])

        expected = np.array([7, 13, 18, 1, 0])

        result = HarmRhythm()._hcdf_peak_interval(hcdf_peak_indexes)

        assert np.allclose(result, expected)

    def test_run(self, data: pd.DataFrame):
        expected = pd.DataFrame([
            {
                "piece": "bach_2.mp3",
                "time": 0,
                "hcdf_peak_interval": 10.0,
                "hcdf_peak_mag": 0
            },
            {
                "piece": "bach_2.mp3",
                "time": 0.7,
                "hcdf_peak_interval": 3.0,
                "hcdf_peak_mag": 0.8
            },
            {
                "piece": "bach_2.mp3",
                "time": 1.2,
                "hcdf_peak_interval": 5.0,
                "hcdf_peak_mag": 1.3
            },
            {
                "piece": "bach_2.mp3",
                "time": 1.6,
                "hcdf_peak_interval": 0.0,
                "hcdf_peak_mag": 0.65
            },
            {
                "piece": "chopin_3.mp3",
                "time": 0,
                "hcdf_peak_interval": 7.0,
                "hcdf_peak_mag": 0
            },
            {
                "piece": "chopin_3.mp3",
                "time": 0.4,
                "hcdf_peak_interval": 8.0,
                "hcdf_peak_mag": 1.24
            },
            {
                "piece": "chopin_3.mp3",
                "time": 0.9,
                "hcdf_peak_interval": 6.0,
                "hcdf_peak_mag": 0.93
            },
            {
                "piece": "chopin_3.mp3",
                "time": 1.3,
                "hcdf_peak_interval": 0.0,
                "hcdf_peak_mag": 1.79
            }
        ])

        expected = expected.set_index(["piece", "time"])

        result = HarmRhythm().run(data)

        print(expected)
        print(result)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        print(compare)

        assert compare.all(axis=None)
