import pytest
import pandas as pd
import numpy as np
from src.features.chroma_resolution import ChromaResolution
from src.features.normalized_chroma import NormalizedChroma
from src.data.pipelines.pipeline import Pipeline


class TestPipeline:
    @pytest.fixture
    def data(scope="class"):
        return pd.DataFrame(
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
                    "c10": -0.13,
                    "c11": 0.02,
                    "c12": -0.05,
                },
            ]
        )

    def test_one_step(self, data):
        pipeline = Pipeline()
        pipeline.add_task(ChromaResolution(0.5))

        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 0.33,
                    "c2": 2.67,
                    "c3": 2.1,
                    "c4": 2.2,
                    "c5": 0.31,
                    "c6": 1.44,
                    "c7": 3.4,
                    "c8": 4.62,
                    "c9": 2.43,
                    "c10": 4.3,
                    "c11": 4.18,
                    "c12": 2.77,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0,
                    "c1": 0,
                    "c2": 0.12,
                    "c3": 0.7,
                    "c4": 0.39,
                    "c5": 0.22,
                    "c6": 1.28,
                    "c7": 0.37,
                    "c8": 0.6,
                    "c9": 1.11,
                    "c10": -0.13,
                    "c11": 0.02,
                    "c12": -0.05,
                },
            ]
        )

        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = pipeline.run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)

    def test_two_steps(self, data):
        pipeline = Pipeline()
        pipeline.add_task(ChromaResolution(0.5))
        pipeline.add_task(NormalizedChroma())

        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "c1": 0.01073170732,
                    "c2": 0.08682926829,
                    "c3": 0.06829268293,
                    "c4": 0.07154471545,
                    "c5": 0.01008130081,
                    "c6": 0.04682926829,
                    "c7": 0.1105691057,
                    "c8": 0.1502439024,
                    "c9": 0.07902439024,
                    "c10": 0.1398373984,
                    "c11": 0.1359349593,
                    "c12": 0.09008130081,
                },
                {
                    "piece": "vivaldi_1.mp3",
                    "time": 0,
                    "c1": 0,
                    "c2": 0.02404809619,
                    "c3": 0.1402805611,
                    "c4": 0.07815631263,
                    "c5": 0.04408817635,
                    "c6": 0.2565130261,
                    "c7": 0.07414829659,
                    "c8": 0.120240481,
                    "c9": 0.2224448898,
                    "c10": -0.02605210421,
                    "c11": 0.004008016032,
                    "c12": -0.01002004008,
                },
            ]
        )

        expected["time"] = pd.to_timedelta(expected["time"], unit="s")
        expected = expected.set_index(["piece", "time"])

        result = pipeline.run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
