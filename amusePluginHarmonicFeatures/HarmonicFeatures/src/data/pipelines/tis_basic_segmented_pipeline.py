from src.data.pipelines.feature_pipeline import FeaturePipeline
from src.data.constants.feature_groups import TIS_BASIC_FEATS
from src.features.tis_vertical import TISVertical
from src.features.stats import Stats
from src.features.mean_and_std import MeanAndStd
from src.features.skewness_and_whiskers import SknewnessAndWhiskers


class TISBasicSegmentedPipeline(FeaturePipeline):
    def __init__(self) -> None:
        stats = Stats(MeanAndStd().funcs)

        super().__init__(
            [TISVertical()],
            TIS_BASIC_FEATS,
            stats
        )
