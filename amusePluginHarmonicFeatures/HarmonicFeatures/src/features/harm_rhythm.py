import numpy as np
import pandas as pd
from numpy.typing import ArrayLike
from src.data.tasks.pipeline_task import PipelineTask
from src.data.constants.features import HarmRhythmFeats
from src.data.constants.feature_groups import HARM_RHYTHM_FEATS


class HarmRhythm(PipelineTask):
    def _hcdf_peak_interval(self, hcdf_peak_indexes: ArrayLike):
        intervals = hcdf_peak_indexes[1:] - hcdf_peak_indexes[0:-1]

        intervals = np.append(intervals, 0)

        return intervals

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        data_cpy = data.copy()

        grouped = data_cpy.groupby('piece')

        for piece, group in grouped:
            data_cpy.loc[
                piece,
                HarmRhythmFeats.HCDF_PEAK_INT] = self._hcdf_peak_interval(
                    group[HarmRhythmFeats.HCDF_PEAK_IDX].values
            )

        return data_cpy[HARM_RHYTHM_FEATS]
