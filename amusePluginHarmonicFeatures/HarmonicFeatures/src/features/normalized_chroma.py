import pandas as pd
from src.data.tasks.pipeline_task import PipelineTask


class NormalizedChroma(PipelineTask):
    """This task normalizes chroma vectors by diving each vector by its
    Manhattan norm
    """

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        return data.div(data.abs().sum(axis=1), axis=0).fillna(0)
