from src.data.pipelines.feature_pipeline import FeaturePipeline
from src.data.constants.feature_groups import COMPLEXITY_FEATS
from src.features.chroma_resolution import ChromaResolution
from src.features.normalized_chroma import NormalizedChroma
from src.features.complexity import Complexity
from src.features.mean_and_std import MeanAndStd


class ComplexityPipeline(FeaturePipeline):
    def __init__(self, chroma_resolution: float) -> None:
        super().__init__(
            [Complexity()],
            COMPLEXITY_FEATS,
            MeanAndStd(),
            [ChromaResolution(chroma_resolution), NormalizedChroma()]
        )
