from src.data.pipelines.feature_pipeline import FeaturePipeline
from src.data.constants.feature_groups import HARM_RHYTHM_FEATS
from src.features.harm_rhythm import HarmRhythm
from src.features.stats import Stats
from src.features.mean_and_std import MeanAndStd
from src.features.skewness_and_whiskers import SknewnessAndWhiskers


class HarmRhythmPipeline(FeaturePipeline):
    def __init__(self) -> None:
        stats = Stats(MeanAndStd().funcs)

        super().__init__(
            [HarmRhythm()],
            HARM_RHYTHM_FEATS,
            stats
        )
