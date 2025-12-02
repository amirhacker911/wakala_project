import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Dimensions } from 'react-native';
import { requestPrediction } from '../api/predictions';

const { width } = Dimensions.get('window');

export default function PredictionScreen({ route }) {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const payload = {
    match_id: route?.params?.match_id || 'demo123',
    home_team: route?.params?.home_team || 'Team A',
    away_team: route?.params?.away_team || 'Team B',
    timestamp: Math.floor(Date.now()/1000),
  };

  async function handlePredict() {
    setLoading(true);
    try {
      const data = await requestPrediction(payload);
      setResult(data);
    } catch (e) {
      console.error(e);
      alert('خطأ بالاتصال بالسيرفر');
    } finally {
      setLoading(false);
    }
  }

  function renderOverlay() {
    if (!result) return null;
    const win = result.winner; // 'home' / 'away' / 'draw'
    const circleStyle = [styles.circle, win === 'home' ? { left: width*0.12 } : { right: width*0.12 }];
    return (
      <View style={styles.overlay} pointerEvents="none">
        <View style={circleStyle}>
          <Text style={styles.circleText}>{Math.round(result.probability*100)}%</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{payload.home_team} vs {payload.away_team}</Text>
      <TouchableOpacity style={styles.btn} onPress={handlePredict} disabled={loading}>
        <Text style={styles.btnText}>{loading ? 'جارٍ التحليل...' : 'توقع الجولة التالية'}</Text>
      </TouchableOpacity>

      <View style={styles.matchCard}>
        <Text style={{color:'#ffd700'}}>{payload.home_team}</Text>
        <Text style={{color:'#fff'}}> — </Text>
        <Text style={{color:'#ffd700'}}>{payload.away_team}</Text>
      </View>

      {renderOverlay()}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex:1, padding:20, backgroundColor:'#000' },
  title: { fontSize:18, color:'#ffd700', marginBottom:20 },
  btn: { backgroundColor:'#ffd700', padding:14, borderRadius:10, alignItems:'center' },
  btnText: { color:'#000', fontWeight:'700' },
  matchCard: { marginTop:30, padding:20, backgroundColor:'#111', borderRadius:12, alignItems:'center' },
  overlay: { position:'absolute', top:180, left:0, right:0, height:200 },
  circle: { position:'absolute', top:10, width:80, height:80, borderRadius:40, borderWidth:4, borderColor:'#ffd700', justifyContent:'center', alignItems:'center', backgroundColor:'rgba(0,0,0,0.4)' },
  circleText: { color:'#ffd700', fontWeight:'800' },
});
