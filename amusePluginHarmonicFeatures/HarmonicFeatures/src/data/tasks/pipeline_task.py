import pandas as pd
from abc import ABC, abstractmethod


class PipelineTask(ABC):
    """This class represents a data processing task that can be included
    in a `Pipeline`.
    """

    @abstractmethod
    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        """Runs the task on the specified `DataFrame` object

        Args:
            data: The `DataFrame` object to process.

        Returns:
            The processed `DataFrame` object.
        """
        pass  # pragma: no cover
