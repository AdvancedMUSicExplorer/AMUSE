import pandas as pd
from numpy.typing import ArrayLike
from typing import Any, Callable
from src.data.tasks.pipeline_task import PipelineTask


class Stats(PipelineTask):
    """This task computes a list of statistical functions on a given
    `DataFrame` object."""

    def __init__(self, stat_funcs: Callable[[ArrayLike], Any]):
        super().__init__()

        self.funcs = stat_funcs

    def run(self, data: pd.DataFrame) -> pd.DataFrame:

        temp = data.groupby("piece").agg(self.funcs)
        temp.columns = temp.columns.to_flat_index()

        return temp
