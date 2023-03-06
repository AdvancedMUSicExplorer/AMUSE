from numpy.typing import ArrayLike
import pandas as pd
import numpy as np
import copy
from lib.HCDF.HCDF import harmonic_change
from src.data.constants.features import HarmRhythmFeats
from src.data.constants.feature_groups import CHROMA_FEATS
from src.data.tasks.pipeline_task import PipelineTask


class HCDFSegmentation(PipelineTask):
    def _get_window_right_bounds(
        self, changes: ArrayLike, n_chromas: ArrayLike
    ):
        right_bounds = copy.deepcopy(changes)

        # Remove first and subtract 1 to all elements
        right_bounds = right_bounds[1:] - 1
        right_bounds = np.append(right_bounds, n_chromas - 1)

        return right_bounds

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        # Grouped by piece with a column containing the tuple
        # (changes, hcdf_changes, harmonic_function)
        grouped = data.groupby('piece', sort=False)

        segmented = []

        for piece, group in grouped:
            # Get left bound of windows
            peak_indexes, peak_mags, _ = harmonic_change(
                group[CHROMA_FEATS].values
            )

            # Remove peak at index 0
            peak_indexes = peak_indexes[1:]
            peak_mags = peak_mags[1:]

            right_bounds = self._get_window_right_bounds(
                peak_indexes, group.shape[0]
            )

            for i in range(peak_indexes.size - 1):
                left_bound_idx = peak_indexes[i]
                right_bound_idx = right_bounds[i]

                piece, time = group.iloc[left_bound_idx].name
                row = group[left_bound_idx:right_bound_idx+1].sum().to_dict()
                row['piece'] = piece
                row['time'] = time
                row[HarmRhythmFeats.HCDF_PEAK_IDX] = peak_indexes[i]
                row[HarmRhythmFeats.HCDF_PEAK_MAG] = peak_mags[i]

                segmented.append(row)

        segmented_df = pd.DataFrame(segmented)
        segmented_df = segmented_df.set_index(['piece', 'time'])

        return segmented_df
