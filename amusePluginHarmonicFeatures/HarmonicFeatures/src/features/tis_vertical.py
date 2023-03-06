import numpy as np
import pandas as pd
from numpy.typing import ArrayLike
from TIVlib import TIV, TIVCollection
from src.data.tasks.pipeline_task import PipelineTask
from src.data.constants.features import TISFeats
from src.data.constants.feature_groups import (
    CHROMA_FEATS,
    TIS_VERTICAL_FEATS,
    TIS_COEFFICIENTS
)
import src.utils.math as math


class TISVertical(PipelineTask):
    def _tiv_mags(self, tivs: TIVCollection):
        return np.abs(tivs.vectors)

    def _coefficient(self, tiv_mags: ArrayLike, k: int) -> ArrayLike:
        return tiv_mags[:, k] / TIV.weights[k]

    def _dissonance(self, tivs: TIVCollection):
        return 1 - (np.linalg.norm(tivs.vectors, axis=1)/np.sqrt(np.sum(
            np.dot(TIV.weights, TIV.weights)))
        )

    def _coef_entropy(self, tiv_coefficients: ArrayLike):
        normalized_coefficients = math.normalize(tiv_coefficients)

        return math.entropy(normalized_coefficients)

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        data_cpy = data.copy()

        tivs = TIVCollection.from_pcp(data_cpy[CHROMA_FEATS].values.T)
        mags = self._tiv_mags(tivs)

        data_cpy[TISFeats.DISSONANCE] = self._dissonance(tivs)
        data_cpy[TISFeats.CHROMATICITY] = self._coefficient(mags, 0)
        data_cpy[TISFeats.DYADICITY] = self._coefficient(mags, 1)
        data_cpy[TISFeats.TRIADICITY] = self._coefficient(mags, 2)
        data_cpy[TISFeats.DIMINISHED_QUALITTY] = self._coefficient(mags, 3)
        data_cpy[TISFeats.DIATONICITY] = self._coefficient(mags, 4)
        data_cpy[TISFeats.WHOLETONENESS] = self._coefficient(mags, 5)
        data_cpy[TISFeats.COEF_ENTROPY] = self._coef_entropy(mags)

        return data_cpy[TIS_VERTICAL_FEATS]
