import pytest
import numpy as np
import pandas as pd
from src.data.pipelines.pipeline import Pipeline
from src.data.tasks.pipeline_task_group import PipelineTaskGroup
from src.data.constants.feature_groups import CHROMA_FEATS
from src.features.template_based import TemplateBased
from src.features.complexity import Complexity
from src.data.tasks.remove_columns import RemoveColumns


class TestFeatureGroup:
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

    def test_pipeline_task_group(self, data):
        expected = pd.DataFrame(
            [
                {
                    "piece": "bach_2.mp3",
                    "time": 0,
                    "ic1": 0.0873,
                    "ic2": 0.0814,
                    "ic3": 0.0717,
                    "ic4": 0.0827,
                    "ic5": 0.0789,
                    "ic6": 0.0808,
                    "chord_maj": 0.006011,
                    "chord_min": 0.005965,
                    "chord_dim": 0.004866,
                    "chord_aug": 0.006651,
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
                    "ic1": 0.0820,
                    "ic2": 0.0577,
                    "ic3": 0.0850,
                    "ic4": 0.1009,
                    "ic5": 0.0826,
                    "ic6": 0.0670,
                    "chord_maj": 0.008792,
                    "chord_min": 0.008448,
                    "chord_dim": 0.005848,
                    "chord_aug": 0.010998,
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
                    "ic1": 0,
                    "ic2": 0,
                    "ic3": 0,
                    "ic4": 0,
                    "ic5": 0,
                    "ic6": 0,
                    "chord_maj": 0,
                    "chord_min": 0,
                    "chord_dim": 0,
                    "chord_aug": 0,
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

        pipeline = Pipeline()
        group = PipelineTaskGroup()

        group.add_task(TemplateBased())
        group.add_task(Complexity())

        pipeline.add_task(group)
        pipeline.add_task(RemoveColumns(CHROMA_FEATS))

        result = pipeline.run(data)

        compare = pd.DataFrame(
            np.isclose(expected, result), columns=expected.columns)

        assert compare.all(axis=None)
