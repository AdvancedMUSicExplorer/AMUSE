import pytest
import pandas as pd
import numpy as np
from src.features.chroma_resolution import ChromaResolution


class TestChromaResolution:
    @pytest.fixture
    def data(scope="class"):
        data = [
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
                "c11": 0.8,
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
                "piece": "bach_2.mp3",
                "time": 0.2,
                "c1": 3.66,
                "c2": 3.02,
                "c3": 2.72,
                "c4": 0.73,
                "c5": 0.81,
                "c6": 0.02,
                "c7": 0.01,
                "c8": 0.14,
                "c9": 2.92,
                "c10": 3.88,
                "c11": 1.06,
                "c12": 0.2,
            },
            {
                "piece": "bach_2.mp3",
                "time": 0.3,
                "c1": 0,
                "c2": 0.44,
                "c3": 0,
                "c4": 0,
                "c5": 0.4,
                "c6": 1.4,
                "c7": 3.1,
                "c8": 2.9,
                "c9": 0.9,
                "c10": 0.96,
                "c11": 1.93,
                "c12": 2.11,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0,
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
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.1,
                "c1": 0,
                "c2": 0.12,
                "c3": 0.7,
                "c4": 0.39,
                "c5": 0.22,
                "c6": 1.28,
                "c7": 0.37,
                "c8": 0.6,
                "c9": 1.11,
                "c10": 0.13,
                "c11": 0.02,
                "c12": 0.05,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.2,
                "c1": 0.02,
                "c2": 0.9,
                "c3": 2.53,
                "c4": 1.57,
                "c5": 2.71,
                "c6": 0.88,
                "c7": 0.33,
                "c8": 0.02,
                "c9": 0.08,
                "c10": 0.92,
                "c11": 0.55,
                "c12": 0.43,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.3,
                "c1": 1.06,
                "c2": 1.01,
                "c3": 0.3,
                "c4": 0.45,
                "c5": 3.03,
                "c6": 2.36,
                "c7": 1.02,
                "c8": 1.99,
                "c9": 0.98,
                "c10": 0.5,
                "c11": 4.51,
                "c12": 0.01,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.4,
                "c1": 0,
                "c2": 0.1,
                "c3": 0,
                "c4": 0,
                "c5": 0.2,
                "c6": 0.54,
                "c7": 1.69,
                "c8": 2.09,
                "c9": 1.54,
                "c10": 0.02,
                "c11": 3.01,
                "c12": 2.04,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.5,
                "c1": 0.1,
                "c2": 0.2,
                "c3": 0.3,
                "c4": 0.4,
                "c5": 0.1,
                "c6": 0.22,
                "c7": 0.56,
                "c8": 1.23,
                "c9": 3.11,
                "c10": 1.23,
                "c11": 0.5,
                "c12": 0.6,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.6,
                "c1": 0.3,
                "c2": 0,
                "c3": 0,
                "c4": 0,
                "c5": 1.33,
                "c6": 1.34,
                "c7": 2.01,
                "c8": 3,
                "c9": 0.77,
                "c10": 0.34,
                "c11": 0.61,
                "c12": 3.1,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.7,
                "c1": 1.02,
                "c2": 0.75,
                "c3": 0,
                "c4": 0,
                "c5": 0.03,
                "c6": 0.13,
                "c7": 1.32,
                "c8": 0.92,
                "c9": 3.17,
                "c10": 4.04,
                "c11": 0,
                "c12": 0,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.8,
                "c1": 0,
                "c2": 0,
                "c3": 0,
                "c4": 0,
                "c5": 2.03,
                "c6": 0.16,
                "c7": 1.16,
                "c8": 3.51,
                "c9": 0.92,
                "c10": 0.93,
                "c11": 2.11,
                "c12": 2.12,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 0.9,
                "c1": 0,
                "c2": 0,
                "c3": 0,
                "c4": 0,
                "c5": 0.4,
                "c6": 0.5,
                "c7": 0,
                "c8": 0.03,
                "c9": 0.05,
                "c10": 0.6,
                "c11": 0.72,
                "c12": 0.89,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 1,
                "c1": 4.14,
                "c2": 3.15,
                "c3": 0.14,
                "c4": 0.41,
                "c5": 0.66,
                "c6": 2.99,
                "c7": 3.98,
                "c8": 1.11,
                "c9": 3.41,
                "c10": 1.02,
                "c11": 0.02,
                "c12": 0.08,
            },
            {
                "piece": "vivaldi_1.mp3",
                "time": 1.1,
                "c1": 0.22,
                "c2": 0.23,
                "c3": 1.24,
                "c4": 2.25,
                "c5": 3.11,
                "c6": 2.01,
                "c7": 1.01,
                "c8": 0.98,
                "c9": 0.99,
                "c10": 1.04,
                "c11": 2.05,
                "c12": 1.19,
            },
        ]

        return pd.DataFrame(data)

    def test_500ms_resolution(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 3.99,
                    "c2": 6.13,
                    "c3": 4.82,
                    "c4": 2.93,
                    "c5": 1.52,
                    "c6": 2.86,
                    "c7": 6.51,
                    "c8": 7.66,
                    "c9": 6.25,
                    "c10": 9.14,
                    "c11": 7.17,
                    "c12": 5.08,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0,
                    "c1": 1.08,
                    "c2": 2.13,
                    "c3": 3.53,
                    "c4": 2.41,
                    "c5": 6.16,
                    "c6": 5.06,
                    "c7": 3.41,
                    "c8": 4.7,
                    "c9": 3.71,
                    "c10": 1.57,
                    "c11": 8.09,
                    "c12": 2.53,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0.5,
                    "c1": 1.42,
                    "c2": 0.95,
                    "c3": 0.3,
                    "c4": 0.4,
                    "c5": 3.89,
                    "c6": 2.35,
                    "c7": 5.05,
                    "c8": 8.69,
                    "c9": 8.02,
                    "c10": 7.14,
                    "c11": 3.94,
                    "c12": 6.71,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 1,
                    "c1": 4.36,
                    "c2": 3.38,
                    "c3": 1.38,
                    "c4": 2.66,
                    "c5": 3.77,
                    "c6": 5,
                    "c7": 4.99,
                    "c8": 2.09,
                    "c9": 4.4,
                    "c10": 2.06,
                    "c11": 2.07,
                    "c12": 1.27,
                },
            ]
        )
        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = ChromaResolution(0.5).run(data)

        # Create dataframe in which each cell will be True if
        # an element in expected is aproximately equal to
        # the corresponding element in result
        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)

    def test_100ms_resolution(self, data):
        expected = data.copy()
        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = ChromaResolution(0.1).run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)

    def test_1s_resolution(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 3.99,
                    "c2": 6.13,
                    "c3": 4.82,
                    "c4": 2.93,
                    "c5": 1.52,
                    "c6": 2.86,
                    "c7": 6.51,
                    "c8": 7.66,
                    "c9": 6.25,
                    "c10": 9.14,
                    "c11": 7.17,
                    "c12": 5.08,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0,
                    "c1": 2.5,
                    "c2": 3.08,
                    "c3": 3.83,
                    "c4": 2.81,
                    "c5": 10.05,
                    "c6": 7.41,
                    "c7": 8.46,
                    "c8": 13.39,
                    "c9": 11.73,
                    "c10": 8.71,
                    "c11": 12.03,
                    "c12": 9.24,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0.5,
                    "c1": 4.36,
                    "c2": 3.38,
                    "c3": 1.38,
                    "c4": 2.66,
                    "c5": 3.77,
                    "c6": 5,
                    "c7": 4.99,
                    "c8": 2.09,
                    "c9": 4.4,
                    "c10": 2.06,
                    "c11": 2.07,
                    "c12": 1.27,
                },
            ]
        )
        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = ChromaResolution(1).run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)

    def test_global_resolution(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "c1": 3.99,
                    "c2": 6.13,
                    "c3": 4.82,
                    "c4": 2.93,
                    "c5": 1.52,
                    "c6": 2.86,
                    "c7": 6.51,
                    "c8": 7.66,
                    "c9": 6.25,
                    "c10": 9.14,
                    "c11": 7.17,
                    "c12": 5.08,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "c1": 6.86,
                    "c2": 6.46,
                    "c3": 5.21,
                    "c4": 5.47,
                    "c5": 13.82,
                    "c6": 12.41,
                    "c7": 13.45,
                    "c8": 15.48,
                    "c9": 16.13,
                    "c10": 10.77,
                    "c11": 14.1,
                    "c12": 10.51,
                },
            ]
        )
        expected = expected.set_index("piece")

        result = ChromaResolution(ChromaResolution.GLOBAL).run(data)

        print(result)
        print(expected.compare(result))

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
