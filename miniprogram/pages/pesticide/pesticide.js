Page({
  data: {
    ratio: '',
    volume: '',
    volumeUnits: ['L', 'mL'],
    volumeUnitIndex: 0,
    weightUnits: ['mL', 'g', 'kg'],
    weightUnitIndex: 0,
    result: null
  },

  onRatioInput(e) { this.setData({ ratio: e.detail.value }); },
  onVolumeInput(e) { this.setData({ volume: e.detail.value }); },
  onVolumeUnitChange(e) { this.setData({ volumeUnitIndex: e.detail.value }); },
  onWeightUnitChange(e) { this.setData({ weightUnitIndex: e.detail.value }); },

  onCalc() {
    let { ratio, volume, volumeUnits, volumeUnitIndex, weightUnits, weightUnitIndex } = this.data;

    if (!ratio || !volume) {
      wx.showToast({ title: '请填写完整', icon: 'none' });
      return;
    }

    ratio = ratio.replace(/：/g, ':').replace(/\s/g, '');
    const ratioNum = ratio.split(':').pop();

    if (!ratioNum || isNaN(Number(ratioNum))) {
      wx.showToast({ title: '比例格式不对，例如 1:500', icon: 'none' });
      return;
    }

    if (Number(ratioNum) < 50 || Number(ratioNum) > 5000) {
      wx.showToast({ title: '比例需在 50~5000 之间', icon: 'none' });
      return;
    }

    const v = Number(volume);
    if (v <= 0 || v > 100) {
      wx.showToast({ title: '药液量需在 1~100 之间', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '计算中...' });

    wx.request({
      url: getApp().globalData.BASE_URL + '/api/pesticide/calc',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: {
        ratio: ratioNum,
        targetVolume: v,
        volumeUnit: volumeUnits[volumeUnitIndex],
        pesticideUnit: weightUnits[weightUnitIndex]   // ← 改成 pesticideUnit
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 0) {
          this.setData({ result: res.data.data });
        } else {
          this.offlineCalc(ratioNum, v, volumeUnits[volumeUnitIndex], weightUnits[weightUnitIndex]);
        }
      },
      fail: () => {
        wx.hideLoading();
        this.offlineCalc(ratioNum, v, volumeUnits[volumeUnitIndex], weightUnits[weightUnitIndex]);
      }
    });
  },

  offlineCalc(ratioNum, volume, volumeUnit, weightUnit) {
    const targetMl = volumeUnit === 'mL' ? volume : volume * 1000;
    const pesticideMl = targetMl / Number(ratioNum);
    const waterMl = targetMl - pesticideMl;

    this.setData({
      result: {
        pesticideAmount: Math.round(pesticideMl * 100) / 100,
        pesticideUnit: weightUnit,
        waterAmount: Math.round(waterMl / 10) / 100,
        waterUnit: 'L',
        disclaimer: '当前为离线计算结果，仅供参考。农药使用量应以产品标签及当地农业技术部门指导为准。'
      }
    });
  }
});