import pandas as pd
from src.data.tasks.pipeline_task import PipelineTask


class SelectColumns(PipelineTask):
    def __init__(self, columns: list[str]) -> None:
        self.columns = columns

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        return data[self.columns]
