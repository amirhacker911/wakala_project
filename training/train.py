# training/train.py
# Train a small CNN on the dataset (assumes images and manifest.json)
import tensorflow as tf
from tensorflow.keras import layers, models
import os, json, argparse
import numpy as np
from PIL import Image
def load_dataset(folder, manifest_file):
    with open(os.path.join(folder, manifest_file),'r') as f:
        manifest = json.load(f)
    X = []
    y = []
    labels_map = {}
    cur = 0
    for entry in manifest:
        lab = entry['label']
        if lab not in labels_map:
            labels_map[lab] = cur; cur+=1
        img = Image.open(os.path.join(folder, entry['file'])).convert('RGB').resize((128,128))
        arr = np.array(img)/255.0
        X.append(arr)
        y.append(labels_map[lab])
    X = np.array(X, dtype=np.float32)
    y = np.array(y, dtype=np.int32)
    return X,y,labels_map
def build_model(num_classes):
    model = models.Sequential([
        layers.Input(shape=(128,128,3)),
        layers.Conv2D(32,3,activation='relu'),
        layers.MaxPool2D(),
        layers.Conv2D(64,3,activation='relu'),
        layers.MaxPool2D(),
        layers.Flatten(),
        layers.Dense(128, activation='relu'),
        layers.Dense(num_classes, activation='softmax')
    ])
    model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
    return model
if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--data', default='synthetic_dataset', help='dataset folder')
    parser.add_argument('--manifest', default='manifest.json')
    parser.add_argument('--epochs', type=int, default=10)
    parser.add_argument('--out', default='model.h5')
    args = parser.parse_args()
    X,y,labels = load_dataset(args.data, args.manifest)
    model = build_model(len(labels))
    model.fit(X,y, epochs=args.epochs, batch_size=32, validation_split=0.1)
    model.save(args.out)
    # save labels mapping
    with open('labels.json','w') as f:
        json.dump(labels, f)
    print('Saved model and labels.json')
