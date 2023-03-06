import pandas as pd
from numpy.typing import ArrayLike
from src.data.tasks.pipeline_task import PipelineTask
from src.data.constants.features import ChordFeats, IntervalFeats
from src.data.constants.feature_groups import (
    CHROMA_FEATS,
    TEMPLATE_FEATS
)


class TemplateBased(PipelineTask):
    """This task computes template-based features related to the occurence of
    certain intervals and triads
    """

    TEMPLATES = {
        IntervalFeats.IC1: [1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        IntervalFeats.IC2: [1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        IntervalFeats.IC3: [1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0],
        IntervalFeats.IC4: [1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0],
        IntervalFeats.IC5: [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0],
        IntervalFeats.IC6: [1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0],
        ChordFeats.MAJ: [1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0],
        ChordFeats.MIN: [1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0],
        ChordFeats.DIM: [1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0],
        ChordFeats.AUG: [1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0],
    }

    def _calc_template_feat(
        self, chroma_vector: ArrayLike, template_key: str
    ) -> ArrayLike:
        """Computes a given template feature for a chroma vector.

        The template is selected by `template_key` which searches the
        `TEMPLATES` dictionary for the corresponding template.

        Args:
            chroma_vector: A 12-dimensional chroma vector or
            an array of such vectors.
            template_key: String key of the template in the
            `TEMPLATES`dictionary.

        Returns:
            The value of the template feature or an array containing its
            value for each chroma vector.
        """
        template = self.TEMPLATES[template_key]

        likelihood_sum = 0
        for q in range(12):
            likelihood = 1

            for k in range(12):
                chroma_value = chroma_vector[:, (q + k) % 12]

                if template[k]:
                    likelihood *= chroma_value

            likelihood_sum += likelihood

        return likelihood_sum

    def run(self, data: pd.DataFrame) -> pd.DataFrame:
        data_copy = data.copy()

        for template_key in self.TEMPLATES.keys():
            data_copy[template_key] = self._calc_template_feat(
                data[CHROMA_FEATS].values, template_key
            )

        return data_copy[TEMPLATE_FEATS]
