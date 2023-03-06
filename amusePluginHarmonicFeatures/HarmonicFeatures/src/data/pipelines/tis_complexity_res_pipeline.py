from src.data.pipelines.feature_pipeline import FeaturePipeline
from src.data.constants.feature_groups import TIS_COMPLEXITY
from src.features.chroma_resolution import ChromaResolution
from src.features.tis_vertical import TISVertical
from src.features.tis_horizontal import TISHorizontal
from src.features.stats import Stats
from src.features.mean_and_std import MeanAndStd
from src.features.skewness_and_whiskers import SknewnessAndWhiskers


class TISComplexityResPipeline(FeaturePipeline):
    def __init__(self, chroma_resolution: float = 0.1) -> None:
        stats = Stats(MeanAndStd().funcs)

        super().__init__(
            [TISVertical(), TISHorizontal()],
            TIS_COMPLEXITY,
            stats,
            [ChromaResolution(chroma_resolution)]
        )
