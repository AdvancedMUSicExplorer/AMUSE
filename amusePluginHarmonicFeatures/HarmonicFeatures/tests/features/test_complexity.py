import pytest
import pandas as pd
import numpy as np
from src.features.complexity import Complexity
from src.data.constants.feature_groups import CHROMA_FEATS


class TestComplexity:
    @pytest.fixture
    def data(scope="class"):
        df = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 0.03,
                    "c2": 0.07,
                    "c3": 0.11,
                    "c4": 0.09,
                    "c5": 0.01,
                    "c6": 0.04,
                    "c7": 0.06,
                    "c8": 0.13,
                    "c9": 0.06,
                    "c10": 0.21,
                    "c11": 0.12,
                    "c12": 0.07,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "c1": 0.02,
                    "c2": 0.08,
                    "c3": 0.14,
                    "c4": 0.05,
                    "c5": 0.01,
                    "c6": 0.15,
                    "c7": 0.10,
                    "c8": 0.05,
                    "c9": 0.03,
                    "c10": 0.07,
                    "c11": 0.18,
                    "c12": 0.12,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.2,
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
            ]
        )

        df["time"] = pd.to_timedelta(df["time"], unit="s")
        df = df.set_index(["piece", "time"])

        return df

    @pytest.fixture
    def flat_vector(scope="class"):
        return np.array(
            [
                [
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                    1 / 12,
                ]
            ]
        )

    @pytest.fixture
    def sparse_vector(scope="class"):
        return np.array([[0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]])

    def test_sort_chroma_fifths(self, data):
        expected = np.array(
            [
                [
                    0.03,
                    0.13,
                    0.11,
                    0.21,
                    0.01,
                    0.07,
                    0.06,
                    0.07,
                    0.06,
                    0.09,
                    0.12,
                    0.04,
                ],
                [0.02, 0.05, 0.14, 0.07, 0.01, 0.12, 0.1, 0.08, 0.03, 0.05,
                    0.18, 0.15],
                [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
            ]
        )

        result = Complexity()._sort_chroma_fifths(data[CHROMA_FEATS].values)

        assert np.array_equal(result, expected)

    def test_sum_chroma_diff(self, data, flat_vector, sparse_vector):
        expected = np.array([0.65, 0.59, 0])

        result = Complexity()._sum_chroma_diff(data[CHROMA_FEATS].values)
        assert np.allclose(result, expected)

        result = Complexity()._sum_chroma_diff(flat_vector)
        assert np.allclose(result, np.array([1]))

        result = Complexity()._sum_chroma_diff(sparse_vector)
        assert np.allclose(result, np.array([0]))

    def test_chroma_std(self, data, flat_vector, sparse_vector):
        expected = np.array([0.82148763, 0.81760848, 0])

        result = Complexity()._chroma_std(data[CHROMA_FEATS].values)
        assert np.allclose(result, expected)

        result = Complexity()._chroma_std(flat_vector)
        assert np.allclose(result, np.array([1]))

        result = Complexity()._chroma_std(sparse_vector)
        assert np.allclose(result, np.array([0.042572892]))

    def test_neg_slope(self, data, flat_vector, sparse_vector):
        expected = np.array([0.641384257, 0.612694997, 0])

        result = Complexity()._neg_slope(data[CHROMA_FEATS].values)
        assert np.allclose(result, expected)

        result = Complexity()._neg_slope(flat_vector)
        assert np.allclose(result, np.array([1]))

        result = Complexity()._neg_slope(sparse_vector)
        assert np.allclose(result, np.array([0.013806706]))

    def test_entropy(self, data, flat_vector, sparse_vector):
        expected = np.array([0.92430871434, 0.91369479299, 0])

        result = Complexity()._entropy(data[CHROMA_FEATS].values)
        assert np.allclose(result, expected)

        result = Complexity()._entropy(flat_vector)
        assert np.allclose(result, np.array([1]))

        result = Complexity()._entropy(sparse_vector)
        assert np.allclose(result, np.array([0]))

    def test_non_sparseness(self, data):
        expected = np.array([0.78985308194, 0.78265321994, 0])

        result = Complexity()._non_sparseness(data[CHROMA_FEATS].values)

        assert np.allclose(result, expected)

    def test_flatness(self, data):
        expected = np.array([0.7923240272, 0.754386555, 0])

        result = Complexity()._flatness(data[CHROMA_FEATS].values)

        print(result)

        assert np.allclose(result, expected)

    def test_angular_deviation(self, data, flat_vector, sparse_vector):
        expected = np.array([0.9294068001, 0.955902087, 0])

        result = Complexity()._angular_deviation(data[CHROMA_FEATS].values)
        assert np.allclose(result, expected)

        result = Complexity()._angular_deviation(flat_vector)
        assert np.allclose(result, np.array([1]))

        result = Complexity()._angular_deviation(sparse_vector)
        assert np.allclose(result, np.array([0]))

    def test_extract(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "comp_diff": 0.65,
                    "comp_std": 0.82148763,
                    "comp_slope": 0.641384257,
                    "comp_entr": 0.92430871434,
                    "comp_sparse": 0.78985308194,
                    "comp_flat": 0.7923240272,
                    "comp_fifth": 0.9294068001,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.1,
                    "comp_diff": 0.59,
                    "comp_std": 0.81760848,
                    "comp_slope": 0.612694997,
                    "comp_entr": 0.91369479299,
                    "comp_sparse": 0.78265321994,
                    "comp_flat": 0.754386555,
                    "comp_fifth": 0.955902087,
                },
                {
                    "piece": "bach_2.mp3",
                    "time": 0.2,
                    "comp_diff": 0,
                    "comp_std": 0,
                    "comp_slope": 0,
                    "comp_entr": 0,
                    "comp_sparse": 0,
                    "comp_flat": 0,
                    "comp_fifth": 0,
                },
            ]
        )
        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = Complexity().run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
