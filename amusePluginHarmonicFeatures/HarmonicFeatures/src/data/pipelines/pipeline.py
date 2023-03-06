import pandas as pd
from src.data.tasks.pipeline_task import PipelineTask


class Pipeline:
    """This class represents a data processing pipeline.

    Attributes:
        tasks: List of tasks that are executed once the pipeline is run.
    """

    def __init__(self) -> None:
        self.tasks: list[PipelineTask] = []

    def add_task(self, t: PipelineTask) -> None:
        """Adds a given task to the pipeline's list of tasks.

        Args:
            t: Task to add.
        """
        self.tasks.append(t)

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        """Runs the pipeline on a given `DataFrame` object by executing
        each task sequentially.

        Args:
            data: The `DataFrame` object to process.

        Returns:
            The processed `DataFrame` object.
        """
        df = data.copy()

        for s in self.tasks:
            df = s.run(df)

        return df
