from typing import Any, Callable
import pandas as pd
import numpy as np
from numpy.typing import ArrayLike
from src.data.tasks.pipeline_task import PipelineTask
from scipy.stats.mstats import gmean
from src.data.constants.features import ComplexityFeats
from src.data.constants.feature_groups import (
    CHROMA_FEATS,
    COMPLEXITY_FEATS
)
import src.utils.math as math


class Complexity(PipelineTask):
    """This task computes tonal complexity features on the specified
    `DataFrame` object.
    """

    def _null_chroma_returns_zero(
        feature: Callable[[Any, ArrayLike], ArrayLike]
    ):
        """Decorator used as an utility for functions that compute
        tonal complexity feature values.

        Forces the value of a tonal complexity feature to be zero if the
        chroma vector passed for calculation is a null vector (all chroma
        values are equal to zero).

        Args:
            feature: Function to decorate.
        """

        def wrapper(self, chroma_vector):
            null_chromas = np.where(~chroma_vector.any(axis=1))[0]
            result = feature(self, chroma_vector)
            result.put(null_chromas, 0)

            return result

        return wrapper

    def _sort_chroma_fifths(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Sorts a given chroma vector or array of chroma vectors in fifths.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            Chroma vector(s) sorted in fifths.
        """
        sorted = chroma_vector.copy()

        for q in range(12):
            sorted[:, q] = chroma_vector[:, (q * 7) % 12]

        return sorted

    @_null_chroma_returns_zero
    def _sum_chroma_diff(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Calculates the absolute difference between all neighboring chroma values.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        diff_sum = np.zeros(chroma_vector.shape[0])

        for q in range(12):
            diff = chroma_vector[:, (q + 1) % 12] - chroma_vector[:, q]
            diff = np.abs(diff)

            diff_sum += diff

        diff_sum = 1 - diff_sum / 2

        return diff_sum

    @_null_chroma_returns_zero
    def _chroma_std(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Computes the standard deviation of the chroma vector.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        std = np.std(chroma_vector, axis=1)

        rescale_factor = 1 / np.sqrt(12)

        std = 1 - std / rescale_factor

        return std

    @_null_chroma_returns_zero
    def _neg_slope(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Computes the negative slope value of the chroma vector
        sorted in a descending order

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        chroma_vector = np.sort(-chroma_vector, axis=1)
        pitch_classes = np.array(list(range(12)))

        coef_matrix = np.vstack([pitch_classes, np.ones(len(pitch_classes))]).T

        # Get the slope values of the linear regression of each chroma vector
        slopes, _ = np.linalg.lstsq(
            coef_matrix, chroma_vector.T, rcond=None)[0]

        rescale_factor = 0.039

        slopes = 1 - np.abs(slopes) / rescale_factor

        return slopes

    def _entropy(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Computes the Shannon entropy of the chroma vector.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        return (1 / np.log2(12)) * math.entropy(chroma_vector, np.log2)

    @_null_chroma_returns_zero
    def _non_sparseness(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Computes the non-sparseness of the chroma vector based on the
        relationship between its l1 and l2 norm.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        l1 = math.l1_norm(chroma_vector)
        l2 = math.l2_norm(chroma_vector)

        non_sparse = 1 - (np.sqrt(12) - np.divide(l1, l2, where=l2 > 0)) / (
            np.sqrt(12) - 1
        )

        return non_sparse

    @_null_chroma_returns_zero
    def _flatness(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Computes a flatness measure of the chroma vector based on the
        relationship between its geometric and arithmetic mean.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        with np.errstate(divide="ignore"):
            geom_mean = gmean(chroma_vector, axis=1)

        arith_mean = np.mean(chroma_vector, axis=1)

        ratio = np.divide(geom_mean, arith_mean, where=arith_mean > 0)

        return ratio

    @_null_chroma_returns_zero
    def _angular_deviation(self, chroma_vector: ArrayLike) -> ArrayLike:
        """Computes the angular deviation of the fifth-ordered chroma vector

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.

        Returns:
            The value of this feature or an array containing the value of
            this feature for each chroma vector.
        """
        fifth_sorted = self._sort_chroma_fifths(chroma_vector)
        pitch_class_dist = 0

        for q in range(12):
            pitch_class_dist += fifth_sorted[:, q] * np.exp(
                2j * np.pi * q / 12)

        pitch_class_dist = np.abs(pitch_class_dist)
        pitch_class_dist = np.sqrt(1 - pitch_class_dist)

        return pitch_class_dist

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        data_cpy = data.copy()

        data_cpy[ComplexityFeats.DIFF] = self._sum_chroma_diff(
            data[CHROMA_FEATS].values
        )
        data_cpy[ComplexityFeats.STD] = self._chroma_std(
            data[CHROMA_FEATS].values
        )
        data_cpy[ComplexityFeats.SLOPE] = self._neg_slope(
            data[CHROMA_FEATS].values
        )
        data_cpy[ComplexityFeats.ENTROPY] = self._entropy(
            data[CHROMA_FEATS].values
        )
        data_cpy[ComplexityFeats.NON_SPARSENESS] = self._non_sparseness(
            data[CHROMA_FEATS].values
        )
        data_cpy[ComplexityFeats.FLATNESS] = self._flatness(
            data[CHROMA_FEATS].values
        )
        data_cpy[ComplexityFeats.FIFTH_ANG_DEV] = self._angular_deviation(
            data[CHROMA_FEATS].values
        )

        return data_cpy[COMPLEXITY_FEATS]
