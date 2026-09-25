Page({
  data: {
    ratio: '',
    volume: '',
    result: null
  },

  onRatioInput(e) {
    this.setData({ ratio: e.detail.value });
  },

  onVolumeInput(e) {
    this.setData({ volume: e.detail.value });
  },

  onCalc() {
    let { ratio, volume } = this.data;

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

    wx.showLoading({ title: '计算中...' });

    wx.request({
      url: getApp().globalData.BASE_URL + '/api/pesticide/calc',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: {
        ratio: ratioNum,
        targetVolume: Number(volume),
        volumeUnit: 'L'
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 0) {
          this.setData({ result: res.data.data });
        } else {
          wx.showToast({ title: '计算失败：' + res.data.message, icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '请求失败', icon: 'none' });
      }
    });
  }
});