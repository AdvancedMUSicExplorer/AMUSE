.PHONY: crossera res_feats all_res_feats single_res_feats \
	all_single_res_feats hcdf_segmentation combine_feats \
	combine_feats_multiple trainset all_trainsets train \
	plot_lda plot_hcdf plot_feature extract_chroma test clean

PYTHON_INTERPRETER = python3

install: 
	${PYTHON_INTERPRETER} -m pip install -r requirements.txt
	${PYTHON_INTERPRETER} -m pip install lib/TIVlib

crossera:
	${PYTHON_INTERPRETER} -m src.data.commands.make_crossera data/external/chroma data/external/crossera

res_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.make_res_feats $(dataset) $(pipeline)

all_res_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.make_all_res_feats $(dataset)

single_res_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.make_single_res_feats $(dataset) $(pipeline)

all_single_res_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.make_all_single_res_feats $(dataset)

hcdf_segmentation:
	${PYTHON_INTERPRETER} -m src.data.commands.make_hcdf_segmentation $(dataset)

segmented_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.make_segmented_feats $(dataset) $(pipeline)

all_segmented_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.make_all_segmented_feats $(dataset)

combine_feats:
	${PYTHON_INTERPRETER} -m src.data.commands.combine_pipeline_output $(dataset) $(pipelines)

combine_feats_multiple:
	${PYTHON_INTERPRETER} -m src.data.commands.combine_features_multiple $(dataset)

trainset:
	${PYTHON_INTERPRETER} -m src.data.commands.make_trainset $(dataset) $(pipeline)

all_trainsets:
	${PYTHON_INTERPRETER} -m src.data.commands.make_all_trainsets $(dataset)

train:
	${PYTHON_INTERPRETER} -m src.models.weiss $(dataset) $(pipeline) $(options)

train_all:
	${PYTHON_INTERPRETER} -m src.data.commands.train_all $(dataset)

predict:
	${PYTHON_INTERPRETER} -m src.data.commands.predict $(dataset) $(pipeline) $(test_set)

plot_lda:
	${PYTHON_INTERPRETER} -m src.tools.plot_lda $(dataset) "$(title)"

plot_hcdf:
	${PYTHON_INTERPRETER} -m src.tools.plot_hcdf $(filepath)

plot_feature_histogram:
	${PYTHON_INTERPRETER} -m src.tools.plot_feature_histogram $(dataset) "$(feature)"

extract_features:
	${PYTHON_INTERPRETER} -m src.tools.extract_features $(filepath) $(features)

extract_chroma:
	${PYTHON_INTERPRETER} -m src.tools.extract_chroma $(dataset)

plot_feature_errorbars:
	${PYTHON_INTERPRETER} -m src.tools.plot_feature_errorbars $(dataset) $(pipeline)

feature_cluster:
	${PYTHON_INTERPRETER} -m src.tools.feature_cluster $(dataset) $(pipeline) --stat=$(stat) --res=$(res)

plot_tonal_disp:
	${PYTHON_INTERPRETER} -m src.tools.plot_tonal_disp $(dataset) $(piece)

plot_tiv_coef:
	${PYTHON_INTERPRETER} -m src.tools.plot_tiv_coef $(dataset) $(piece) $(coef)

plot_piece_tivs:
	${PYTHON_INTERPRETER} -m src.tools.plot_piece_tivs $(dataset) $(piece)

feature_boxplots:
	${PYTHON_INTERPRETER} -m src.tools.feature_boxplots $(dataset) $(pipeline) $(feature) $(stat) $(res)

feature_importance:
	${PYTHON_INTERPRETER} -m src.tools.feature_importance $(dataset) $(pipeline) $(res)

test:
	pytest --cov-report term-missing --cov=src tests/

clean:
ifdef dataset
	rm -rf data/interim/$(dataset)/*
	rm -rf data/processed/$(dataset)/*
	rm -rf data/models/$(dataset)/*
	rm -rf data/trainout/conf_matrices/$(dataset)/*
	rm -rf data/trainout/$(dataset)*.csv
else
	@echo "Missing argument: dataset"
endif
