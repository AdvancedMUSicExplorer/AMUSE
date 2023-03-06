import numpy as np
from src.features.stats import Stats


class MeanAndStd(Stats):
    """This task computes the mean and standard deviation of all columns in a
    `DataFrame` object."""

    def __init__(self):
        def std(data):
            return np.std(data)

        super().__init__([np.mean, std])
