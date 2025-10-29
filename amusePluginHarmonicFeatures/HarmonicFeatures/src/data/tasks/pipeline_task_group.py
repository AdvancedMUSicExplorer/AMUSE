import pandas as pd
from src.data.tasks.pipeline_task import PipelineTask


class PipelineTaskGroup(PipelineTask):
    """This class represents a `PipelineTask` that can itself contain other tasks.

    Unlike what happens in a `Pipeline`, in which tasks are run
    sequentially (meaning they pass the output `DataFrame`onto the next task),
    a `PipelineTaskGroup` will always pass the same `DataFrame` object to
    each task and merge all resulting `DataFrame` objects.

    Attributes:
        tasks: list of tasks that are run once the group is executed.
    """

    def __init__(self) -> None:
        self.tasks: list[PipelineTask] = []

    def add_task(self, t: PipelineTask) -> None:
        """Adds a given task to the list of tasks.

        Args:
            t: Task to add.
        """
        self.tasks.append(t)

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        """Runs the list of tasks on the specified `DataFrame` object.

        Args:
            data: The `DataFrame` object to process.

        Returns:
            The processed `DataFrame` object.
        """
        data_copy = data.copy()

        for t in self.tasks:
            df = t.run(data_copy)

            data_copy = data_copy.join(df, rsuffix='_')

        return data_copy
