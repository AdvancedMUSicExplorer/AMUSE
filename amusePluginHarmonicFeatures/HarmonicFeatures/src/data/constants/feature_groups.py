from src.data.constants.features import (
    ChromaFeats,
    HarmRhythmFeats,
    IntervalFeats,
    ChordFeats,
    ComplexityFeats,
    TISFeats
)


CHROMA_FEATS = [
    ChromaFeats.C1,
    ChromaFeats.C2,
    ChromaFeats.C3,
    ChromaFeats.C4,
    ChromaFeats.C5,
    ChromaFeats.C6,
    ChromaFeats.C7,
    ChromaFeats.C8,
    ChromaFeats.C9,
    ChromaFeats.C10,
    ChromaFeats.C11,
    ChromaFeats.C12,
]

INTERVAL_FEATS = [
    IntervalFeats.IC1,
    IntervalFeats.IC2,
    IntervalFeats.IC3,
    IntervalFeats.IC4,
    IntervalFeats.IC5,
    IntervalFeats.IC6,
]

CHORD_FEATS = [
    ChordFeats.MAJ,
    ChordFeats.MIN,
    ChordFeats.DIM,
    ChordFeats.AUG,
]

TEMPLATE_FEATS = INTERVAL_FEATS + CHORD_FEATS

COMPLEXITY_FEATS = [
    ComplexityFeats.DIFF,
    ComplexityFeats.STD,
    ComplexityFeats.SLOPE,
    ComplexityFeats.ENTROPY,
    ComplexityFeats.NON_SPARSENESS,
    ComplexityFeats.FLATNESS,
    ComplexityFeats.FIFTH_ANG_DEV,
]

TIS_COEFFICIENTS = [
    TISFeats.CHROMATICITY,
    TISFeats.DYADICITY,
    TISFeats.TRIADICITY,
    TISFeats.DIMINISHED_QUALITTY,
    TISFeats.DIATONICITY,
    TISFeats.WHOLETONENESS
]

TIS_VERTICAL_FEATS = [TISFeats.DISSONANCE] + \
    TIS_COEFFICIENTS + \
    [TISFeats.COEF_ENTROPY]

TIS_BASIC_FEATS = [TISFeats.DISSONANCE] + TIS_COEFFICIENTS

TIS_HORIZONTAL_FEATS = [
    TISFeats.COS_TONAL_DISP,
    TISFeats.EUC_TONAL_DISP,
    TISFeats.COS_DIST,
    TISFeats.EUC_DIST
]

TIS_COMPLEXITY = [
    TISFeats.COS_TONAL_DISP,
    TISFeats.EUC_TONAL_DISP,
    TISFeats.COS_DIST,
    TISFeats.EUC_DIST,
    TISFeats.COEF_ENTROPY
]

HARM_RHYTHM_FEATS = [
    HarmRhythmFeats.HCDF_PEAK_INT,
    HarmRhythmFeats.HCDF_PEAK_MAG
]
