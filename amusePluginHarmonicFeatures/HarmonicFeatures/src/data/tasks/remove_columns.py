import pandas as pd
from src.data.tasks.pipeline_task import PipelineTask


class RemoveColumns(PipelineTask):
    """When run, this task removes all columns specified
    columsn from a given `DataFrame`
    """

    def __init__(self, columns: list[str]) -> None:
        self.columns = columns

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        return data.drop(columns=self.columns)
