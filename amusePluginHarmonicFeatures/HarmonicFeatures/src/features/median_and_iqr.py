import numpy as np
from scipy.stats import iqr
from src.features.stats import Stats


class MedianAndIQR(Stats):
    """This task computes the median and IQR of all columns in a
    `DataFrame` object."""

    def __init__(self):
        super().__init__([np.median, iqr])
