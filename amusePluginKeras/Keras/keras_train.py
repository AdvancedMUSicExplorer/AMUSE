import sys

import arff
import os
import numpy as np
import keras.backend as K
import tensorflow as tf
import net
from tensorflow.contrib.learn.python.learn.learn_io.data_feeder import setup_train_data_feeder

if __name__ == '__main__':
    
    if len(sys.argv) == 8:
        # read arguments
        input_path = sys.argv[1]
        output_path = sys.argv[2]
        epochs = int(sys.argv[3])
        batch_size = int(sys.argv[4])
        window_size = int(sys.argv[5])
        optimizer = sys.argv[6]
        loss = sys.argv[7]
        
        # load trainer input
        file = arff.load(open(input_path))
        
        data = np.asarray(file['data'])
        attributes = file['attributes']
        attribute_names = [x for (x, y) in attributes]
        
        divide = attribute_names.index('NumberOfCategories')
        
        train_data = data[:, :divide-1]
        labels = data[:, divide+1:]
        
        num_partitions = train_data.shape[0]
        num_classes = labels.shape[1]
        if window_size == -1:
            window_size = train_data.shape[1]
        
        train_data = np.reshape(train_data, (num_partitions, -1, window_size, 1))
        
        num_windows = train_data.shape[1]
        input_shape = (num_windows, window_size, 1)
        
        model = net.build(input_shape, num_classes)
        model.compile(optimizer=optimizer, loss=loss)
        
        history = model.fit(train_data, labels, epochs=epochs, batch_size=batch_size)
        
        model.save(output_path)
        
    else:
        print("Not enough parameters used!!!")
        sys.exit()
