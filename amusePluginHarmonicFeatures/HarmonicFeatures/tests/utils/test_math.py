import pytest
import numpy as np
import src.utils.math as math


class TestMath:
    def test_l1_norm(self):
        vectors = np.array([[0.5, 0.3, -1.2, 2.3], [-1.5, 0, 0.3, -0.7]])

        expected = np.array([4.3, 2.5])

        result = math.l1_norm(vectors)

        assert np.array_equal(result, expected)

    def test_l2_norm(self):
        vectors = np.array([[0.5, 0.3, -1.2, 2.3], [-1.5, 0, 0.3, -0.7]])

        expected = np.array([2.65894716006, 1.68226038413])

        result = math.l2_norm(vectors)

        assert np.allclose(result, expected)

    @pytest.mark.parametrize(
        "vector1, vector2, expected",
        [
            (
                np.array([[1, 2, 3, 1.5, 2.5], [4, 5, 6, 4.5, 5.5]]),
                np.array([[0, 1, 2, 1.2, 3], [0.2, 1.2, 1, 3, 2]]),
                np.array([17.3, 37.3]),
            ),
            (
                np.array([[1 + 2j, 0.5 + 0.6j], [0.2 + 1.7j, 0.8 - 0.01j]]),
                np.array(
                    [[3.2 - 1.2j, -0.9 + 0.9j], [-0.2 - 5j, 2.2j + 0.07j]]
                ),
                np.array(
                    [
                        4.609999999999999 + 5.11j,
                        8.482700000000001 + 0.47600000000000003j,
                    ]
                ),
            ),
            (np.array([1, 2, 3]), np.array([4, 5, 6]), 32),
        ],
    )
    def test_dot_product(self, vector1, vector2, expected):
        result = math.dot_product(vector1, vector2)

        assert np.allclose(result, expected)

    @pytest.mark.parametrize(
        "vector1, vector2, expected",
        [
            (
                np.array(
                    [
                        [
                            0.85781311 - 0.74768543j,
                            3.82338512 - 2.79280801j,
                            -1.8524121 - 5.86753884j,
                            -1.06091578 - 6.46863619j,
                            -5.40284242 - 4.88700477j,
                            -2.53270646 + 0.0j,
                        ],
                        [
                            0.39170881 - 1.57540913j,
                            0.12793734 - 2.56415877j,
                            -2.67232376 - 2.55221932j,
                            -0.08322454 - 1.24646607j,
                            -2.38542633 - 4.43415179j,
                            -3.6618799 + 0.0j,
                        ],
                        [0j, 0j, 0j, 0j, 0j, 0j],
                    ]
                ),
                np.array(
                    [
                        [
                            0.00740464 - 1.34512277j,
                            1.72727273 - 0.01628888j,
                            -0.61285266 - 3.98354232j,
                            -2.36873041 + 4.69323328j,
                            6.66875638 + 0.899154j,
                            -0.04702194 + 0.0j,
                        ]
                    ]
                ),
                np.array([1.86484487, 1.71646859, 0]),
            )
        ],
    )
    def test_complex_cosine_dist(self, vector1, vector2, expected):
        result = math.complex_cosine_dist(vector1, vector2)

        assert np.allclose(result, expected)

    def test_normalize(self):
        values = np.array([
            [1.0, 3.0, 4.0, 5.0],
            [2.0, 2.0, 2.0, 2.0],
            [0.0, 0.0, 0.0, 0.0]
        ])

        expected = np.array([
            [0.07692307692, 0.2307692308, 0.3076923077, 0.3846153846],
            [0.25, 0.25, 0.25, 0.25],
            [0, 0, 0, 0]
        ])

        result = math.normalize(values)

        assert np.allclose(result, expected)

    def test_entropy(self):
        values = np.array([
            [0.14, 0.26, 0.48, 0.02],
            [0.25, 0.25, 0.25, 0.25],
            [0.0, 0.0, 0.0, 0.0]
        ])

        expected = np.array([1.0560406125, 1.3862943611, 0])

        result = math.entropy(values)

        assert np.allclose(result, expected)

    @pytest.mark.parametrize("values, expected", [
        (np.array([0.14, 0.26, 0.48, 0.02]), (-0.1975, 0.6225)),
        (np.array([0.99, 0.88, 0.77, 0.66]), (0.495, 1.155)),
        (np.array([0.0, 0.0, 0.0, 0.0]), (0.0, 0.0))
    ])
    def test_whisker_values(self, values, expected):
        result = math.whisker_values(values)

        assert np.allclose(result, expected)
