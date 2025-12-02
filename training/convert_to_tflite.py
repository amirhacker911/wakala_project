# training/convert_to_tflite.py
import tensorflow as tf
import argparse, os
if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--h5', default='model.h5')
    parser.add_argument('--out', default='model.tflite')
    args = parser.parse_args()
    model = tf.keras.models.load_model(args.h5)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    open(args.out, 'wb').write(tflite_model)
    print('Wrote', args.out)
