import pandas as pd
from src.data.constants.others import INTERIM_DIR


class PipelineOutputCombiner:
    @classmethod
    def combine(
            cls, dataset: str, pipelines: list[str],
            index_col: str) -> pd.DataFrame:

        combined_data = None

        for p in pipelines:
            file = f'{INTERIM_DIR}/{dataset}/{p}.csv'

            data = pd.read_csv(
                file, dtype={'piece': str}, index_col=index_col
            )

            if combined_data is None:
                combined_data = data.copy()
            else:
                combined_data = combined_data.join(data, rsuffix='_')

        return combined_data
