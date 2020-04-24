import sys
from scipy.io import arff

import dcase_util
import arff
import os
import librosa
import pickle
import numpy as np
import matplotlib.pyplot as plt
import keras.backend as K
import tensorflow as tf
from tensorflow.contrib.learn.python.learn.learn_io.data_feeder import setup_train_data_feeder
from keras.models import load_model

if __name__ == '__main__':
    
    if len(sys.argv) == 5:
        # read arguments
        input_path = sys.argv[1]
        model_path = sys.argv[2]
        output_path = sys.argv[3]
        window_size = int(sys.argv[4])
        
        # load classifier input
        file = arff.load(open(input_path))
        
        data = np.asarray(file['data'])
        attributes = file['attributes']
        attribute_names = [x for (x, y) in attributes]
        
        classification_data = data
        if 'Id' in attribute_names:
            divide = attribute_names.index('Id')
            classification_data = data[:, :divide]
        
        num_partitions = classification_data.shape[0]
        if window_size == -1:
            window_size = classification_data.shape[1]
        classification_data = np.reshape(classification_data, (num_partitions, -1, window_size, 1))
        
        model = load_model(model_path)
        
        result = model.predict(classification_data)
        
        num_classes = result.shape[1]
        # save the results
        file = open(output_path, 'w')
        file.write("@RELATION ClassificationSet\n\n")
        for i in range(num_classes):
            file.write("@ATTRIBUTE 'Predicted_Class_{}' NUMERIC\n".format(i))
        file.write("\n@DATA\n")
        for confidences in result:
            for j in range(num_classes-1):
                file.write("{}, ".format(confidences[j]))
            file.write("{}\n".format(confidences[num_classes-1]))
        file.close
        
    else:
        print("Not enough parameters used!!!")
        sys.exit()
