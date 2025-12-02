import tensorflow as tf, os
MODEL_DIR = os.path.join(os.path.dirname(__file__), 'tflite_models')
h5_path = os.path.join(MODEL_DIR, 'model.h5')
tflite_path = os.path.join(MODEL_DIR, 'model.tflite')

def convert_to_tflite():
    if not os.path.exists(h5_path):
        return False, 'model.h5 missing'
    model = tf.keras.models.load_model(h5_path)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    with open(tflite_path, 'wb') as f:
        f.write(tflite_model)
    return True, tflite_path
