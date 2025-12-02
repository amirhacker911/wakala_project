import argparse
from pathlib import Path
import subprocess
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--input', required=True, help='Path to input .h5 model')
parser.add_argument('--output', required=True, help='Path to output .tflite model')
args = parser.parse_args()

input_path = Path(args.input)
output_path = Path(args.output)

if not input_path.exists():
    print('Input model not found:', input_path)
    sys.exit(1)

conv_script = Path('training/convert_to_tflite.py')
if conv_script.exists():
    print('Using project convert_to_tflite.py')
    subprocess.check_call([sys.executable, str(conv_script), '--input', str(input_path), '--output', str(output_path)])
else:
    try:
        import tensorflow as tf
        model = tf.keras.models.load_model(str(input_path))
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        tflite_model = converter.convert()
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with open(output_path, 'wb') as f:
            f.write(tflite_model)
        print('Converted to', output_path)
    except Exception as e:
        print('Conversion failed:', e)
        sys.exit(2)
