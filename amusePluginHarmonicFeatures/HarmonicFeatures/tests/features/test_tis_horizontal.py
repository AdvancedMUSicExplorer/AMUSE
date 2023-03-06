import pytest
import numpy as np
import pandas as pd
from numpy.typing import ArrayLike
from TIVlib import TIVCollection
from src.features.tis_horizontal import TISHorizontal


class TestTISHorizontal:
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

    @pytest.mark.parametrize(
        "distance_type, coef, expected",
        [
            (
                TISHorizontal.DIST_COSINE,
                None,
                np.array([1.0988581, 0.88569352, 0.47083198, 0, 1.16067857]),
            ),
            (
                TISHorizontal.DIST_EUCLIDEAN,
                None,
                np.array(
                    [
                        7.024411157159626,
                        4.205619975303846,
                        4.4070228830168,
                        3.9649816028134306,
                        5.148215450137535,
                    ]
                ),
            ),
            (
                TISHorizontal.DIST_COSINE,
                4,
                np.array(
                    [
                        1.01360024,
                        1.32393132,
                        0.08582717,
                        0,
                        0.74733264
                    ]
                ),
            ),
            (
                TISHorizontal.DIST_EUCLIDEAN,
                4,
                np.array(
                    [
                        4.553345,
                        2.852856,
                        1.427971,
                        1.919748,
                        1.424419
                    ]
                ),
            ),
        ],
    )
    def test_tonal_dispersion(
        self, chroma_data: ArrayLike, distance_type: int,
        coef: int, expected: ArrayLike
    ):
        tivs = TIVCollection.from_pcp(chroma_data.T)

        result = TISHorizontal()._tonal_dispersion(
            chroma_data, tivs, distance_type, coef
        )

        assert np.allclose(result, expected)

    @pytest.mark.parametrize(
        "distance_type, expected",
        [
            (TISHorizontal.DIST_COSINE, np.array([1.9128754, 0.92266401, 0, 0, 0])),
            (
                TISHorizontal.DIST_EUCLIDEAN,
                np.array(
                    [
                        10.928152516237695,
                        6.080080353530224,
                        7.556838197613272,
                        5.225397157960991,
                        0,
                    ]
                ),
            ),
        ],
    )
    def test_distance(
        self, chroma_data: ArrayLike, distance_type: int, expected: ArrayLike
    ):
        tivs = TIVCollection.from_pcp(chroma_data.T)

        result = TISHorizontal()._distance(tivs, distance_type)

        assert np.allclose(result, expected)

    def test_run(self, data: pd.DataFrame):
        expected = pd.DataFrame([
            {
                "piece": "bach_2.mp3",
                "time": 0,
                "cos_tonal_disp": 0.92781653,
                "euc_tonal_disp": 5.99694061,
                "cos_dist": 1.62210025,
                "euc_dist": 8.25962446
            },
            {
                "piece": "bach_2.mp3",
                "time": 0.7,
                "cos_tonal_disp": 0.83445766,
                "euc_tonal_disp": 2.73590814,
                "cos_dist": 0,
                "euc_dist": 3.6818924
            },
            {
                "piece": "bach_2.mp3",
                "time": 1.2,
                "cos_tonal_disp": 0,
                "euc_tonal_disp": 2.68011437,
                "cos_dist": 0,
                "euc_dist": 7.79135677
            },
            {
                "piece": "bach_2.mp3",
                "time": 1.6,
                "cos_tonal_disp": 1.52347408,
                "euc_tonal_disp": 8.11866062,
                "cos_dist": 0,
                "euc_dist": 0
            },
            {
                "piece": "chopin_3.mp3",
                "time": 0,
                "cos_tonal_disp": 0.51068575,
                "euc_tonal_disp": 5.49711035,
                "cos_dist": 1.59634496,
                "euc_dist": 9.51889966
            },
            {
                "piece": "chopin_3.mp3",
                "time": 0.4,
                "cos_tonal_disp": 1.18997034,
                "euc_tonal_disp": 4.36515155,
                "cos_dist": 1.83722253,
                "euc_dist": 8.82507864
            },
            {
                "piece": "chopin_3.mp3",
                "time": 0.9,
                "cos_tonal_disp": 0.91610324,
                "euc_tonal_disp": 5.64469452,
                "cos_dist": 1.17674747,
                "euc_dist": 7.91044942
            },
            {
                "piece": "chopin_3.mp3",
                "time": 1.3,
                "cos_tonal_disp": 0.46834717,
                "euc_tonal_disp": 3.95545254,
                "cos_dist": 0,
                "euc_dist": 0
            }
        ])

        expected = expected.set_index(["piece", "time"])

        result = TISHorizontal().run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        print(compare)

        assert compare.all(axis=None)
