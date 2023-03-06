from scipy.stats import skew
from src.features.stats import Stats
import src.utils.math as math


class SknewnessAndWhiskers(Stats):
    """This task computes the Skewness and whisker values of all columns in a
    `DataFrame` object."""

    def __init__(self):
        super().__init__(
            [skew, self.lower_whisker, self.upper_whisker]
        )

    def lower_whisker(self, data):
        """Returns the lower whisker value of a number array."""

        lower, _ = math.whisker_values(data)

        return lower

    def upper_whisker(self, data):
        """Returns the upper whisker value of a number array."""

        _, upper = math.whisker_values(data)

        return upper
