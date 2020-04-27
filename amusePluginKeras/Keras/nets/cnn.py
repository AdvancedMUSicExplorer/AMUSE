from keras import models
from keras import layers

def build(input_shape, num_classes):
    model = models.Sequential()
    model.add(layers.Conv2D(32, kernel_size=(3, 3), padding="same", activation='relu', input_shape=input_shape))
    model.add(layers.Conv2D(32, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.MaxPooling2D(pool_size=(2, 2)))
    model.add(layers.Dropout(0.25))
    model.add(layers.Conv2D(64, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.Conv2D(64, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.MaxPooling2D(pool_size=(2, 4)))
    model.add(layers.Dropout(0.25))
    model.add(layers.Conv2D(128, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.Conv2D(128, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.MaxPooling2D(pool_size=(2, 4)))
    model.add(layers.Dropout(0.25))
    model.add(layers.Conv2D(256, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.Conv2D(256, kernel_size=(3, 3), padding="same", activation='relu'))
    model.add(layers.GlobalMaxPooling2D())
    model.add(layers.Dropout(0.25))
    model.add(layers.Dense(1024, activation='relu'))
    model.add(layers.Dropout(0.5))
    model.add(layers.Dense(num_classes, activation='sigmoid'))
    return model
