def train_from_data(X, y, output_path='model.joblib'):
    try:
        from sklearn.ensemble import RandomForestClassifier
        from sklearn.preprocessing import LabelEncoder
        from sklearn.model_selection import cross_val_score
        import joblib, os, json, time, tempfile
        model = RandomForestClassifier(n_estimators=100, random_state=42)
        le = LabelEncoder()
        y_enc = le.fit_transform(y)
        cv_score = None
        try:
            scores = cross_val_score(model, X, y_enc, cv=3)
            cv_score = float(scores.mean())
        except:
            cv_score = None
        model.fit(X, y_enc)
        model_dir = os.path.dirname(output_path) or '.'
        os.makedirs(model_dir, exist_ok=True)
        tmp = tempfile.NamedTemporaryFile(delete=False, dir=model_dir, suffix='.tmp')
        tmp_name = tmp.name
        tmp.close()
        joblib.dump({'model': model, 'label_encoder': le}, tmp_name)
        os.replace(tmp_name, output_path)
        meta = {'saved_at': time.time(), 'model_path': output_path, 'n_samples': len(X), 'cv_score': cv_score}
        meta_path = os.path.join(model_dir, 'model_meta.json')
        with open(meta_path, 'w', encoding='utf-8') as fh:
            json.dump(meta, fh)
        return True
    except Exception as e:
        print('train error', e)
        return False
