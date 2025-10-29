from src.data.pipelines.pipeline import Pipeline
from src.data.tasks.pipeline_task import PipelineTask
from src.data.tasks.pipeline_task_group import PipelineTaskGroup
from src.data.tasks.select_columns import SelectColumns
from src.features.stats import Stats


class FeaturePipeline(Pipeline):
    def __init__(
        self,
        feature_tasks: list[PipelineTask],
        feature_cols: list[str],
        stats_task: Stats,
            prep_tasks: list[PipelineTask] = []) -> None:

        super().__init__()

        for task in prep_tasks:
            self.add_task(task)

        group = PipelineTaskGroup()

        for task in feature_tasks:
            group.add_task(task)

        self.add_task(group)

        self.add_task(SelectColumns(feature_cols))

        self.add_task(stats_task)
